package com.example.tundra;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DisplayMessageActivity extends AppCompatActivity {
    //timer variables
//    TimePicker alarmTimePicker;
//    PendingIntent pendingIntent;
//    AlarmManager alarmManager;

    SeekBar timer_sb;
    TextView timer_tv;
    Button start_btn;
    CountDownTimer countDownTimer;
    Boolean counterIsActive = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        timer_sb = findViewById(R.id.timer_sb);
        timer_tv = findViewById(R.id.timer_tv);
        start_btn = findViewById(R.id.start_btn);
        timer_sb.setMax(14400); // 4 hours
        timer_sb.setProgress(1550); // 25 minutes
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

        //get intent

        //set alarm variables
//        alarmTimePicker = (TimePicker) findViewById(R.id.time_setter);
//        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

    }

//    public void OnToggleClicked(View view)
//    {
//        long time;
//        //initialize when button is pressed // this we can maybe set to the proximity sensor?
//        if(((ToggleButton) view).isChecked())
//        {
//            Toast.makeText(DisplayMessageActivity.this,"ALARM ON",Toast.LENGTH_SHORT).show();
//            Calendar calendar = Calendar.getInstance();
//
//            //get time information from picker
//            calendar.set(Calendar.HOUR_OF_DAY,alarmTimePicker.getCurrentHour());
//            calendar.set(Calendar.MINUTE,alarmTimePicker.getCurrentMinute());
//
//            //set new intent for the alarm reciever
//            Intent intent = new Intent(this, AlarmReceiver.class);
//
//
//            pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);
//            Toast.makeText(DisplayMessageActivity.this,"intent made", Toast.LENGTH_SHORT).show();
//
//            //time
//            time = (calendar.getTimeInMillis() - (calendar.getTimeInMillis() % 60000));
//
//            if (System.currentTimeMillis() > time)
//            {
//                //setting time  as am or pm
//                if(calendar.AM_PM == 0)
//                    time = time + (1000*60 *60 *12);
//                else
//                    time = time + (1000 *60 *60 *24);
//
//            }
//            String temp = String.valueOf(time);
//            Toast.makeText(DisplayMessageActivity.this,temp, Toast.LENGTH_SHORT).show();
//            //repeat until toggle is hit
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time,pendingIntent);
//
//        }
//        else
//        {
//            alarmManager.cancel(pendingIntent);
//            Toast.makeText(DisplayMessageActivity.this,"ALARM OFF", Toast.LENGTH_SHORT).show();
//        }
//    }

    public void start_timer(View view) {
        if(counterIsActive == false){
            counterIsActive = true;
            timer_sb.setEnabled(false);
            start_btn.setText("STOP");
            countDownTimer = new CountDownTimer(timer_sb.getProgress() * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    update((int) millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {
                    reset();
                }
            }.start();
        }else{
            reset();
        }
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
}