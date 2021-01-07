package com.husker.mio.processes;

import com.husker.mio.MIOProcess;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TextWritingProcess extends MIOProcess<TextWritingProcess> {

    private OutputStreamGetter outputStreamGetter;
    private String text;
    private Charset encoding = StandardCharsets.UTF_8;

    public TextWritingProcess(){
    }

    public TextWritingProcess(String text, File file){
        outputStreamGetter = () -> new FileOutputStream(file, false);
        this.text = text;
    }

    public TextWritingProcess(String text, File file, boolean append){
        outputStreamGetter = () -> new FileOutputStream(file, append);
        this.text = text;
    }

    public TextWritingProcess(String text, OutputStream outputStream){
        outputStreamGetter = () -> outputStream;
        this.text = text;
    }

    public TextWritingProcess setOutputStream(OutputStream outputStream){
        outputStreamGetter = () -> outputStream;
        return this;
    }

    public TextWritingProcess setEncoding(Charset encoding){
        this.encoding = encoding;
        return this;
    }

    public Charset getEncoding(){
        return encoding;
    }

    protected void run() throws Exception {
        setFullSize(text.getBytes(encoding).length);
        copyStreamData(new ByteArrayInputStream(text.getBytes(encoding)), outputStreamGetter.get());
    }

    protected void beforeStart() throws Exception {
        if(outputStreamGetter == null)
            throw new NullPointerException("OutputStream is not specified");
        if(text == null)
            throw new NullPointerException("Text is not specified");
    }

    private interface OutputStreamGetter{
        OutputStream get() throws Exception;
    }
}
