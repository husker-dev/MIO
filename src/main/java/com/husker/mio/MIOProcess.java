package com.husker.mio;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class MIOProcess<T extends MIOProcess<?>> {

    private boolean paused = false;
    private boolean stopped = true;
    private final List<Consumer<ProgressArguments<T>>> listeners = new ArrayList<>();
    private Thread thread;

    // Data
    private int bufferSize = 1024;
    private long current, full;

    // Speedometer
    private long speed, speed_current, speed_lastStart;

    protected abstract void run() throws Exception;
    protected abstract void beforeStart() throws Exception;

    public T start() throws Exception {
        if(!stopped)
            return (T) this;
        stopped = false;
        resetSpeedometer();
        beforeStart();
        invokeEvent();
        thread = new Thread(() -> {
            try {
                run();
            }catch (InterruptedException ex){
                // on stop
            }catch (Exception ex){
                ex.printStackTrace();
            }
            stopped = true;
        });
        thread.start();
        return (T) this;
    }

    public void waitForEnd(){
        try {
            thread.join();
        } catch (InterruptedException ignored) {}
    }

    public T startSync() throws Exception {
        start();
        waitForEnd();
        return (T) this;
    }

    public void setBufferSize(int size){
        bufferSize = size;
    }

    public int getBufferSize(){
        return bufferSize;
    }

    public void setPaused(boolean paused){
        if(this.paused == paused)
            return;
        this.paused = paused;
        resetSpeedometer();
    }

    public boolean isPaused(){
        return paused;
    }

    public boolean isStopped(){
        return stopped;
    }

    public void stop(){
        stopped = true;
    }

    public T addProgressListener(Consumer<ProgressArguments<T>> listener){
        listeners.add(listener);
        return (T) this;
    }

    protected void checkForActive() throws InterruptedException {
        if(isStopped())
            throw new InterruptedException("Process is stopped");
        if(isPaused()){
            while(isPaused()){
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public long getFullSize(){
        return full;
    }

    public long getCurrentSize(){
        return current;
    }

    public long getSpeed(){
        return speed;
    }

    public double getPercent(){
        return (100.0 / getFullSize()) * getCurrentSize();
    }

    protected void setFullSize(long full){
        this.full = full;
    }

    protected void addCurrent(long addition){
        current += addition;
        speed_current += addition;
        invokeEvent();
    }

    private void resetSpeedometer(){
        speed_current = 0;
        speed_lastStart = System.currentTimeMillis();
    }

    protected void invokeEvent(){
        double deltaTime = System.currentTimeMillis() - speed_lastStart;
        if(deltaTime > 0)
            speed = (long)(speed_current / deltaTime);
        else
            speed = 0;

        listeners.forEach(listener -> {
            try {
                listener.accept(new ProgressArguments(this));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    protected static <S extends Closeable> void safeStream(S stream, StreamConsumer<S> event) throws Exception{
        safeStream(stream, event, true);
    }

    protected static <S extends Closeable> void safeStream(S stream, StreamConsumer<S> event, boolean close) throws Exception{
        try{
            event.accept(stream);
            if(close)
                stream.close();
        } catch (Exception e){
            if(stream != null)
                stream.close();
            throw e;
        }
    }

    protected static <S1 extends Closeable, S2 extends Closeable> void safeStream(S1 stream1, S2 stream2, StreamBiConsumer<S1, S2> event) throws Exception{
        safeStream(stream1, stream2, event, true, true);
    }

    protected static <S1 extends Closeable, S2 extends Closeable> void safeStream(S1 stream1, S2 stream2, StreamBiConsumer<S1, S2> event, boolean close1, boolean close2) throws Exception{
        try{
            event.accept(stream1, stream2);
            if(close1)
                stream1.close();
            if(close2)
                stream2.close();
        } catch (Exception ex){
            if(stream1 != null)
                stream1.close();
            if(stream2 != null)
                stream2.close();
            throw ex;
        }
    }

    protected interface StreamConsumer<A> {
        void accept(A a) throws Exception;
    }

    protected interface StreamBiConsumer<A, B> {
        void accept(A a, B b) throws Exception;
    }

    protected void copyStreamData(InputStream in, OutputStream os) throws Exception {
        copyStreamData(in, os, true, true);
    }

    protected void copyStreamData(InputStream in, OutputStream os, boolean close1, boolean close2) throws Exception {
        safeStream(in, os, (input, out) -> {
            byte[] buffer = new byte[getBufferSize()];
            int length;
            while ((length = input.read(buffer)) >= 0) {
                checkForActive();
                out.write(buffer, 0, length);
                addCurrent(length);
            }
        }, close1, close2);
    }

}
