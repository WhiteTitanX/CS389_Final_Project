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
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DBManager {
    private RequestQueue queue;
    public boolean done = false;
    private MainActivity ma;
    final private Handler handler = new Handler(Looper.getMainLooper());
    Runnable dbConnectionRefresh;
    private static final String LOG_TAG = DBManager.class.getSimpleName(), ACCESS_TOKEN = "tlbJzoaAl6m81uXG3xU76zIIiVthlym3jBP94Q90";
    private long lastUpdated;
    private RequestFuture<JSONObject> future;

    public DBManager(MainActivity main) {
        ma = main;
    }

    public void connectToDatabase() {
        Thread t = new Thread() {
            @Override
            public void run() {
                queue = Volley.newRequestQueue(ma);
                future = RequestFuture.newFuture();
                connectToDatabase(DataType.INFO);
                //Other calls for different types are done after adding each type to the queue (has to do with future not syncing properly)
            }
        };
        t.start();
    }

    private void connectToDatabase(final DataType type) {
        final String url = getURL(type);
        Log.d(LOG_TAG, "Connecting, Type is " + type);
        try {
            if (type == DataType.INFO) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, future, errorResponse(type)) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("x-api-key", ACCESS_TOKEN);
                        return params;
                    }
                };
                queue.add(jsonObjectRequest);
                JSONObject response = future.get(10, TimeUnit.SECONDS);
                Log.d(LOG_TAG, "Response Received for " + type + "! Storing...");
                lastUpdated = System.currentTimeMillis();
                ma.storeData(response, null, type);
                connectToDatabase(DataType.CURRENT);
            } else if (type == DataType.CURRENT) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(LOG_TAG, "Response Received for " + type + "! Storing...");
                                lastUpdated = System.currentTimeMillis();
                                ma.storeData(response, null, type);
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
                connectToDatabase(DataType.PAST);
            } else if (type == DataType.PAST) {
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Log.d(LOG_TAG, "Response Received for " + type + "! Storing...");
                                lastUpdated = System.currentTimeMillis();
                                ma.storeData(null, response, type);
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
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            Log.d(LOG_TAG, "Error: " + e.getMessage());
            e.printStackTrace();
        }


    /*        // Request a string response from the provided URL.
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            lastUpdated = System.currentTimeMillis();
                            ma.storeData(response, null, type);
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
                            ma.storeData(null, response, type);
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
        } */
    }

    private String getURL(DataType type) {
        if (type == DataType.INFO)
            return "https://jq7jhckkx7.execute-api.us-east-1.amazonaws.com/dev/overseerinfo";
        else if (type == DataType.PAST)
            return "https://isz3wa9q77.execute-api.us-east-1.amazonaws.com/dev/overseerhistory";
        else if (type == DataType.CURRENT)
            return "https://9hel5x9p0d.execute-api.us-east-1.amazonaws.com/dev/overseer";
        else
            return null;
    }

    private Response.ErrorListener errorResponse(final DataType type) {
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
                        ma.getDatabase().connectToDatabase(type);
                    }
                };
                handler.postDelayed(dbConnectionRefresh, TimeUnit.SECONDS.toMillis(REFRESH_INTERVAL));
            }
        };
    }

    public void destroyDBHandler() {
        if (dbConnectionRefresh != null)
            handler.removeCallbacks(dbConnectionRefresh);
    }

    public RequestQueue getQueue() {
        return queue;
    }

    public long getLastUpdated() {
        return System.currentTimeMillis() - lastUpdated;
    }

    public enum DataType {CURRENT, PAST, INFO}
}
