package org.monitor.server;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class IMGWStationData implements DataDownload{
    private Station[] stationsData;

    public IMGWStationData() {
        stationsData = new Station[62];
    }
    @Override
    public boolean checkIfAvailable() {
        return HTTPOperations.isSiteUp("https://danepubliczne.imgw.pl");
    }

    @Override
    public boolean checkIfUpdate() {
        return true;
    }

    @Override
    public void download() {
        System.out.println("Downloading station data from IMGW servers...");

        try {
            JSONArray stationData = new JSONArray(Jsoup.connect("https://danepubliczne.imgw.pl/api/data/synop/format/json").ignoreContentType(true).execute().body());

            JSONObject tempObj = null;

            LocalDateTime tempDate = null;

            String[] tempSplitted = null;

            for(int i = 0; i < stationData.length(); i++) {
                tempObj = stationData.getJSONObject(i);

                tempSplitted = (tempObj.get("data_pomiaru") == JSONObject.NULL ? "" : tempObj.getString("data_pomiaru")).split("-");

                if(tempSplitted.length > 0) {
                    tempDate = LocalDateTime.of(Integer.parseInt(tempSplitted[0]),
                            Integer.parseInt(tempSplitted[1]),
                            Integer.parseInt(tempSplitted[2]),
                            tempObj.getInt("godzina_pomiaru"), 0);
                }
                else {
                    tempDate = LocalDateTime.of(9999,9,9,9,9);
                }

                stationsData[i] = new Station(tempObj.getString("stacja"),
                        tempDate,
                        tempObj.get("temperatura") == JSONObject.NULL ? -999 : tempObj.getFloat("temperatura"),
                        tempObj.get("predkosc_wiatru") == JSONObject.NULL ? -999 : tempObj.getInt("predkosc_wiatru"),
                        tempObj.get("wilgotnosc_wzgledna") == JSONObject.NULL ? -999 : tempObj.getFloat("wilgotnosc_wzgledna"),
                        tempObj.get("suma_opadu") == JSONObject.NULL ? -999 : tempObj.getFloat("suma_opadu"),
                        tempObj.get("cisnienie") == JSONObject.NULL ? -999 : tempObj.getFloat("cisnienie"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Station[] getData() {
        return stationsData;
    }
}
