package com.example.mdp_grp29.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.mdp_grp29.R;

import java.util.ArrayList;

public class BluetoothHelper {

    private static BluetoothHelper instance = null;
    private Activity main;
    private BluetoothService bluetoothService = null;
    private boolean isConnected = false;
    private String deviceName = "";
    private Toolbar btToolbar;
    private TextView btTextView;
    public static StringBuffer mOutStringBuffer;

    private CountDownTimer reconnectTimer;

    private ArrayAdapter<String> persistentIncomingComms;
    private ArrayAdapter<String> persistentOutgoingComms;

    public BroadcastReceiver broadcastReceiver = null;

    public static BluetoothHelper getInstance(Activity main, Handler mHandler){
        if(instance == null)
            instance = new BluetoothHelper(main, mHandler);
        else
            instance.bluetoothService.setNewHandler(mHandler);
        return instance;
    }

    // TODO: Save all bluetooth incoming and outgoing messages in this instance class instead

    public BluetoothHelper(Activity main, Handler mHandler) {
        this.main = main;
        btToolbar = main.findViewById(R.id.btToolbar);
        btTextView = main.findViewById(R.id.tvBluetoothStatus);
        mOutStringBuffer = new StringBuffer();
        this.bluetoothService = new BluetoothService(mHandler);
    }

    public ArrayAdapter<String> getPersistentOutgoingComms(){
        return persistentOutgoingComms;
    }

    public ArrayAdapter<String> getPersistentIncomingComms(){
        return persistentIncomingComms;
    }

    public void storePersistentCommsData(ArrayAdapter<String> incomingComms, ArrayAdapter<String> outgoingComms){

        persistentIncomingComms = incomingComms;
        persistentOutgoingComms = outgoingComms;

    }

    // Update the Bluetooth Toolbar
    public void updateBluetoothTBStatus() {
        if(btToolbar == null){
            Log.d("BluetoothComponent", "bluetoothToolBar not found!");
            return;
        }
        if (isConnected) {
            btToolbar.setBackgroundColor(ContextCompat.getColor(main.getApplicationContext(), R.color.green));
            btTextView.setText(main.getString(R.string.bt_status_on) + ": " + deviceName);
        } else {
            btToolbar.setBackgroundColor(ContextCompat.getColor(main.getApplicationContext(), R.color.grey));
            btTextView.setText(main.getString(R.string.bt_status_off));
        }
    }

    public void connectBluetoothDevice(String address){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothDevice deviceMac = bluetoothAdapter.getRemoteDevice(address);
        bluetoothService.connect(deviceMac, false);
    }

    public void disconnectBluetoothDevice(){
        bluetoothService.stop();
    }

    public void sendBluetoothMessage(String message){
        bluetoothService.write(message.getBytes());
        mOutStringBuffer.setLength(0);
    }

    public BluetoothService getBluetoothService() { return this.bluetoothService; }

    public String getDeviceName(){ return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public void setConnectionStatus(boolean isConnected){
        this.isConnected = isConnected;
        if(!isConnected)
            setDeviceName("");
        updateBluetoothTBStatus();
    }

    public boolean getConnectionStatus()
    {
        return isConnected;
    }


}
