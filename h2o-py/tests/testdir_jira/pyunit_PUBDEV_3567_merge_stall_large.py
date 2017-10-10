import sys
sys.path.insert(1,"../../")
import h2o
from tests import pyunit_utils
import numpy as np

def pubdev_3567():


    #sillyWendyTest1()
    #mattTest1()
    train = h2o.import_file(pyunit_utils.locate("smalldata/jira/frameA1.csv"), header=1)
    test = h2o.import_file(pyunit_utils.locate("smalldata/jira/frameB1.csv"), header=1)
    mergedAns = h2o.import_file(pyunit_utils.locate("smalldata/jira/merged1.csv"), header=1)
    mergedAnsLeft = h2o.import_file(pyunit_utils.locate("smalldata/jira/merged1Left.csv"), header=1)
    mergedAnsRight = h2o.import_file(pyunit_utils.locate("smalldata/jira/merged1Right.csv"), header=1)
    merged = train.merge(test,by_x=["A"],by_y=["A"],method="auto") # default is radix
    mergedLeft = train.merge(test,by_x=["A"],by_y=["A"],all_x=True)
    mergedRight = train.merge(test,by_x=["A"],by_y=["A"],all_y=True)    # new feature

    pyunit_utils.compare_numeric_frames(mergedAnsRight, mergedRight, 1, tol=1e-10)
    pyunit_utils.compare_numeric_frames(mergedAns, merged, 1, tol=1e-10)
    pyunit_utils.compare_numeric_frames(mergedAnsLeft, mergedLeft, 1, tol=1e-10)
    pyunit_utils.compare_numeric_frames(mergedAnsRight, mergedRight, 1, tol=1e-10)

if __name__ == "__main__":
    pyunit_utils.standalone_test(pubdev_3567)
else:
    pubdev_3567()
