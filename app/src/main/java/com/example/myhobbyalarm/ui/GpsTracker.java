package com.example.myhobbyalarm.ui;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

//현재 위취를 찾아 주소로 변환 처리하는 클래스이다.
public class GpsTracker extends Service implements LocationListener {

    private final Context mContext;
    Location location;
    double latitude;
    double longitude;

    //언제 gps값을 업데이트 할 지 결정할 수 있다.
    //거리가 변경되었을 때 위치정보를 받을 수있다.
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    //일정 시간이 지나면 위치정보를 받을 수 있다.
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    protected LocationManager locationManager;


    public GpsTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    /**
     * Provider
     * 위성을 이용하는 방식 : GPS_PROVIDER
     * 네트워크를 이용하는 방식 : NETWORK_PROVIDER
     * locationManager 서비스를 이용하여 위치가 변경될 때
     */

    public Location getLocation() {
        try {

            //위치 관리자 객체 참조
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            //GPS프로바이터와 네트워크 프로바이더 사용가능 여부
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {

                int hasFineLocationPermission = ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION);


                if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                        hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {


                } else
                    return null;


                if (isNetworkEnabled) {


                    //위치 정보를 원하는 시간, 거리마다 갱신해준다.
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null)
                    {
                        //가장 최근의 위치정보를 가져온다.
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null)
                        {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }

                        /**
                         * 어플을 완전히 죽인 후에 들어와도 최근 위치 정보를 가져올 수 있도록 구현할라고....하는데 그런데...굳이 그럴필요가 있나
                         */
                    }
                }


                if (isGPSEnabled)
                {
                    if (location == null)
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
                        if (locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null)
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.d("@@@", ""+e.toString());
        }

        return location;
    }

    public double getLatitude()
    {
        if(location != null)
        {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    public double getLongitude()
    {
        if(location != null)
        {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**onLocationChanged
     *
     * 위치가 바뀔 때 새로운 위치값을 받아올 수 있도록 한다. (12/11에 추가함)
     *
     */
    @Override
    public void onLocationChanged(Location location) {
        if(location!=null) {
            latitude=location.getLatitude();
            longitude=location.getLongitude();
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
