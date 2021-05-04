package com.example.apphienden;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private Button btnOn,btnOff;
    private String address = null;
    private EditText editText;
    private ProgressDialog progress;
    private SeekBar seekBar;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean isReady=false;
    private RadioButton rdoOn,rdoOff;
    private RadioGroup radioGroup;
    private SwipeRefreshLayout swipeRefreshLayout;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rdoOn=findViewById(R.id.rdo_on);
        rdoOff=findViewById(R.id.rdo_off);
        radioGroup=findViewById(R.id.radioGroup);
        seekBar=findViewById(R.id.seekBar);
        btnOn=findViewById(R.id.btn_send);
        btnOff=findViewById(R.id.btn_off);
        editText=findViewById(R.id.edt_val);
        swipeRefreshLayout=findViewById(R.id.swiperefresh);
        address="00:20:12:08:BB:97";
        seekBar.setMin(1);
        seekBar.setMax(50);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
        if(myBluetooth.isEnabled()){
            BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
            ConnectBT connectBT=new ConnectBT();
            connectBT.execute();
            isReady=true;
        }

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isReady){
                    String val=editText.getText().toString().trim();
                    if(val.isEmpty()){
                        Toast.makeText(MainActivity.this, "vui lòng điền vào ô", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String speed="SPD:"+(51-seekBar.getProgress())+"#";
                        String isOn="OFF";
                        if(getState()) isOn="ON";
                        String state="STT:"+isOn;
                        String mess="MESS:"+val+"#";
                        StringBuilder sb=new StringBuilder();
                        sb.append(state);
                        if(isOn.equals("ON")){
                            sb.append("#");
                            sb.append(speed);
                            sb.append(mess);
                        }
                        sb.append("\n");
                        sendMess(sb.toString());
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "bluetooth adapter chưa sẵn sàng", Toast.LENGTH_SHORT).show();
                    myBluetooth=BluetoothAdapter.getDefaultAdapter();
                }
            }
        });
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Disconnect();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                ConnectBT connectBT=new ConnectBT();
                connectBT.execute();
                isReady=true;
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    boolean getState(){
        if(rdoOn.isChecked()){
            return true;
        }
        return false;
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }
    private void sendMess(String val)
    {
        if (btSocket!=null)
        {
            try
            {
                Log.d("AAA1","not null");
                btSocket.getOutputStream().write((val+"\n").toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
        else{
            Log.d("AAA1","null");
        }
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
                Log.d("AAA1","error : "+e.getMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                Toast.makeText(MainActivity.this,"Connection Failed. Is it a SPP Bluetooth? Try again.", Toast.LENGTH_SHORT).show();
//                finish();
            }
            else
            {
                Toast.makeText(MainActivity.this, "Connected.", Toast.LENGTH_SHORT).show();
                isBtConnected = true;
            }
            progress.dismiss();
        }
        
    }


}