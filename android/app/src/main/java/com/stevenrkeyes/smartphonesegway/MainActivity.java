package com.stevenrkeyes.smartphonesegway;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.lang.Math;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // handles for the IMU
    private SensorManager senSensorManager;
    private Sensor senGravity;
    private Sensor senGyroscope;

    // for storing IMU data
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    // options for bluetooth
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private static final UUID my_uuid = UUID.fromString("0000000-0000-1000-8000-00805f9b34fb");
    private static String address = "20:15:04:15:80:55";

    static double thetadot;
    static double theta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize things related to the IMU
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senGravity = senSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senSensorManager.registerListener(this, senGravity, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senGyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        // Set the onclick function for the button
        final Button bluetooth_button = (Button) findViewById(R.id.bluetooth_button);
        bluetooth_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // toast to indicate button pressed
                Toast toast = Toast.makeText(getApplicationContext(), "It works!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        // initilialize stuff for bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
    }

    @Override
    public void onResume() {
        super.onResume();

        // On resume, reconnect to the bluetooth device
        // Set up a pointer to the remote node using its address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Both the MAC address (acquired above) and Service ID (UUID) are required for the connection
        try {
            btSocket = device.createRfcommSocketToServiceRecord(my_uuid);
        } catch (IOException e) {
            errorExit("Fata Error", "In onResume(), socket create failed: " + e.getMessage() + ".");
        }

        // turn of discovery because it's resource intensive
        btAdapter.cancelDiscovery();

        // Establish the connection
        try {
            btSocket.connect();
            Toast toast = Toast.makeText(getApplicationContext(), "Socket Connected", Toast.LENGTH_SHORT);
            toast.show();
        } catch (IOException e) {
            try {
                // for android >4.2 or something, the bluetooth is a little different
                // or requires some kind of fallback here, i don't really get it
                btSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                btSocket.connect();
            } catch (Exception e2) {
                try {
                    btSocket.close();
                    Toast toast = Toast.makeText(getApplicationContext(), e2.getMessage(), Toast.LENGTH_SHORT);
                    toast.show();
                } catch (IOException e3) {
                    errorExit("Fatal Error", "In onResume, unable to close socket during connection failure: " + e3.getMessage() + ".");
                }
            }
        }

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fata Error", "In onResume(), output stream creation failed: " + e.getMessage() + ".");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // close the bluetooth when the app is paused

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause(), failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause(), failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, Toast.LENGTH_LONG);
        msg.show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume(), an exception occurred during write: " + e.getMessage();
            msg = msg + ".\n\nCheck that the SPP UUID: " + my_uuid.toString() + " exists on server.\n\n";
            errorExit("Fatal Error", msg);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        // use the sensor event to check what kind of sensor it is
        Sensor mySensor = e.sensor;
        if (mySensor.getType() == Sensor.TYPE_GRAVITY) {
            float x = e.values[0];
            float y = e.values[1];
            float z = e.values[2];
            //((android.widget.TextView) findViewById(R.id.x_reading)).setText(Float.toString(x));
            //((android.widget.TextView) findViewById(R.id.y_reading)).setText(Float.toString(y));
            //((android.widget.TextView) findViewById(R.id.z_reading)).setText(Float.toString(z));

            // in radians
            theta = Math.atan2((double)z, (double)y) + Math.PI;

            /*if (z < 0) {
                // send "a" for forward
                sendData("a");
            }
            else {
                // send "b" for backward
                sendData("b");
            }*/
            // todo: combine z from accelerometer and x from gyro to get a PD conroller
        }
        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // in rad/s
            float x = e.values[0];
            thetadot = -x;
        }
        ((android.widget.TextView) findViewById(R.id.x_reading)).setText(Double.toString(thetadot));
        ((android.widget.TextView) findViewById(R.id.y_reading)).setText(Double.toString(theta));

        // controller
        double error_theta = 3.3 - theta;
        double error_thetadot = 0 - thetadot;
        double command_value = 2*error_theta + -7.5*error_thetadot;
        String packet = "a" + String.format("%.2g", command_value) + "z";
        ((android.widget.TextView) findViewById(R.id.z_reading)).setText(packet);
        sendData(packet);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
