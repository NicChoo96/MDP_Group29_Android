package com.example.mdp_grp29.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.mdp_grp29.R;

import java.util.ArrayList;

public class BluetoothComponent {

    private static BluetoothComponent instance = null;
    private Activity main;
    private BroadcastReceiver bReceiver = null;
    private BluetoothService bluetoothService = null;
    public boolean isConnected = false;
    private String deviceName = "";
    private Toolbar btToolbar;
    private TextView btTextView;
    public static StringBuffer mOutStringBuffer;

    private ArrayList<String> persistentIncomingComms;
    private ArrayList<String> persistentOutgoingComms;

    public static BluetoothComponent getInstance(Activity main, Handler mHandler){
        if(instance == null)
            instance = new BluetoothComponent(main, mHandler);
        else
            instance.bluetoothService.setNewHandler(mHandler);
        return instance;
    }

    // TODO: Save all bluetooth incoming and outgoing messages in this instance class instead

    public BluetoothComponent(Activity main, Handler mHandler) {
        this.main = main;
        btToolbar = main.findViewById(R.id.btToolbar);
        btTextView = main.findViewById(R.id.tvBluetoothStatus);
        persistentIncomingComms = new ArrayList<String>();
        persistentOutgoingComms = new ArrayList<String>();
        if(this.bluetoothService == null){
            mOutStringBuffer = new StringBuffer();
            this.bluetoothService = new BluetoothService(mHandler);
        }
        else
            this.bluetoothService.setNewHandler(mHandler);
    }

    public ArrayList<String> getPersistentOutgoingComms(){
        return persistentOutgoingComms;
    }

    public ArrayList<String> getPersistentIncomingComms(){
        return persistentIncomingComms;
    }

    public void storePersistentCommsData(ArrayAdapter<String> incomingComms, ArrayAdapter<String> outgoingComms){

        if(persistentIncomingComms == null)
            persistentIncomingComms = new ArrayList<String>();

        if(persistentOutgoingComms == null)
            persistentOutgoingComms = new ArrayList<String>();

        for(int i = persistentIncomingComms.size(); i < incomingComms.getCount(); i++){
            persistentIncomingComms.add(incomingComms.getItem(i));
        }
        for(int i = persistentOutgoingComms.size(); i < outgoingComms.getCount(); i++){
            persistentOutgoingComms.add(outgoingComms.getItem(i));
        }
    }

    // Update the Bluetooth Toolbar
    public void updateBluetoothTBStatus() {
        if (isConnected) {
            btToolbar.setBackgroundColor(ContextCompat.getColor(main.getApplicationContext(), R.color.green));
            btTextView.setText(main.getString(R.string.bt_status_on) + ": " + deviceName);
        } else {
            btToolbar.setBackgroundColor(ContextCompat.getColor(main.getApplicationContext(), R.color.grey));
            btTextView.setText(main.getString(R.string.bt_status_off));
        }
    }

    public void setBluetoothService(BluetoothService bluetoothService) { this.bluetoothService = bluetoothService; }
    public BluetoothService getBluetoothService() { return this.bluetoothService; }

    public String getDeviceName(){ return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    // Method: Register broadcast receivers
    public void registerBroadcastReceivers() {
        if(bReceiver == null)
            return;
        main.registerReceiver(bReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        main.registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        main.registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        main.registerReceiver(bReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        main.registerReceiver(bReceiver, new IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
    }

    public void destroyReceivers(){
        if(bReceiver == null)
            return;
        main.unregisterReceiver(bReceiver);
    }

    public void setbReceiver(BroadcastReceiver bReceiver){ this.bReceiver = bReceiver; }

}
