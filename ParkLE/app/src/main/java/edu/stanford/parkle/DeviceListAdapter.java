package edu.stanford.parkle;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

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

        int rID;
        if (vh.im != null) {
            int strength = bi.rssi;
            if (strength < -90) {
                rID = R.drawable.wifi_empty;
            } else if (strength < -80) {
                rID = R.drawable.wifi_1bar;
            } else if (strength < -70) {
                rID = R.drawable.wifi_2bars;
            } else if (strength < -67) {
                rID = R.drawable.wifi_3bars;
            } else {
                rID = R.drawable.wifi_full;
            }
            Picasso.with(v.getContext()).load(rID)
                    .fit().centerCrop()
                    .into(vh.im);
        }

        if (vh.sigStrength != null) {
            vh.sigStrength.setText(Integer.toString(bi.rssi));
        }

        if (vh.mac != null) {
            vh.mac.setText(bi.device.getAddress());
            //vh.mac.setText(bi.device.getName());
        }

        return v;
    }

    static class ViewHolder {
        public ImageView im;
        public TextView sigStrength;
        public TextView mac;
    }
}
