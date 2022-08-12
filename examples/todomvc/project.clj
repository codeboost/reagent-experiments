(defproject todomvc-reagent "0.6.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.597"]
                 [reagent "0.10.0"]
                 [figwheel "0.5.19"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.19"]]

  :resource-paths ["resources" "target"]
  :clean-targets ^{:protect false} [:target-path]

  :profiles {:dev {:cljsbuild
                   {:builds {:client
                             {:figwheel {:on-jsload "todomvc.core/run"}
                              :compiler {:main "todomvc.core"
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
             :nrepl-port 7003
             :http-server-root "public"}

  :cljsbuild {:builds {:client
                       {:source-paths ["src"]
                        :compiler {:output-dir "target/public/client"
                                   :asset-path "client"
                                   :output-to "target/public/client.js"}}}})
