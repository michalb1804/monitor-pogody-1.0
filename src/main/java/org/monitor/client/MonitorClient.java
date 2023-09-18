package org.monitor.client;

import org.monitor.server.MonitorServer;
import org.monitor.server.RequestStatus;

public class MonitorClient {
    public static void sendDataRequest(String requestedData) {
        MonitorServer.setRequestChannel(requestedData);
        MonitorServer.setRequestStatus(RequestStatus.RECEIVED);
    }

    public static void waitForData() {

    }
}
