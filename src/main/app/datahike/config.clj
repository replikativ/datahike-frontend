(ns app.datahike.config
  (:require [mount.core :refer [defstate]]))

(defstate config
  :start (-> "resources/config.edn" slurp read-string))
