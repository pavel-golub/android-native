package com.diveboard.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.diveboard.mobile.ApplicationController;
import com.diveboard.util.Callback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

public class SpotService {
    private ConnectivityManager connectivityManager;
    private ApplicationController context;

    public SpotService(ApplicationController context) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.context = context;
    }

    public void searchSpot(String term, LatLng position, LatLngBounds bounds, Callback<List<Spot2>> callback, Callback<String> errorCallback) {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        SpotRepository repository;
        if (networkInfo != null && networkInfo.isConnected()) {
            repository = new SpotOnlineRepository(context);
        } else {
            repository = new SpotOfflineRepository(context);
        }
        repository.search(term, position, bounds, callback, errorCallback);
    }
}