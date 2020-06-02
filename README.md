A front-end to Datahike written in Fulcro.
It connects to a Datahike-server instance.


<img src="./public/dh-frontend.gif" width="140" height="140" />

# Quick start

## Start Datahike-frontend server

    In a shell: clj -A:dev:cider-clj -J-Dtrace -J-Dghostwheel.enabled=true
    Then in emacs in the project: cider-connect-clj | localhost | <the port it suggests>
    In the repl: user> (start)
    In the browser: localhost:4000
    If restart needed after compilation pblm:
    (tools-ns/refresh)
    (start)

## Start Datahike-frontend client 
 
    In the shell: npx shadow-cljs server
    In IntelliJ's REPL: (shadow/repl :main)
    If you want to use emacs then: cider-jack-in-cljs
    Choose options related to 'shadow'
    You might need to compile the js code here: http://localhost:9630
    The nrepl is server in port 9000
 
## Datahike-server
You need to start a Datahike server as well. 
It is expected to listen on port 3000.
It should also allow connection from localhost:4000 (i.e. a different origin). Replace the `wrap-cors` declaration in `server.clj` by the following:

      (wrap-cors :access-control-allow-origin [#"http://localhost" #"http://localhost:8080" #"http://localhost:4000"]
                 :access-control-allow-methods [:get :put :post :delete])


# Current status
- Transacting works only when you are on the :eavt Datoms view. (I.e. on the view appearing after a query and showing each entity on a specific row, transactions do not work yet.)

- Only query_pull type queries work.
- Queries seem to have issues when the 'where' expression has multiple clauses.
