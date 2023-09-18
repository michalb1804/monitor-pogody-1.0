package org.monitor.server;

import org.jsoup.nodes.Document;
import org.monitor.exceptions.ImageDownloadException;

import java.awt.image.RenderedImage;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class SatelliteImages implements DataDownload {
    private LocalDateTime lastUpdate;
    private LocalDateTime newUpdateTime;
    private final String URL_PREFIX;

    private RenderedImage[] images;

    public SatelliteImages() {
        lastUpdate = null;
        URL_PREFIX = "https://pl.sat24.com/image?type=visual5HDComplete&region=pl&timestamp=";
        images = new RenderedImage[6];
    }
    @Override
    public boolean checkIfAvailable() {
        return HTTPOperations.isSiteUp("http://www.sat24.com/pl/pl");
    }
    @Override
    public boolean checkIfUpdate() {
        System.out.println("Checking available updates in SAT24 images...");

        Document siteContent = HTTPOperations.getHTMLContent("http://www.sat24.com/pl/pl");
        assert siteContent != null;
        String[] lastHour = siteContent
                .getElementsByClass("image-tab")
                .get(9)
                .children()
                .get(1)
                .attributes()
                .get("data-utc")
                .split(":");

        LocalDateTime dateNewest = LocalDateTime.now(ZoneOffset.UTC);
        dateNewest = dateNewest.withHour(Integer.parseInt(lastHour[0]));
        newUpdateTime = dateNewest.withMinute(Integer.parseInt(lastHour[1]));

        if (lastUpdate == null && checkIfAvailable()) {
            return true;
        }

        return lastUpdate.isBefore(newUpdateTime);
    }

    @Override
    public void download() {
        System.out.println("Found new SAT24 image! Downloading...");

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            images[5] = HTTPOperations.downloadImage(URL_PREFIX + newUpdateTime.format(formatter));
            images[4] = HTTPOperations.downloadImage(URL_PREFIX + newUpdateTime.minusMinutes(10).format(formatter));
            images[3] = HTTPOperations.downloadImage(URL_PREFIX + newUpdateTime.minusMinutes(20).format(formatter));
            images[2] = HTTPOperations.downloadImage(URL_PREFIX + newUpdateTime.minusMinutes(30).format(formatter));
            images[1] = HTTPOperations.downloadImage(URL_PREFIX + newUpdateTime.minusMinutes(40).format(formatter));
            images[0] = HTTPOperations.downloadImage(URL_PREFIX + newUpdateTime.minusMinutes(50).format(formatter));
        } catch (ImageDownloadException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Successfully downloaded new SAT24 imaginery.");
    }

    public RenderedImage[] getData() {
        return images;
    }
}
