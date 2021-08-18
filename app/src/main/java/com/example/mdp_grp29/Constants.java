package com.example.mdp_grp29;

public interface Constants {
    // Message Type that BluetoothService handler sent
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key Names that BluetoothService handler receive
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";
}
