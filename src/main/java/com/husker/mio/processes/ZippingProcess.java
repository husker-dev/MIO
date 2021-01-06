package com.husker.mio.processes;

import com.husker.mio.FSUtils;
import com.husker.mio.MIOProcess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZippingProcess extends MIOProcess<ZippingProcess> {

    private File toZip, destination;
    private ZipEntry currentZipEntry;

    public ZippingProcess(){
    }

    public ZippingProcess(File toZip){
        this.toZip = toZip;
        this.destination = FSUtils.setExtension(toZip, "zip");
    }

    public ZippingProcess(File toZip, File dest){
        this.toZip = toZip;
        this.destination = dest;
    }

    public ZippingProcess(String toZip){
        this(new File(toZip));
    }

    public ZippingProcess(String toZip, String dest){
        this(new File(toZip), new File(dest));
    }

    public ZippingProcess setToZip(File file){
        toZip = file;
        if(destination == null)
            destination = FSUtils.setExtension(toZip, "zip");
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
        setFullSize(FSUtils.getFileSize(toZip));

        FileOutputStream fos = new FileOutputStream(destination);
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        List<File> files = FSUtils.getFileTree(toZip);
        for(File file : files){
            checkForActive();

            ZipEntry zipEntry;

            if (file.isFile()) {
                try(FileInputStream fis = new FileInputStream(file)) {
                    String relativePath = file.getName();
                    if (toZip.isDirectory())
                        relativePath = file.getAbsolutePath().replace(toZip.getAbsolutePath() + File.separator, "");

                    zipEntry = new ZipEntry(relativePath);
                    this.currentZipEntry = zipEntry;
                    zipOut.putNextEntry(zipEntry);
                    byte[] buffer = new byte[getBufferSize()];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        checkForActive();
                        zipOut.write(buffer, 0, length);
                        addCurrent(length);
                    }
                }
            } else {
                zipEntry = new ZipEntry(file.getName() + "/");
                zipOut.putNextEntry(zipEntry);
            }
            applyAttributes(zipEntry, file);
            zipOut.closeEntry();
        }
        zipOut.close();
        fos.close();
    }

    private void applyAttributes(ZipEntry entry, File file){
        try {
            while (true) {
                BasicFileAttributes attributes = Files.readAttributes(Paths.get(file.getAbsolutePath()), BasicFileAttributes.class);
                entry.setCreationTime(attributes.creationTime());
                entry.setLastAccessTime(attributes.lastAccessTime());
                entry.setLastModifiedTime(attributes.lastModifiedTime());

                if ((attributes.creationTime() == null || attributes.creationTime().toMillis() == entry.getCreationTime().toMillis()) &&
                        (attributes.lastAccessTime() == null || attributes.lastAccessTime().toMillis() == entry.getLastAccessTime().toMillis()) &&
                        (attributes.lastModifiedTime() == null || attributes.lastModifiedTime().toMillis() == entry.getLastModifiedTime().toMillis())
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
}
