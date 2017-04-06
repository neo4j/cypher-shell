.DEFAULT: help
.PHONY: help build clean zip run untested-zip test integration-test tyrekicking-test mutation-test install

gitdescribe = $(shell git describe --tags --match [0-9]*)
lasttag = $(shell git describe --tags --match [0-9]* --abbrev=0)
version ?= $(lasttag)
commitcount = $(shell git rev-list $(lasttag)..HEAD --count)

ifeq ($(commitcount),0)
	release ?= 1
	distribution ?= stable
	buildversion = $(version)
else
	release ?= 0.$(commitcount).1
	distribution ?= unstable
	buildversion = $(gitdescribe)
endif

debversion ?= $(version)-$(release)

GRADLE = ./gradlew -PbuildVersion=$(buildversion)

jarfile = cypher-shell-all.jar
rpmfile = cypher-shell-$(version)-$(release).noarch.rpm
debfile = cypher-shell_$(debversion)_all.deb

outputs = cypher-shell cypher-shell.bat $(jarfile)
artifacts=$(patsubst %,cypher-shell/build/install/cypher-shell/%,${outputs})
rpm_artifacts=$(patsubst %,out/rpm/BUILD/%,${artifacts})
deb_artifacts=$(patsubst %,out/debian/cypher-shell-$(debversion)/%,${artifacts})
deb_files=$(wildcard packaging/debian/*)
deb_targets=$(patsubst packaging/debian/%,out/debian/cypher-shell-$(debversion)/debian/%,${deb_files})

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
	$(GRADLE) clean

rmhosts: ## Remove known hosts file
	rm -rf ~/.neo4j/known_hosts

launch: rmhosts clean build run ## Removes known hosts file, cleans, builds, and runs the shell

prefix ?= /usr/local
install: build cypher-shell.1 ## Install cypher-shell (requires pandoc for manual)
	mkdir -p $(DESTDIR)/$(prefix)/bin
	mkdir -p $(DESTDIR)/$(prefix)/share/cypher-shell/lib
	mkdir -p $(DESTDIR)/$(prefix)/share/man/man1
	cp cypher-shell/build/install/cypher-shell/cypher-shell $(DESTDIR)/$(prefix)/bin
	cp cypher-shell/build/install/cypher-shell/$(jarfile) $(DESTDIR)/$(prefix)/share/cypher-shell/lib
	cp cypher-shell.1 $(DESTDIR)/$(prefix)/share/man/man1

%.1: %.1.md
	pandoc -s -o $@ $<

%/integrationTest/results.bin:
	$(GRADLE) integrationTest

%/test/results.bin:
	$(GRADLE) check

$(artifacts):
	$(GRADLE) installDist

%/reports/pitest/index.html:
	$(GRADLE) pitest

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

out/rpm/RPMS/noarch/$(rpmfile): out/rpm/SPECS/cypher-shell.spec $(rpm_artifacts) out/rpm/BUILD/Makefile out/rpm/BUILD/cypher-shell.1.md
	rpmbuild --define "_topdir $(CURDIR)/out/rpm" -bb --clean $<

.PHONY: rpm
rpm: out/$(rpmfile) ## Build the RPM package

out/debian/cypher-shell-$(debversion)/debian/changelog: packaging/debian/changelog
	mkdir -p $(dir $@)
	VERSION=$(debversion) DISTRIBUTION=$(distribution) DATE="$(shell date -R)" envsubst '$${VERSION} $${DISTRIBUTION} $${DATE}' < $< > $@

out/debian/cypher-shell-$(debversion)/debian/%: packaging/debian/%
	mkdir -p $(dir $@)
	cp $< $@

out/debian/cypher-shell-$(debversion)/%: %
	mkdir -p $(dir $@)
	cp $< $@

out/debian/$(debfile): $(deb_artifacts) $(deb_targets) out/debian/cypher-shell-$(debversion)/cypher-shell.1.md
	(cd out/debian/cypher-shell-$(debversion) && debuild -A -uc -us)

out/%.deb: out/debian/%.deb
	cp $< $@

.PHONY: debian
debian: out/$(debfile) ## Build the Debian package
