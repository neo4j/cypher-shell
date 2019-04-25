Name:       neo4j-java-adapter-jre-8-headless
Version:    1.0.0
Release:    1%{?dist}
Summary:    Meta package so that Neo4j can be compatible with java 8 from oracle
License:    GPLv3

Provides: jre-headless = 1:1.8.0
Requires: jre-1.8.0
BuildArch: noarch

%description
Meta package so that Neo4j can be compatible with java 8 from Oracle.
This does NOT include the Oracle java package, or any other source code.

%prep
%build
%install
%files