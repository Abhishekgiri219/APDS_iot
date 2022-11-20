package com.example.apds;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    TextView textView , textViewA;
    protected LocationManager locationManager;

    SensorManager sm = null;

    List list;

    long locationLastUpdate, locationNowUpdate;

    long lastUpdate, actualTime;
    float oldAccVal = 0.0f;
    float[] gravity = new float[3];
    float[] linear_acceleration = new float[3];

    SensorEventListener accSensor = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        public void onSensorChanged(SensorEvent event) {

            actualTime = Calendar.getInstance().getTimeInMillis();

            if(actualTime - lastUpdate > 20){

                final float alpha = 0.8f;

                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                linear_acceleration[0] = event.values[0] - gravity[0];
                linear_acceleration[1] = event.values[1] - gravity[1];
                linear_acceleration[2] = event.values[2] - gravity[2];

                float val = 0;

                for(int i = 0 ; i < 3 ; i++){
                    val += linear_acceleration[i]*linear_acceleration[i];
                }

                val = (float) Math.sqrt(val);

                if(val - oldAccVal > 9.81){
                    Toast.makeText(getApplicationContext(), "Accident Happened ! force is " + ((val - oldAccVal)/9.81) + " g", Toast.LENGTH_SHORT).show();
                }

                textViewA = findViewById(R.id.text_viewA);

                String msg = "x: "+linear_acceleration[0]+"\ny: "+linear_acceleration[1]+"\nz: "+linear_acceleration[2] + "\n";

                msg += val + "";

                textViewA.setText(msg);

                oldAccVal = val;

                lastUpdate = actualTime;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastUpdate = Calendar.getInstance().getTimeInMillis();

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, 100 );
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 101 );
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION }, 102 );
        } else
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);



        list = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(list.size()>0){
            sm.registerListener(accSensor, (Sensor) list.get(0), SensorManager.SENSOR_DELAY_UI);
        }else{
            Toast.makeText(getBaseContext(), "Error: No Sensor.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        locationNowUpdate = Calendar.getInstance().getTimeInMillis();

        if(locationNowUpdate - locationLastUpdate > 3000) {
            textView = findViewById(R.id.text_view);
            String msg = "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude() + ", speed: " + location.getSpeed();
            textView.setText(msg);

            locationLastUpdate = locationNowUpdate;
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    @Override
    protected void onStop() {
        if(list.size()>0){
            sm.unregisterListener(accSensor);
        }
        super.onStop();
    }
}