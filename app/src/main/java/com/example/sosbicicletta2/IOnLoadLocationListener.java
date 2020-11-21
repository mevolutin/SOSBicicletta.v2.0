package com.example.sosbicicletta2;



import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface IOnLoadLocationListener {
    void onLoadLocationSuccess(List<LatLngDriver> latLngs);
    void onLoadLoactionFailed(String message);
}
