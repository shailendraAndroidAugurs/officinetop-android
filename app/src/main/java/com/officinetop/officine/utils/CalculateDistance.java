package com.officinetop.officine.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.officinetop.officine.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

class CalculateDistance extends AsyncTask<String, String, String> {
    private final Context context;
    private final String SourcesLat;
    private final String SourcesLong;
    private final String DesLat;
    private final String DestLong;
    private final TextView tv_workshopKm;

    public CalculateDistance(Context context, String SourcesLat, String SourcesLong, String DesLat, String DestLong, TextView textView) {
        this.context = context;
        this.SourcesLat = SourcesLat;
        this.SourcesLong = SourcesLong;
        this.DesLat = DesLat;
        this.DestLong = DestLong;
        this.tv_workshopKm = textView;
        /*Log.d("CalculateDistanceClass", "SLat" +SourcesLat);
        Log.d("CalculateDistanceClass", "SLong" +SourcesLong);
        Log.d("CalculateDistanceClass", "DLat" +DesLat);
        Log.d("CalculateDistanceClass", "DLong" +DestLong);
*/

    }

    @Override
    protected String doInBackground(String... params) {
        BufferedReader reader = null;
        HttpsURLConnection urlConnection = null;
        URL url;
        String forecastJsonStr;
        try {

            /*Log.d("CalculateDistanceClass", "SLat" +SourcesLat);
            Log.d("CalculateDistanceClass", "SLong" +SourcesLong);
            Log.d("CalculateDistanceClass", "DLat" +DesLat);
            Log.d("CalculateDistanceClass", "DLong" +DestLong);*/


            url = new URL("https://maps.google.com/maps/api/directions/json?origin="
                    + SourcesLat + "," + SourcesLong + "&destination=" + DesLat
                    + "," + DestLong + "&sensor=false&mode=driving&units=metric &key=" + context.getString(R.string.google_map_key));
//&sensor=false&mode=driving&alternatives=true

            //params[0] : Source latitude
            //params[1] : Source longitude
            //params[2] : Destination latitude
            //params[3] : Destination longitude
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            Log.d("CalculateDistanceClass", "url: " +url.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream inputStream;
        try {
            assert urlConnection != null;
            inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
            return forecastJsonStr;
        } catch (IOException e) {
            Log.e("Exception", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("Exception", "Error closing stream", e);
                }
            }
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            JSONObject object = new JSONObject(s);
            if (object.get("status").equals("OK ")) {
                JSONArray routesArry = object.optJSONArray("routes");
                if (routesArry != null && routesArry.length() > 0) {
                    JSONObject routesObject = routesArry.optJSONObject(0);
                    if (routesObject != null) {
                        JSONArray legArrry = routesObject.optJSONArray("legs");
                        if (legArrry != null && legArrry.length() > 0) {
                            JSONObject legObject = legArrry.optJSONObject(0);
                            if (legObject != null) {
                                JSONObject distanceObject = legObject.optJSONObject("distance");
                                if (distanceObject != null) {
                                    String totalDistance = distanceObject.optString("text");
                                    Log.e("TotalDistance", totalDistance);

                                    //Distance may come in meter or kilometer, If distance between source and destination is less than 1KM it shows distane in meter
                                    double dis;
                                    if (totalDistance.contains("km")) {
                                        totalDistance = totalDistance.replace(" km", "");
                                        dis = Double.parseDouble(totalDistance);
                                    } else {
                                        totalDistance = totalDistance.replace(" m", "");
                                        dis = Double.parseDouble(totalDistance) / 1000;
                                    }

                                    totalDistance = String.valueOf(dis);
                                    Log.e("TotalDistance in km/m", totalDistance);
                                    Log.d(" test Distance", totalDistance);
                                    tv_workshopKm.setText(context.getString(R.string.prepend_euro_symbol_string/*append_km*/, totalDistance));
                                }
                            }
                        }
                    }
                }
            }
        } catch (
                JSONException e) {
            e.printStackTrace();
        }
    }
}
