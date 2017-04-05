.DEFAULT: help
.PHONY: help build clean zip run untested-zip test integration-test tyrekicking-test mutation-test install

gitdescribe = $(shell git describe --tags --match [0-9]*)
lasttag = $(shell git describe --tags --match [0-9]* --abbrev=0)
version ?= $(lasttag)
commitcount = $(shell git rev-list $(lasttag)..HEAD --count)

ifeq ($(commitcount),0)
	release ?= 1
else
	release ?= 0.$(commitcount).1
endif

jarfile = cypher-shell-$(gitdescribe)-all.jar
rpmfile = cypher-shell-$(version)-$(release).noarch.rpm

outputs = cypher-shell cypher-shell.bat $(jarfile)
artifacts=$(patsubst %,cypher-shell/build/install/cypher-shell/%,${outputs})
rpm_artifacts=$(patsubst %,out/rpm/BUILD/%,${artifacts})

help: ## Print this help text
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

run: $(artifacts) ## Build and run cypher-shell with no arguments
	cypher-shell/build/install/cypher-shell/cypher-shell

zip: out/cypher-shell.zip test integration-test tyrekicking-test ## Build and run all tests on zip distribution file in 'out/'

untested-zip: out/cypher-shell.zip ## Build (but don't test) zip distribution file in 'out/'

build: $(artifacts) ## Build cypher-shell

test: cypher-shell/build/test-results/binary/test/results.bin ## Run all unit tests

integration-test: cypher-shell/build/test-results/binary/integrationTest/results.bin ## Run all integration tests

tyrekicking-test: tmp/.tests-pass ## Test that the shell script can actually start

mutation-test: cypher-shell/build/reports/pitest/index.html ## Generate a mutation testing report

clean: ## Clean build directories
	rm -rf out
	rm -rf tmp
	./gradlew clean

rmhosts: ## Remove known hosts file
	rm -rf ~/.neo4j/known_hosts

launch: rmhosts clean build run ## Removes known hosts file, cleans, builds, and runs the shell

prefix ?= /usr/local
install: build ## Install cypher-shell
	mkdir -p $(DESTDIR)/$(prefix)/bin
	mkdir -p $(DESTDIR)/$(prefix)/share/cypher-shell/lib
	cp cypher-shell/build/install/cypher-shell/cypher-shell $(DESTDIR)/$(prefix)/bin
	cp cypher-shell/build/install/cypher-shell/$(jarfile) $(DESTDIR)/$(prefix)/share/cypher-shell/lib

%/integrationTest/results.bin:
	./gradlew integrationTest

%/test/results.bin:
	./gradlew check

$(artifacts):
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

tmp/temp/cypher-shell/cypher-shell: $(artifacts)
	rm -rf tmp
	mkdir -p tmp/temp
	cp -r cypher-shell/build/install/cypher-shell tmp/temp/cypher-shell

out/cypher-shell.zip: tmp/cypher-shell.zip
	mkdir -p out
	cp $< $@

out/rpm/SPECS/cypher-shell.spec: packaging/rpm/cypher-shell.spec
	bash -c "mkdir -p out/rpm/{BUILD,RPMS,SOURCES,BUILDROOT,SPECS,SRPMS}/"
	VERSION=$(version) RELEASE=$(release) envsubst '$${VERSION} $${RELEASE}' < $< > $@

out/rpm/BUILD/%: %
	mkdir -p $(dir $@)
	cp $< $@

out/%.rpm: out/rpm/RPMS/noarch/%.rpm
	cp $< $@

out/rpm/RPMS/noarch/$(rpmfile): out/rpm/SPECS/cypher-shell.spec $(rpm_artifacts) out/rpm/BUILD/Makefile
	rpmbuild --define "_topdir $(CURDIR)/out/rpm" -bb --clean $<

.PHONY: rpm
rpm: out/$(rpmfile) ## Build the RPM package
