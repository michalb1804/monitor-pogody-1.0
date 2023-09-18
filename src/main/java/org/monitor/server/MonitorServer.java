package org.monitor.server;

public class MonitorServer {
    private static boolean toClose = false;
    private static boolean clientRequest = false;
    private static String requestChannel = "";
    private static RequestStatus requestStatus = RequestStatus.NO_REQUEST;
    public static void start() {
        System.out.println("Starting server...");

        //INICJALIZACJA OBIEKTÓW ZWIĄZANYCH Z DANYMI
        SatelliteImages satellite = new SatelliteImages();
        System.out.println("\n-------------------\nSAT24 server available: " + satellite.checkIfAvailable() + "\n");

        System.out.println("Server has started.\n");

//        //SPRAWDZENIE CO MINUTĘ CZY DOSTĘPNE AKTUALIZACJE - JEŚLI TAK - POBIERZ DANE NA NOWO I WYŚLIJ DO KLIENTA
//        while(!toClose) {
//            if(satellite.checkIfUpdate()) {
//                satellite.download();
//                sendToClient();
//            }
//
//            try {
//                Thread.sleep(60000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }

        //CIĄGŁE SPRAWDZANIE CZY NIE PRZYSZEDŁ REQUEST OD KLIENTA O DANE
        while(!toClose) {
            if(clientRequest) {
                clientRequest = false;

                switch(requestChannel) {
                    case "sat-24":
                        requestStatus = RequestStatus.PROCESSING;

                        if(satellite.checkIfUpdate())
                            satellite.download();

                        requestStatus = RequestStatus.READY;

                        sendToClient();
                        break;
                    default:
                        requestStatus = RequestStatus.ERROR;
                        break;
                }
            }
        }

        System.out.println("Server shutdown.");
        System.exit(0);
    }

    public static void sendToClient() {

    }

    public static void setRequestStatus(RequestStatus status) {
        requestStatus = status;
    }

    public static void setRequestChannel(String request) {
        requestChannel = request;
    }
}
