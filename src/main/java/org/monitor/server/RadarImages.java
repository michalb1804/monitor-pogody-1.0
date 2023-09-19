package org.monitor.server;

import org.monitor.exceptions.ImageDownloadException;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class RadarImages implements DataDownload{
    private LocalDateTime lastUpdate;

    private final String URL_PREFIX;

    private BufferedImage[] images;

    public RadarImages() {
        lastUpdate = null;
        URL_PREFIX =  "https://danepubliczne.imgw.pl/datastore/getfiledown/Oper/Polrad/Produkty/POLCOMP/COMPO_CMAX_250.comp.cmax/";
        images = new BufferedImage[6];
    }
    @Override
    public boolean checkIfAvailable() {
        return HTTPOperations.isSiteUp("https://danepubliczne.imgw.pl");
    }

    @Override
    public boolean checkIfUpdate() {
        System.out.println("Checking available updates in radar images...");

        LocalDateTime lastTimeAvailable = LocalDateTime.now(ZoneOffset.UTC).withSecond(59).withNano(999999999);

        boolean brokenImage = true;

        while(brokenImage) {
            if(lastTimeAvailable.getMinute() % 5 == 0) {
                try {
                    HTTPOperations.downloadImage(URL_PREFIX + lastTimeAvailable.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + "0000dBZ.cmax.png").getHeight();
                    brokenImage = false;
                } catch (ImageDownloadException e) {
                    throw new RuntimeException(e);
                } catch (NullPointerException ptr) {
                    lastTimeAvailable = lastTimeAvailable.minusMinutes(1);
                }
            }
            else {
                lastTimeAvailable = lastTimeAvailable.minusMinutes(1);
            }
        }

        if(lastUpdate == null && checkIfAvailable()) {
            lastUpdate = lastTimeAvailable;
            return true;
        }

        if(lastTimeAvailable.isAfter(lastUpdate)) {
            lastUpdate = lastTimeAvailable;
            return true;
        }

        return false;
    }

    @Override
    public void download() {
        System.out.println("Found new radar image! Downloading...");

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            images[5] = HTTPOperations.downloadImage(URL_PREFIX + lastUpdate.format(formatter) + "0000dBZ.cmax.png");
            images[4] = HTTPOperations.downloadImage(URL_PREFIX + lastUpdate.minusMinutes(5).format(formatter) + "0000dBZ.cmax.png");
            images[3] = HTTPOperations.downloadImage(URL_PREFIX + lastUpdate.minusMinutes(10).format(formatter) + "0000dBZ.cmax.png");
            images[2] = HTTPOperations.downloadImage(URL_PREFIX + lastUpdate.minusMinutes(15).format(formatter) + "0000dBZ.cmax.png");
            images[1] = HTTPOperations.downloadImage(URL_PREFIX + lastUpdate.minusMinutes(20).format(formatter) + "0000dBZ.cmax.png");
            images[0] = HTTPOperations.downloadImage(URL_PREFIX + lastUpdate.minusMinutes(25).format(formatter) + "0000dBZ.cmax.png");
        } catch (ImageDownloadException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Successfully downloaded new radar imaginery.");
    }

    public BufferedImage[] getData() {
        return images;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }
}
