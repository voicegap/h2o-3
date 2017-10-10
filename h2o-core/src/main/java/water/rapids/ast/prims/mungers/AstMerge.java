package water.rapids.ast.prims.mungers;

import water.H2O;
import water.MRTask;
import water.fvec.CategoricalWrappedVec;
import water.fvec.Frame;
import water.fvec.Vec;
import water.rapids.Env;
import water.rapids.Merge;
import water.rapids.ast.AstPrimitive;
import water.rapids.ast.AstRoot;
import water.rapids.ast.params.AstNum;
import water.rapids.ast.params.AstNumList;
import water.rapids.vals.ValFrame;

import java.util.ArrayList;


/**
 * plyr's merge: Join by any other name.
 * Sample AstRoot: (merge $leftFrame $rightFrame allLeftFlag allRightFlag)
 * <p/>
 * Joins two frames; all columns with the same names will be the join key.  If
 * you want to join on a subset of identical names, rename the columns first
 * (otherwise the same column name would appear twice in the result).
 * <p/>
 * If the client side wants to allow named columns to be merged, the client
 * side is reponsible for renaming columns as needed to bring the names into
 * alignment as above.  This can be as simple as renaming the RHS to match the
 * LHS column names.  Duplicate columns NOT part of the merge are still not
 * allowed - because the resulting Frame will end up with duplicate column
 * names which blows a Frame invariant (uniqueness of column names).
 * <p/>
 * If allLeftFlag is true, all rows in the leftFrame will be included, even if
 * there is no matching row in the rightFrame, and vice-versa for
 * allRightFlag.  Missing data will appear as NAs.  Both flags can be true.
 */
public class AstMerge extends AstPrimitive {
  @Override
  public String[] args() {
    return new String[]{"left", "rite", "all_left", "all_rite", "by_left", "by_right", "method"};
  }

  @Override
  public String str() {
    return "merge";
  }

  @Override
  public int nargs() {
    return 1 + 7;
  } // (merge left rite all.left all.rite method)

  // Size cutoff before switching between a hashed-join vs a sorting join.
  // Hash tables beyond this count are assumed to be inefficient, and we're
  // better served by sorting all the join columns and doing a global
  // merge-join.
  static final int MAX_HASH_SIZE = 120000000;

