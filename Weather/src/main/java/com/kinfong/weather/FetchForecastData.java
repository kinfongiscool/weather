package com.kinfong.weather;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Vic on 13/8/26.
 * ude@learnovatelabs.com
 */
public class FetchForecastData {

    private final String API_URL = "https://api.forecast.io/forecast/";
    private final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private String API_KEY;

    protected int status;
    private HttpResponse response;
    protected JSONObject data;

    AndroidHttpClient client = AndroidHttpClient.newInstance("Android");


    public FetchForecastData(Double latitude, Double longitude, String API_KEY){
        this.API_KEY = API_KEY;
        String forecastUrl = buildForecastUrl(latitude, longitude);
        new FetchDataAsync().execute(forecastUrl);
    }

    /**
     * Build forecast url for Dark Sky API.
     * @param latitude Double latitude desired
     * @param longitude Double longitude desired
     * @return String formatted string for use in Dark Sky API
     */
    private String buildForecastUrl(Double latitude, Double longitude) {
        return API_URL + API_KEY + "/" + latitude.toString() + "," + longitude.toString();
    }

    public HttpResponse getResponse() {
        return response;
    }

    public JSONObject getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }

    /**
     * AsyncTask to retrieve data from Dark Sky API service.
     */
    private class FetchDataAsync extends AsyncTask <String, Void, HttpResponse> {

        @Override
        protected HttpResponse doInBackground(String... urls) {
            String link = urls[0];
            HttpGet request = new HttpGet(link);
            AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
            HttpResponse response = null;

            try {
                response = client.execute(request);
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    response.getEntity().writeTo(output);
                    output.close();

                    String result = output.toString();

                    data = new JSONObject(result);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPostExecute(HttpResponse res){
            status = res.getStatusLine().getStatusCode();
            response = res;
            client.close();
        }
    }
}