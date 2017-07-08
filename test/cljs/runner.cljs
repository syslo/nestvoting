(ns runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [tests.math.signature]
            [tests.math.el-gamal]
            [tests.math.groups]
            [tests.math.hashing]))

(doo-tests 'tests.math.signature
           'tests.math.el-gamal
           'tests.math.groups
           'tests.math.hashing)
