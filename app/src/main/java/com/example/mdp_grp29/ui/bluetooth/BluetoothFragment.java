package com.example.mdp_grp29.ui.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mdp_grp29.Constants;
import com.example.mdp_grp29.R;
import com.example.mdp_grp29.bluetooth.BluetoothComponent;
import com.example.mdp_grp29.bluetooth.BluetoothService;
import com.example.mdp_grp29.databinding.FragmentBluetoothBinding;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothFragment extends Fragment {

    private BluetoothViewModel bluetoothViewModel;
    private FragmentBluetoothBinding binding;


    // Tag string
    private static final String TAG = "BluetoothActivity";

    // Interface for Bluetooth OS Service
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothService bluetoothService = null;
    private String device = "";
    public static StringBuffer mOutStringBuffer;
    private boolean registered = false;

    private BluetoothComponent bluetoothComponent;

    // Relate to GUI
    private ListView lvPairedDevices, lvAvailDevices;
    private TextView tvNoPairDevices, tvNoAvailDevices;
    private Button btnDiscover, btnScan, btnRefresh, btnDisconnect;
    private ProgressBar pbPair, pbAvail;
    private ProgressDialog progress;

    // Variable - List
    private ArrayAdapter<String> newDevicesArrayAdapter, pairedDevicesArrayAdapter;

    // Variables
    private String previousConnectedAddress = "";
    private boolean isReconnecting = false;

    private Activity main;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bluetoothViewModel =
                new ViewModelProvider(this).get(BluetoothViewModel.class);

        binding = FragmentBluetoothBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        bluetoothViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        //main.setContentView(R.layout.fragment_bluetooth);

        main = getActivity();

        // Relate to UI
        lvPairedDevices = view.findViewById(R.id.lvPairedDevices);
        lvAvailDevices = view.findViewById(R.id.lvAvailDevices);
        tvNoPairDevices = view.findViewById(R.id.tvNoPairDevices);
        tvNoAvailDevices = view.findViewById(R.id.tvNoAvailDevices);
        btnDiscover = view.findViewById(R.id.button_Discover);
        btnScan = view.findViewById(R.id.button_BluetoothScan);
        btnRefresh = view.findViewById(R.id.button_Refresh);
        btnDisconnect = view.findViewById(R.id.button_Disconnect);
        pbPair = view.findViewById(R.id.pbPair);
        pbAvail = view.findViewById(R.id.pbAvail);

        btnScan.setText(R.string.button_stop);

        // List View of Devices
        pairedDevicesArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.device_name);
        lvPairedDevices.setAdapter(pairedDevicesArrayAdapter);
        lvPairedDevices.setOnItemClickListener(myListClickListener);

        newDevicesArrayAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1);
        lvAvailDevices.setAdapter(newDevicesArrayAdapter);
        lvAvailDevices.setOnItemClickListener(myListClickListener);

        // Make device discoverable
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableDiscovery();
            }
        });

        // Scan for available devices
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAvailableDeviceList();
//                Bundle result = new Bundle();
//                result.putString("thisNutz", "Nutz");
//                getParentFragmentManager().setFragmentResult("myKey", result);
//                showToast("Result sent");
            }
        });

        // Refresh paired devices for update
        btnRefresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                updatePairDevicesList();
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                bluetoothService.stop();
            }
        });



        // Get Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("Device does not support Bluetooth");
            return;
        } else {
            // Bluetooth is not enabled
            if (!bluetoothAdapter.isEnabled())
                main.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        BluetoothService.REQUEST_ENABLE_BT);
            else
            {
                //unpairAllDevices();
                updatePairDevicesList();
            }
        }

        bluetoothComponent = BluetoothComponent.getInstance(main, mHandler);
        bluetoothComponent.updateBluetoothTBStatus();
        bluetoothService = bluetoothComponent.getBluetoothService();
        bluetoothComponent.setbReceiver(bReceiver);

        if(bluetoothService.getState() == BluetoothService.STATE_CONNECTED)
            btnDisconnect.setVisibility(View.VISIBLE);

        // Register broadcast receivers
        bluetoothComponent.registerBroadcastReceivers();
    }

    private void connectionUpdateSequence(boolean isConnected){
        if(isConnected){
            bluetoothComponent.setDeviceName(device);
            bluetoothComponent.updateBluetoothTBStatus();
            btnDisconnect.setVisibility(View.VISIBLE);
            bluetoothComponent.isConnected = true;
        }else{
            device = "";
            bluetoothComponent.setDeviceName(device);
            bluetoothComponent.isConnected = false;
        }
    }

    // Check for BT Permission
    private void checkBTPermissions() {
        int permissionCheck = main.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += main.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        // Request for BT Permission
        if (permissionCheck != 0)
            main.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
    }

    // C2 - Initiate Scanning
    // Method: Enable discovery of bluetooth device to other devices
    private void enableDiscovery() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        } else
            showToast("Device is already discoverable");
    }

    // C2 - Select List of Devices - Available
    // Find nearby available bluetooth devices
    public void updateAvailableDeviceList() {
        if (pbAvail.getVisibility() == View.VISIBLE) {
            // Cancel if device is already discovering
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            btnScan.setText(R.string.bt_scan_avail_btn);
            showToast("Scanning stopped.");
            // Hide progress bar
            pbAvail.setVisibility(View.GONE);
        } else {
            // Update list of available devices
            newDevicesArrayAdapter.clear();
            // Change btn text
            btnScan.setText(R.string.button_stop);
            // Show button progress bar
            pbAvail.setVisibility(View.VISIBLE);
            // Notify user the action taken
            showToast("Scanning for devices...");

            // Cancel if device is already discovering
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();

            // Check if user has enabled BT permission
            checkBTPermissions();

            // Start finding for available Bluetooth devices
            bluetoothAdapter.startDiscovery();
            main.registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    // TODO: Only for debugging and testing, remember to comment out
// To clear all the list of paired devices connected to device before.
//    public void unpairAllDevices(){
//        if(btAdapter != null){
//            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
//            for (BluetoothDevice device : pairedDevices) {
//                try {
//                    Method m = device.getClass().getMethod("removeBond", (Class[]) null);
//                    m.invoke(device, (Object[]) null);
//                } catch (Exception e) {
//                    Log.w(TAG, "Failed to un-pair device.");
//                }
//            }
//        }
//    }

    // C2 - Select List of Devices - Pair
    // Find nearby paired bluetooth devices
    public void updatePairDevicesList() {
        pairedDevicesArrayAdapter.clear();
        pbPair.setVisibility(View.VISIBLE);
        lvPairedDevices.setVisibility(View.GONE);
        tvNoPairDevices.setVisibility(View.GONE);

        // .getBondedDevices() to retrieve list of paired devices attached to device
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();
        final boolean hasList = pairedDevices.size() > 0;

        if (hasList) { // Found at least one paired devices
            for (BluetoothDevice bt : pairedDevices)
                // Add found paired devices to list
                list.add(bt.getName() + "\n MAC Address: " + bt.getAddress());
            pairedDevicesArrayAdapter.addAll(list);
        }

        // Timer to stop if too long
        new CountDownTimer(500, 500) {
            @Override
            public void onTick(long l) {
            }

            public void onFinish() {
                lvPairedDevices.setVisibility(View.VISIBLE);
                pbPair.setVisibility(View.GONE);
                if (hasList)
                    tvNoPairDevices.setVisibility(View.GONE);
                else
                    tvNoPairDevices.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private AdapterView.OnItemClickListener myListClickListener =
            new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
                    // Get device's MAC address - the last 17 chars in the View
                    String info = ((TextView) v).getText().toString();
                    String address = info.substring(info.length() - 17);
                    device = info.substring(0, info.length() - 30);
                    //singletonDevice.setDeviceName(device);
                    connectBluetoothDevice(address);
                }
            };

    // C2 - Connect bluetooth devices
    public void connectBluetoothDevice(String address) {
        final BluetoothDevice deviceMac = bluetoothAdapter.getRemoteDevice(address);
        bluetoothService = new BluetoothService(mHandler);
        bluetoothComponent.setBluetoothService(bluetoothService);
        // Connect to device that was clicked
        bluetoothService.connect(deviceMac, false);

        previousConnectedAddress = address;

        if (!bluetoothComponent.isConnected)
            // Notify user that app is trying to connect to device
            progress = ProgressDialog.show(main,
                    "Connecting...", "Please wait.");
        else
            // Notify user that app connect again after disconnected somehow
            progress = ProgressDialog.show(main,
                    "Disconnecting...", "Please wait.");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Close ProgressDialog
                progress.dismiss();
            }
        }, 3000); // Delay of 1s
    }

    // C2 - Display connection status
    // Handler that get info back from BluetoothService.java
    public final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        // Bluetooth Service: Connected to device
                        case BluetoothService.STATE_CONNECTED:
                            Log.d("Handler Log: ", "STATE_CONNECTED");
                            connectionUpdateSequence(true);
                            break;
                        // Bluetooth Service: Connecting to device
                        case BluetoothService.STATE_CONNECTING:
                            Log.d("Handler Log: ", "STATE_CONNECTING");
                            showToast("Connecting...");
                            connectionUpdateSequence(false);
                            break;
                        // Bluetooth Service: Listening for devices
                        case BluetoothService.STATE_LISTEN:
                            Log.d("Handler Log: ", "STATE_LISTEN");
                            connectionUpdateSequence(false);
                        case BluetoothService.STATE_NONE:
                            connectionUpdateSequence(false);
                            bluetoothComponent.updateBluetoothTBStatus();
                            btnDisconnect.setVisibility(View.GONE);
                            break;
