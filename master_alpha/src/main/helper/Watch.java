package main.helper;

public class Watch {
	private long startTime = 0;
    private long stopTime = 0;
    private long totalTime = 0;
    private boolean running = false;

    
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    
    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
        totalTime += stopTime-startTime;
    }

    
    //elaspsed time in milliseconds
    public long getElapsedTime() {
        long elapsed;
        if (running) {
             elapsed = (System.currentTimeMillis() - startTime);
        }
        else {
            elapsed = (stopTime - startTime);
        }
        return elapsed;
    }
    
    
    //elaspsed time in seconds
    public long getElapsedTimeSecs() {
        long elapsed;
        if (running) {
            elapsed = ((System.currentTimeMillis() - startTime) / 1000);
        }
        else {
            elapsed = ((stopTime - startTime) / 1000);
        }
        return elapsed;
    }
    
    //elaspsed time in milliseconds
    public long getTotalElapsedTime() {
        long elapsed;
        if (running) {
             elapsed = (System.currentTimeMillis() - startTime);
             elapsed +=totalTime;
        }
        else {
            elapsed = totalTime;
        }
        return elapsed;
    }
    
    //elaspsed time in seconds
    public long getTotalElapsedTimeSecs() {
        long elapsed;
        if (running) {
            elapsed = ((System.currentTimeMillis() - startTime) );
            elapsed +=totalTime;
            elapsed = elapsed / 1000;
        }
        else {
            elapsed = (totalTime / 1000);
        }
        return elapsed;
    }
}
