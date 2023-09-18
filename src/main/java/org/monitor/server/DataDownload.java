package org.monitor.server;

public interface DataDownload {
    boolean checkIfAvailable();
    boolean checkIfUpdate();
    void download();
}
