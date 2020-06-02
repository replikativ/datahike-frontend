A front-end to Datahike written in Fulcro.
It connects to a Datahike-server instance.

# Quick start

## SERVER, i.e. Clj side
 in a shell: clj -A:dev:cider-clj -J-Dtrace -J-Dghostwheel.enabled=true
 then in emacs in the project: cider-connect-clj | localhost | <the port it suggests>
 in the repl: user> (start)
 in the browser: localhost:4000
 If restart needed after compilation pblm:
 (tools-ns/refresh)
 (start)

## CLIENT, cljs
 Better not to use emacs if server part is already using emacs. Better use shell + Intellij:
 In the shell: npx shadow-cljs server
 In Intellij REPL: (shadow/repl :main)
 Still, if you really want to use emacs then: cider-jack-in-cljs
 Choose options related to 'shadow'
 You might need to compile the js code here: http://localhost:9630
 The http server is here: http://localhost:8000/
 The nrepl is server in port 9000

## Datahike-server
 You need to start a Datahike server as well. It is expected to listen on port 3000 and to allow connection from localhost:4000 (i.e. a different origin).




# Current status
- Transacting works only when you are on the :eavt Datoms view. (I.e. when on the view showing each entity on a specific row, transactions do not work yet.)

- Only query_pull type queries work.
- Queries seem to have issues when the 'where' expression has multiple clauses.
