package org.monitor;

import org.monitor.server.MonitorServer;
public class Main extends Thread {
    public static void main(String[] args) {
        Thread serverThread = new Thread(MonitorServer::start);
        serverThread.start();
    }
}