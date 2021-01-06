package com.husker.mio.processes;

import com.husker.mio.FSUtils;
import com.husker.mio.MIOProcess;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class DeletingProcess extends MIOProcess<DeletingProcess> {

    private File toDelete;
    private File currentFile;

    public DeletingProcess(){
    }

    public DeletingProcess(File toDelete){
        this.toDelete = toDelete;
    }

    public DeletingProcess(String toDelete){
        this.toDelete = new File(toDelete);
    }

    public DeletingProcess setToDelete(File file){
        this.toDelete = file;
        return this;
    }

    public DeletingProcess setToDelete(String file){
        toDelete = new File(file);
        return this;
    }

    protected void run() throws Exception {
        if(!Files.exists(Paths.get(toDelete.getAbsolutePath())))
            return;
        setFullSize(FSUtils.getFileSize(toDelete));
        if(toDelete.isDirectory()) {
            ArrayList<File> allFiles = FSUtils.getFileTree(toDelete, true);
            ArrayList<File> files = allFiles.stream().filter(File::isFile).collect(Collectors.toCollection(ArrayList::new));
            ArrayList<File> folders = allFiles.stream().filter(File::isDirectory).collect(Collectors.toCollection(ArrayList::new));

            for(File file : files) {
                checkForActive();
                currentFile = file;
                FSUtils.delete(file);
                addCurrent(file.length());
            }
            for(File folder : folders)
                FSUtils.delete(folder);
        }else{
            FSUtils.delete(toDelete);
            addCurrent(toDelete.length());
        }
    }

    protected void beforeStart() throws Exception {
        if(toDelete == null)
            throw new NullPointerException("File to delete is not specified");
    }

    public File getCurrentDeletingFile(){
        return currentFile;
    }
}
