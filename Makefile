.DEFAULT: help
.PHONY: help build clean zip run untested-zip test integration-test tyrekicking-test mutation-test install info

gitdescribe := $(shell git describe --tags --match [0-9]*)
lasttag := $(shell git describe --tags --match [0-9]* --abbrev=0)

version ?= $(lasttag)
versionlabel = $(shell echo ${version} | awk '{ sub("^[0-9]+.[0-9]+.[0-9]+-?", "", $$1); print }')
versionnumber = $(shell echo ${version} | awk '{ sub("-.*$$", "", $$1); print }')

pkgversion ?= 1
# If no label it is assumed to be a stable release
ifeq ($(versionlabel),)
	release := $(pkgversion)
	distribution ?= stable
	buildversion := $(version)
	debversion := $(versionnumber)
else
	release := 0.$(versionlabel).$(pkgversion)
	distribution ?= unstable
	buildversion := $(gitdescribe)
	debversion := $(versionnumber)~$(versionlabel)
endif

rpmversion := $(versionnumber)-$(release)

GRADLE = ./gradlew -PbuildVersion=$(buildversion)

jarfile := cypher-shell.jar
rpmfile := cypher-shell-$(rpmversion).noarch.rpm
debfile := cypher-shell_$(debversion)_all.deb
outputs := cypher-shell cypher-shell.bat $(jarfile)
artifacts:=$(patsubst %,cypher-shell/build/install/cypher-shell/%,${outputs})
rpm_artifacts:=$(patsubst %,out/rpm/BUILD/%,${artifacts})
deb_artifacts:=$(patsubst %,out/debian/cypher-shell-$(debversion)/%,${artifacts})
deb_files:=$(wildcard packaging/debian/*)
deb_targets:=$(patsubst packaging/debian/%,out/debian/cypher-shell-$(debversion)/debian/%,${deb_files})

DOCKERUUIDRPM   := $(shell uuidgen)
YUMREPO_VOLUMEID := $(shell uuidgen)
YUMREPO_IMAGEID  := repomaker/$(shell uuidgen | head -c 5)

help: ## Print this help text
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

info: ## Print variables used in the build (some of which can be overridden)
	@echo "--- Overridable ---"
	@echo "version:       ${version}"
	@echo "pkgversion:    ${pkgversion}"
	@echo "--- Calculated  ---"
	@echo "versionnumber: ${versionnumber}"
	@echo "versionlabel:  ${versionlabel}"
	@echo "distribution:  ${distribution}"
	@echo "buildversion:  ${buildversion}"
	@echo "release:       ${release}"
	@echo "debversion:    ${debversion}"
	@echo "rpmversion:    ${rpmversion}"

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

# ======================= RPM CYPHER-SHELL =======================

out/rpm/SPECS/cypher-shell.spec: packaging/rpm/cypher-shell.spec
	mkdir -p $(dir $@)
	VERSION=$(versionnumber) RELEASE=$(release) envsubst '$${VERSION} $${RELEASE}' < $< > $@

out/rpm/BUILD/%: %
	mkdir -p $(dir $@)
	cp $< $@

out/%.rpm: out/rpm/RPMS/noarch/%.rpm
	mkdir -p $(dir $@)
	cp $< $@

tmp/rpm-test/%.rpm: out/rpm/RPMS/noarch/%.rpm
	mkdir -p $(dir $@)
	cp $< $@

tmp/rpm-test/Dockerfile: packaging/test/rpm/Dockerfile
	mkdir -p $(dir $@)
	RPMFILE=$(rpmfile) envsubst '$${RPMFILE}' < $< > $@

out/rpm/RPMS/noarch/$(rpmfile): out/rpm/SPECS/cypher-shell.spec $(rpm_artifacts) out/rpm/BUILD/Makefile out/rpm/BUILD/cypher-shell.1.md
	rpmbuild --define "_topdir $(CURDIR)/out/rpm" -bb --clean $<

.PHONY: rpm
rpm: out/$(rpmfile) ## Build the RPM package

.PHONY: rpm-test
rpm-test: tmp/rpm-test/$(rpmfile) tmp/rpm-test/Dockerfile ## Test the RPM package (requires Docker)
	cd $(dir $<) && docker build . -t $(DOCKERUUIDRPM) && docker run --rm $(DOCKERUUIDRPM) --version



# ======================= DEBIAN =======================


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
	cd out/debian/cypher-shell-$(debversion) && debuild -A -uc -us

out/%.deb: out/debian/%.deb
	cp $< $@

tmp/debian-test/%.deb: out/debian/%.deb
	mkdir -p $(dir $@)
	cp $< $@

tmp/debian-test/Dockerfile: packaging/test/debian/Dockerfile
	mkdir -p $(dir $@)
	DEBFILE=$(debfile) envsubst '$${DEBFILE}' < $< > $@

.PHONY: debian
debian: out/$(debfile) ## Build the Debian package

DOCKERUUIDDEB := $(shell uuidgen)
.PHONY: debian-test
debian-test: tmp/debian-test/$(debfile) tmp/debian-test/Dockerfile ## Test the Debian package (requires Docker)
	cd $(dir $<) && docker build . -t $(DOCKERUUIDRPM) && docker run --rm $(DOCKERUUIDRPM) --version
