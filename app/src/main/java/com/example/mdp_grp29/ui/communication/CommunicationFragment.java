package com.example.mdp_grp29.ui.communication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.example.mdp_grp29.databinding.FragmentCommunicationBinding;

import java.util.ArrayList;

public class CommunicationFragment extends Fragment {

    private CommunicationViewModel communicationViewModel;
    private FragmentCommunicationBinding binding;
    private ArrayAdapter<String> incomingMessageArrayAdapter, outgoingMessageArrayAdapter;
    private ListView incomingMessageLV, outgoingMessageLV;
    private Button send_text_button;
    private EditText outgoing_text_edit;

    private BluetoothHelper bluetoothHelper;

    private final String TAG = "CommunicationFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        communicationViewModel =
                new ViewModelProvider(this).get(CommunicationViewModel.class);

        binding = FragmentCommunicationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        communicationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        // Setup Listview for incoming and outgoing messages
        incomingMessageLV = view.findViewById(R.id.incomingMessageLV);
        incomingMessageArrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.messages);
        incomingMessageLV.setAdapter(incomingMessageArrayAdapter);

        outgoingMessageLV = view.findViewById(R.id.outgoingMessageLV);
        outgoingMessageArrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.messages);
        outgoingMessageLV.setAdapter(outgoingMessageArrayAdapter);

        send_text_button = view.findViewById(R.id.send_text_button);
        outgoing_text_edit = view.findViewById(R.id.outgoing_text_edit);


        // Get sent text from CommunicationFragment
        // C1 - Main functionality to transmit message
        send_text_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String outgoingText = outgoing_text_edit.getText().toString();
                if(!outgoingText.equals("")){
                    if (bluetoothHelper.getBluetoothService().getState() != BluetoothService.STATE_CONNECTED) {
                        showToast("Connection Lost. Please try again." + bluetoothHelper.getBluetoothService().getState());
                        // TODO: Please change the connection lost functionality below to reconnect instead of just total lost
                        bluetoothHelper.setDeviceName("");
                        bluetoothHelper.updateBluetoothTBStatus();
                        return;
                    }
                    // Send message out
                    bluetoothHelper.sendBluetoothMessage(outgoingText);
                    outgoingMessageArrayAdapter.add(outgoingText);
                    outgoingMessageLV.setSelection(outgoingMessageArrayAdapter.getCount()-1);
                    // Reset string buffer
                    BluetoothHelper.mOutStringBuffer.setLength(0);
                }
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        bluetoothHelper = BluetoothHelper.getInstance(getActivity(), mHandler);

        // Persist the view communication data after it got switched back to the communication page
        populateExistingArrayAdapter(incomingMessageLV, incomingMessageArrayAdapter, bluetoothHelper.getPersistentIncomingComms());
        populateExistingArrayAdapter(outgoingMessageLV, outgoingMessageArrayAdapter, bluetoothHelper.getPersistentOutgoingComms());
    }

    private void populateExistingArrayAdapter(ListView listView, ArrayAdapter<String> arrayAdapter, ArrayAdapter<String> persistentArray){
        if(persistentArray == null)
            return;
        arrayAdapter = persistentArray;
        listView.setSelection(arrayAdapter.getCount() - 1);
    }

    private void showToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
                            bluetoothHelper.setConnectionStatus(true);
                            break;
                        // Bluetooth Service: Connecting to device
                        case BluetoothService.STATE_CONNECTING:
                            Log.d("Handler Log: ", "STATE_CONNECTING");
                            showToast("Connecting...");
                            bluetoothHelper.setConnectionStatus(false);
                            break;
                        // Bluetooth Service: Listening for devices
                        case BluetoothService.STATE_LISTEN:
                            Log.d("Handler Log: ", "STATE_LISTEN");
                            bluetoothHelper.setConnectionStatus(false);
                        case BluetoothService.STATE_NONE:
                            Log.d("Handler Log: ", "STATE_NONE");
                            bluetoothHelper.setConnectionStatus(false);
                            break;
                         // C8 - Reconnection of Bluetooth device
                        case BluetoothService.STATE_DISCONNECTED:
                            Log.d(TAG, "STATE_DISCONNECTED");
                            showToast("Connection lost, attempting for reconnection...");
                            bluetoothHelper.setConnectionStatus(false);
                            break;
                    }
                    break;
                    // TODO: To be relocated to BluetoothComponent soon
                case Constants.MESSAGE_READ:
                    Log.d("Handler Log: ", "MESSAGE_READ");
                    byte[] readBuf = (byte[]) msg.obj;
                    // Construct string from valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    // Add incoming message to incoming list view
                    Log.d(TAG, readMessage);
                    incomingMessageArrayAdapter.add(readMessage);
                    incomingMessageLV.setSelection(incomingMessageArrayAdapter.getCount()-1);
                    Log.d("Handler Log: ", "MESSAGE_READ - " + readMessage);
                    break;
                case Constants.MESSAGE_TOAST:
                    Log.d("Handler Log: ", "MESSAGE_TOAST");
                    if (getActivity().getApplicationContext() != null) {
                        String theMsg = msg.getData().getString(Constants.TOAST);
                        if (theMsg.equalsIgnoreCase("device connection was lost")) {
                            showToast(theMsg);
                            // Send string to MainActivity that devices lost connection
                            bluetoothHelper.updateBluetoothTBStatus();
                            // Send message to MainActivity that no device currently connected
                            bluetoothHelper.setDeviceName("");
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onDestroyView() {
        bluetoothHelper.storePersistentCommsData(incomingMessageArrayAdapter, outgoingMessageArrayAdapter);
        super.onDestroyView();
        Log.e(TAG, "Communication Destroyed");
        binding = null;
    }
}