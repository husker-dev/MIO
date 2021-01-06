package com.husker.mio.processes;

import com.husker.mio.MIOProcess;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadingProcess extends MIOProcess<DownloadingProcess> {

    private URL url;
    private File toFile;

    public DownloadingProcess(){
    }

    public DownloadingProcess(String url, File toFile) throws MalformedURLException {
        this.url = new URL(url);
        this.toFile = toFile;
    }

    public DownloadingProcess(URL url, File toFile){
        this.url = url;
        this.toFile = toFile;
    }

    public DownloadingProcess(String url, String toFile) throws MalformedURLException {
        this.url = new URL(url);
        this.toFile = new File(toFile);
    }

    public DownloadingProcess(URL url, String toFile){
        this.url = url;
        this.toFile = new File(toFile);
    }

    public DownloadingProcess setUrl(String url) throws MalformedURLException {
        this.url = new URL(url);
        return this;
    }

    public DownloadingProcess setUrl(URL url) {
        this.url = url;
        return this;
    }

    public DownloadingProcess setToFile(File file){
        this.toFile = file;
        return this;
    }

    public DownloadingProcess setToFile(String file){
        this.toFile = new File(file);
        return this;
    }

    protected void run() throws Exception {
        setFullSize(getFileSize());
        try (BufferedInputStream is = new BufferedInputStream(url.openStream())) {
            FileOutputStream fileOutputStream = new FileOutputStream(toFile.getAbsolutePath());
            byte[] buffer = new byte[getBufferSize()];
            int length;
            while ((length = is.read(buffer)) >= 0) {
                fileOutputStream.write(buffer, 0, length);
                addCurrent(length);
            }
            fileOutputStream.close();
        }
    }

    protected void beforeStart() throws Exception {
        if(url == null)
            throw new NullPointerException("Url is not specified");
        if(toFile == null)
            throw new NullPointerException("Download destination is not specified");
    }

    private long getFileSize() throws Exception{
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            return conn.getContentLengthLong();
        } catch (Exception e) {
            return 0;
        }
    }
}
