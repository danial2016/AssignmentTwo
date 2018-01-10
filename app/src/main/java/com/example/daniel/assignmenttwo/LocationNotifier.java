package com.example.daniel.assignmenttwo;

/**
 * Created by Daniel on 2017-10-05.
 */

public class LocationNotifier {
    private long delay = 2000;
    private boolean running = false;
    private Runnable runnable = null;
    private Notifier notifier;

    public LocationNotifier(long delay){
        this.delay = delay;
    }

    void startNotification(){
        notifier = new Notifier();
        running = true;
        notifier.start();
    }

    void stopNotification(){
        notifier.interrupt();
        notifier = null;
    }

    void setNotification(Runnable script){
        this.runnable = script;
    }

    private void performNotification(){
        if(runnable != null){
            runnable.run();
        }
    }

    private class Notifier extends Thread{
        public void run(){
            while(running){
                try {

                    performNotification();
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    running = false;
                }
            }
        }
    }
}
