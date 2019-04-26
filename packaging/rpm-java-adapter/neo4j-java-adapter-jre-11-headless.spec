Name:       neo4j-java-adapter-jre-11-headless
Version:    1.0.0
Release:    1%{?dist}
Summary:    Meta package so that Neo4j can be compatible with java 11 headless in both openjdk and oracle
License:    GPLv3

Provides: jre-headless = 11
Requires: jre-11-headless
BuildArch: noarch

%description
Meta package so that Neo4j can be compatible with java 8 and java 11 in both OpenJDK and Oracle.
Contains no source.

%prep
%build
%install
%files