//                        // C8 - Reconnection of Bluetooth device
//                        case BluetoothService.STATE_NONE:
//                            Log.d("Handler Log: ", "STATE_DISCONNECTED");
//                            Log.d(TAG, "Connection lost, attempting for reconnection...");
//                            connectBluetoothDevice(previousConnectedAddress);
                    }
                    break;
                case Constants.MESSAGE_READ:
                    Log.d("Handler Log: ", "MESSAGE_READ");
                    byte[] readBuf = (byte[]) msg.obj;
                    // Construct string from valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    // Send message to MainActivity that was received in buffer
                    //sendTextToMain(readMessage);
                    Log.d("Handler Log: ", "MESSAGE_READ - " + readMessage);
                case Constants.MESSAGE_DEVICE_NAME:
                    Log.d("Handler Log: ", "MESSAGE_DEVICE_NAME");
                    // Save the connected device's name
                    device = msg.getData().getString(Constants.DEVICE_NAME);
                    Log.d("Handler Log: ", "MESSAGE_DEVICE_NAME - " + device);
                    if (main.getApplicationContext() != null) {
                        if (device != null) {
                            showToast("Connected to: " + device);
                            connectionUpdateSequence(true);
                            bluetoothComponent.updateBluetoothTBStatus();
                            progress.dismiss();
                            // Send device name currently connected to MainActivity
//                            Intent i = new Intent(BluetoothActivity.this,
//                                    MainActivity.class);
//                            startActivity(i);
                        }
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    Log.d("Handler Log: ", "MESSAGE_TOAST");
                    if (main.getApplicationContext() != null) {
                        String theMsg = msg.getData().getString(Constants.TOAST);
                        if (theMsg.equalsIgnoreCase("device connection was lost")) {
                            showToast(theMsg);
                            // Send string to MainActivity that devices lost connection
                            bluetoothComponent.updateBluetoothTBStatus();
                            // Send message to MainActivity that no device currently connected
                            bluetoothComponent.setDeviceName("");
                        }
                    }
                    break;
            }
        }
    };

