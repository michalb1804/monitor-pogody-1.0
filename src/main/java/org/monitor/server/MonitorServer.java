//package org.monitor.server;
//
//import org.monitor.client.MonitorClient;
//
//public class MonitorServer {
//    private static boolean toClose = false;
//    private static boolean clientRequest = false;
//    private static String requestChannel = "";
//    private static RequestStatus requestStatus = RequestStatus.NO_REQUEST;
//    public static void start() {
//        System.out.println("Starting server...");
//
//        //INICJALIZACJA OBIEKTÓW ZWIĄZANYCH Z DANYMI
//        SatelliteImages satellite = new SatelliteImages();
//        System.out.println("\n-------------------\nSAT24 server available: " + satellite.checkIfAvailable() + "\n");
//
//        System.out.println("Server has started.\n");
//
//        //CIĄGŁE SPRAWDZANIE CZY NIE PRZYSZEDŁ REQUEST OD KLIENTA O DANE
//        while(!toClose) {
//            synchronized (MonitorServer.class) {
//                if(clientRequest) {
//                    clientRequest = false;
//
//                    switch(requestChannel) {
//                        case "sat-24":
//                            requestStatus = RequestStatus.PROCESSING;
//
//                            if(satellite.checkIfUpdate())
//                                satellite.download();
//
//                            requestStatus = RequestStatus.READY;
//
//                            sendToClient(satellite.getData());
//                            break;
//                        default:
//                            requestStatus = RequestStatus.ERROR;
//                            break;
//                    }
//                }
//            }
//        }
//
//        System.out.println("Server shutdown.");
//        System.exit(0);
//    }
//
//    public static void sendToClient(Object[] requestedData) {
//        MonitorClient.changeResponseStatus();
//        MonitorClient.receiveData(requestedData);
//        clientRequest = false;
//        requestChannel = "";
//        requestStatus = RequestStatus.NO_REQUEST;
//    }
//
//    public static void setRequestStatus(RequestStatus status) {
//        requestStatus = status;
//    }
//
//    public static RequestStatus getRequestStatus() {
//        return requestStatus;
//    }
//
//    public static void setRequestChannel(String request) {
//        requestChannel = request;
//    }
//
//    public static void setClientRequest() {
//        clientRequest = true;
//    }
//
//    public static String getRequestChannel() {
//        return requestChannel;
//    }
//
//    public static boolean getClientRequestStatus() {
//        return clientRequest;
//    }
//}

package org.monitor.server;

import org.monitor.client.MonitorClient;

import java.time.LocalDateTime;

public class MonitorServer {
    private static boolean toClose = false;
    private static boolean isClientRequestPending = false;
    private static RequestChannel requestChannel = RequestChannel.NO_REQUEST;
    private static RequestStatus requestStatus = RequestStatus.NO_REQUEST;

    public static LocalDateTime lastTime;

    public static void start() {
        System.out.println("Starting server...");

        SatelliteImages satellite = new SatelliteImages();
        RadarImages radar = new RadarImages();
        IMGWStationData stationData = new IMGWStationData();
        System.out.println("\n-------------------\nSAT24 server available: " + satellite.checkIfAvailable());
        System.out.println("IMGW database available: " + radar.checkIfAvailable() + "\n");

        System.out.println("Server has started.\n");

        while (!toClose) {
            synchronized (MonitorServer.class) {
                try {
                    while (!isClientRequestPending) {
                        MonitorServer.class.wait();
                    }

                    isClientRequestPending = false;

                    switch (requestChannel) {
                        case SAT_24:
                            requestStatus = RequestStatus.PROCESSING;

                            if (satellite.checkIfUpdate()) {
                                satellite.download();
                            }

                            lastTime = satellite.getLastUpdate();

                            requestStatus = RequestStatus.READY;

                            sendToClient(satellite.getData());
                            break;
                        case RADAR:
                            requestStatus = RequestStatus.PROCESSING;

                            if (radar.checkIfUpdate()) {
                                radar.download();
                            }

                            lastTime = radar.getLastUpdate();

                            requestStatus = RequestStatus.READY;

                            sendToClient(radar.getData());
                            break;
                        case IMGW_STATION:
                            requestStatus = RequestStatus.PROCESSING;

                            if(stationData.checkIfUpdate()) {
                                stationData.download();
                            }

                            requestStatus = RequestStatus.READY;

                            sendToClient(stationData.getData());
                            break;
                        default:
                            requestStatus = RequestStatus.ERROR;
                            break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("Server shutdown.");
        System.exit(0);
    }

    public static void sendToClient(Object[] requestedData) {
        //MonitorClient.changeResponseStatus();
        MonitorClient.receiveData(requestedData);
        requestChannel = RequestChannel.NO_REQUEST;
        requestStatus = RequestStatus.NO_REQUEST;
    }

    public static void setRequestStatus(RequestStatus status) {
        requestStatus = status;
    }

    public static RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public static void setRequestChannel(RequestChannel request) {
        requestChannel = request;
    }

    public static void setClientRequest() {
        isClientRequestPending = true;
        synchronized (MonitorServer.class) {
            MonitorServer.class.notify();
        }
    }

    public static RequestChannel getRequestChannel() {
        return requestChannel;
    }

    public static boolean isClientRequestPending() {
        return isClientRequestPending;
    }

    public static void closeServer() {
        toClose = true;
        synchronized (MonitorServer.class) {
            MonitorServer.class.notify();
        }
    }
}
