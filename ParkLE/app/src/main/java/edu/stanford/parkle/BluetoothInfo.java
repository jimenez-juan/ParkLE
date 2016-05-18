package edu.stanford.parkle;


import android.bluetooth.BluetoothDevice;

public class BluetoothInfo {
    BluetoothDevice device;
    int rssi;

    public BluetoothInfo(BluetoothDevice device, int rssi) {
        this.device = device;
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof BluetoothInfo)) return false;
        BluetoothInfo otherInfo = (BluetoothInfo) other;
        return this.device.getAddress().equals(otherInfo.device.getAddress());
    }
}
