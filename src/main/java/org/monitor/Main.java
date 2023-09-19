package org.monitor;

import org.monitor.client.MonitorClient;
import org.monitor.server.MonitorServer;
public class Main extends Thread {
    public static void main(String[] args) {
        Thread serverThread = new Thread(MonitorServer::start);
        serverThread.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        MonitorClient.start();
    }
}