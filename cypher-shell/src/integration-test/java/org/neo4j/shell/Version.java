package org.neo4j.shell;

import static java.lang.String.format;

@SuppressWarnings("WeakerAccess")
public class Version implements Comparable<Version> {
    private final int major;
    private final int minor;
    private final int patch;

    Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int major() {
        return major;
    }

    public int minor() {
        return minor;
    }

    public int patch() {
        return patch;
    }

    @Override
    public int compareTo(Version o) {
        int comp = Integer.compare(major, o.major);
        if (comp == 0) {
            comp = Integer.compare(minor, o.minor);
            if (comp == 0) {
                comp = Integer.compare(patch, o.patch);
            }
        }
        return comp;
    }

    @Override
    public String toString() {
        return format("%d.%d.%d", major, minor, patch);
    }
}
