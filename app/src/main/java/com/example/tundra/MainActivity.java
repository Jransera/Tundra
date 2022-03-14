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
import android.icu.util.BuddhistCalendar;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //all the shit we may need
    public static final String EXTRA_MESSAGE = "com.example.tundra.MESSAGE";


    TextView sensorStatusTV;
    SensorManager sensorManager;
    Sensor proximitySensor;
    userData u_data = null;
    List<rank<Integer,Long>> rankings = null;
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
                    try {
                        u_data = readCsv();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("MyActivity","rewritten "+u_data.toString());
                    rankUpdate(u_data);
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
            try {
                u_data = readCsv();
                if(u_data == null){
                    Log.d("MyActivity","null after readCSV");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("MyActivity","init "+u_data.toString());

        }

        //read rankings on start this is for testing but It could be useful for the other pages
        if(rankings == null){
            rankings = readRank();
            Log.d("MyActivity","init: " + rankings);
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

    private void setDataFile(){
        String filename = "/data/data/com.example.tundra/files/data.csv";
        // String path = "src/main/res/raw/";
        String headers = "Rank,Total_Time,Avg_Time,Latest_Time,Succ_Rate,N_Sessions,N_Tries";
        String info = "0,0,0,0,0,0,0";

        try{
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filename));
            outputStreamWriter.write(headers+'\n'+info);
            outputStreamWriter.close();
            Log.d("MyActvity","set data file");
        }
        catch(IOException e){
            Log.e("Myactivity","RESET:File write failed:"+e.toString());
        }
    }

    //read a CSV line by line currently only reads the data csv
    //create a userdata object that holds neccessary data
    private userData readCsv() throws IOException {
        InputStream is = null;
        File f = new File("/data/data/com.example.tundra/files","data.csv");
        Log.d("MyActivity",f.getPath());

        if(f.exists()){
            Log.d("MyActvity","file existed");
        }

        if(!f.exists()){
            boolean newFile = f.createNewFile();

            if(newFile){
                setDataFile();
            }

            Log.d("MyActvity","finished making new file");
        }

        //open the data file as a inputstream
        try{
             is = new FileInputStream(f);
        }catch(IOException e){
            Log.e("MyActivity","error opening data file");
        }

        //create a buffered reader so we can read line by line
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line ="";
        userData sample = null;
        try {



            if((line = reader.readLine()) == null){
                Log.d("MyActvity","file was empty");
                reader.close();
                setDataFile();
                readCsv();
            }

            //step over headers
//            line = reader.readLine();
//            Log.d("MyActivity","lines:" + line);

            while ((line = reader.readLine()) != null) {

                Log.d("MyActivity","lines:" + line);

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
                //set the id over some random value lol need to come up with some hash way to do it
                sample.setID(178);

                //Toast.makeText(DisplayMessageActivity.this,"created", Toast.LENGTH_SHORT).show();

                //Log.d("MyActivity","just created:"+sample.toString());

                //reader.close();
            }
        } catch (IOException e){
            Log.wtf("MyActivity","error reading data file on line " +line, e);
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


    //this function is going to seem stupid but easier to do two functions than try to figure it out in 1
    private void rankUpdate(userData u_data)
    {
        List<rank<Integer,Long>> rank_info = readRank();

        Log.d("MyActivity","original: ");
        rankPublish(rank_info);

        //sort the list of pairs
        int l = rank_info.size();

        //go back and update current user info
        for(int i=0;i<l;i++)
        {
            if(rank_info.get(i).getL() == u_data.getID()){
                rank<Integer,Long> t= new rank(u_data.getID(),u_data.getTotalTime());
                rank_info.set(i,t);
                break;
            }

            else if(i == l-1 && !(rank_info.get(i).getL() == u_data.getID())){
                rank<Integer,Long> t= new rank(u_data.getID(),u_data.getTotalTime());
                rank_info.add(t);
            }
        }


        //now sort the new data
        for(int x=1;x<l;x++)
        {
            long key = rank_info.get(x).getR();
            int y = x-1;
            while((y>-1)&&(rank_info.get(y).getR() > key)){

                rank_info.set(y+1,rank_info.get(y));
                y--;
            }
            rank_info.set(y+1, rank_info.get(x));
        }
        Log.d("MyActivity","post sort: ");
        rankPublish(rank_info);

        //rewrite to the file
        writeRank(rank_info);

    }


    //read in the current ranks with ID and Time
    //this is a helper for rankUpdate But I made it a helper so that we can call this whenever without
    //having to update the entire time
    //however it SHOULD only be called after an update has happened
    private List<rank<Integer,Long>> readRank(){
        List<rank<Integer,Long>> rank_info = new ArrayList<rank<Integer,Long>>(); //an array so we can resort user data if needed

        InputStream is = null;
        File f = new File("/data/data/com.example.tundra/files","ranking.csv");

        //open the data file as a inputstream
        try{
            is = new FileInputStream(f);
        }catch(IOException e){
            Log.e("MyActivity","error opening Rank file");
        }

        //create a buffered reader so we can read line by line
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

                //create a temporary rank;
                rank<Integer,Long> temp = new rank(Integer.parseInt(tokens[0]),Long.parseLong(tokens[1]));

                //add to list
                rank_info.add(temp);
            }
        } catch (IOException e){
            Log.wtf("MyActivity","Ranking: error reading data file on line " +line, e);
            e.printStackTrace();

        }

        return rank_info;
    }

    //This is a helper function for debugging only, will just print out the rank list in the debug
    private void rankPublish(List<rank<Integer,Long>> list){

        for(int x=0;x<list.size();x++){
            int l = list.get(x).getL();
            long r = list.get(x).getR();
            Log.d("MyActivity","rankings: "+ x + "ID: " +l + "total: " +r);
        }
    }



    //used to rewrite rank file
    //helper function for update this was just split for code clarity
    private void writeRank(List<rank<Integer,Long>> out){
        String filename = "ranking.csv";
        String headers = "ID,Total_Time";
        String line = "";
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(filename, this.MODE_PRIVATE));
            outputStreamWriter.write(headers + '\n');

            for (int i = 0; i < out.size(); i++)
            {

                line = (out.get(i).getL()) + "," + (out.get(i).getR()) + "\n";
                Log.d("MyActivtiy","wrote to file: "+line);
                outputStreamWriter.write(line);

            }

            outputStreamWriter.close();
        }
        catch(IOException e){
            Log.e("Myactivity","Rank: failed to rewrite"+e.toString());
        }

    }


    //DO NOT CALL THIS UNLESS YOU REALLY WANT TO RESET USER STATS
    //ONLY MAKING IT FOR GENERAL PURPOSE/TESTING BUT YEAH MAYBE IF THERE IS A
    //RESET BUTTON IN SETTINGS WE CAN USE IT THEN.
    private void resetData(userData data)
    {
        String filename = "data.csv";
        String path = "src/main/res/raw/";
        String headers = "Rank,Total_Time,Avg_Time,Latest_Time,Succ_Rate,N_Sessions,N_Tries";
        String info = "0,0,0,0,0,0,0";

        try{
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(filename,this.MODE_PRIVATE));
            outputStreamWriter.write(headers+'\n'+info);
            outputStreamWriter.close();
        }
        catch(IOException e){
            Log.e("Myactivity","RESET:File write failed:"+e.toString());
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

            //hide the button?
            Button rl = findViewById(R.id.init_button);



            Bundle bundle = new Bundle();
            bundle.putSerializable("user_info",u_data);

            Fragment selectedFragment = null;

            ArrayList<rank<Integer,Long>> al = new ArrayList<>(rankings.size());
            al.addAll(rankings);

            switch(item.getItemId()) {
                case R.id.study:
                    selectedFragment = new StudyFragment();
                    if(rl.getVisibility() != View.GONE)
                    {
                        rl.setVisibility(View.GONE);
                    }

                    break;
                case R.id.settings:
                    selectedFragment = new SettingsFragment();
                    if(rl.getVisibility() != View.GONE)
                    {
                        rl.setVisibility(View.GONE);
                    }
                    break;
                case R.id.rankings:
                    selectedFragment = new RankingsFragment();
                    if(rl.getVisibility() != View.GONE)
                    {
                        rl.setVisibility(View.GONE);
                    }

                    break;
                case R.id.home:
                    for(int i = 0; i<getSupportFragmentManager().getBackStackEntryCount(); ++i){
                        getSupportFragmentManager().popBackStack();
                        if(rl.getVisibility() != View.VISIBLE)
                        {
                            rl.setVisibility(View.VISIBLE);
                        }
                    }
                    return true;
            }

            bundle.putSerializable("rankings",al);

            selectedFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,selectedFragment).addToBackStack("Fragment")
                    .commit();
            return true;
        }
    };




}