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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.mdp_grp29.Constants;
import com.example.mdp_grp29.R;
import com.example.mdp_grp29.bluetooth.BluetoothHelper;
import com.example.mdp_grp29.bluetooth.BluetoothService;
import com.example.mdp_grp29.databinding.FragmentBluetoothBinding;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

@SuppressWarnings("deprecation")
public class BluetoothFragment extends Fragment {

    private BluetoothViewModel bluetoothViewModel;
    private FragmentBluetoothBinding binding;


    // Tag string
    private static final String TAG = "BluetoothFragment";

    // Interface for Bluetooth OS Service
    private BluetoothAdapter bluetoothAdapter = null;
    public static StringBuffer mOutStringBuffer;
    private boolean registered = false;

    private BluetoothHelper bluetoothHelper;

    private boolean isDiscovering = false;

    // Relate to GUI
    private ListView lvPairedDevices, lvAvailDevices;
    private TextView tvNoPairDevices, tvNoAvailDevices;
    private Button btnDiscover, btnScan, btnRefresh, btnDisconnect, btnUnpair;
    private ProgressBar pbPair, pbAvail;
    private ProgressDialog progress;

    // Variable - List
    private ArrayAdapter<String> newDevicesArrayAdapter, pairedDevicesArrayAdapter;
    private CountDownTimer scanTimer;

    // Variables
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

        // Register broadcast receivers
        registerBroadcastReceivers();

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
        btnUnpair = view.findViewById(R.id.unpair_all_button);