//    // Method: Pass data to MainActivity.java
//    private void sendToMain(String msg) {
//        updateBluetoothTBStatus();
//        Intent intent = new Intent("getConnectedDevice");
//        intent.putExtra("message", msg);
//        LocalBroadcastManager.getInstance(main).sendBroadcast(intent);
//    }
//
//    // Method: Send text received from bluetooth connection
//    private void sendTextToMain(String msg) {
//        Intent intent = new Intent("getTextFromDevice");
//        // Can include extra data.
//        intent.putExtra("text", msg);
//        LocalBroadcastManager.getInstance(main).sendBroadcast(intent);
//    }

//    // Method: Register required receivers
//    private void registerLocalReceivers() {
//        if (registered) return;
//        LocalBroadcastManager.getInstance(main).registerReceiver(mTextReceiver,
//                new IntentFilter("getTextToSend"));
//        LocalBroadcastManager.getInstance(main).registerReceiver(mCtrlReceiver,
//                new IntentFilter("getCtrlToSend"));
//        registered = true;
//    }

//    // Method: Destroy all receivers
//    private void destroyReceivers() {
//        if (!registered) return;
//        LocalBroadcastManager.getInstance(main).unregisterReceiver(mTextReceiver);
//        LocalBroadcastManager.getInstance(main).unregisterReceiver(mCtrlReceiver);
//        registered = false;
//    }

    // Broadcast receiver for bluetooth
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Event: When a remote device is found during discovery
                Log.d("BluetoothActivity", "bReceiver: ACTION_FOUND");
                tvNoAvailDevices.setVisibility(View.GONE);
                lvAvailDevices.setVisibility(View.VISIBLE);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String newDevice = device.getName() + "\n MAC Address: " + device.getAddress();
                newDevicesArrayAdapter.add(newDevice);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // Event: When bluetooth has completed scanning
                Log.d("BluetoothActivity", "bReceiver: ACTION_DISCOVERY_FINISHED");
                pbAvail.setVisibility(View.GONE);
                if (newDevicesArrayAdapter.getCount() == 0) {
                    tvNoAvailDevices.setVisibility(View.VISIBLE);
                    lvAvailDevices.setVisibility(View.GONE);
                }
                btnScan.setText(R.string.bt_scan_avail_btn);
                pbAvail.setVisibility(View.GONE);
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                Log.d("BluetoothActivity", "bReceiver: ACTION_CONNECTION_STATE_CHANGED");
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                Log.d("BluetoothActivity", "bReceiver: ACTION_BOND_STATE_CHANGED");
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Device is paired, update list of paired devices
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED)
                    updatePairDevicesList();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Log.d("BluetoothActivity", "bReceiver: ACTION_ACL_DISCONNECT_REQUESTED");
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.d("BluetoothActivity", "bReceiver: ACTION_ACL_DISCONNECTED");
                bluetoothComponent.isConnected = false;
            }
        }
    };

