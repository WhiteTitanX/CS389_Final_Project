package com.cs389f20.diamonds;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DBManager {
    private RequestQueue queue;
    private MainActivity ma;
    final private Handler handler = new Handler(Looper.getMainLooper());
    Runnable dbConnectionRefresh;
    private static final String LOG_TAG = DBManager.class.getSimpleName(), ACCESS_TOKEN = "tlbJzoaAl6m81uXG3xU76zIIiVthlym3jBP94Q90";
    private long lastUpdated;

    public DBManager(MainActivity main) {
        ma = main;
    }

    public void connectToDatabase() {
        connectToDatabase("current");
        connectToDatabase("past");
    }

    private void connectToDatabase(final String type) {
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(ma);
        final String url = (type.equals("current") ? "https://9hel5x9p0d.execute-api.us-east-1.amazonaws.com/dev/overseer" : "https://isz3wa9q77.execute-api.us-east-1.amazonaws.com/dev/overseerhistory");

        if (type.equals("current")) {
            // Request a string response from the provided URL.
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            lastUpdated = System.currentTimeMillis();
                            ma.storeData(response, null);
                        }
                    }, errorResponse(type)) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/json");
                    params.put("x-api-key", ACCESS_TOKEN);
                    return params;
                }
            };
            queue.add(jsonObjectRequest);
        } else {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            lastUpdated = System.currentTimeMillis();
                            ma.storeData(null, response);
                        }
                    }, errorResponse(type)) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/json");
                    params.put("x-api-key", ACCESS_TOKEN);
                    return params;
                }
            };
            queue.add(jsonArrayRequest);
        }
    }

    public void destroyDBHandler() {
        handler.removeCallbacks(dbConnectionRefresh);
    }

    public RequestQueue getQueue() {
        return queue;
    }

    public long getLastUpdated() {
        return System.currentTimeMillis() - lastUpdated;
    }

    private Response.ErrorListener errorResponse(final String type) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "ERROR: Couldn't connect to database ");
                int REFRESH_INTERVAL = 10;
                //Can't connect to internet
                if (Objects.requireNonNull(error.getCause()).toString().contains("UnknownHostException")) {
                    Toast.makeText(ma.getApplicationContext(), "Can't connect to the internet.", Toast.LENGTH_LONG).show();
                    Log.w(LOG_TAG, "Can't connect to internet. Retrying every " + REFRESH_INTERVAL + " seconds");

                } else {
                    Toast.makeText(ma.getApplicationContext(), "Can't connect to database. Try restarting the app.", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                    REFRESH_INTERVAL = 15;
                }
                dbConnectionRefresh = new Runnable() {
                    @Override
                    public void run() {
                        if (!type.equals("current") && !type.equals("past"))
                            ma.getDatabase().connectToDatabase();
                        else
                            ma.getDatabase().connectToDatabase(type);
                    }
                };
                handler.postDelayed(dbConnectionRefresh, TimeUnit.SECONDS.toMillis(REFRESH_INTERVAL));
            }
        };
    }
}
