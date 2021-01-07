package com.husker.mio.processes;

import com.husker.mio.FSUtils;
import com.husker.mio.MIOProcess;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class UnzippingProcess extends MIOProcess<UnzippingProcess> {

    private File toUnzip, destination;
    private ZipEntry currentZipEntry;

    public UnzippingProcess(){
    }

    public UnzippingProcess(File toUnzip, File destination){
        this.toUnzip = toUnzip;
        this.destination = destination;
    }

    public UnzippingProcess(File toUnzip){
        this.toUnzip = toUnzip;
        this.destination = new File(toUnzip.getAbsolutePath()).getParentFile();
    }

    public UnzippingProcess(String toUnzip, String destination){
        this(new File(toUnzip), new File(destination));
    }

    public UnzippingProcess(String toUnzip){
        this(new File(toUnzip));
    }

    public UnzippingProcess setToUnzip(File file){
        toUnzip = file;
        return this;
    }

    public UnzippingProcess setDestination(File file){
        destination = file;
        return this;
    }

    protected void beforeStart() throws Exception {
        if(toUnzip == null)
            throw new NullPointerException("File to unzip is not specified");
        if(destination == null)
            throw new NullPointerException("Destination file is not specified");
    }

    protected void run() throws Exception {
        setFullSize(FSUtils.getUnzippedSize(toUnzip));

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(toUnzip));
        ZipEntry entry = zipIn.getNextEntry();

        while (entry != null) {
            checkForActive();
            File file = new File(destination + File.separator + entry.getName());
            currentZipEntry = entry;

            if (!entry.isDirectory())
                copyStreamData(zipIn, new FileOutputStream(file), false, true);
            else {
                try {
                    Files.createDirectories(Paths.get(file.getAbsolutePath()));
                }catch (Exception ignored){}
            }

            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();

        // Applying file attributes
        ZipFile zipfile = new ZipFile(toUnzip);
        Enumeration<? extends ZipEntry> zipEnum = zipfile.entries();
        while(zipEnum.hasMoreElements()) {
            checkForActive();
            entry = zipEnum.nextElement();
            File file = new File(destination + File.separator + entry.getName());
            applyAttributes(file, entry);
        }
        zipfile.close();
    }

    private void applyAttributes(File file, ZipEntry entry){
        try {
            while(true) {
                Files.getFileAttributeView(Paths.get(file.getAbsolutePath()), BasicFileAttributeView.class).setTimes(entry.getLastModifiedTime(), entry.getLastAccessTime(), entry.getCreationTime());

                BasicFileAttributes attributes = Files.readAttributes(Paths.get(file.getAbsolutePath()), BasicFileAttributes.class);

                if( (entry.getCreationTime() == null || attributes.creationTime().toMillis() == entry.getCreationTime().toMillis()) &&
                        (entry.getLastAccessTime() == null || attributes.lastAccessTime().toMillis() == entry.getLastAccessTime().toMillis()) &&
                        (entry.getLastModifiedTime() == null || attributes.lastModifiedTime().toMillis() == entry.getLastModifiedTime().toMillis())
                )
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public ZipEntry getCurrentZipEntry(){
        return currentZipEntry;
    }

    public String getCurrentFileName(){
        return getCurrentZipEntry().getName();
    }
}
