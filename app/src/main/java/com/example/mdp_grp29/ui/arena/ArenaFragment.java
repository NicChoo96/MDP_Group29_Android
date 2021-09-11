package com.example.mdp_grp29.ui.arena;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mdp_grp29.Command;
import com.example.mdp_grp29.Constants;
import com.example.mdp_grp29.R;
import com.example.mdp_grp29.Vector2D;
import com.example.mdp_grp29.arena_objects.ArenaPersistentData;
import com.example.mdp_grp29.arena_objects.Obstacles;
import com.example.mdp_grp29.arena_objects.RobotCar;
import com.example.mdp_grp29.bluetooth.BluetoothHelper;
import com.example.mdp_grp29.bluetooth.BluetoothService;
import com.example.mdp_grp29.databinding.FragmentArenaBinding;

public class ArenaFragment extends Fragment {

    private final String TAG = "ArenaFragment";

    private ArenaViewModel arenaViewModel;
    private FragmentArenaBinding binding;
    private ArenaView arenaView;
    private ImageView focusButton;
    private Button sendObsButton;
    private ImageButton upButton;
    private ImageButton downButton;
    private ImageButton leftButton;
    private ImageButton rightButton;
    private Button startButton;
    private Button resetBotButton;
    private TextView robotInfoTextView;
    private TextView obstacleInfoTextView;

    private boolean hasStarted = false;

    public static ArenaFragment instance;
    private Vibrator vibrator;
    private Activity main;

    private ArrayAdapter<String> statusHistoryArrayAdapter;
    private ListView statusHistoryLV;

    private BluetoothHelper bluetoothHelper;

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
        bluetoothHelper = BluetoothHelper.getInstance(getActivity(), mHandler);
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

        main = getActivity();

        arenaView = view.findViewById(R.id.arenaView);
        focusButton = view.findViewById(R.id.focus_button);
        sendObsButton = view.findViewById(R.id.send_obstacles_button);
        upButton = view.findViewById(R.id.up_button);
        downButton = view.findViewById(R.id.down_button);
        leftButton = view.findViewById(R.id.left_button);
        rightButton = view.findViewById(R.id.right_button);
        startButton = view.findViewById(R.id.start_button);
        resetBotButton = view.findViewById(R.id.reset_robot_button);
        robotInfoTextView = view.findViewById(R.id.robot_info_text_view);
        obstacleInfoTextView = view.findViewById(R.id.obstacle_text_view);
        statusHistoryLV = view.findViewById(R.id.status_history_listview);

