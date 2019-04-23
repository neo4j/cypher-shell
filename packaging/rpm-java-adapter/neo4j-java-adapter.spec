Name:       neo4j-java-adapter
Version:    1.0.0
Release:    1%{?dist}
Summary:    Meta package so that Neo4j can be compatible with java 8 and java 11 in both openjdk and oracle
License:    GPLv3

Provides: jre = 11, java = 11, jre-headless = 11
Requires: jre-11
BuildArch: noarch

%description
Meta package so that Neo4j can be compatible with java 8 and java 11 in both OpenJDK and Oracle.
Contains no source.

%prep
%build
%install
%files