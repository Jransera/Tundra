package com.example.tundra;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DisplayMessageActivity extends AppCompatActivity {
    //timer variables
    TimePicker alarmTimePicker;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        //get intent
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        //set string as text

        TextView textView = findViewById(R.id.textView);
        textView.setText(message);

        //set alarm variables
        alarmTimePicker = (TimePicker) findViewById(R.id.time_setter);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    public void OnToggleClicked(View view)
    {
        long time;
        //initialize when button is pressed // this we can maybe set to the proximity sensor?
        if(((ToggleButton) view).isChecked())
        {
            Toast.makeText(DisplayMessageActivity.this,"ALARM ON",Toast.LENGTH_SHORT).show();
            Calendar calendar = Calendar.getInstance();

            //get time information from picker
            calendar.set(Calendar.HOUR_OF_DAY,alarmTimePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE,alarmTimePicker.getCurrentMinute());

            //set new intent for the alarm reciever
            Intent intent = new Intent(this, AlarmReceiver.class);


            pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);
            Toast.makeText(DisplayMessageActivity.this,"intent made", Toast.LENGTH_SHORT).show();

            //time
            time = (calendar.getTimeInMillis() - (calendar.getTimeInMillis() % 60000));

            if (System.currentTimeMillis() > time)
            {
                //setting time  as am or pm
                if(calendar.AM_PM == 0)
                    time = time + (1000*60 *60 *12);
                else
                    time = time + (1000 *60 *60 *24);

            }
            String temp = String.valueOf(time);
            Toast.makeText(DisplayMessageActivity.this,temp, Toast.LENGTH_SHORT).show();
            //repeat until toggle is hit
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time,pendingIntent);

        }
        else
        {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(DisplayMessageActivity.this,"ALARM OFF", Toast.LENGTH_SHORT).show();
        }
    }

}