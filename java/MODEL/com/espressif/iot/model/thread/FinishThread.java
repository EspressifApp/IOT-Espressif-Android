package com.espressif.iot.model.thread;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class FinishThread extends Thread {
    private LinkedBlockingQueue<String> mFinishQueue = new LinkedBlockingQueue<String>();

    public abstract void execute();

    @Override
    public void run() {
        execute();

        mFinishQueue.add("FINISH");
    }

    public void finish() {
        interrupt();
        try {
            mFinishQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
