package com.husker.mio.processes;

import com.husker.mio.MIOProcess;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TextReadingProcess extends MIOProcess<TextReadingProcess> {

    private InputStreamGetter inputStreamGetter;
    private Charset encoding = StandardCharsets.UTF_8;
    private StringBuilder currentString = new StringBuilder();

    public TextReadingProcess(){
    }

    public TextReadingProcess(File file){
        inputStreamGetter = () -> {
            setFullSize(file.length());
            return new FileInputStream(file);
        };
    }

    public TextReadingProcess(InputStream inputStream){
        inputStreamGetter = () -> inputStream;
    }

    public TextReadingProcess(URL url){
        inputStreamGetter = () -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                setFullSize(conn.getContentLengthLong());
            } catch (Exception e) {
                setFullSize(0);
            }
            return url.openStream();
        };
    }

    public TextReadingProcess setInputStream(InputStream inputStream){
        inputStreamGetter = () -> inputStream;
        return this;
    }

    public TextReadingProcess setEncoding(Charset encoding){
        this.encoding = encoding;
        return this;
    }

    public Charset getEncoding(){
        return encoding;
    }

    protected void run() throws Exception {
        currentString = new StringBuilder();

        try(InputStreamReader is = new InputStreamReader(inputStreamGetter.get(), encoding)){
            char[] buffer = new char[getBufferSize()];
            int length;
            while ((length = is.read(buffer)) >= 0) {
                currentString.append(buffer);
                addCurrent(length);
            }
        }
    }

    public String readText() throws Exception {
        startSync();
        return getCurrentText();
    }

    public String getCurrentText(){
        return currentString.toString();
    }

    protected void beforeStart() throws Exception {
        if(inputStreamGetter == null)
            throw new NullPointerException("InputStream is not specified");
    }

    private interface InputStreamGetter{
        InputStream get() throws Exception;
    }
}
