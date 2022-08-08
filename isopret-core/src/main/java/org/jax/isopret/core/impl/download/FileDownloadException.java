package org.jax.isopret.core.impl.download;

public class FileDownloadException extends Exception {

    public FileDownloadException() {
        super();
    }

    public FileDownloadException(String msg) {
        super(msg);
    }

    public FileDownloadException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
