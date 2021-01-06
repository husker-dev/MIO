package com.husker.mio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
        beforeStart();
        thread = new Thread(() -> {
            try {
                resetSpeedometer();
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
}
