package edu.mga.team3.activitytracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Stats extends AppCompatActivity {
    GraphView graph;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        graph = (GraphView) findViewById(R.id.graph);
        graph.getSeries().clear();
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("");
        gridLabel.setVerticalAxisTitle("");
        //getActivities("DAILY");
    }

    public void showDailyActivities(View view) {
        getActivities("DAILY");
    }

    public void showWeeklyActivities(View view) {
        getActivities("WEEKLY");
    }

    public void showMonthlyActivities(View view) {
        getActivities("MONTHLY");
    }

    private void getActivities(String category) {
        Log.i("Stats", "Showing default activities ");
        String url = "https://fresh-metrics-246800.appspot.com/activity/" + category;
        final TextView textView = (TextView) findViewById(R.id.textView4);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            graph = (GraphView) findViewById(R.id.graph);
                            graph.clearAnimation();
                            graph.removeAllSeries();
                            int maxX = 0;
                            int minX = 0;
                            int minY = 0;
                            int maxY = 0;
                            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                            String statOf = null;
                            // Display the first 500 characters of the response string.
                            Log.i("Stats", "Response " + response);
                            JSONArray array = response.getJSONArray("details");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject stat = array.getJSONObject(i);
                                Log.d("RESPONSE - stat", stat.toString());
                                //String key = stat.keys().next().toString();
                                //Log.i("Stats", "key " + key);
                                double timeOfYear = Double.parseDouble(stat.get("key").toString().split(" ")[1]);
                                double steps = Double.parseDouble(stat.get("steps").toString());
                                Log.i("Stats", "timeOfYear " + timeOfYear + " steps " + steps);
                                Log.i("Stats", "timeOfYear " + stat.get("key").toString());
                                if(statOf == null) // Set only if not found earlier
                                    statOf = stat.get("key").toString().split(" ")[0]; // DAY /WEEK /MONTH

                                switch (statOf) {
                                    case "DAY":
                                        maxX = 210;
                                        minX = 150;
                                        break;
                                    case "WEEK":
                                        maxX = 31;
                                        minX = 21;
                                        break;
                                    case "MONTH":
                                        maxX = 8;
                                        minX = 3;
                                        break;
                                }
                                //System.out.println(key.split(" ")[1]);
                                //System.out.println(key.split(" ")[4]);
                                DataPoint dataPoint = new DataPoint(timeOfYear, steps);
                                series.appendData(dataPoint, true, 12, true);
                                //String category = stat.get
                            }
                            GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
                            gridLabel.setHorizontalAxisTitle(statOf + " of Year");
                            gridLabel.setVerticalAxisTitle("Steps");

                            graph.addSeries(series);
                            graph.getViewport().setScalable(true);

// activate horizontal scrolling
                            graph.getViewport().setScrollable(true);

// activate horizontal and vertical zooming and scrolling
                            graph.getViewport().setScalableY(true);

