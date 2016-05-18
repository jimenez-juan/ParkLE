package edu.stanford.parkle;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter<BluetoothInfo> {

    public DeviceListAdapter(Context context, int resource, ArrayList<BluetoothInfo> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        // If view has not yet been created, inflate it. Then
        // create a new ViewHolder to store the view information
        // for the given row so that findViewByID is not called
        // more times than necessary.
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.item_bluetooth_device, null);
            ViewHolder vh = new ViewHolder();
            vh.im = (ImageView) v.findViewById(R.id.bluetooth_image);
            vh.sigStrength = (TextView) v.findViewById(R.id.signal_strength);
            vh.mac = (TextView) v.findViewById(R.id.mac_address);
            v.setTag(vh);
        }

        ViewHolder vh = (ViewHolder) v.getTag();

        BluetoothInfo bi = getItem(position);

        if (vh.im != null) {
            vh.im.setImageResource(R.drawable.ic_bluetooth_black_24dp);
        }

        if (vh.sigStrength != null) {
            vh.sigStrength.setText(Integer.toString(bi.rssi));
        }

        if (vh.mac != null) {
            vh.mac.setText(bi.device.getAddress());
        }

        return v;
    }

    static class ViewHolder {
        public ImageView im;
        public TextView sigStrength;
        public TextView mac;
    }
}
