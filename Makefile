.PHONY: help dist clean zip

help: ## Print this help text
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

zip: out/temp/cypher-shell/bin/cypher-shell ## Build zip distribution file in 'out/'
	cd out/temp && zip -r cypher-shell.zip cypher-shell
	mv out/temp/cypher-shell.zip out/cypher-shell.zip
	rm -rf out/temp

out/temp/cypher-shell/bin/cypher-shell: out dist
	rm -rf out/temp
	mkdir -p out/temp
	cp -r cypher-shell/build/install/cypher-shell out/temp/cypher-shell

run: dist ## Build and run cypher-shell with no arguments
	cypher-shell/build/install/cypher-shell/bin/cypher-shell

dist: ## Build and test cypher-shell
	./gradlew clean check installDist

out:
	mkdir -p out

clean: ## Clean out-directories
	rm -rf out
	./gradlew clean
