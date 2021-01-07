package com.husker.mio;

import com.husker.mio.processes.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class MIO {

    public static void zip(File[] toZip, File destination) throws Exception {
        new ZippingProcess(toZip, destination).startSync();
    }

    public static void zip(File toZip, File destination) throws Exception {
        new ZippingProcess(toZip, destination).startSync();
    }

    public static void zip(File toZip) throws Exception {
        new ZippingProcess(toZip).startSync();
    }

    public static void zip(File[] toZip) throws Exception {
        new ZippingProcess(toZip).startSync();
    }

    public static void zip(String[] toZip) throws Exception {
        new ZippingProcess(toZip).startSync();
    }

    public static void zip(String toZip) throws Exception {
        new ZippingProcess(toZip).startSync();
    }

    public static void zip(String[] toZip, String destination) throws Exception {
        new ZippingProcess(toZip, destination).startSync();
    }

    public static void unzip(File toUnzip, File destination) throws Exception {
        new UnzippingProcess(toUnzip, destination).startSync();
    }

    public static void unzip(File toUnzip) throws Exception {
        new UnzippingProcess(toUnzip).startSync();
    }

    public static void unzip(String toUnzip) throws Exception {
        new UnzippingProcess(toUnzip).startSync();
    }

    public static void unzip(String toUnzip, String destination) throws Exception {
        new UnzippingProcess(toUnzip, destination).startSync();
    }

    public static void delete(File toDelete) throws Exception {
        new DeletingProcess(toDelete).startSync();
    }

    public static void delete(String toDelete) throws Exception {
        new DeletingProcess(toDelete).startSync();
    }

    public static void copy(File from, File to) throws Exception {
        new CopyingProcess(from, to).startSync();
    }

    public static void copy(String from, String to) throws Exception {
        new CopyingProcess(from, to).startSync();
    }

    public static void copy(File from, File to, boolean useNative) throws Exception {
        new CopyingProcess(from, to, useNative).startSync();
    }

    public static void copy(String from, String to, boolean useNative) throws Exception {
        new CopyingProcess(from, to, useNative).startSync();
    }

    public static void download(URL url, File toFile) throws Exception {
        new DownloadingProcess(url, toFile).startSync();
    }

    public static void download(String url, File toFile) throws Exception {
        new DownloadingProcess(url, toFile).startSync();
    }

    public static void download(URL url, String toFile) throws Exception {
        new DownloadingProcess(url, toFile).startSync();
    }

    public static void download(String url, String toFile) throws Exception {
        new DownloadingProcess(url, toFile).startSync();
    }

    public static String readFileText(File file) throws Exception {
        return new TextReadingProcess(file).readText();
    }

    public static String readFileText(String file) throws Exception {
        return new TextReadingProcess(new File(file)).readText();
    }

    public static String readText(File file) throws Exception {
        return new TextReadingProcess(file).readText();
    }

    public static String readText(URL url) throws Exception {
        return new TextReadingProcess(url).readText();
    }

    public static String readText(InputStream inputStream) throws Exception {
        return new TextReadingProcess(inputStream).readText();
    }

    public static void writeText(String text, File file) throws Exception {
        new TextWritingProcess(text, file).startSync();
    }

    public static void writeText(String text, String file) throws Exception {
        new TextWritingProcess(text, new File(file)).startSync();
    }

    public static void writeText(String text, OutputStream outputStream) throws Exception {
        new TextWritingProcess(text, outputStream).startSync();
    }
}
