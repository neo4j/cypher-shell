.PHONY: all

#current_dir = $(shell pwd)

all:
	bash -c "mkdir -p $(CURDIR)/tmp/{BUILD,RPMS,SOURCES,BUILDROOT,SPECS,SRPMS}/"
	VERSION=$(version) envsubst '${VERSION}' < cypher-shell.spec > $(CURDIR)/tmp/SPECS/cypher-shell.spec
	#wget https://github.com/neo4j/cypher-shell/archive/${version}.tar.gz -O tmp/SOURCES/${version}.tar.gz
	#wget https://github.com/spacecowboy/cypher-shell/archive/packaging.zip -O tmp/SOURCES/1.0.1.tar.gz
	cd tmp && rpmbuild --define "_topdir $(CURDIR)/tmp" -ba --clean SPECS/cypher-shell.spec
