package com.example.mdp_grp29.bluetooth;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.example.mdp_grp29.R;

public class BluetoothComponent {

    private static BluetoothComponent instance = null;
    private Handler mHandler = null;
    private Activity main;
    private BroadcastReceiver bReceiver = null;
    private BluetoothService bluetoothService;
    public boolean isConnected = false;
    private String deviceName = "";

    public static BluetoothComponent getInstance(Activity main, Handler mHandler){
        if(instance == null)
            instance = new BluetoothComponent(main, mHandler);
        return instance;
    }

    public BluetoothComponent(Activity main, Handler mHandler) {
        this.main = main;
        this.mHandler = mHandler;
        bluetoothService = new BluetoothService(mHandler);
    }

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

    public void setNewHandler(Handler mHandler){
        this.mHandler = mHandler;
    }
    public void setbReceiver(BroadcastReceiver bReceiver){ this.bReceiver = bReceiver; }

}
