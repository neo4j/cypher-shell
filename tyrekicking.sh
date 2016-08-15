#!/bin/bash -eu
# Should be run in the out folder, containing cypher-shell.zip

function prepare {
  unzip cypher-shell.zip
}

function prepare-bundle {
  mkdir -p cypher-shell/tools
  mv cypher-shell/*.jar cypher-shell/tools
}

function testscript {
  if cypher-shell/cypher-shell "RETURN 1;"; then
    echo "$1 Success!"
  else
    echo "$1 Failure!"
    exit 1
  fi
}

prepare
## Standalone test
testscript "Standalone"
## Fake bundling test
prepare-bundle
testscript "Bundling"
