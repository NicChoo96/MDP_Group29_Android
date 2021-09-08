package com.example.mdp_grp29.ui.arena;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mdp_grp29.Command;
import com.example.mdp_grp29.Constants;
import com.example.mdp_grp29.R;
import com.example.mdp_grp29.arena_objects.ArenaGrid;
import com.example.mdp_grp29.arena_objects.ArenaPersistentData;
import com.example.mdp_grp29.arena_objects.Obstacles;
import com.example.mdp_grp29.bluetooth.BluetoothComponent;
import com.example.mdp_grp29.bluetooth.BluetoothService;
import com.example.mdp_grp29.databinding.FragmentArenaBinding;

public class ArenaFragment extends Fragment {

    private ArenaViewModel arenaViewModel;
    private FragmentArenaBinding binding;
    private ArenaView arenaView;
    private ImageView focusButton;
    private Button sendObsButton;
    private ImageButton upButton;
    private ImageButton downButton;
    private ImageButton leftButton;
    private ImageButton rightButton;

    public static ArenaFragment instance;
    private Vibrator vibrator;

    private ArrayAdapter<String> statusHistoryArrayAdapter;
    private ListView statusHistoryLV;

    private BluetoothComponent bluetoothComponent;

    public ArenaPersistentData arenaPersistentData = ArenaPersistentData.getInstance();

    public static ArenaFragment getInstance() {
        return instance;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        instance = this;

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        arenaViewModel =
                new ViewModelProvider(this).get(ArenaViewModel.class);

        binding = FragmentArenaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        //getActivity().setContentView(R.layout.fragment_home);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        bluetoothComponent = BluetoothComponent.getInstance(getActivity(), mHandler);
    }

    public void vibrateDevice(int time){
        vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        arenaView = view.findViewById(R.id.arenaView);
        focusButton = view.findViewById(R.id.focus_button);
        sendObsButton = view.findViewById(R.id.send_obstacles_button);
        upButton = view.findViewById(R.id.up_button);
        downButton = view.findViewById(R.id.down_button);
        leftButton = view.findViewById(R.id.left_button);
        rightButton = view.findViewById(R.id.right_button);

        upButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.MoveRobot(ArenaView.MoveArrow.UP);
            }
        });

        downButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.MoveRobot(ArenaView.MoveArrow.DOWN);
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.MoveRobot(ArenaView.MoveArrow.LEFT);
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.MoveRobot(ArenaView.MoveArrow.RIGHT);
            }
        });

        sendObsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                bluetoothComponent.sendBluetoothMessage(aggregateObstacleMessage());
            }
        });

        focusButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.ResetArenaView();
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private String aggregateObstacleMessage(){
        String obstacleCommand = Command.OBSTACLE;
        Obstacles currentObstacles = arenaPersistentData.getObstaclesData();

        for(int i = 0; i < currentObstacles.getObstacleCount(); i++){
            obstacleCommand += String.format(":%d:%d:%s",
                    (int)currentObstacles.getObstaclePos(i).x,
                    (int)currentObstacles.getObstaclePos(i).y,
                    currentObstacles.getObstacleDir(i).toString());
        }
        return obstacleCommand;
    }

    public void sendRobotMovement(ArenaView.MoveArrow moveArrow){
        switch(moveArrow){
            case UP:
                bluetoothComponent.sendBluetoothMessage(Command.FORWARD);
                break;
            case LEFT:
                bluetoothComponent.sendBluetoothMessage(Command.LEFT);
                break;
            case RIGHT:
                bluetoothComponent.sendBluetoothMessage(Command.RIGHT);
                break;
            case DOWN:
                bluetoothComponent.sendBluetoothMessage(Command.BACK);
                break;
        }
    }

    public final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        // Bluetooth Service: Connected to device
                        case BluetoothService.STATE_CONNECTED:
                            Log.d("Handler Log: ", "STATE_CONNECTED");
                            bluetoothComponent.setConnectionStatus(true);
                            break;
                        // Bluetooth Service: Connecting to device
                        case BluetoothService.STATE_CONNECTING:
                            Log.d("Handler Log: ", "STATE_CONNECTING");
                            showToast("Connecting...");
                            bluetoothComponent.setConnectionStatus(false);
                            break;
                        // Bluetooth Service: Listening for devices
                        case BluetoothService.STATE_LISTEN:
                            Log.d("Handler Log: ", "STATE_LISTEN");
                            bluetoothComponent.setConnectionStatus(false);
                        case BluetoothService.STATE_NONE:
                            bluetoothComponent.setConnectionStatus(false);
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
                    bluetoothComponent.setDeviceName(msg.getData().getString(Constants.DEVICE_NAME));
                    Log.d("Handler Log: ", "MESSAGE_DEVICE_NAME - " + bluetoothComponent.getDeviceName());
                    if (getActivity().getApplicationContext() != null) {
                        if (bluetoothComponent.getDeviceName() != null) {
                            showToast("Connected to: " + bluetoothComponent.getDeviceName());
                            bluetoothComponent.setConnectionStatus(true);
                        }
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    Log.d("Handler Log: ", "MESSAGE_TOAST");
                    if (getActivity().getApplicationContext() != null) {
                        String theMsg = msg.getData().getString(Constants.TOAST);
                        if (theMsg.equalsIgnoreCase("device connection was lost")) {
                            showToast(theMsg);
                            // Send string to MainActivity that devices lost connection
                            bluetoothComponent.setConnectionStatus(false);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}