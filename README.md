Contains Cypher Shell 1.1. __PLEASE NOTE!__ newer versions have moved to https://github.com/neo-technology/neo4j/tree/4.3/public/community/cypher-shell.

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

## How to build packages

Packages require you to have `pandoc` available. It should be
available in your local package manager.

Then just do

```
make debian rpm
```

To test the packages you need to have Docker installed:

```
make debian-test rpm-test
```

To get the versions correct when building packages you can override
some variables, for example with:

```
make debian pkgversion=2
```

See `make info` for a list of variables and what the results will be.

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
