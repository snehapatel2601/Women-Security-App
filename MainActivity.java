package app.imgproject.securityapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;


import android.util.Log;
import android.widget.*;
import android.view.*;
import android.content.*;

import java.util.ArrayList;

import android.telephony.SmsManager;

import android.content.ActivityNotFoundException;
import android.speech.RecognizerIntent;

import android.location.Location;

import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;


public class MainActivity extends AppCompatActivity
        implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener

{

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;


    String name, buddy, parent;
    EditText t1, t2, t3;
    TextView tv;
    protected static final int RESULT_SPEECH = 1;
    private SharedPreferences permissionStatus;
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t1 = (EditText) findViewById(R.id.editText);
        t2 = (EditText) findViewById(R.id.editText1);
        t3 = (EditText) findViewById(R.id.editText2);
        try {
            SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);

            name = sh.getString("name", "");
            buddy = sh.getString("buddy", "");
            parent = sh.getString("parent", "");
            if (name.length() != 0) {
                setContentView(R.layout.speak);
                tv = (TextView) findViewById(R.id.textView2);

            }
        } catch (Exception ex) {
        }

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "ACCESS_FINE_LOCATION permission OK");
        } else {
            Log.d(TAG, "ACCESS_FINE_LOCATION permission NG");
            Toast.makeText(getApplicationContext(), "check all permission",
                    Toast.LENGTH_LONG).show();
            return;
        }
        PendingResult<Status> pendingResult =
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest,
                        this);
        Log.d(TAG, "Location update started ..............: ");


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;



    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());

            String url = "http://maps.google.com/maps?&daddr="+
                    lat+"," +lng;


            try {
                String firstmsg = "i need help \n my location is " +url ;

                SmsManager smsManager = SmsManager.getDefault();
              smsManager.sendTextMessage(buddy,
                      null, firstmsg, null, null);
                smsManager.sendTextMessage(parent,
                        null, firstmsg, null, null);
                Toast.makeText(getApplicationContext(), firstmsg,
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "SMS faild, please try again later!",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }




        } else {
            Toast.makeText(getApplicationContext(),
                    "location is null",
                    Toast.LENGTH_LONG).show();
           // Log.d(TAG, "location is null ...............");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    public void submitclick(View v)
    {
    EditText t1 = (EditText) findViewById(R.id.editText);
        EditText t2 = (EditText) findViewById(R.id.editText1);
        EditText t3 = (EditText) findViewById(R.id.editText2);

        name = t1.getText().toString();
        buddy = t2.getText().toString();
        parent = t3.getText().toString();

        if(name.length()==0 || buddy.length()==0
        || parent.length()==0 )

        {
            Toast.makeText(this,
                    "Value cannot be empty",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

            SharedPreferences.Editor myEdit = sharedPreferences.edit();

            myEdit.putString("name", name);
            myEdit.putString("buddy", buddy);
            myEdit.putString("parent", parent);

            myEdit.commit();
            Toast.makeText(this, "Welcome " + name, Toast.LENGTH_SHORT).show();


            String firstmsg = "Hi " + "\n , I have added as a helping person. from :" + name;

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(buddy,
                    null, firstmsg, null, null);

            Toast.makeText(this, "SMS Sent!",
                    Toast.LENGTH_LONG).show();
            setContentView(R.layout.speak);
            tv = (TextView) findViewById(R.id.textView2);
        }
        catch(Exception ex)
        {
            Toast.makeText(this, ex.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void speakclick(View v)
    {
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                "en-US");
        try {
            startActivityForResult(intent, RESULT_SPEECH);
            tv.setText("");
        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(this,
                    "Ops! Your device doesn't support Speech to " +
                            "Text", Toast.LENGTH_SHORT);
            t.show();
        }
    }

    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
   switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data
                            .getStringArrayListExtra(
                                    RecognizerIntent.EXTRA_RESULTS);
                    tv.setText(text.get(0));
                     if (tv.getText().toString().equalsIgnoreCase("help") &&  tv.getText().toString().contains("help")) {
                          updateUI();
                      }
                }
                break;
            }
        }
    }
}// end of mainActivity
