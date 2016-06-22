## How to build

Use `./gradlew tasks` to list possible tasks. But you probably want either

*  `installDist`
   which will build a runnable script for you at `build/install/neo4j-shell`

* `distTar` or `distZip`
   which builds a runnable script and packages it up for you under `build/distributions`

You can then just run the executable under `bin/neo4j-shell`.
