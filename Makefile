.PHONY: help dist clean zip

help: ## Print this help text
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

zip: out/neo4j-shell/bin/neo4j-shell ## Build zip distribution file in 'out/'
	# Temporary during dev phase, rename scripts
	mv $< $<-alpha
	mv $<.bat $<-alpha.bat

	cd out && zip -r neo4j-shell.zip neo4j-shell
	rm -rf out/neo4j-shell

out/neo4j-shell/bin/neo4j-shell: out dist
	cp -r neo4j-shell/build/install/neo4j-shell out/neo4j-shell

dist: ## Build and test neo4j-shell
	./gradlew clean check installDist

out:
	mkdir -p out

clean: ## Clean out-directories
	rm -rf out
	./gradlew clean
