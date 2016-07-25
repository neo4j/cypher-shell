## How to build

Use `make help` (`gradlew tasks`) to list possible tasks. But you probably want either

*  `make dist` (`gradlew installDist`)
   which will build a runnable script for you at `cypher-shell/build/install/cypher-shell`

* `make zip`
   which builds a runnable script and packages it up for you as: `out/neo4j-shell.zip`

You can then just run the executable under the `bin/` sub-directory.

## How to run, the fast way

This clears any previously known neo4j hosts, starts a throw-away
instance of neo4j, and connects to it.

```sh
rm -rf ~/.neo4j/known_hosts
docker run --detach -p 7687:7687 -e NEO4J_AUTH=none neo4j:3.0
make run
```

## Development

### Integration tests

#### Pre Requisites for running integration tests

Neo4j server with bolt driver configured.

Authenticated by username `neo4j` password `neo`

#### To run

Integration tests are usually skipped when you run `make test` (`gradlew test`)

Use `make integration-test` (`gradlew integrationTest`) to specifically run integration tests.
