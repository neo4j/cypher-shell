Name:       neo4j-java-adapter-jre-11-oracle
Version:    1.0.0
Release:    1%{?dist}
Summary:    Meta package so that Neo4j can be compatible with java 11 from Oracle.
License:    GPLv3

Provides: jre-11 = 11, java-11 = 11, jre-11-headless = 11
Requires: jre >= 11
Conflicts:
BuildArch: noarch

%description
Meta package so that Neo4j can be compatible with java 11 from Oracle.
Contains no source.

%prep
%build
%install
%files