  @Override
  public ValFrame apply(Env env, Env.StackHelp stk, AstRoot asts[]) {
    Frame l = stk.track(asts[1].exec(env)).getFrame();
    Frame r = stk.track(asts[2].exec(env)).getFrame();
    boolean allLeft = asts[3].exec(env).getNum() == 1;
    boolean allRite = asts[4].exec(env).getNum() == 1;
    int[] byLeft = check(asts[5]);
    int[] byRite = check(asts[6]);
    String method = asts[7].exec(env).getStr();

    // byLeft and byRight contains the columns to match between
    // check them
    if (byLeft.length == 0) {
      assert byRite.length==0;
      // Now find common column names here on the Java side. As for Python caller currently.
      ArrayList<Integer> leftTmp = new ArrayList<>();
      ArrayList<Integer> riteTmp = new ArrayList<>();
      for (int i=0; i<l._names.length; i++) {
        int idx = r.find(l._names[i]);
        if (idx != -1) {
          leftTmp.add(i);
          riteTmp.add(idx);
        }
      }
      if (leftTmp.size() == 0) throw new IllegalArgumentException("No join columns specified and there are no common names");
      byLeft = new int[leftTmp.size()];
      byRite = new int[riteTmp.size()];
      for (int i=0; i < byLeft.length; i++)
      {
        byLeft[i] = leftTmp.get(i).intValue();
        byRite[i] = riteTmp.get(i).intValue();
      }
    }

    if (byLeft.length != byRite.length)
      throw new IllegalArgumentException("byLeft and byRight are not the same length");
    int ncols = byLeft.length;  // Number of join columns dealt with so far
    l.moveFirst(byLeft);
    r.moveFirst(byRite);
    for (int i = 0; i < ncols; i++) {
        Vec lv = l.vecs()[i];
        Vec rv = r.vecs()[i];
        if (lv.get_type() != rv.get_type())
          throw new IllegalArgumentException("Merging columns must be the same type, column " + l._names[ncols] +
              " found types " + lv.get_type_str() + " and " + rv.get_type_str());
        if (lv.isString())
          throw new IllegalArgumentException("Cannot merge Strings; flip toCategoricalVec first");
    }

    // GC now to sync nodes and get them to use young gen for the working memory. This helps get stable
    // repeatable timings.  Otherwise full GCs can cause blocks. Adding System.gc() here suggested by Cliff
    // during F2F pair-programming and it for sure worked.
    // TODO - would be better at the end to clean up, but there are several exit paths here.
    new MRTask() {
      @Override
      public void setupLocal() {
        System.gc();
      }
    }.doAllNodes();

    if (method.equals("radix") || method.equals("auto")) {
      // Build categorical mappings, to rapidly convert categoricals from the left to the right
      // With the sortingMerge approach there is no variance here: always map left to right
      if (allLeft && allRite)
        throw new IllegalArgumentException("all.x=TRUE and all.y=TRUE is not supported.  Choose one only.");

      boolean onlyLeftAllOff = (allLeft && !allRite) || !allRite;
      int[][] id_maps = new int[ncols][];
      for (int i = 0; i < ncols; i++) { // flip the frame orders for allRite
        Vec lv = onlyLeftAllOff?l.vec(i):r.vec(i);
        Vec rv = onlyLeftAllOff?r.vec(i):l.vec(i);
        if (onlyLeftAllOff?lv.isCategorical():rv.isCategorical()) {
          assert onlyLeftAllOff?rv.isCategorical():lv.isCategorical();  // if not, would have thrown above
          id_maps[i] = onlyLeftAllOff?CategoricalWrappedVec.computeMap(lv.domain(), rv.domain()):CategoricalWrappedVec.computeMap(rv.domain(), lv.domain());
        }
      }

      if (onlyLeftAllOff) {
        return sortingMerge(l, r, allLeft, allRite, ncols, id_maps);
      } else {  // implement allRite here by switching leftframe and riteframe.  However, column order is wrong, re-order before return
        ValFrame tempFrame = sortingMerge(r,l,allRite,allLeft, ncols, id_maps);
        Frame mergedFrame = tempFrame.getFrame();  // need to switch order of merged frame
        int allColNum = mergedFrame.numCols();
        int[] colMapping = new int[allColNum];  // index into combined frame but with correct order
        for (int index = 0; index < ncols; index++) {
          colMapping[index] = index;    // no change to column order in the key columns
        }
        int offset = r.numCols()-ncols;
        for (int index = ncols; index < l.numCols(); index++) { // set the order for right frame
          colMapping[index] = offset+index;        // move the left columns to the front
        }
        offset = l.numCols()-ncols;
        for (int index=l.numCols(); index < allColNum; index++) {
          colMapping[index] = index-offset;
        }

        mergedFrame.reOrder(colMapping);  // reorder the frame columns for allrite = true
        return tempFrame;

      }
    }
    throw H2O.unimpl();
  }

  /**
   * Use a sorting merge/join, probably because the hash table size exceeded
   * MAX_HASH_SIZE; i.e. the number of unique keys in the hashed Frame exceeds
   * MAX_HASH_SIZE.  Join is done on the first ncol columns in both frames,
   * which are already known to be not-null and have matching names and types.
   * The walked and hashed frames are sorted according to allLeft; if allRite
   * is set then allLeft will also be set (but not vice-versa).
   *
   * @param left    is the LHS frame; not-null.
   * @param right   is the RHS frame; not-null.
   * @param allLeft all rows in the LHS frame will appear in the result frame.
   * @param allRite all rows in the RHS frame will appear in the result frame.
   * @param ncols   is the number of columns to join on, and these are ordered
   *                as the first ncols of both the left and right frames.
   * @param id_maps if not-null denote simple integer mappings from one
   *                categorical column to another; the width is ncols
   */

  private ValFrame sortingMerge(Frame left, Frame right, boolean allLeft, boolean allRite, int ncols, int[][] id_maps) {
    int cols[] = new int[ncols];
    for (int i = 0; i < ncols; i++) cols[i] = i;
    return new ValFrame(Merge.merge(left, right, cols, cols, allLeft, id_maps));
  }

  private int[] check(AstRoot ast) {
    double[] n;
    if (ast instanceof AstNumList) n = ((AstNumList) ast).expand();
    else if (ast instanceof AstNum)
      n = new double[]{((AstNum) ast).getNum()};  // this is the number of breaks wanted...
    else throw new IllegalArgumentException("Requires a number-list, but found a " + ast.getClass());
    int[] ni = new int[n.length];
    for (int i = 0; i < ni.length; ++i)
      ni[i] = (int) n[i];
    return ni;
  }
}
