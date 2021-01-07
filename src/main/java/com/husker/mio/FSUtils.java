package com.husker.mio;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FSUtils {

    public static long getFileSize(File file){
        if(file.isFile())
            return file.length();
        return getChildren(file).stream().mapToLong(FSUtils::getFileSize).sum();
    }

    public static ArrayList<File> getFileTree(File root){
        return getFileTree(root, false);
    }

    public static ArrayList<File> getFileTree(File root, boolean includeRoot){
        if(root.isFile())
            return new ArrayList<>(Collections.singletonList(root));

        ArrayList<File> children = new ArrayList<>();
        if(includeRoot)
            children.add(root);
        getChildren(root).forEach(child -> children.addAll(getFileTree(child, true)));

        return children;
    }

    public static ArrayList<File> getChildren(File file){
        File[] children = file.listFiles();
        if(children == null)
            return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(children));
    }

    public static File setExtension(File file, String extension){
        if(!file.getName().contains("."))
            return new File(file.getPath() + "." + extension);
        String path = file.getPath();
        return new File(path.replace(path.substring(path.lastIndexOf(".")), "." + extension));
    }

    public static File removeExtension(File file){
        if(!file.getName().contains("."))
            return file;
        String path = file.getPath();
        return new File(path.replace(path.substring(path.lastIndexOf(".")), ""));
    }

    public static long getUnzippedSize(File zip){
        try {
            ZipFile zipfile = new ZipFile(zip);
            Enumeration<? extends ZipEntry> zipEnum = zipfile.entries();

            long size = 0;
            while(zipEnum.hasMoreElements()) {
                ZipEntry entry = zipEnum.nextElement();
                if(!entry.isDirectory() && entry.getSize() != -1)
                    size += entry.getSize();
            }
            zipfile.close();
            return size;
        }catch (Exception ex){
            return -1;
        }
    }

    public static void delete(File file){
        if(file.isDirectory())
            for (File child : FSUtils.getChildren(file))
                delete(child);
        while (Files.exists(Paths.get(file.getAbsolutePath()))) {
            try {
                Files.delete(Paths.get(file.getAbsolutePath()));
            }catch (Exception ignored){}
        }
    }

    public static File[] stringToFiles(String... paths){
        File[] files = new File[paths.length];
        for(int i = 0; i < paths.length; i++)
            files[i] = new File(paths[i]);
        return files;
    }

    public static void copyAttributes(File from, ZipEntry to){
        try {
            while (true) {
                BasicFileAttributes attributes = Files.readAttributes(Paths.get(from.getAbsolutePath()), BasicFileAttributes.class);
                to.setCreationTime(attributes.creationTime());
                to.setLastAccessTime(attributes.lastAccessTime());
                to.setLastModifiedTime(attributes.lastModifiedTime());

                if ((attributes.creationTime() == null || attributes.creationTime().toMillis() == to.getCreationTime().toMillis()) &&
                        (attributes.lastAccessTime() == null || attributes.lastAccessTime().toMillis() == to.getLastAccessTime().toMillis()) &&
                        (attributes.lastModifiedTime() == null || attributes.lastModifiedTime().toMillis() == to.getLastModifiedTime().toMillis())
                )
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
