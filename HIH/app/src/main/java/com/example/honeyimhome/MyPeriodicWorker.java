package com.example.honeyimhome;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


public class MyPeriodicWorker extends Worker{

    SharedPreferences sp;
    String phone;
    String content;
    Context context;
    LocationRequest request;
    FusedLocationProviderClient client;
    LocationCallback callback;


    public MyPeriodicWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;

        this.callback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location loc = locationResult.getLastLocation();
                if(loc.getAccuracy() < 50){
                    MyLocation current = new MyLocation(loc.getLatitude(), loc.getLongitude(), loc.getAccuracy());
                    client.removeLocationUpdates(callback);

                    Gson gson = new Gson();
                    Type type = new TypeToken<MyLocation>(){}.getType();
                    MyLocation previous = gson.fromJson(sp.getString("locGson", null), type);

                    SharedPreferences.Editor editor = sp.edit();
                    editor.remove("locGson");
                    editor.putString("locGson", gson.toJson(current));
                    editor.apply();

                    if (previous != null ){

                        boolean flag = true;

                        if (Math.abs(current.getLatitude() - previous.getLatitude()) > 50){
                            flag = false;
                        }
                        if(flag && Math.abs(current.getLongitude() - previous.getLongitude()) > 50){
                            flag = false;
                        }

                        if (!flag){
                            MyLocation home = gson.fromJson(sp.getString("home", null), type);

                            flag = true;

                            if (Math.abs(current.getLatitude() - previous.getLatitude()) > 50){
                                flag = false;
                            }
                            if(flag && Math.abs(current.getLongitude() - previous.getLongitude()) > 50){
                                flag = false;
                            }

                            if (flag){
                                // Send sms
                                SmsManager.getDefault().sendTextMessage(phone, null, "Honey I'm Home!",null, null);
                            }
                        }

                    }


                }
            }
        };
    }

    @NonNull
    @Override
    public Result doWork() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            phone = sp.getString("phoneNumber", "");
            content = sp.getString("content", "");

            request = LocationRequest.create();
            request.setInterval(1000);
            request.setFastestInterval(1000);
            request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            client.requestLocationUpdates(request, callback, Looper.myLooper());



        }

        return Result.success();

    }


}
