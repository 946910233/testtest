package com.example.a94691.testtest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.text.DecimalFormat;

public class LocationService extends Service {
    public static String address;
    public static LocationUtil locationService;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        locationService = new LocationUtil(getApplicationContext());
        locationService.registerListener(bdLocationListener);
        locationService.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationService.unregisterListener(bdLocationListener);
    }


    public static double longitude;
    public static double latitude;
    public static String city;
    public static String status;
    private static LatLng latLng;
    private static LatLng oldLatLng;
    private static double speed;
    private static double distance;
    private static double totalDistance;
    BDLocationListener bdLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            longitude = bdLocation.getLongitude();
            latitude = bdLocation.getLatitude();
            speed = bdLocation.getSpeed();
            city = bdLocation.getCity();
            latLng = new LatLng(latitude, longitude);
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                status = "gps定位";
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                status = "网络定位";
            }

            distance = DistanceUtil.getDistance(oldLatLng, latLng);//两距离之差
            if (speed == 0 || distance >= 12) {//假如速度为0 距离大于12  肯定是误差数据，设定为0
                distance = 0;
            }

            DecimalFormat df = new DecimalFormat("#.00");//保留两位小数
            df.format(distance);
            df.format(speed);
            totalDistance = distance + totalDistance;//每秒根据经纬度距离累加


            oldLatLng = latLng;

            Log.i("rongjiajie", "" + latitude + "," + longitude);
            //发送广播

            new Thread(new Runnable() {//下面的代码通过广播的形式传给activity
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.putExtra("totalDistance", totalDistance);
                    intent.putExtra("speed", speed);
                    intent.putExtra("status", status);
                    intent.putExtra("distance", distance);
                    intent.setAction("com.example.a94691.testtest");
                    sendBroadcast(intent);
                }
            }).start();


        }
    };


}
