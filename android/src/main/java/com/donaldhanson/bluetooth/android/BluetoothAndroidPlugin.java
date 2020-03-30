package com.donaldhanson.bluetooth.android;

import com.getcapacitor.JSObject;
import com.getcapacitor.JSArray;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Set;

@NativePlugin(
        permissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
        }
)
public class BluetoothAndroidPlugin extends Plugin {

    private BluetoothAdapter _bluetoothAdapter;
    private HashMap<String, BluetoothConnection> _connections = new HashMap<>();

    @Override
    public void handleOnStart() {
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @PluginMethod()
    public void list(PluginCall call) {
        Set<BluetoothDevice> bondedDevices = _bluetoothAdapter.getBondedDevices();

        JSArray devices = new JSArray();

        for (BluetoothDevice device : bondedDevices) {
            devices.put(deviceToJSObject(device));
        }

        JSObject ret = new JSObject();
        ret.put("result", devices);
        call.success(ret);
    }

    @PluginMethod()
    public void connect(PluginCall call) {
        String address = call.getString("id");
        if (address == null) {
            call.reject("Property id is required");
            return;
        }

        BluetoothConnection connection = _connections.get(address);

        if (connection != null) {
            if (connection.getIsConnected()) {
                JSObject ret = new JSObject();
                ret.put("result", true);
                call.resolve(ret);
                return;
            }

            _connections.remove(address);
        }

        BluetoothDevice device = _bluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            call.reject("Device not found");
            return;
        }

        connection = new BluetoothConnection(_bluetoothAdapter, device);

        BluetoothConnection.ConnectCallback callback = new BluetoothConnection.ConnectCallback(call);

        connection.connect(callback);

        _connections.put(address, connection);
    }

    @PluginMethod()
    public void disconnect(PluginCall call) {
        String address = call.getString("id");
        if (address == null) {
            call.reject("Property id is required");
            return;
        }

        BluetoothConnection connection = _connections.get(address);

        if (connection != null) {
            connection.disconnect();
            _connections.remove(address);
        }

        JSObject ret = new JSObject();
        call.resolve(ret);
    }

    @PluginMethod()
    public void isConnected(PluginCall call) {
        String address = call.getString("id");
        if (address == null) {
            call.reject("Property id is required");
            return;
        }

        BluetoothConnection connection = _connections.get(address);

        boolean isConnected = false;
        if (connection != null) {
            isConnected = connection.getIsConnected();
        }

        JSObject ret = new JSObject();
        ret.put("result", isConnected);
        call.resolve(ret);
    }

    @PluginMethod()
    public void write(PluginCall call) {
        String address = call.getString("id");
        if (address == null) {
            call.reject("Property id is required");
            return;
        }

        String data = call.getString("data");
        if (data == null) {
            call.reject("Property data is required");
            return;
        }

        BluetoothConnection connection = _connections.get(address);

        if (connection == null || !connection.getIsConnected()) {
            call.reject("Device is not connected");
            return;
        }

        connection.write(data);

        JSObject ret = new JSObject();
        call.resolve(ret);
    }

    private JSObject deviceToJSObject(BluetoothDevice device) {
        JSObject object = new JSObject();
        object.put("name", device.getName());
        object.put("address", device.getAddress());
        object.put("id", device.getAddress());
        if (device.getBluetoothClass() != null) {
            object.put("class", device.getBluetoothClass().getDeviceClass());
        }
        return object;
    }
}
