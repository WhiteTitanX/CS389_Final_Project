package com.cs389f20.diamonds;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DBManager
{
    private RequestQueue queue;
    private MainActivity ma;
    final private Handler handler = new Handler(Looper.getMainLooper());
    Runnable dbConnectionRefresh;
    private static final String LOG_TAG = DBManager.class.getSimpleName(),  ACCESS_TOKEN = "tlbJzoaAl6m81uXG3xU76zIIiVthlym3jBP94Q90";
    private long lastUpdated;

    private List<Property> properties;
    public DBManager(MainActivity main)
    {
        ma = main;
    }

    public void connectToDatabase()
    {
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(ma);
        String url = "https://9hel5x9p0d.execute-api.us-east-1.amazonaws.com/dev/overseer";

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequestRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                       lastUpdated = System.currentTimeMillis();
                        ma.storeData(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(LOG_TAG, "ERROR: Couldn't connect to database ");
                        int REFRESH_INTERVAL = 10;
                        //Can't connect to internet
                        if (error.getCause().toString().contains("UnknownHostException")) {
                            Toast.makeText(ma.getApplicationContext(), "Can't connect to the internet.", Toast.LENGTH_LONG).show();
                            Log.d(LOG_TAG, "Can't connect to internet. Retrying every " + REFRESH_INTERVAL + " seconds");

                        } else {
                            Toast.makeText(ma.getApplicationContext(), "Can't connect to database. Try restarting the app.", Toast.LENGTH_LONG).show();
                            error.printStackTrace();
                            REFRESH_INTERVAL = 15;
                        }
                        dbConnectionRefresh = new Runnable() {
                            @Override
                            public void run() {
                                ma.getDatabase().connectToDatabase();
                            }
                        };
                        handler.postDelayed(dbConnectionRefresh, TimeUnit.SECONDS.toMillis(REFRESH_INTERVAL));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("x-api-key", ACCESS_TOKEN);
                return params;
            }
        };

        queue.add(jsonObjectRequestRequest);
    }

    public void destroyDBHandler()
    {
        handler.removeCallbacks(dbConnectionRefresh);
    }

    public RequestQueue getQueue()
    {
        return queue;
    }

    public long getLastUpdated()
    {
        return System.currentTimeMillis() - lastUpdated;
    }
}
