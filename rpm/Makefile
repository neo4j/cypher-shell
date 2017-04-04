.PHONY: build

build:
	bash -c "mkdir -p tmp/{BUILD,RPMS,SOURCES,BUILDROOT,SPECS,SRPMS}/"
	cp cypher-shell.spec tmp/SPECS/
	#wget https://github.com/neo4j/cypher-shell/archive/1.0.1.tar.gz -O tmp/SOURCES/1.0.1.tar.gz
	#wget https://github.com/spacecowboy/cypher-shell/archive/packaging.zip -O tmp/SOURCES/1.0.1.tar.gz
	cd tmp && rpmbuild --define "_topdir /home/jonas/workspace/cypher-shell/rpm/tmp" -ba SPECS/cypher-shell.spec
