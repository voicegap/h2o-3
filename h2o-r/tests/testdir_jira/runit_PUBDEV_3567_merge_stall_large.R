setwd(normalizePath(dirname(R.utils::commandArgs(asValues=TRUE)$"f")))
source("../../scripts/h2o-r-test-setup.R")

test <- function() {
  data <- h2o.uploadFile(locate("smalldata/jira/frameA2.csv"), header=TRUE)
  data2 <- h2o.uploadFile(locate("smalldata/jira/frameB2.csv"), header=TRUE)
  mergedRight <- h2o.uploadFile(locate("smalldata/jira/merged2Right.csv"), header=TRUE)
  f3 <- h2o.merge(data, data2, all.y=TRUE)
  compareFrames(f3, mergedRight, prob=1)
  merged <- h2o.uploadFile(locate("smalldata/jira/merged2.csv"), header=TRUE)
  f <- h2o.merge(data, data2)
  compareFrames(f, merged, prob=1)
  mergedLeft <- h2o.uploadFile(locate("smalldata/jira/merged2Left.csv"), header=TRUE)
  f2 <- h2o.merge(data, data2, all.x=TRUE)
  compareFrames(f2, mergedLeft, prob=1)
}

doTest("PUBDEV-784", test)
