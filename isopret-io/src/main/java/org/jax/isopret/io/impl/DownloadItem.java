package org.jax.isopret.io.impl;

import java.net.URL;

public class DownloadItem {

    private final URL url;
    private final String basename;
    private final boolean isGzip;

    public DownloadItem(URL url, String basename){
        this(url, basename, false);
    }
    public DownloadItem(URL url, String basename, boolean gzip){
        this.url = url;
        this.basename = basename;
        this.isGzip = gzip;
    }


    public URL url() {
        return url;
    }

    public String basename() {
        return basename;
    }

    public boolean isGzip() {
        return isGzip;
    }
}
