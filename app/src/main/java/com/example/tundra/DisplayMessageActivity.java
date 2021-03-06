package com.example.tundra;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class DisplayMessageActivity extends AppCompatActivity {

    //timing shit
    SeekBar timer_sb;
    TextView timer_tv;
    Button start_btn;
    CountDownTimer countDownTimer;
    Boolean counterIsActive = false;
    userData u_data;

    //gyro stuff
    SensorManager sensorManager;
    Sensor gyroSensor;
    boolean gyroPresent;
    TextView face;

    //magnetometer values
    Sensor magSensor;
    boolean magPresent;

    boolean rotating = false;
    boolean moved;

    View view;
    int state = 0;
    long time;




    //called when activity first started
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        //get user info
        Intent i = getIntent();
        u_data = (userData)i.getSerializableExtra("userInfo");

        //timing initialization and connection to IO
        timer_sb = findViewById(R.id.timer_sb);
        timer_tv = findViewById(R.id.timer_tv);
        start_btn = findViewById(R.id.start_btn);
        timer_sb.setMax(14400); // 4 hours
        timer_sb.setProgress(1500); // 25 minutes

        //create the changebar listener reads progress
        timer_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                update(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //gyro initialization
        face = (TextView)findViewById(R.id.face);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(sensorList.size() > 0){
            gyroPresent = true;
            gyroSensor = sensorList.get(0);
            Log.d("MyActivity","the sensor was there");

            face.setText("Set the Timer and press Start!");
            face.setTextSize(32f);
        }
        else{
            gyroPresent = false;
            face.setText("No accell Present!");
        }


    }



    //write to the user data file,
    private void writeToFile(String data, Context context)
    {
        try{
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("data.txt",Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch(IOException e)
        {
            Log.e("Exception","File failed to write:" + e.toString());
        }
    }

    //update the userdata after the timer goes off
    //has second check to ensure tagging is correct
    //1 == successful study session
    //0 == stopped midway and failed session
    private userData updateData(userData data,long time,int tag){

        //update on success
        if(tag == 1) {
            data.setTotalTime(data.getTotalTime() + time);
            data.setNumSessions(data.getNumSessions() + 1);
            data.setAvg(data.getTotalTime() / data.getNumSessions());
            data.setLatest(time);
            data.setNumTries(data.getNumTries() + 1);
            data.setSuccRate((float)data.getNumSessions() / (float)data.getNumTries());
        }

        else if(tag == 0)
        {
            data.setNumTries(data.getNumTries() + 1);
            data.setSuccRate((float)data.getNumSessions() / (float)data.getNumTries());

        }

        return data;
    }

    //go back
    public void goBack(View view){
        //this should be put into an onclick for a new button;
        Intent intent = new Intent();
        intent.putExtra("user_info", u_data);
        setResult(1, intent);
        DisplayMessageActivity.super.onBackPressed();
    }


    public void start(View newView){
        face.setText("Flip phone face down to begin!");
        if(gyroPresent) {
            sensorManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        view =newView;
        state = 0;
    }


//Start the timer
    public void start_timer(View view){

        Log.d("MyActivity","started timer");

        time = timer_sb.getProgress() * 1000;
        if(counterIsActive == false) {
            counterIsActive = true;
            timer_sb.setEnabled(false);
            start_btn.setText("STOP");
            //long time = timer_sb.getProgress() * 1000;

            countDownTimer = new CountDownTimer(time, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    update((int) millisUntilFinished / 1000);

                }

                @Override
                public void onFinish() {
                    long previous;
                    //get total time and add them to eachother

                    if(gyroPresent){
                        sensorManager.unregisterListener(gyroListener);
                    }

                    previous = u_data.getTotalTime();
                    //Log.d("MyActivity","init:"+u_data.toString());
                    u_data = updateData(u_data, time, 1);
                    Log.d("MyActivity", "update:" + u_data.toString());
                    alarm();
                    reset();
                    state = 0;

                    //reset the variables





                }
            }.start();

        }else{


        }


     //reset the timer and the changebar;
    }




    private void reset() {
        timer_tv.setText("0:240");
        timer_sb.setProgress(1500);
        countDownTimer.cancel();
        start_btn.setText("START");
        timer_sb.setEnabled(true);
        counterIsActive = false;
    }

    // Define update() method
    private void update(int progress) {
        int minutes = progress / 60;
        int seconds = progress % 60;
        String secondsFinal = "";
        if(seconds <= 9){
            secondsFinal = "0" + seconds;
        }else{
            secondsFinal = "" + seconds;
        }
        timer_sb.setProgress(progress);
        timer_tv.setText("" + minutes + ":" + secondsFinal);

        if(state == 4){
            //update the user data after a failure
            reset();
            alarm();
            u_data = updateData(u_data,time,0);


            //reset the variables
            rotating = false;
            moved = false;

            if(gyroPresent){
                sensorManager.unregisterListener(gyroListener);
            }



        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(counterIsActive){
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(counterIsActive){
            countDownTimer.cancel();
        }




    }

    private String create_text(){
        String text ="";
        int max = 2;
        int min = 0;

        int val = (int) Math.floor(Math.random()*(max-min+1)+min);

        switch (val){
            case 0:
                text = "Hey We Said No Phones During Studying";
                break;
            case 1:
                text = "Woah There, Don't Get Distracted";
                break;
            case 2:
                text = "Tik Tok Can Wait Just A Bit Longer";
                break;
        }

        text += "! Try Again!";

        return text;
    }

    //the alarm system we use to notify the user
    private void alarm()
    {

        if(state == 4) {
            face.setText(create_text());
        }else{
            face.setText("GOOD JOB! Go Again?");
        }




        // we will use vibrator first
        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(this.VIBRATOR_SERVICE);
        vibrator.vibrate(4000);


        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        // setting default ringtone
        Ringtone ringtone = RingtoneManager.getRingtone(this, alarmUri);

        long ringDelay = 3500;

        // play ringtone
        ringtone.play();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ringtone.stop();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task,ringDelay);

    }


    //gyro functions
    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    protected void onStop(){
        super.onStop();
        if(gyroPresent){
            sensorManager.unregisterListener(gyroListener);
        }
    }

    //state handling for rotational value
    //0 = intial
    //1 = rotating
    //2 = facedown
    //3 = facedown but moved


    private SensorEventListener gyroListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            //Log.d("MyActivity","in listener");

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float z_value = sensorEvent.values[2];
                float x_value = sensorEvent.values[0];
                float y_value = sensorEvent.values[1];

                Log.d("Sensor","Z: " + z_value + " ,X: "+x_value + " ,Y: " + y_value);

                if(state == 1  && z_value <= -9.8f){
                    Log.d("Sensor","facedown");
                    state = 2;
                    start_timer(view);

                }

                else if(state ==2) {
                    if ((x_value > 2f && x_value < -2f) || (y_value > 2f && y_value < -2f) || z_value > -9.8) {

                        state = 3;
                        //testing
                        //rotating = false;
                        moved = true;
                        Log.d("Sensor", "set down");
                    }
                }
                else if(state ==3) {
                    if ((x_value > .3f && x_value < -.3f) || (y_value > .3f && y_value < -.3f) || z_value > -9.8) {

                        state = 4;
                        //testing
                        //rotating = false;
                        moved = true;
                        Log.d("Sensor ", "Alarm");
                    }
                }

                else if (z_value >= 0) {
                    //face.setText("faceup");
                    //Log.d("Sensor","faceup");
//                    if(state> 2){
//                        state =4;
//                    }
                    //state = 0;
                } else {

                    if (!rotating) {


                        rotating = true;
                        state = 1;

                        face.setText("upsidedown");
                        Log.d("Sensor", "rotating");

                    }
                }

            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

//    public void writeStatus(String s) throws IOException {
//        String filename = "/data/data/com.example.tundra/files/sensorData.txt";
//        //String path = "src/main/res/raw/";
//       //String headers = "Rank,Total_Time,Avg_Time,Latest_Time,Succ_Rate,N_Sessions,N_Tries";
//
//        File f = new File("/data/data/com.example.tundra/files","sensorData.txt");
//
//        if(!f.exists()){
//            f.createNewFile();
//        }
//
//
//        try{
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(filename,this.MODE_APPEND));
//            outputStreamWriter.write(s+'\n');
//            outputStreamWriter.close();
//        }
//        catch(IOException e){
//            Log.e("Myactivity","File write failed:"+e.toString());
//        }
//    }



}