package org.monitor.client;

import org.monitor.server.MonitorServer;
import org.monitor.server.RequestChannel;
import org.monitor.server.RequestStatus;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;

public class MonitorClient {
    private static ResponseStatus responseStatus = ResponseStatus.NO_RESPONSE;
    private static Object[] receivedData = null;

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
        JFrame frame = new JFrame("Monitor Pogody v1.0");
        frame.setLayout(new GridLayout(3, 1));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1080, 720);
        frame.setVisible(true);

        JButton SATDataDownloadButton = new JButton("Show SAT24 data");
        SATDataDownloadButton.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(SATDataDownloadButton);

        JButton RadarDataDownloadButton = new JButton("Show radar data");
        RadarDataDownloadButton.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(RadarDataDownloadButton);

        JTextField loadingMessage = new JTextField();
        loadingMessage.setText("");
        loadingMessage.setFont(new Font("Arial", Font.BOLD, 36));
        loadingMessage.setEditable(false);
        loadingMessage.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(loadingMessage);

        SATDataDownloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadingMessage.setText("SAT24 images downloading... Please wait");
                frame.revalidate();
                frame.repaint();

                new Thread(() -> {
                    sendDataRequest(RequestChannel.SAT_24);

                    while (responseStatus != ResponseStatus.DATA_RECEIVED) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    JFrame satFrame = new JFrame("SAT24 viewer");
                    satFrame.setLayout(new BorderLayout());
                    satFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    satFrame.setSize(1080, 720);

                    JLabel image = new JLabel(new ImageIcon(((BufferedImage) receivedData[0]).getScaledInstance(700,550,0)));
                    satFrame.add(image, BorderLayout.CENTER);

                    JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 5, 0);
                    slider.setMajorTickSpacing(5);
                    slider.setMinorTickSpacing(1);
                    slider.setPaintTicks(true);
                    slider.setPreferredSize(new Dimension(500,75));

                    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
                    labelTable.put(0, new JLabel(MonitorServer.lastTime.minusMinutes(50).format(DateTimeFormatter.ofPattern("HH:mm"))));
                    labelTable.put(1, new JLabel(MonitorServer.lastTime.minusMinutes(40).format(DateTimeFormatter.ofPattern("HH:mm"))));
                    labelTable.put(2, new JLabel(MonitorServer.lastTime.minusMinutes(30).format(DateTimeFormatter.ofPattern("HH:mm"))));
                    labelTable.put(3, new JLabel(MonitorServer.lastTime.minusMinutes(20).format(DateTimeFormatter.ofPattern("HH:mm"))));
                    labelTable.put(4, new JLabel(MonitorServer.lastTime.minusMinutes(10).format(DateTimeFormatter.ofPattern("HH:mm"))));
                    labelTable.put(5, new JLabel(MonitorServer.lastTime.format(DateTimeFormatter.ofPattern("HH:mm"))));

                    slider.setLabelTable(labelTable);
                    slider.setPaintLabels(true);

                    slider.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            int sliderValue = slider.getValue();

                            switch(sliderValue) {
                                case 0:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[0]).getScaledInstance(700,550,0)));
                                    break;
                                case 1:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[1]).getScaledInstance(700,550,0)));
                                    break;
                                case 2:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[2]).getScaledInstance(700,550,0)));
                                    break;
                                case 3:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[3]).getScaledInstance(700,550,0)));
                                    break;
                                case 4:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[4]).getScaledInstance(700,550,0)));
                                    break;
                                case 5:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[5]).getScaledInstance(700,550,0)));
                                    break;
                            }
                        }
                    });

                    satFrame.add(slider, BorderLayout.PAGE_END);
                    satFrame.setVisible(true);

                    SwingUtilities.invokeLater(() -> {
                        loadingMessage.setText("");
                        frame.revalidate();
                        frame.repaint();
                    });
                }).start();
            }
        });

        RadarDataDownloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadingMessage.setText("Radar images downloading... Please wait");
                frame.revalidate();
                frame.repaint();

                new Thread(() -> {
                    sendDataRequest(RequestChannel.RADAR);

                    while (responseStatus != ResponseStatus.DATA_RECEIVED) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    JFrame satFrame = new JFrame("Radar viewer");
                    satFrame.setLayout(new BorderLayout());
                    satFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    satFrame.setSize(1080, 720);

                    JLabel image = new JLabel(new ImageIcon(((BufferedImage) receivedData[0]).getScaledInstance(700,550,0)));
                    satFrame.add(image, BorderLayout.CENTER);

                    JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 5, 0);
                    slider.setMajorTickSpacing(5);
                    slider.setMinorTickSpacing(1);
                    slider.setPaintTicks(true);
                    slider.setPreferredSize(new Dimension(500,75));

                    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
                    labelTable.put(0, new JLabel(MonitorServer.lastTime.minusMinutes(25).format(DateTimeFormatter.ofPattern("HH:mm"))));
                    labelTable.put(1, new JLabel(MonitorServer.lastTime.minusMinutes(20).format(DateTimeFormatter.ofPattern("HH:mm"))));
                    labelTable.put(2, new JLabel(MonitorServer.lastTime.minusMinutes(15).format(DateTimeFormatter.ofPattern("HH:mm"))));
                    labelTable.put(3, new JLabel(MonitorServer.lastTime.minusMinutes(10).format(DateTimeFormatter.ofPattern("HH:mm"))));
                    labelTable.put(4, new JLabel(MonitorServer.lastTime.minusMinutes(5).format(DateTimeFormatter.ofPattern("HH:mm"))));
                    labelTable.put(5, new JLabel(MonitorServer.lastTime.format(DateTimeFormatter.ofPattern("HH:mm"))));

                    slider.setLabelTable(labelTable);
                    slider.setPaintLabels(true);

                    slider.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            int sliderValue = slider.getValue();

                            switch(sliderValue) {
                                case 0:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[0]).getScaledInstance(700,550,0)));
                                    break;
                                case 1:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[1]).getScaledInstance(700,550,0)));
                                    break;
                                case 2:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[2]).getScaledInstance(700,550,0)));
                                    break;
                                case 3:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[3]).getScaledInstance(700,550,0)));
                                    break;
                                case 4:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[4]).getScaledInstance(700,550,0)));
                                    break;
                                case 5:
                                    image.setIcon(new ImageIcon(((BufferedImage) receivedData[5]).getScaledInstance(700,550,0)));
                                    break;
                            }
                        }
                    });

                    satFrame.add(slider, BorderLayout.PAGE_END);
                    satFrame.setVisible(true);

                    SwingUtilities.invokeLater(() -> {
                        loadingMessage.setText("");
                        frame.revalidate();
                        frame.repaint();
                    });
                }).start();
            }
        });
    }

    public static void receiveData(Object[] dataToReceive) {
        receivedData = dataToReceive;
        responseStatus = ResponseStatus.DATA_RECEIVED;
    }
}