//    // Get sent text from CommunicationFragment
//    // C1 - Main functionality to transmit message
//    private BroadcastReceiver mTextReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Get extra data included in the Intent
//            String theText = intent.getStringExtra("tts");
//            Log.d("Bluetooth mTReceiver: ", theText);
//            if (theText != null) {
//                if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
//                    showToast("Connection Lost. Please try again." + bluetoothService.getState());
//                    device = "";
////                    singletonDevice.setDeviceName(device);
//                    updateBluetoothTBStatus();
//                    sendToMain("");
//                    destroyReceivers();
//                    return;
//                }
//                // Send message out
//                bluetoothService.write(theText.getBytes());
//                // Reset string buffer
//                mOutStringBuffer.setLength(0);
//            }
//        }
//    };
//
//    // Get robot movements from MapFragment
//    private BroadcastReceiver mCtrlReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Get extra data included in the Intent
//            String control = intent.getStringExtra("control");
//            Log.d("Bluetooth mCReceiver: ", control);
//            if (control != null) {
//                if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
//                    showToast("Connection Lost.. Please try again." + bluetoothService.getState());
//                    device = "";
////                    singletonDevice.setDeviceName("");
//                    sendToMain("");
//                    destroyReceivers();
//                    updateBluetoothTBStatus();
//                    return;
//                }
//                // Send message out
//                byte[] send = control.getBytes();
//                bluetoothService.write(send);
//                mOutStringBuffer.setLength(0);
//            }
//        }
//    };

    // Display message
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(bluetoothAdapter != null){
            //Request Bluetooth to be enabled
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                main.startActivityForResult(enableIntent, 200);
            } else if (bluetoothService == null) {
                bluetoothService = new BluetoothService(mHandler);
                mOutStringBuffer = new StringBuffer();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //main.unregisterReceiver(bReceiver);
        binding = null;
    }
}