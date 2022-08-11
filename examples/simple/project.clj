(defproject simple-reagent "0.6.0"
  :dependencies [[binaryage/chromex "0.9.2"]
                 [binaryage/devtools "1.0.0"]
                 [figwheel "0.5.19"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"]
                 [reagent "0.10.0"]
                 [reagent-utils "0.3.3"]]


  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.19"]]

  :resource-paths ["resources" "target"]
  :clean-targets ^{:protect false} [:target-path]

  :profiles {:dev {:cljsbuild
                   {:builds {:client
                             {:figwheel {:on-jsload "simpleexample.core/run"}
                              :compiler {:main "simpleexample.core"
                                         :optimizations :none}}}}
                   :dependencies [[binaryage/devtools "1.0.0"]
                                  [cider/piggieback "0.5.2"]
                                  [figwheel-sidecar "0.5.19"]
                                  [nrepl "0.8.3"]]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}

             :prod {:cljsbuild
                    {:builds {:client
                              {:compiler {:optimizations :advanced
                                          :elide-asserts true
                                          :pretty-print false}}}}}}

  :figwheel {:repl true
             :nrepl-port 7002
             :http-server-root "public"}

  :cljsbuild {:builds {:client
                       {:source-paths ["src"]
                        :compiler {:output-dir "target/public/client"
                                   :asset-path "client"
                                   :output-to "target/public/client.js"}}}})
