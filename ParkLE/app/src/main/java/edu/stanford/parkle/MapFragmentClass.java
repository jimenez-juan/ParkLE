package edu.stanford.parkle;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by juanj on 5/17/16.
 */
public class MapFragmentClass extends Fragment implements OnMapReadyCallback{


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_fragment_layout, container, false);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng marker1 = new LatLng(37.427729, -122.181958);
        LatLng marker2 = new LatLng(37.423194, -122.171299);
        LatLng marker3 = new LatLng(37.423491, -122.173796);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker3, 14));
        googleMap.addMarker(new MarkerOptions().title("Preference 1").position(marker1).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        googleMap.addMarker(new MarkerOptions().title("Preference 2").position(marker2).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        googleMap.addMarker(new MarkerOptions().title("Preference 3").position(marker3).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapFragment MF = (MapFragment) getFragmentManager().findFragmentById(R.id.mapid);
        MF.getMapAsync(this);
    }

}