        // List View of Devices
        pairedDevicesArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.device_name);
        lvPairedDevices.setAdapter(pairedDevicesArrayAdapter);
        lvPairedDevices.setOnItemClickListener(listViewClickListener);

        newDevicesArrayAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1);
        lvAvailDevices.setAdapter(newDevicesArrayAdapter);
        lvAvailDevices.setOnItemClickListener(listViewClickListener);

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
                updatePairDevicesList();
            }
        }

        btnUnpair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unpairAllDevices();
                showToast("All Devices Unpaired!");
            }
        });

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
                bluetoothHelper.disconnectBluetoothDevice();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

        bluetoothHelper = BluetoothHelper.getInstance(main, mHandler);

        bluetoothHelper.broadcastReceiver = bReceiver;

        mOutStringBuffer = new StringBuffer();

        if(bluetoothHelper.getBluetoothService().getState() == BluetoothService.STATE_CONNECTED)
            updateBluetoothUI(true);
        else
            updateBluetoothUI(false);

        updateAvailableDeviceList();
    }

    private void updateBluetoothUI(boolean isConnected){
        bluetoothHelper.setConnectionStatus(isConnected);
        if(isConnected)
            btnDisconnect.setVisibility(View.VISIBLE);
        else
            btnDisconnect.setVisibility(View.GONE);
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
        if (!isDiscovering) {
            // Update list of available devices
            newDevicesArrayAdapter.clear();
            // Change btn text
            btnScan.setText(R.string.button_stop);
            // Show button progress bar
            pbAvail.setVisibility(View.VISIBLE);
            // Notify user the action taken
            showToast("Scanning for devices...");

            if(bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();

            // Check if user has enabled BT permission
            checkBTPermissions();

            // Start finding for available Bluetooth devices
            bluetoothAdapter.startDiscovery();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            main.registerReceiver(bReceiver, filter);
            // Timer to stop if too long
            scanTimer = new CountDownTimer(10000, 10000) {
                @Override
                public void onTick(long l) {
                }

                public void onFinish() {
                    updateAvailableDeviceList();
                    showToast("Scanning Available Devices has timed out...");
                }
            }.start();

            isDiscovering = true;

        } else {
            isDiscovering = false;
            // Cancel if device is already discovering
            if(bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            btnScan.setText(R.string.bt_scan_avail_btn);
            showToast("Scanning stopped.");
            // Hide progress bar
            pbAvail.setVisibility(View.GONE);
        }
    }

    // To clear all the list of paired devices connected to device before.
    public void unpairAllDevices(){
        if(bluetoothAdapter != null){
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                try {
                    Method m = device.getClass().getMethod("removeBond", (Class[]) null);
                    m.invoke(device, (Object[]) null);

                    pairedDevicesArrayAdapter.clear();
                    updatePairDevicesList();
                    return;
                } catch (Exception e) {
                    Log.w(TAG, "Failed to un-pair device.");
                }
            }
            showToast("No device to unpair!");
        }
    }

    // C2 - Select List of Devices - Pair
    // Find nearby paired bluetooth devices
    public void updatePairDevicesList() {
        pairedDevicesArrayAdapter.clear();
        pbPair.setVisibility(View.VISIBLE);
        lvPairedDevices.setVisibility(View.GONE);
        tvNoPairDevices.setVisibility(View.GONE);

        // .getBondedDevices() to retrieve list of paired devices attached to device
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<String> newDeviceList = new ArrayList<>();
        final boolean hasList = pairedDevices.size() > 0;

        if (hasList) { // Found at least one paired devices
            for (BluetoothDevice bluetoothDevice : pairedDevices){
                // Add found paired devices to list
                newDeviceList.add(bluetoothDevice.getName() + "\n MAC Address: " + bluetoothDevice.getAddress());
                Log.d(TAG, bluetoothDevice.getName());
            }
            pairedDevicesArrayAdapter.addAll(newDeviceList);
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

    // Method: Register broadcast receivers
    private void registerBroadcastReceivers() {
        if(bReceiver == null)
            return;
        main.registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        main.registerReceiver(bReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        main.registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        main.registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        main.registerReceiver(bReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        main.registerReceiver(bReceiver, new IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
    }

    private AdapterView.OnItemClickListener listViewClickListener =
            new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
                    // Get device's MAC address - the last 17 chars in the View
                    String info = ((TextView) v).getText().toString();
                    String address = info.substring(info.length() - 17);
                    String device = info.substring(0, info.length() - 30);
                    //singletonDevice.setDeviceName(device);
                    if(!bluetoothHelper.getConnectionStatus())
                        connectBluetoothDevice(address);
                }
            };

    // C2 - Connect bluetooth devices
    public void connectBluetoothDevice(String address) {
        // Connect to device that was clicked
        bluetoothHelper.connectBluetoothDevice(address);
        //bluetoothHelper.previousConnectedAddress = address;

        if (!bluetoothHelper.getConnectionStatus()){
            // Notify user that app is trying to connect to device
            progress = ProgressDialog.show(main,
                    "Connecting...", "Please wait.");
        }
        else{
            // Notify user that app connect again after disconnected somehow
            progress = ProgressDialog.show(main,
                    "Disconnecting...", "Please wait.");
            bluetoothHelper.setConnectionStatus(false);
            btnDisconnect.setVisibility(View.GONE);
        }

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
                            Log.d(TAG, "STATE_CONNECTED");
                            updateBluetoothUI(true);
                            break;
                        // Bluetooth Service: Connecting to device
                        case BluetoothService.STATE_CONNECTING:
                            Log.d(TAG, "STATE_CONNECTING");
                            showToast("Connecting...");
                            updateBluetoothUI(false);
                            break;
                        // Bluetooth Service: Listening for devices
                        case BluetoothService.STATE_LISTEN:
                            Log.d(TAG, "STATE_LISTEN");
                            updateBluetoothUI(false);
                        case BluetoothService.STATE_NONE:
                            updateBluetoothUI(false);
                            break;
                        // C8 - Reconnection of Bluetooth device
                        case BluetoothService.STATE_DISCONNECTED:
                            Log.d(TAG, "STATE_DISCONNECTED");
                            showToast("Connection lost, attempting for reconnection...");
                            updateBluetoothUI(false);
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    Log.d(TAG, "MESSAGE_READ");
                    byte[] readBuf = (byte[]) msg.obj;
                    // Construct string from valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    // Send message to MainActivity that was received in buffer
                    //sendTextToMain(readMessage);
                    Log.d(TAG, "MESSAGE_READ - " + readMessage);
                case Constants.MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "MESSAGE_DEVICE_NAME");
                    // Save the connected device's name
                    String device = msg.getData().getString(Constants.DEVICE_NAME);
                    Log.d(TAG, "MESSAGE_DEVICE_NAME - " + device);
                    if (main.getApplicationContext() != null) {
                        if (device != null) {
                            bluetoothHelper.setDeviceName(device);
                            showToast("Connected to: " + device);
                            updateBluetoothUI(true);
                            if(progress != null)
                                progress.dismiss();
                        }
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    Log.d(TAG, "MESSAGE_TOAST");
                    if (main.getApplicationContext() != null) {
                        String theMsg = msg.getData().getString(Constants.TOAST);
                        Log.d(TAG, theMsg);
                        if (theMsg.equalsIgnoreCase("device connection was lost")) {
                            showToast(theMsg);
                        }else if(theMsg.equalsIgnoreCase("Device disconnected"))
                            showToast(theMsg);
                    }
                    break;
            }
        }
    };

    // Broadcast receiver for bluetooth
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                case BluetoothDevice.ACTION_FOUND:
                    // Event: When a remote device is found during discovery
                    Log.d(TAG, "bReceiver: ACTION_FOUND");
                    tvNoAvailDevices.setVisibility(View.GONE);
                    lvAvailDevices.setVisibility(View.VISIBLE);
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    String newDevice = device.getName() + "\n MAC Address: " + device.getAddress();
                    if(device.getName() != null)
                        newDevicesArrayAdapter.add(newDevice);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    // Event: When bluetooth has completed scanning
                    Log.d(TAG, "bReceiver: ACTION_DISCOVERY_FINISHED");
                    pbAvail.setVisibility(View.GONE);
                    if (newDevicesArrayAdapter.getCount() == 0) {
                        tvNoAvailDevices.setVisibility(View.VISIBLE);
                        lvAvailDevices.setVisibility(View.GONE);
                    }
                    btnScan.setText(R.string.bt_scan_avail_btn);
                    pbAvail.setVisibility(View.GONE);
                    break;
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    Log.d(TAG, "bReceiver: ACTION_CONNECTION_STATE_CHANGED");
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    Log.d(TAG, "bReceiver: ACTION_BOND_STATE_CHANGED");
                    BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Device is paired, update list of paired devices
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED)
                        updatePairDevicesList();
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.d(TAG, "bReceiver: ACTION_ACL_CONNECTED");

                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    Log.d(TAG, "bReceiver: ACTION_ACL_DISCONNECT_REQUESTED");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "bReceiver: ACTION_ACL_DISCONNECTED");
                    if(progress != null)
                        if(progress.isShowing()){
                            progress.dismiss();
                    }else{
                        if(!bluetoothHelper.getServiceManualDisconnect()){
                            showToast("Connection has been lost, attempting to reconnect...");
                            //bluetoothHelper.connectBluetoothDevice(bluetoothHelper.previousConnectedAddress);
                        }
                    }
                    updateBluetoothUI(false);
                    break;
            }
        }
    };

    // Display message
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        scanTimer.cancel();
        main.unregisterReceiver(bReceiver);
    }
}