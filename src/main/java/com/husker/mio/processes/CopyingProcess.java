package com.husker.mio.processes;

import com.husker.mio.FSUtils;
import com.husker.mio.MIOProcess;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardCopyOption.*;

public class CopyingProcess extends MIOProcess<CopyingProcess> {

    private boolean useNative = false;
    private boolean copyOnlyContent = false;
    private File from, to;
    private File currentFromFile, currentToFile;

    public CopyingProcess(File from, File to, boolean useNative){
        this.from = from;
        this.to = to;
        this.useNative = useNative;
    }

    public CopyingProcess(String from, String to, boolean useNative){
        this.from = new File(from);
        this.to = new File(to);
        this.useNative = useNative;
    }

    public CopyingProcess(File from, File to){
        this.from = from;
        this.to = to;
    }

    public CopyingProcess(String from, String to){
        this.from = new File(from);
        this.to = new File(to);
    }

    public CopyingProcess(){
    }

    public CopyingProcess setFromFile(File file){
        this.from = file;
        return this;
    }

    public CopyingProcess setToFile(File file){
        this.to = file;
        return this;
    }

    public CopyingProcess setFromFile(String file){
        this.from = new File(file);
        return this;
    }

    public CopyingProcess setToFile(String file){
        this.to = new File(file);
        return this;
    }

    public CopyingProcess setCopyOnlyContent(boolean copyOnlyContent){
        this.copyOnlyContent = copyOnlyContent;
        return this;
    }

    public boolean isCopyOnlyContent(){
        return copyOnlyContent;
    }

    public CopyingProcess setUseNative(boolean useNative){
        this.useNative = useNative;
        return this;
    }

    public boolean isUseNative(){
        return useNative;
    }

    public File getCurrentCopyingFromFile(){
        return currentFromFile;
    }

    public File getCurrentCopyingToFile(){
        return currentToFile;
    }

    protected void run() throws Exception {
        setFullSize(FSUtils.getFileSize(from));
        Files.createDirectories(Paths.get(to.getAbsolutePath()));
        if(useNative)
            runNIO();
        else
            runIO();
        listFiles(this::applyAttributes);
    }

    private void runIO() throws Exception{
        listFiles((from, to) -> {
            checkForActive();
            currentFromFile = from;
            currentToFile = to;

            if(from.isFile()){
                copyStreamData(new FileInputStream(from), new FileOutputStream(to));
            }else
                Files.createDirectories(Paths.get(to.getAbsolutePath()));
        });
    }

    private void runNIO() throws Exception{
        listFiles((from, to) -> {
            checkForActive();
            currentFromFile = from;
            currentToFile = to;

            Path source = Paths.get(from.getAbsolutePath());
            Path destination = Paths.get(to.getAbsolutePath());

            Files.copy(source, destination, COPY_ATTRIBUTES);
            if(!Files.isDirectory(destination))
                addCurrent(source.toFile().length());
        });
    }

    private void listFiles(CopyConsumer consumer) throws Exception{
        for(File child : FSUtils.getFileTree(from, !copyOnlyContent)) {
            File childCopy = new File(to, child.getAbsolutePath().replace(getFromPath() + File.separator, ""));
            consumer.accept(child, childCopy);
        }
    }

    private String getFromPath(){
        if(copyOnlyContent)
            return from.getAbsolutePath();
        else
            return new File(from.getAbsolutePath()).getParentFile().getAbsolutePath();
    }

    protected void beforeStart() throws Exception {
        if(from == null)
            throw new NullPointerException("File to copy is not specified");
        if(to == null)
            throw new NullPointerException("Copy destination is not specified");
        if(!Files.exists(Paths.get(from.getAbsolutePath())))
            throw new NullPointerException("File to copy doesn't exist");
    }

    private void applyAttributes(File from, File to){
        try {
            while(true) {
                BasicFileAttributes fromAttributes = Files.readAttributes(Paths.get(from.getAbsolutePath()), BasicFileAttributes.class);
                BasicFileAttributes toAttributes = Files.readAttributes(Paths.get(to.getAbsolutePath()), BasicFileAttributes.class);

                Files.getFileAttributeView(Paths.get(to.getAbsolutePath()), BasicFileAttributeView.class).setTimes(fromAttributes.lastModifiedTime(), fromAttributes.lastAccessTime(), fromAttributes.creationTime());

                if( (fromAttributes.creationTime() == null || fromAttributes.creationTime().toMillis() == toAttributes.creationTime().toMillis()) &&
                        (fromAttributes.lastAccessTime() == null || fromAttributes.lastAccessTime().toMillis() == toAttributes.lastAccessTime().toMillis()) &&
                        (fromAttributes.lastModifiedTime() == null || fromAttributes.lastModifiedTime().toMillis() == toAttributes.lastModifiedTime().toMillis())
                )
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private interface CopyConsumer {
        void accept(File from, File to) throws Exception;
    }
}
