package org.monitor.client;

import org.monitor.server.MonitorServer;
import org.monitor.server.RequestChannel;
import org.monitor.server.RequestStatus;
import org.monitor.server.Station;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Hashtable;

public class MonitorClient {
    private static ResponseStatus responseStatus = ResponseStatus.NO_RESPONSE;
    private static Object[] receivedData = null;
    private static JFrame frame;
    private static JTextField loadingMessage;

    public static void sendDataRequest(RequestChannel requestedData) {
        synchronized (MonitorServer.class) {
            MonitorServer.setRequestChannel(requestedData);
            MonitorServer.setRequestStatus(RequestStatus.RECEIVED);
            MonitorServer.setClientRequest();
        }
        responseStatus = ResponseStatus.DATA_PROCESSING;
    }

    public static void start() {
        System.out.println("Starting client...");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        startGUI();
    }

    public static void startGUI() {
        frame = new JFrame("Monitor Pogody v1.0");
        frame.setLayout(new GridLayout(4, 1));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1080, 720);
        frame.setVisible(true);

        JButton SATDataDownloadButton = new JButton("Show SAT24 data");
        SATDataDownloadButton.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(SATDataDownloadButton);

        JButton RadarDataDownloadButton = new JButton("Show radar data");
        RadarDataDownloadButton.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(RadarDataDownloadButton);

        JButton StationDataDownloadButton = new JButton("Show IMGW station data");
        StationDataDownloadButton.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(StationDataDownloadButton);

        loadingMessage = new JTextField();
        loadingMessage.setText("");
        loadingMessage.setFont(new Font("Arial", Font.BOLD, 36));
        loadingMessage.setEditable(false);
        loadingMessage.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(loadingMessage);

        SATDataDownloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showData("SAT24", RequestChannel.SAT_24, 50, 10);
            }
        });

        RadarDataDownloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showData("Radar", RequestChannel.RADAR, 25, 5);
            }
        });

        StationDataDownloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadingMessage.setText("IMGW stations data downloading... Please wait");
                frame.revalidate();
                frame.repaint();

                new Thread(() -> {
                    sendDataRequest(RequestChannel.IMGW_STATION);

                    while (responseStatus != ResponseStatus.DATA_RECEIVED) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    showTable();
                }).start();
            }
        });
    }

    private static void showData(String title, RequestChannel requestChannel, int minutesToSubtract, int resolution) {
        loadingMessage.setText(title + " images downloading... Please wait");
        frame.revalidate();
        frame.repaint();

        new Thread(() -> {
            sendDataRequest(requestChannel);

            while (responseStatus != ResponseStatus.DATA_RECEIVED) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            showImageViewer(title, minutesToSubtract, resolution);
        }).start();
    }

    private static void showImageViewer(String title, int minutesToSubtract, int resolution) {
        JFrame imageFrame = new JFrame(title + " viewer");
        imageFrame.setLayout(new BorderLayout());
        imageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        imageFrame.setSize(1080, 720);

        JLabel image = new JLabel(new ImageIcon(((BufferedImage) receivedData[0]).getScaledInstance(700, 550, 0)));
        imageFrame.add(image, BorderLayout.CENTER);

        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 5, 0);
        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPreferredSize(new Dimension(500, 75));

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = 0; i <= 5; i++) {
            int minutes = minutesToSubtract - i * resolution;
            labelTable.put(i, new JLabel(MonitorServer.lastTime.minusMinutes(minutes).format(DateTimeFormatter.ofPattern("HH:mm"))));
        }

        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int sliderValue = slider.getValue();
                image.setIcon(new ImageIcon(((BufferedImage) receivedData[sliderValue]).getScaledInstance(700, 550, 0)));
            }
        });

        imageFrame.add(slider, BorderLayout.PAGE_END);
        imageFrame.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            loadingMessage.setText("");
            frame.revalidate();
            frame.repaint();
        });
    }

    private static void showTable() {
        JFrame stationFrame = new JFrame("Station data viewer");
        stationFrame.setLayout(new BorderLayout());
        stationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        stationFrame.setSize(1080, 720);
        stationFrame.setVisible(true);

        String[] columnNames = {"City", "Time (UTC)", "Temperature (C)", "Wind (kph)", "Humidity (%)", "Rainfall (mm)", "Pressure (hPa)"};
        String[][] data = new String[62][7];

        for(int i = 0; i < 62; i++) {
            data[i][0] = ((Station)receivedData[i]).getCity();
            data[i][1] = ((Station)receivedData[i]).getMeasurementTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            data[i][2] = Float.toString(((Station)receivedData[i]).getTemperature());
            data[i][3] = Integer.toString(((Station)receivedData[i]).getWindSpeed());
            data[i][4] = Float.toString(((Station)receivedData[i]).getHumidity());
            data[i][5] = Float.toString(((Station)receivedData[i]).getTotalRainfall());
            data[i][6] = Float.toString(((Station)receivedData[i]).getPressure());
        }

        JTable table = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());

        for(int i = 2; i <= 6; i++) {
            sorter.setComparator(i, new NumericComparator());
        }

        table.setRowSorter(sorter);

        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getColumn(columnNames[0]).setMinWidth(200);
        table.getColumn(columnNames[2]).setMinWidth(100);
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getDefaultRenderer(Object.class);
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 18));

        JScrollPane sp = new JScrollPane(table);
        stationFrame.add(sp);

        stationFrame.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            loadingMessage.setText("");
            frame.revalidate();
            frame.repaint();
        });

    }

    public static void receiveData(Object[] dataToReceive) {
        receivedData = dataToReceive;
        responseStatus = ResponseStatus.DATA_RECEIVED;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            start();
        });
    }

    private static class NumericComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            try {
                Float float1 = Float.parseFloat(s1);
                Float float2 = Float.parseFloat(s2);
                return float1.compareTo(float2);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}

