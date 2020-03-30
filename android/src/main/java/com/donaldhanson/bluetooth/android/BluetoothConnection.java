package com.donaldhanson.bluetooth.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.getcapacitor.Bridge;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class BluetoothConnection
{
    private static final String TAG = "BluetoothSerialService";
    private static final boolean D = true;
    private static final String delimiter = "\n";

    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Constants that indicate the current connection state
    private static final int STATE_NONE = 0;       // we're doing nothing
    private static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    private static final int STATE_CONNECTED = 2;  // now connected to a remote device
    private static final int STATE_FAILED = 3;     // connection could not be established
    private static final int STATE_LOST = 4;       // connection was lost
    private static final int STATE_DISCONNECTED = 5; // connection was intentionally disconnected

    private BluetoothDevice _device;
    private BluetoothAdapter _adapter;
    private ConnectThread _connectThread;
    private TransmissionThread _transmissionThread;
    private int mState;
    private HashMap<String, PluginCall> subscribedCalls = new HashMap<>();
    StringBuffer buffer = new StringBuffer();

    BluetoothConnection(BluetoothAdapter adapter, BluetoothDevice device) {
        _adapter = adapter;
        _device = device;
    }

    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    synchronized boolean getIsConnected() {
        return mState == STATE_CONNECTED;
    }

    synchronized void connect(ConnectCallback callback) {
        buffer.setLength(0);
        cancelThreads();

        _connectThread = new ConnectThread(_adapter, _device, false, callback);
        _connectThread.start();
        setState(STATE_CONNECTING);
    }

    synchronized void disconnect() {
        cancelThreads();
        setState(STATE_DISCONNECTED);
    }

    synchronized void subscribe(PluginCall call) {
        subscribedCalls.put(call.getCallbackId(), call);
    }

    synchronized void unsubscribe(String callbackId, Bridge bridge) {
        if (callbackId != null) {
            if (subscribedCalls.containsKey(callbackId)) {
                PluginCall call = subscribedCalls.remove(callbackId);
                if (call != null) {
                    call.release(bridge);
                }
            }
        }
    }

    synchronized void unsubscribeAll(Bridge bridge) {
        for(Map.Entry<String, PluginCall> entry: subscribedCalls.entrySet()) {
            PluginCall call = entry.getValue();
            if (call != null) {
                call.release(bridge);
            }
        }
        subscribedCalls.clear();
    }

    void write(String data) {
        TransmissionThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = _transmissionThread;
        }
        r.write(data.getBytes());
    }

    private synchronized void cancelThreads() {
        if (_connectThread != null) {
            _connectThread.cancel();
            _connectThread = null;
        }

        if (_transmissionThread != null) {
            _transmissionThread.cancel();
            _transmissionThread = null;
        }
    }

    private synchronized void connectionSuccessful(BluetoothSocket socket, String socketType) {
        cancelThreads();

        _transmissionThread = new TransmissionThread(socket, socketType);
        _transmissionThread.start();
        setState(STATE_CONNECTED);
    }

    private synchronized void connectionFailed() {
        cancelThreads();

        setState(STATE_FAILED);
    }

    private synchronized void connectionLost() {
        cancelThreads();

        setState(STATE_LOST);
    }

    private synchronized void messageRead(String data) {
        buffer.append(data);

        sendDataToSubscribers();
    }

    private void sendDataToSubscribers() {
        String data = readUntil(delimiter);
        if (data != null && data.length() > 0) {
            sendDataToSubscribers(data);
            sendDataToSubscribers();
        }
    }

    private String readUntil(String c) {
        String data = "";
        int index = buffer.indexOf(c, 0);
        if (index > -1) {
            data = buffer.substring(0, index + c.length());
            buffer.delete(0, index + c.length());
        }
        return data;
    }


    private void sendDataToSubscribers(String data) {
        for(Map.Entry<String, PluginCall> entry: subscribedCalls.entrySet()) {
            PluginCall call = entry.getValue();
            if (call != null) {
                JSObject object = new JSObject();
                object.put("result", data);
                call.success(object);
            }
        }
    }

    private synchronized void messageReadRaw(byte[] data) {

    }

    static class ConnectCallback {
        private final PluginCall _call;

        ConnectCallback(PluginCall call) {
            _call = call;
        }

        void onSuccess() {
            _call.resolve();
        }

        void onFail(String reason) {
            _call.reject(reason);
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private /*final*/ BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final BluetoothAdapter mmAdapter;
        private String mSocketType;
        private ConnectCallback mmCallbacks;

        ConnectThread(BluetoothAdapter adapter, BluetoothDevice device, boolean secure, ConnectCallback callbacks) {
            mmAdapter = adapter;
            mmDevice = device;
            mmCallbacks = callbacks;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                if (secure) {
                    mmSocket = device.createRfcommSocketToServiceRecord(UUID_SPP);
                } else {
                    mmSocket = device.createInsecureRfcommSocketToServiceRecord(UUID_SPP);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mmAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                Log.i(TAG,"Connecting to socket...");
                mmSocket.connect();
                Log.i(TAG,"Connected");
            } catch (IOException e) {
                Log.e(TAG, e.toString());

                // Some 4.1 devices have problems, try an alternative way to connect
                // See https://github.com/don/BluetoothSerial/issues/89
                try {
                    Log.i(TAG,"Trying fallback...");
                    mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    mmSocket.connect();
                    Log.i(TAG,"Connected");
                } catch (Exception e2) {
                    Log.e(TAG, "Couldn't establish a Bluetooth connection.");
                    try {
                        mmSocket.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "unable to close() " + mSocketType + " socket during connection failure", e3);
                    }
                    connectionFailed();
                    if (mmCallbacks != null) {
                        mmCallbacks.onFail(e2.getMessage());
                        mmCallbacks = null;
                    }
                    return;
                }
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnection.this) {
                _connectThread = null;
            }

            // Start the connected thread
            connectionSuccessful(mmSocket, mSocketType);
            if (mmCallbacks != null) {
                mmCallbacks.onSuccess();
                mmCallbacks = null;
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class TransmissionThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        TransmissionThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    String data = new String(buffer, 0, bytes);

                    // Send the new data String to the UI Activity
                    messageRead(data);

                    // Send the raw bytestream to the UI Activity.
                    // We make a copy because the full array can have extra data at the end
                    // when / if we read less than its size.
                    if (bytes > 0) {
                        byte[] rawdata = Arrays.copyOf(buffer, bytes);
                        messageReadRaw(rawdata);
                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
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
