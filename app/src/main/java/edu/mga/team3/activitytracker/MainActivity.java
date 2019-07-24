package edu.mga.team3.activitytracker;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnDataPointListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mApiClient;
    private TextView steps;
    private TextView todayUntilNow;
    String liveSteps = "0";
    String todaysSteps = "0";
    int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    RequestQueue queue ;  // this = context

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        steps = (TextView) findViewById(R.id.steps);
        todayUntilNow = (TextView) findViewById(R.id.untilTime);
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        steps.setText("0");
        todayUntilNow.setText("0");
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        queue = Volley.newRequestQueue(this);
        liveSteps = "0";
    }

    public void onStatsButtonClick(View view) {
        Intent intent = new Intent(this, Stats.class);
        startActivity(intent);
    }
    @Override
    public void onConnected(Bundle bundle) {
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
            .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA, DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .setDataSourceTypes( DataSource.TYPE_DERIVED)
            .build();

    ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
        @Override
        public void onResult(DataSourcesResult dataSourcesResult) {
            for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                DataType type = dataSource.getDataType();

                if (DataType.TYPE_STEP_COUNT_DELTA.equals(type)
                        || DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(type)) {
                    Log.i(TAG, "Register Fitness Listener: " + type);
                    registerFitnessDataListener(dataSource, type);//DataType.TYPE_STEP_COUNT_DELTA);
                }
            }
        }
    };

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
            .setResultCallback(dataSourcesResultCallback);

    }

    /*//Working
    @Override
    public void onConnected(Bundle bundle) {
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes( DataType.TYPE_STEP_COUNT_CUMULATIVE )
                .setDataSourceTypes( DataSource.TYPE_RAW )
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                    if( DataType.TYPE_STEP_COUNT_CUMULATIVE.equals( dataSource.getDataType() ) ) {
                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }*/

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
    Log.e(TAG, "registerFitnessDataListener");
        SensorRequest request = new SensorRequest.Builder()
                .setDataSource( dataSource )
                .setDataType( dataType )
                .setSamplingRate( 3, TimeUnit.SECONDS )
                .build();

        Fitness.SensorsApi.add( mApiClient, request, this )
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.e( "GoogleFit", "SensorApi successfully added" );
                        }
                    }
                });
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if( !authInProgress ) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult( MainActivity.this, REQUEST_OAUTH );
            } catch(IntentSender.SendIntentException e ) {

            }
        } else {
            Log.e( "GoogleFit", "authInProgress" );
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_OAUTH ) {
            authInProgress = false;
            if( resultCode == RESULT_OK ) {
                if( !mApiClient.isConnecting() && !mApiClient.isConnected() ) {
                    mApiClient.connect();
                }
            } else if( resultCode == RESULT_CANCELED ) {
                Log.e( "GoogleFit", "RESULT_CANCELED" );
            }
        } else {
            Log.e("GoogleFit", "requestCode NOT request_oauth");
        }
    }
    @Override
    public void onDataPoint(DataPoint dataPoint) {
        for( final Field field : dataPoint.getDataType().getFields() ) {
            final Value value = dataPoint.getValue( field );
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(), "Field: " + field.getName() + " Value: " + value, Toast.LENGTH_SHORT).show();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm");
                    Date currentTime = Calendar.getInstance().getTime();
                    String recordedSteps = value.toString();
                    liveSteps = String.valueOf(Integer.parseInt(liveSteps) +  Integer.parseInt(recordedSteps));
                    if(today == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
                        todaysSteps = String.valueOf(Integer.parseInt(recordedSteps) +  Integer.parseInt(todaysSteps));
                    } else {
                        todaysSteps = "0";
                    }
                    String time = dateFormat.format(currentTime);
                    steps.setText(liveSteps);
                    //steps.setText("10");
                    todayUntilNow.setText(todaysSteps);
                    Log.e(TAG, "Registering steps " + recordedSteps + " for " + time);
                    registerSteps(recordedSteps, time);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    public void registerSteps(String steps, String date) {
        Log.d(TAG, "registerSteps");
        String url = "https://fresh-metrics-246800.appspot.com/activity/";
        // Optional Parameters to pass as POST request
        JSONObject js = new JSONObject();
        try {
            js.put("steps",steps);
            js.put("untilTime",date);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Make request for JSONObject
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST, url, js,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString() + " Hurrey!!! Registered...");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }

        };
        queue.add(jsonObjReq);
    }
}
