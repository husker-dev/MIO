package com.husker.mio.processes;

import com.husker.mio.FSUtils;
import com.husker.mio.MIOProcess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZippingProcess extends MIOProcess<ZippingProcess> {

    private File[] toZip;
    private File destination;
    private ZipEntry currentZipEntry;

    public ZippingProcess(){
    }

    public ZippingProcess(File... toZip){
        this.toZip = toZip;
        this.destination = FSUtils.setExtension(toZip[0], "zip");
    }

    public ZippingProcess(File[] toZip, File dest){
        this.toZip = toZip;
        this.destination = dest;
    }

    public ZippingProcess(String... toZip){
        this(FSUtils.stringToFiles(toZip));
    }

    public ZippingProcess(String[] toZip, String dest){
        this(FSUtils.stringToFiles(toZip), new File(dest));
    }

    public ZippingProcess setToZip(File[] file){
        toZip = file;
        if(destination == null)
            destination = FSUtils.setExtension(toZip[0], "zip");
        return this;
    }

    public ZippingProcess setDestination(File file){
        destination = file;
        return this;
    }

    protected void beforeStart() throws Exception {
        if(toZip == null)
            throw new NullPointerException("File to zip is not specified");
        if(destination.exists())
            throw new IOException("Zipped file is already exist");
    }

    protected void run() throws Exception {
        long fullSize = 0;
        for(File file : toZip)
            fullSize += FSUtils.getFileSize(file);
        setFullSize(fullSize);

        safeStream(new ZipOutputStream(new FileOutputStream(destination)), stream -> {
            for(File file : toZip)
                addToArchive(stream, file);
        });
    }

    private void addToArchive(ZipOutputStream zipOut, File toZip) throws Exception {
        List<File> files = FSUtils.getFileTree(toZip, true);
        for(File file : files){
            checkForActive();

            String relativePath = file.getAbsolutePath().replace(new File(toZip.getAbsolutePath()).getParentFile().getAbsolutePath() + File.separator, "");
            zipOut.putNextEntry(currentZipEntry = new ZipEntry(relativePath + (file.isDirectory() ? File.separator : "")));

            if (file.isFile())
                copyStreamData(new FileInputStream(file), zipOut, true, false);

            FSUtils.copyAttributes(file, currentZipEntry);
            zipOut.closeEntry();
        }
    }

    public ZipEntry getCurrentZipEntry(){
        return currentZipEntry;
    }
}
