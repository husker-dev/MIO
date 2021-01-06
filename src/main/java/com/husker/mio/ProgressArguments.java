package com.husker.mio;

public class ProgressArguments<T extends MIOProcess<?>> {

    private final long full, current, speed;
    private final double percent;
    private final MIOProcess<T> process;

    public ProgressArguments(MIOProcess<T> process){
        this.process = process;
        this.full = process.getFullSize();
        this.current = process.getCurrentSize();
        this.speed = process.getSpeed();
        this.percent = process.getPercent();
    }

    public T getProcess(){
        return (T) process;
    }

    public double getPercent(){
        return percent;
    }

    public long getCurrentSize(){
        return current;
    }

    public long getFullSize(){
        return full;
    }

    public long getSpeed(){
        return speed;
    }

    public String toString() {
        return "ProgressArguments{" +
                "process=" + process.getClass().getSimpleName() +
                ", " + current + "/" + full +
                ", speed=" + speed +
                ", percent=" + percent + "%" +
                '}';
    }
}
