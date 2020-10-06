package com.cs389f20.diamonds;

import android.util.Log;

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

public class DBManager
{
    private RequestQueue queue;
    private MainActivity ma;
    private static final String LOG_TAG = DBManager.class.getSimpleName(),  ACCESS_TOKEN = "tlbJzoaAl6m81uXG3xU76zIIiVthlym3jBP94Q90";
    public DBManager(MainActivity main)
    {
        ma = main;
    }

    public List<Building> getBuildings(Property p)
    {
        //returns the updated List<Building> for a property with any new values (and maybe even keys?)


        return null;
    }

    public List<Property> getProperties()
    {
        //returns a list of properties ???
        return null;
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
                       ma.storeData(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(LOG_TAG, "ERROR: Couldn't connect to database ");

                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("x-api-key", ACCESS_TOKEN);
                return params;
            }
        };

        queue.add(jsonObjectRequestRequest);
    }

    public RequestQueue getQueue()
    {
        return queue;
    }
}
