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
        timer_sb.setProgress(1550); // 25 minutes

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

        //accelerometer initialization
        face = (TextView)findViewById(R.id.face);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if(sensorList.size() > 0){
            gyroPresent = true;
            gyroSensor = sensorList.get(0);
        }
        else{
            gyroPresent = false;
            face.setText("No accelerometer Present!");
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




//Start the timer
    public void start_timer(View view) {
        long time = timer_sb.getProgress() * 1000;
        if(counterIsActive == false){
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
                    previous = u_data.getTotalTime();
                    //Log.d("MyActivity","init:"+u_data.toString());
                    u_data = updateData(u_data,time,1);
                    Log.d("MyActivity","update:"+u_data.toString());
                    alarm();
                    reset();

                    //this should be put into an onclick for a new button;
                    Intent intent = new Intent();
                    intent.putExtra("user_info",u_data);
                    setResult(1,intent);
                    DisplayMessageActivity.super.onBackPressed();

                }
            }.start();
        }else{
            //update the user data after a failure
            u_data = updateData(u_data,time,0);
            reset();

            //put into a button to close the studying page
            Intent intent = new Intent();
            intent.putExtra("user_info",u_data);
            setResult(1,intent);
            DisplayMessageActivity.super.onBackPressed();
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

    //the alarm system we use to notify the user
    private void alarm()
    {
        Toast.makeText(this, "Alarm! Wake up! Wake up!", Toast.LENGTH_LONG).show();

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
        if(gyroPresent){
            sensorManager.registerListener(gyroListener,gyroSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(gyroPresent){
            sensorManager.unregisterListener(gyroListener);
        }
    }

    private SensorEventListener gyroListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
         float y_value = sensorEvent.values[1];
         if(y_value ==0){
             face.setText("Still");
         }
         else{
             face.setText("Rotating");
         }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

}