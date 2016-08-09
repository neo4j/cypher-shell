.PHONY: help build clean zip

help: ## Print this help text
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

zip: out/temp/cypher-shell/bin/cypher-shell ## Build zip distribution file in 'out/'
	cd out/temp && zip -r cypher-shell.zip cypher-shell
	mv out/temp/cypher-shell.zip out/cypher-shell.zip
	rm -rf out/temp

out/temp/cypher-shell/bin/cypher-shell: out build
	rm -rf out/temp
	mkdir -p out/temp
	cp -r cypher-shell/build/installShadow/cypher-shell out/temp/cypher-shell

run: build ## Build and run cypher-shell with no arguments
	cypher-shell/build/installShadow/cypher-shell/bin/cypher-shell

build: ## Build and test cypher-shell
	./gradlew installShadowApp

test: ## Run all unit tests
	./gradlew check pitest

integration-test: ## Run all integration tests
	./gradlew integrationTest

out:
	mkdir -p out

clean: ## Clean out-directories
	rm -rf out
	./gradlew clean
