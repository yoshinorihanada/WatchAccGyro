package a5817073.aoyama.datacollection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends WearableActivity {
    private SensorManager sensorManager;
    private Sensor acceleromterSensor;
    private Sensor gyroscopeSensor;
    private static final String TAG = "MainActivity";
    private int id;
    private long time;
    private ArrayList<ArrayList<Float>> data1;//acceleration data
    private ArrayList<ArrayList<Float>> data2;//gyroscope data
    private ArrayList<Long> timeData;
    private ArrayList<Long> currentTimeData;
    private boolean isMeasuring;
    private Button startButton;
    private boolean isFirst;
    private EditText idText;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.start);
        idText = findViewById(R.id.id_text);
        isMeasuring = false;
        time = 01;
        id = 0;

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        acceleromterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Enables Always-on
        setAmbientEnabled();

    }

    SensorEventListener _SensorEventListener=   new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d(TAG, "acc: " + x + ", " + y + ", " + z);
                if(isMeasuring){
                    for(int i= 0; i<3;i++)
                    {
                        data1.get(i).add(event.values[i]);
                        data2.get(i).add((float)0);
                    }
                    if (isFirst)
                    {
                        time = event.timestamp;
                        isFirst = false;
                    }
                    timeData.add(event.timestamp - time);
                    currentTimeData.add(System.currentTimeMillis());
                }
            }
            else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                Log.d(TAG, "gyr: " + x + ", " + y + ", " + z);
                if(isMeasuring){
                    for(int i= 0; i<3;i++)
                    {
                        data1.get(i).add((float)0);
                        data2.get(i).add(event.values[i]);
                    }
                    if (isFirst)
                    {
                        time = event.timestamp;
                        isFirst = false;
                    }
                    timeData.add(event.timestamp - time);
                    currentTimeData.add(System.currentTimeMillis());
                }
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }



    };

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void myBtnClick(View v) {
        if(isMeasuring){
            isMeasuring = false;
            startButton.setText("START");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    OutputFile();
                }
            }).start();

        }
        else {
            if (idText.getText().toString().equals("")) {
                Toast.makeText(this, "Input your ID", Toast.LENGTH_SHORT).show();
            }
            else {
                isMeasuring = true;
                isFirst = true;
                startButton.setText("STOP");
                data1 = new ArrayList<>();
                data2 = new ArrayList<>();
                timeData = new ArrayList<>();
                currentTimeData = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    ArrayList<Float> arr1 = new ArrayList<>();
                    ArrayList<Float> arr2 = new ArrayList<>();
                    data1.add(arr1);
                    data2.add(arr2);

                }
                id = Integer.parseInt(idText.getText().toString());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(_SensorEventListener, acceleromterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(_SensorEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(_SensorEventListener);

    }



    private void OutputFile() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_kkmmss");
        String filename = sdf.format(date) + ".csv";
        Log.d(TAG, filename);
        try {
            FileOutputStream fout = openFileOutput("Watch_" + String.format("%03d", id) + "_" + filename, MODE_PRIVATE);
            String comma = ",";
            String newline = "\n";
            fout.write("ax,ay,az,gx,gy,gz,time,localTime\n".getBytes());
            for (int i = 0; i < data1.get(0).size(); i++) {
                for (int j = 0; j < 3; j++)
                {
                    fout.write(String.valueOf(data1.get(j).get(i)).getBytes());
                    fout.write(comma.getBytes());
                }
                for (int j = 0; j < 3; j++)
                {
                    fout.write(String.valueOf(data2.get(j).get(i)).getBytes());
                    fout.write(comma.getBytes());
                }
                fout.write(String.format("%.6f", Float.parseFloat(timeData.get(i).toString())/1000000000f).getBytes());
                fout.write(comma.getBytes());
                fout.write(String.valueOf(currentTimeData.get(i)).getBytes());
                fout.write(newline.getBytes());
            }
            fout.close();
            Log.d(TAG, "File created.");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Cannot open file.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "Cannot write string.");
            e.printStackTrace();
        }
    }
}
