package io.split.engine.common;

public interface PushManager {
    void start();
    void stop();
    void startWorkers();
    void stopWorkers();
}
