package com.example.mdp_grp29.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.os.Handler;

import com.example.mdp_grp29.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {
    private final String TAG = "BluetoothService";

    private final String NAME_SECURE = "BluetoothSecure";
    private final String NAME_INSECURE = "BluetoothInsecure";

    private final UUID UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final UUID UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread, mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;


    public static enum ConnectionState {
        IDLE(0), // Connection: Idle
        LISTEN(1), // Connection: Listening for incoming connections
        CONNECTING(2), // Connection: Initiating an outgoing connection
        CONNECTED(3), // Connection: Connected to a remote device
        DISCONNECTED(4); // Connection: Disconnected from remote device

        public int value;

        ConnectionState(int i){
            value = i;
        }
    }

    private ConnectionState mCurrentState;
    private ConnectionState mNewState;

    /**
     * Constructor: Prepares a new Bluetooth session
     * @param handler: Send messages back to the UI Activity
     */
    public BluetoothService(Handler handler){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        mNewState = mCurrentState = ConnectionState.IDLE;
    }

    private synchronized void updateBluetoothStatus(){
        Log.d(TAG, "Bluetooth Status updating from:  " + mNewState.toString() + " -> " + mCurrentState.toString());
        mNewState = mCurrentState;

        // Give Handler the new state to update UI Activity
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState.value, -1).sendToTarget();
    }

    public synchronized ConnectionState getState(){ return mCurrentState; }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mCurrentState == ConnectionState.CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        //Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        //Update UI title
        updateBluetoothStatus();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send connected device name back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        updateBluetoothStatus(); // Update UI title
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mCurrentState != ConnectionState.CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param isFail
     * True: Indicate that the connection attempt failed and notify the UI Activity.
     * False: Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionDown(boolean isFail) {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        if(isFail)
            bundle.putString(Constants.TOAST, "Unable to connect device.");
        else
            bundle.putString(Constants.TOAST, "Device connection was lost.");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mCurrentState = ConnectionState.IDLE;
        // Update UI title
        updateBluetoothStatus();

        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    /**
     * Start the BluetoothChat service.
     * Specifically start AcceptThread to begin a session in listening (server) mode.
     * Called by onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "Bluetooth Service Starting...");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
        // Update UI title
        updateBluetoothStatus();
    }

    // Stop all threads
    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        mCurrentState = ConnectionState.IDLE;
        updateBluetoothStatus(); // Update UI title
    }

    /** Thread: Attempt to make outgoing connection with device.
     *  It runs straight through; the connection either succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = secure ? device.createRfcommSocketToServiceRecord(UUID_SECURE):
                        device.createRfcommSocketToServiceRecord(UUID_INSECURE);
            } catch (IOException e) {
                //Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            mCurrentState = ConnectionState.CONNECTING;
        }

        public void run() {
            //Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Cancel discovery to prevent slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionDown(true);
                return;
            }

            // Reset the ConnectThread when completed
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start Connected Thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(boolean secure){
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try{
                // UUID is the app's UUID string, also used by the client code.
                tmp = secure ? mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_SECURE):
                              mAdapter.listenUsingRfcommWithServiceRecord(NAME_INSECURE, UUID_INSECURE);
            }catch(IOException e){
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run(){
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    //manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    /**
     * Thread: Runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                //Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mCurrentState = ConnectionState.CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mCurrentState == ConnectionState.CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1,
                            buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionDown(false);
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}