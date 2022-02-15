package com.example.tundra;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //all the shit we may need
    public static final String EXTRA_MESSAGE = "com.example.tundra.MESSAGE";
    TextView sensorStatusTV;
    SensorManager sensorManager;
    Sensor proximitySensor;

    @Override
    public <T extends View> T findViewById(int id) {
        return super.findViewById(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorStatusTV = findViewById(R.id.sensorStatusTV);

        //call sensor service
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //case handling
        if(proximitySensor == null){
            Toast.makeText(this, "no Prox Sensor",Toast.LENGTH_SHORT).show();
            //finish();
        }
        else{
            //set a listener -> (type, sensor to listen, interval)
            sensorManager.registerListener(proximitySensorEventListener,proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

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

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    public void sendMessages(View view){
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE,message);
        startActivity(intent);
    }
}