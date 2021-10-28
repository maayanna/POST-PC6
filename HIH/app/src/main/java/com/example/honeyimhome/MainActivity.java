package com.example.honeyimhome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


public class MainActivity extends AppCompatActivity {

    Button trackButton;
    Button setHomeButon;
    Button clearHomeButton;
    Button setPhone;
    Button testSms;

    TextView longitude;
    TextView latitude;
    TextView accuracy;
    TextView homePostition;

    LocationTracker myTracker;
    SharedPreferences sp;
    LocationBroadcast myBroadcast;
    LocalSendSmsBroadcastReceiver smsBroadcast;


    final MainActivity main = this;
    final Context context = this;
    MyLocation myLoc;

    String number;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trackButton = (Button) findViewById(R.id.trackButton);
        setHomeButon = (Button) findViewById(R.id.setHome);
        clearHomeButton = (Button) findViewById(R.id.clearHome);
        setPhone = (Button) findViewById(R.id.setPhone);
        testSms = (Button) findViewById(R.id.testSms);

        longitude = (TextView) findViewById(R.id.longitudeView);
        latitude = (TextView) findViewById(R.id.latitudeView);
        accuracy = (TextView) findViewById(R.id.accuracyView);
        homePostition = (TextView) findViewById(R.id.homeLocationView);

        myTracker = new LocationTracker(this);
        myBroadcast = new LocationBroadcast();
        smsBroadcast = new LocalSendSmsBroadcastReceiver();

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        Gson gson = new Gson();
        Type type = new TypeToken<MyLocation>() {}.getType();
        String home = sp.getString("home", null);
        number = sp.getString("phoneNumber", null);
        final MyLocation homeLocation = gson.fromJson(home, type);

        // If already in sp
        if (homeLocation != null){

            String homeLoc = "YOUR HOME LOCATION IS : " + homeLocation.getLatitude() + ", " + homeLocation.getLongitude();
            homePostition.setText(homeLoc);
            clearHomeButton.setVisibility(View.VISIBLE);

        }
        else{
            clearHomeButton.setVisibility(View.INVISIBLE);
        }

        if (number != null){

            testSms.setVisibility(View.VISIBLE);

        }
        else{
            testSms.setVisibility(View.INVISIBLE);
        }


        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (trackButton.getText().equals("STOP TRACKING LOCATION")){
                    myTracker.stopTracking();
                    trackButton.setText("START TRACKING LOCATION");

                }

                else{
                    if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        myTracker.startTracking();
                        trackButton.setText("STOP TRACKING LOCATION");
                    }
                    else{
                        ActivityCompat.requestPermissions(main, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 34);
                    }
                }
            }
        });


        setHomeButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String homeLoc = "YOUR HOME LOCATION IS : " + myLoc.getLatitude() + ", " + myLoc.getLongitude();
                homePostition.setText(homeLoc);
                clearHomeButton.setVisibility(View.VISIBLE);

                // Keep it in sp for next time
                Gson gson = new Gson();
                String homegson = gson.toJson(myLoc);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("home", homegson)
                        .apply();

            }
        });

        clearHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sp.edit();
                editor.remove("home")
                        .apply();
                homePostition.setText("");
                clearHomeButton.setVisibility(View.INVISIBLE);
            }
        });

        setPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check permissions

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(main, new String[]{Manifest.permission.SEND_SMS}, 35);
                }
//                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED){
//                    ActivityCompat.requestPermissions(main, new String[]{Manifest.permission.READ_SMS}, 2);
//                }
//                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
//                    ActivityCompat.requestPermissions(main, new String[]{Manifest.permission.READ_SMS}, 3);
//                }
                // Having all permission
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("SET THE PHONE NUMBER PLEASE");
                    final EditText inputPhone = new EditText(MainActivity.this);
                    builder.setView(inputPhone);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String result = inputPhone.getText().toString();
                            if (result.length() > 0){
                                testSms.setVisibility(View.VISIBLE);
                                number = result;
                            }
                            else{
                                testSms.setVisibility(View.INVISIBLE);
                                number = null;
                            }
                            Gson gson = new Gson();
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("phoneNumber", gson.toJson(result));
                            editor.apply();

                        }
                    });
                    builder.setNegativeButton("CANCEL", null);
                    builder.show();


                }
            }
        });

        testSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("POST_PC.ACTION_SEND_SMS");
                intent.putExtra("phoneNumber", number);
                intent.putExtra("content", "Honey I'm Sending a Test Message!");
                intent.setAction("POST_PC.ACTION_SEND_SMS");

                LocalBroadcastManager.getInstance(main).sendBroadcast(intent);
                smsBroadcast.onReceive(context, intent);


            }
        });

        // thx brahan
        IntentFilter myFilter = new IntentFilter();
        // For on receive

        myFilter.addAction("stop");
        myFilter.addAction("track");
        context.registerReceiver(myBroadcast, myFilter);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 34 :
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    myTracker.startTracking();
                    trackButton.setText("STOP TRACKING LOCATION");
                    Toast.makeText(this, "LOCATION WAS ALLOWED", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                        Toast.makeText(this, "PLEASE ALLOW LOCALISATION", Toast.LENGTH_SHORT).show();
                    }
                }

            case 35 :
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
//                    myTracker.startTracking();
//                    trackButton.setText("STOP TRACKING LOCATION");
                    Toast.makeText(this, "SEND SMS WAS ALLOWED", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){
                        Toast.makeText(this, "PLEASE ALLOW SENS SMS", Toast.LENGTH_SHORT).show();
                    }
                }
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcast);
    }


    class LocationBroadcast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            setHomeButon.setVisibility(View.INVISIBLE);
            String todo = intent.getAction();

            if (todo.equals("track")){

                String loc = intent.getStringExtra("locGson");
                Gson gson = new Gson();
                Type type = new TypeToken<MyLocation>(){}.getType();
                myLoc = gson.fromJson(loc, type);


                // when tracking user's location and the accuracy is smaller than 50 meters,
                // the user can see a new button "set location as home".
                if (myLoc.getAccuracy() < 50 && trackButton.getText().equals("STOP TRACKING LOCATION")){
                    setHomeButon.setVisibility(View.VISIBLE);
                    // When clear home ? just when clear ?
                }

                String setLatitude = "LATITUDE : " + myLoc.getLatitude();
                latitude.setText(setLatitude);

                String setLongitude = "LONGITUDE : " + myLoc.getLongitude();
                longitude.setText(setLongitude);

                String setAccuracy = "ACCURACY : " + myLoc.getAccuracy();
                accuracy.setText(setAccuracy);


            }
            else{

                // action = stop

                setHomeButon.setVisibility(View.INVISIBLE);
                // apparement pas besoin
//                homePostition.setText("");

                // When clear home ? just when clear ?
//                clearHomeButton.setVisibility(View.INVISIBLE);
                latitude.setText("");
                longitude.setText("");
                accuracy.setText("");
            }

        }
    }
}
