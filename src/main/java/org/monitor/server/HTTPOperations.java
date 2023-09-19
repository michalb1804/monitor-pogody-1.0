package org.monitor.server;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.monitor.exceptions.ImageDownloadException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPOperations {
    public static boolean isSiteUp(String site) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(site).openConnection();

            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException ioException) {
            return false;
        }
    }

    public static BufferedImage downloadImage(String site) throws ImageDownloadException {
        try {
            return ImageIO.read(new URL(site));
        } catch (IOException ioException) {
            throw new ImageDownloadException("Wystąpił błąd przy pobieraniu obrazka.");
        }
    }

    public static Document getHTMLContent(String site) {
        try {
            return Jsoup.connect(site).get();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }
}