        statusHistoryArrayAdapter = arenaPersistentData.loadHistory();
        if(statusHistoryArrayAdapter == null)
            statusHistoryArrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.statuses);
        statusHistoryLV.setAdapter(statusHistoryArrayAdapter);


        resetBotButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.resetRobot();
            }
        });

        upButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.moveRobot(RobotCar.MoveArrow.UP);
            }
        });

        downButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.moveRobot(RobotCar.MoveArrow.DOWN);
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.moveRobot(RobotCar.MoveArrow.LEFT);
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.moveRobot(RobotCar.MoveArrow.RIGHT);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(!hasStarted){
                    bluetoothHelper.sendBluetoothMessage(Command.START_EXPLORATION);
                }else{
                    bluetoothHelper.sendBluetoothMessage(Command.TERMINATE);
                }
            }
        });

        sendObsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                bluetoothHelper.sendBluetoothMessage(aggregateObstacleMessage());
            }
        });

        focusButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.resetArenaView();
            }
        });
    }

    public void updateRobotInfoTextView(Vector2D robotPos, int robotDirection){
        String robotDirectionInfo = "Err";
        switch(robotDirection){
            case RobotCar.NORTH:
                robotDirectionInfo = "N";
                break;
            case RobotCar.SOUTH:
                robotDirectionInfo = "S";
            break;
            case RobotCar.WEST:
                robotDirectionInfo = "W";
                break;
            case RobotCar.EAST:
                robotDirectionInfo = "E";
                break;
        }
        String robotInfo = String.format("Robot X:%d, Y:%d, Dir:%s", (int)robotPos.x, (int)robotPos.y, robotDirectionInfo);
        robotInfoTextView.setText(robotInfo);
    }

    public void updateObstacleInfoTextView(int obstacleSelected, Vector2D obstaclePos){
        String obstacleInfo = String.format("Obs[%d] X:%d, Y:%d", (obstacleSelected+1), (int)obstaclePos.x, (int)obstaclePos.y);
        obstacleInfoTextView.setText(obstacleInfo);
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

    public void sendRobotMovement(RobotCar.MoveArrow moveArrow){
        String sendMessage = "";
        switch(moveArrow){
            case UP:
                sendMessage = Command.FORWARD;
                break;
            case LEFT:
                sendMessage = Command.ROTATE_LEFT;
                break;
            case RIGHT:
                sendMessage = Command.ROTATE_RIGHT;
                break;
            case DOWN:
                sendMessage = Command.BACK;
                break;
        }
        bluetoothHelper.sendBluetoothMessage(sendMessage);
    }

    private void updateRemoteStatus(String readMessage){
        String command = Command.STATUS;
        String[] status = readMessage.split(":");
        if(readMessage.contains(command) && status.length == 2){
            for(int i = 0; i < Command.REMOTE_STATUS.length; i++){

                if(status[1].equals(Command.REMOTE_STATUS[i][0])){
                    statusHistoryArrayAdapter.insert(Command.REMOTE_STATUS[i][1], 0);
                    return;
                }

            }
        }
    }

    private void updateObstacleTargetImage(String readMessage){
        String command = Command.TARGET;
        float xPos, yPos;
        String dir;
        int imageID, obstacleIndex;
        String[] status = readMessage.split(":");
        if(readMessage.contains(command) && status.length == 3){
            // Set arena obstacles target image
            try{
                obstacleIndex = Integer.parseInt(status[1]);
                imageID = Integer.parseInt(status[2]);
                arenaView.updateObstacleTargetImage(obstacleIndex-1, imageID);
            }catch(Exception e){
                Log.d(TAG, "Invalid Target Image Command");
            }
        }
    }

    private void updateExplorationStatus(String readMessage){
        String command = Command.STATUS;
        String[] status = readMessage.split(":");
        if(readMessage.contains(command) && status.length == 2){
            for(int i = 0; i < Command.REMOTE_STATUS.length; i++){

                if(status[1].equals("RS")){
                    hasStarted = true;
                    startButton.setText("Stop");
                }
                else if(status[1].equals("C")){
                    hasStarted = false;
                    startButton.setText("Start");
                }
            }
        }
    }

    private void updateRemoteRobotStatus(String readMessage){
        String command = Command.ROBO;
        float xPos, yPos;
        String dir;
        String[] status = readMessage.split(":");
        if(readMessage.contains(command) && status.length == 4){
            xPos = Float.parseFloat(status[1]);
            yPos = Float.parseFloat(status[2]);
            dir = status[3];
            arenaView.setRobotPos(new Vector2D(xPos, yPos), dir);
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
                case Constants.MESSAGE_READ:
                    Log.d("Handler Log: ", "MESSAGE_READ");
                    byte[] readBuf = (byte[]) msg.obj;
                    // Construct string from valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    // This is where all the incoming robot messages that controls the android application
                    updateRemoteStatus(readMessage);
                    updateRemoteRobotStatus(readMessage);
                    updateObstacleTargetImage(readMessage);
                    updateExplorationStatus(readMessage);

                    Log.d("Handler Log: ", "MESSAGE_READ - " + readMessage);
                case Constants.MESSAGE_DEVICE_NAME:
                    Log.d("Handler Log: ", "MESSAGE_DEVICE_NAME");
                    // Save the connected device's name
                    bluetoothHelper.setDeviceName(msg.getData().getString(Constants.DEVICE_NAME));
                    Log.d("Handler Log: ", "MESSAGE_DEVICE_NAME - " + bluetoothHelper.getDeviceName());
                    if (getActivity().getApplicationContext() != null) {
                        if (bluetoothHelper.getDeviceName() != null) {
                            showToast("Connected to: " + bluetoothHelper.getDeviceName());
                            bluetoothHelper.setConnectionStatus(true);
                        }
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    Log.d("Handler Log: ", "MESSAGE_TOAST");
                    if (getActivity().getApplicationContext() != null) {
                        String theMsg = msg.getData().getString(Constants.TOAST);
                        if (theMsg.equalsIgnoreCase("device connection was lost")) {
                            showToast(theMsg);
                            bluetoothHelper.setConnectionStatus(false);
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
        arenaPersistentData.saveHistory(statusHistoryArrayAdapter);
    }
}