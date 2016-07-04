## How to build

Use `./gradlew tasks` to list possible tasks. But you probably want either

*  `installDist`
   which will build a runnable script for you at `neo4j-shell/build/install`

* `distTar` or `distZip`
   which builds a runnable script and packages it up for you under `neo4j-shell/build/distributions`

You can then just run the executable under `bin/`.
