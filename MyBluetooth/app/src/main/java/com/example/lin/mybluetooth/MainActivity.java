package com.example.lin.mybluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private Button mybutton = null;
    private TextView myText = null;
    private BluetoothSocket bluetoothSocket = null;
    private ReceiveDatas receiveThread = null;
    private SendDatas sendThread = null;
    private AppCompatActivity myActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mybutton = (Button)findViewById(R.id.btn2);
        mybutton.setOnClickListener(new ButtonListener());

        myText = (TextView)findViewById(R.id.View);
        myText.append("\n...In onCreate()...");

        myActivity = this;
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


    class ButtonListener implements OnClickListener
    {
        public void onClick(View v)
        {
            //Get BluetoothAdapter
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            //Make sure adapter is null
            if(adapter != null)
            {
                myText.append("\n...Found the Bluetooth Devices...");

                //Make sure adapter can be used
                if(!adapter.isEnabled())
                {
                    //If adapter cann't be used, create an intent objext and start Activity
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intent);
                }

                //Get all Bluetooth Devices objects
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                if(devices.size()>0)
                {
                    for(Iterator iterator = devices.iterator();iterator.hasNext();)
                    {
                        BluetoothDevice device = (BluetoothDevice)iterator.next();
                        myText.append("\n...Server Address:" + device.getAddress() + "...");
                        myText.append("\n...Server Name:" + device.getName() + "...");
                        ParcelUuid[] uuids = device.getUuids();
                        for (ParcelUuid u : uuids)
                        {
                            myText.append("\n...Server UUID:" + String.valueOf(u) + "...");
                            Log.d("mytag", String.valueOf(u));
                        }

                        UUID uuid = null;

                        //if (uuids.length > 0)
                        //    uuid = UUID.fromString(String.valueOf(uuids[0]));
                        // else
                        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                        //Create Socket to Server
                        try{
                            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                            //bluetoothSocket = device.createRfcommSocketToServiceRecord();
                            //bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                            //bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                            myText.append("\n...socket established and data link opened... ");
                        }catch (IOException e){
                            myText.append("\n...In onResume() and socket create failed: " + e.getMessage() + ".");
                            return;
                        }

                        adapter.cancelDiscovery();

                        //Connect to Server
                        try{
                            bluetoothSocket.connect();
                            myText.append("\n...Connection established and data link opened... ");
                        }catch (IOException e){
                            myText.append("\n...In onResume() and Connection create failed: " + e.getMessage() + ".");
                            return;
                        }

                        //Create the thread that receive data from server.
                        myText.append("\n...Create a thread to listen server...\n\n ");
                        receiveThread = new ReceiveDatas(bluetoothSocket);
                        receiveThread.start();

                        //Create the thread that send data to server.
                        myText.append("\n...Create a thread to send to server...\n\n ");
                        sendThread = new SendDatas(bluetoothSocket);
                        sendThread.start();
                    }
                }
            }
            else
            {
                myText.append("\n...We didn't find bluetooth devices. ");
            }
        }
    }

    //the thread that receive data from server.
    class ReceiveDatas extends Thread
    {

        private BluetoothSocket mmSocket = null;
        private InputStream mmInStream = null;
        private String readMessage = "";

        public ReceiveDatas(BluetoothSocket socket)
        {
            this.mmSocket = socket;
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
        }

        @Override
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes = 0;

            while (true)
            {
                try {
                    bytes = mmInStream.read(buffer);
                } catch (IOException e) {
                    try {
                        mmInStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        return;
                    }
                    return;
                }
                readMessage = new String(buffer, 0, bytes);
                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myText.append("Message from server:" + readMessage);
                    }
                });
            }
        }
    }

    //the thread that send data to server.
    class SendDatas extends Thread
    {

        private BluetoothSocket mmSocket = null;
        private OutputStream mmOutStream = null;
        private String sendMessage = "";

        public SendDatas(BluetoothSocket socket)
        {
            this.mmSocket = socket;
            OutputStream tmpOut = null;

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmOutStream = tmpOut;
        }

        @Override
        public void run()
        {
            final String message = "Hello, the information is from Android.\n";
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            myActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myText.append("Message to server:" + message);
                }
            });
        }
    }

}



