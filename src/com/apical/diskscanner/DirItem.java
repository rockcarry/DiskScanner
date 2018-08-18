package com.apical.diskscanner;

public class DirItem {
    String  dir;
    int     offset;
    int     number;

    public DirItem(String d, int i, int n) {
        dir    = d;
        offset = i;
        number = n;
    }
}
