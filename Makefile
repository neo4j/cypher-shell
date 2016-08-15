.PHONY: help build clean zip test integration-test tyrekicking-test

help: ## Print this help text
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

run: build ## Build and run cypher-shell with no arguments
	cypher-shell/build/install/cypher-shell/cypher-shell

build: ## Build and test cypher-shell
	./gradlew installDist

test: ## Run all unit tests
	./gradlew check pitest

integration-test: ## Run all integration tests
	./gradlew integrationTest

tyrekicking-test: tmp/cypher-shell.zip tmp/.tests-pass ## Test that the shell script can actually start

clean: ## Clean out-directories
	rm -rf out
	rm -rf tmp
	./gradlew clean

zip: out/cypher-shell.zip ## Build and test zip distribution file in 'out/'

untested-zip: tmp/cypher-shell.zip ## Build (but don't test) zip distribution file in 'tmp/'

out/cypher-shell.zip: tmp/cypher-shell.zip tmp/.tests-pass
	mkdir -p out
	cp $< $@

tmp/.tests-pass: tmp/cypher-shell.zip tyrekicking.sh
	cp tyrekicking.sh tmp/
	cd tmp && bash tyrekicking.sh
	touch $@

tmp/cypher-shell.zip: tmp/temp/cypher-shell/bin/cypher-shell ## Build (but don't test) zip distribution
	cd tmp/temp && zip -r cypher-shell.zip cypher-shell
	mv tmp/temp/cypher-shell.zip tmp/cypher-shell.zip
	rm -rf tmp/temp

tmp/temp/cypher-shell/bin/cypher-shell: build
	rm -rf tmp
	mkdir -p tmp/temp
	cp -r cypher-shell/build/install/cypher-shell tmp/temp/cypher-shell
