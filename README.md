## How to build

Use `make help` (`gradlew tasks`) to list possible tasks. But you
probably want either

-  `make build` (`gradlew installDist`) which will build an
   uber-jar and runnable script for you at
   `cypher-shell/build/install/cypher-shell`

- `make zip` which builds an uber-jar with runnable script and
   packages it up for you as: `out/cypher-shell.zip` Note that this
   will run a test on the script which requires a instance of neo4j
   (see Integration tests below).

- `make untested-zip` which builds the same zip file but doesn't test
  it. It will be placed in `tmp/cypher-shell.zip`.

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

If authentication is required, it is assumed to be username `neo4j`
and password `neo`.

#### To run

Integration tests are usually skipped when you run `make test`
(`gradlew test`)

Use `make integration-test` (`gradlew integrationTest`) to
specifically run integration tests.

#### How to run the fast way

This clears any previously known neo4j hosts, starts a throw-away
instance of neo4j, and runs the integration tests against it.

```sh
rm -rf ~/.neo4j/known_hosts
docker run --detach -p 7687:7687 -e NEO4J_AUTH=none neo4j:3.0
make integration-test
```