// activate vertical scrolling
                            graph.getViewport().setScrollableY(true);
                            graph.getViewport().setXAxisBoundsManual(true);
                            graph.getViewport().setYAxisBoundsManual(true);
                            graph.getViewport().setMaxX(maxX);
                            graph.getViewport().setMinX(minX);
                            //graph.getViewport().setMaxY(maxY);
                            graph.getViewport().setMinY(minY);
                            //JSONObject stats = response.getJSONObject("stepsByCategory");
                            //Log.i("Stats", "stats " + stats);
                            textView.setText("Response is: " + response);
                        } catch (
                                JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("That didn't work!");
            }
        });
        queue.add(stringRequest);

    }

   /* private void getActivities() {
        Log.i("Stats", "Showing default activities ");
        String url = "https://fresh-metrics-246800.appspot.com/activity/";
        final TextView textView = (TextView) findViewById(R.id.textView4);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            GraphView graph = (GraphView) findViewById(R.id.graph);
                            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

                            // Display the first 500 characters of the response string.
                            Log.i("Stats", "Response " + response);
                            JSONArray array = response.getJSONArray("details");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject stat = array.getJSONObject(i);
                                Log.d("RESPONSE - stat", stat.toString());
                                //String key = stat.keys().next().toString();
                                //Log.i("Stats", "key " + key);
                                double timeOfYear = Double.parseDouble(stat.get("key").toString().split(" ")[1]);
                                double steps = Double.parseDouble(stat.get("steps").toString());
                                Log.i("Stats", "timeOfYear " + timeOfYear + " steps " + steps);
                                //System.out.println(key.split(" ")[1]);
                                //System.out.println(key.split(" ")[4]);
                                DataPoint dataPoint = new DataPoint(timeOfYear, steps);
                                series.appendData(dataPoint, true, 1000, true);
                                //String category = stat.get
                            }
                            GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
                            gridLabel.setHorizontalAxisTitle("Day of Year");
                            gridLabel.setVerticalAxisTitle("Steps");
                            graph.addSeries(series);
                            //JSONObject stats = response.getJSONObject("stepsByCategory");
                            //Log.i("Stats", "stats " + stats);
                            textView.setText("Response is: " + response);
                        } catch (
                                JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("That didn't work!");
            }
        });
        queue.add(stringRequest);

    }

    private void showActivities() {
        Log.i("Stats", "Showing default activities ");
        String url = "https://fresh-metrics-246800.appspot.com/activity/";
        final TextView textView = (TextView) findViewById(R.id.textView4);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        textView.setText("Response is: "+ response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("That didn't work!");
            }
        });
        queue.add(stringRequest);
    }*/

    /*private void showActivities(String category) {
        String url = "https://fresh-metrics-246800.appspot.com/activity/" + category;
        final TextView textView = (TextView) findViewById(R.id.textView4);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        textView.setText("Response is: "+ response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("That didn't work!");
            }
        });
        queue.add(stringRequest);
    }*/
   /* private void showStats() {
        final JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        int counter = 100;
                        TableLayout ll = findViewById(R.id.books_layout);
                        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
                        lp.setMargins(2, 5, 2, 5);
                        lp.width = 400;
                        Log.d("RESPONSE", response.toString());

                        // Check the length of our response (to see if the user has any repos)
                        if (response.length() > 0) {
                            // The user does have books, so let's loop through them all.
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    // For each repo, add a new line to our repo list.
                                    JSONObject book = response.getJSONObject(i);
                                    Log.d("RESPONSE - Branch", book.toString());
                                    //String title = book.get("title").toString();
                                    //String description = book.get("description").toString();
                                    //String author = book.get("author").toString();
                                    //String thumbnailUrl = book.get("thumbnailUrl").toString();
                                    //Double price = Double.valueOf(book.get("price").toString());

                                    TableRow row = new TableRow(ll.getContext());
                                    row.setId(++counter + i);
                                    row.setLayoutParams(lp);

                                    if (i % 2 == 0)
                                        row.setBackgroundColor(Color.rgb(240, 240, 240));
                                    else
                                        row.setBackgroundResource(R.drawable.border);

                                    TextView id = new TextView(row.getContext());
                                    id.setId(++counter + i);
                                    id.setLayoutParams(lp);
                                    id.setText(book.get("id").toString());
                                    row.addView(id);

                                    TextView title = new TextView(row.getContext());
                                    title.setId(++counter + i);
                                    title.setLayoutParams(lp);
                                    title.setText(book.get("branchName").toString());
                                    row.addView(title);

                                    TextView description = new TextView(row.getContext());
                                    description.setId(++counter + i);
                                    description.setLayoutParams(lp);
                                    description.setText(book.get("city").toString());
                                    row.addView(description);

                                    TextView price = new TextView(row.getContext());
                                    price.setId(++counter + i);
                                    price.setLayoutParams(lp);
                                    price.setText(book.get("zip").toString());
                                    row.addView(price);

                                    ll.addView(row, i, lp);

                                } catch (JSONException e) {
                                    // If there is an error then output this to the logs.
                                    Log.e("Volley", "Invalid JSON Object.");
                                }

                            }

                            TableRow header = new TableRow(ll.getContext());
                            header.setId(counter - 1);
                            header.setBackgroundColor(Color.rgb(188, 244, 188));
                            header.setLayoutParams(lp);

                            TextView l_id = new TextView(header.getContext());
                            l_id.setId(counter - 2);
                            l_id.setLayoutParams(lp);
                            l_id.setText("Id");
                            header.addView(l_id);

                            TextView l_title = new TextView(header.getContext());
                            l_title.setId(counter - 2);
                            l_title.setLayoutParams(lp);
                            l_title.setText("Name");
                            header.addView(l_title);

                            TextView l_description = new TextView(header.getContext());
                            l_description.setId(counter - 3);
                            l_description.setLayoutParams(lp);
                            l_description.setText("City");
                            header.addView(l_description);

                            TextView l_price = new TextView(header.getContext());
                            l_price.setId(counter - 4);
                            l_price.setLayoutParams(lp);
                            l_price.setText("Zip");
                            header.addView(l_price);

                            ll.addView(header, 0, lp);
                        } else {
                            Log.e("NoBranch", "No branch found");
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // If there a HTTP error then add a note to our repo list.
                        setRepoListText();
                        Log.e("Volley", error.toString());
                    }
                }
        );
        requestQueue.add(arrReq);
    }*/
}
