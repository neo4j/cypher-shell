Name: cypher-shell-java11
Provides: cypher-shell
Version: ${VERSION}
Release: ${RELEASE}%{?dist}
Summary: Command line shell for Neo4j

License: GPLv3
URL: https://github.com/neo4j/cypher-shell
Source0: https://github.com/neo4j/cypher-shell/archive/%{version}.tar.gz

Conflicts: cypher-shell-java8
Requires: which, jre >= 11
BuildArch: noarch
Prefix: /usr

%description
A command line shell where you can execute Cypher against an instance
of Neo4j.

%prep
# This macro will unpack the tarball into the appropriate build directory
# Expects tarball to unpack into a directory called {name}-{version}
#%setup -q
%build
#make clean build

%install
rm -rf ${RPM_BUILD_ROOT}
# Calls make with correct DESTDIR
%make_install prefix=/usr

%clean
rm -rf ${RPM_BUILD_ROOT}

%files
%defattr(-,root,root)
%{_bindir}/cypher-shell
%{_datadir}/cypher-shell
%doc %{_mandir}/man1/cypher-shell.1.gz
