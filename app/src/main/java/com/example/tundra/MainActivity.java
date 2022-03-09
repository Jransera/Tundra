package com.example.tundra;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    //all the shit we may need
    public static final String EXTRA_MESSAGE = "com.example.tundra.MESSAGE";
    TextView sensorStatusTV;
    SensorManager sensorManager;
    Sensor proximitySensor;
    userData u_data = null;
    ActivityResultLauncher<Intent> someActivityResultLauncher=
            registerForActivityResult( new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == 1) {
                Intent data = result.getData();
                if(data != null){
                    u_data =(userData)data.getSerializableExtra("user_info");
                    Log.d("MyActivity","returned"+u_data.toString());

                    //should update the csv
                    writeCsv(u_data);
                    //reread the csv to double check lol
                    u_data = readCsv();
                }

            }


        }
    });


    @Override
    public <T extends View> T findViewById(int id) {
        return super.findViewById(id);
    }

    //initialization of app when first run
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //create the activity handler so that we can pass through data


        //read in csv and set user data as soon as app opens
        if(u_data == null) {
            u_data = readCsv();
            Log.d("MyActivity","init"+u_data.toString());

        }

        //prox sensor handling
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorStatusTV = findViewById(R.id.sensorStatusTV);

        //call sensor service
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //case handling
        if(proximitySensor == null){
            Toast.makeText(this, "no Prox Sensor",Toast.LENGTH_SHORT).show();
            //if no sensor it will close app
            //finish();
        }
        else{
            //set a listener -> (type, sensor to listen, interval)
            sensorManager.registerListener(proximitySensorEventListener,proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        //bottom menu handling
        BottomNavigationView bottomNav =findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navlistener);



    }

    //read a CSV line by line currently only reads the data csv
    //create a userdata object that holds neccessary data
    private userData readCsv(){
        InputStream is = getResources().openRawResource(R.raw.data);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line ="";
        userData sample = null;
        try {
            //step over headers
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                //split line
                String[] tokens = line.split(",");

                //create a new sample,
                sample = new userData();

                //set all of the setters from the file.
                //probably should be done at the start of the app?

                sample.setRank(Integer.parseInt(tokens[0]));
                sample.setTotalTime(Long.parseLong(tokens[1]));
                sample.setAvg((Long.parseLong(tokens[2])));
                sample.setLatest((Long.parseLong(tokens[3])));
                sample.setSuccRate(Float.parseFloat(tokens[4]));
                sample.setNumSessions(Integer.parseInt(tokens[5]));
                sample.setNumTries(Integer.parseInt(tokens[6]));


                //Toast.makeText(DisplayMessageActivity.this,"created", Toast.LENGTH_SHORT).show();

                //Log.d("MyActivity","just created:"+sample.toString());


            }
        } catch (IOException e){
            Log.wtf("MyActivity","error reading data file on line" +line, e);
            e.printStackTrace();

        }
        return sample;
    }

    //write to csv, should be done on close i think, or after an update?
    public void writeCsv(userData data)
    {
        String filename = "data.csv";
        String path = "src/main/res/raw/";
        String headers = "Rank,Total_Time,Avg_Time,Latest_Time,Succ_Rate,N_Sessions,N_Tries";
        String info = data.publish();

        try{
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(filename,this.MODE_PRIVATE));
            outputStreamWriter.write(headers+'\n'+info);
            outputStreamWriter.close();
        }
        catch(IOException e){
            Log.e("Myactivity","File write failed:"+e.toString());
        }
    }


    //sensor handling and other shit
    SensorEventListener proximitySensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
                if(event.values[0] == 0){
                    sensorStatusTV.setText("Near");
                }
                else{
                    sensorStatusTV.setText("Away");
                }
            }
        }


        //idk what this does i forget lol
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    //send the new activity,
    public void sendMessages(View view){

        //recreate the user data so we can pass information to change
        //userData u_data = readCsv();
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra("userInfo",u_data);
        someActivityResultLauncher.launch(intent);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navlistener = new BottomNavigationView.OnNavigationItemSelectedListener(){


        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment selectedFragment = null;
            switch(item.getItemId()) {
                case R.id.study:
                    selectedFragment = new StudyFragment();
                    break;
                case R.id.settings:
                    selectedFragment = new SettingsFragment();
                    break;
                case R.id.rankings:
                    selectedFragment = new RankingsFragment();
                    break;
                case R.id.home:
                    for(int i = 0; i<getSupportFragmentManager().getBackStackEntryCount(); ++i){
                        getSupportFragmentManager().popBackStack();
                    }
                    return true;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,selectedFragment).addToBackStack("Fragment")
                    .commit();
            return true;
        }
    };




}