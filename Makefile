.PHONY: help build clean zip run untested-zip test integration-test tyrekicking-test mutation-test

help: ## Print this help text
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

run: cypher-shell/build/install/cypher-shell/cypher-shell ## Build and run cypher-shell with no arguments
	cypher-shell/build/install/cypher-shell/cypher-shell

zip: out/cypher-shell.zip ## Build and run all tests on zip distribution file in 'out/'

untested-zip: tmp/cypher-shell.zip ## Build (but don't test) zip distribution file in 'tmp/'

build: cypher-shell/build/install/cypher-shell/cypher-shell ## Build cypher-shell

test: cypher-shell/build/test-results/binary/test/results.bin ## Run all unit tests

integration-test: cypher-shell/build/test-results/binary/integrationTest/results.bin ## Run all integration tests

tyrekicking-test: tmp/.tests-pass ## Test that the shell script can actually start

mutation-test: cypher-shell/build/reports/pitest/index.html ## Generate a mutation testing report

%/integrationTest/results.bin:
	./gradlew integrationTest

%/test/results.bin:
	./gradlew check

%/install/cypher-shell/cypher-shell:
	./gradlew installDist

%/reports/pitest/index.html:
	./gradlew pitest

tmp/.tests-pass: tmp/cypher-shell.zip tyrekicking.sh
	cp tyrekicking.sh tmp/
	cd tmp && bash tyrekicking.sh
	touch $@

tmp/cypher-shell.zip: tmp/temp/cypher-shell/cypher-shell
	cd tmp/temp && zip -r cypher-shell.zip cypher-shell
	mv tmp/temp/cypher-shell.zip tmp/cypher-shell.zip

tmp/temp/cypher-shell/cypher-shell: cypher-shell/build/install/cypher-shell/cypher-shell
	rm -rf tmp
	mkdir -p tmp/temp
	cp -r cypher-shell/build/install/cypher-shell tmp/temp/cypher-shell

out/cypher-shell.zip: tmp/cypher-shell.zip test integration-test tyrekicking-test
	mkdir -p out
	cp $< $@

clean: ## Clean build directories
	rm -rf out
	rm -rf tmp
	./gradlew clean

rmhosts:
	rm -rf ~/.neo4j/known_hosts

launch: rmhosts clean build run ## Removes known hosts file, cleans, builds, and runs the shell
