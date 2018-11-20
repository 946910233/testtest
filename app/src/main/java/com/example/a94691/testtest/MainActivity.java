package com.example.a94691.testtest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private boolean isFirstLoaction = true;
    public LocationClient mLocationClient;
    public BDLocationListener myListener = new MyLocationListener();

    private Intent locationService;
    private TextView latitudeText;
    private TextView longitudeText;
    private MyReceiver receiver = null;
    private TextView distanceText;
    private TextView totalDistanceText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext

        //开启服务
        locationService = new Intent(getApplicationContext(), LocationService.class);
        startService(locationService);


        setContentView(R.layout.activity_main);
        //6.0运动时权限
        mMapView = (MapView) findViewById(R.id.bmapView);
        latitudeText = (TextView) findViewById(R.id.latitude);
        longitudeText = (TextView) findViewById(R.id.longitude);
        distanceText = (TextView) findViewById(R.id.distance);
        totalDistanceText = (TextView) findViewById(R.id.totalDistance);


        //注册广播接收器获取广播
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.a94691.testtest");
        MainActivity.this.registerReceiver(receiver, filter);


        //初始化经纬度以及详细地址，判断是否为android6.0系统版本，如果是，需要动态添加权限
        if (Build.VERSION.SDK_INT >= 23) {
            showContacts();
        } else {
            //注册监听函数
            initMap();
        }
    }


    /**
     * 获取广播数据，在LocationService获取广播的数据，显示到界面上
     *
     * @author
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
           double  totalDistance = bundle.getDouble("totalDistance");
            double   distance = bundle.getDouble("distance");
            double speed = bundle.getDouble("speed");
            String status = bundle.getString("status");

            distanceText.setText(""+distance);
            totalDistanceText.setText("总"+totalDistance + "");
            latitudeText.setText(status + "");
            longitudeText.setText("速度"+speed );
        }
    }




    private void initMap() {
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        //配置定位SDK参数
        initLocation();//定位的配置信息
        //获取地图控件引用
        mBaiduMap = mMapView.getMap();

        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //允许定位
        mBaiduMap.setMyLocationEnabled(true);

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        mLocationClient.registerLocationListener(myListener);    //注册监听函数

        //开启定位
        mLocationClient.start();
        //图片点击事件，回到定位点
        mLocationClient.requestLocation();
    }

    //配置定位SDK参数
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setPriority(LocationClientOption.GpsFirst);    //设置GPS定位优先级
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
        // .getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        option.setOpenGps(true); // 打开gps

        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //mMapView地图销毁，不作处理
            if (mMapView == null) {
                return;
            }



            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);

            if (isFirstLoaction) {//这段代码的作用是把你 的圆点位置显示在地图上，如果不设置否则不管你定位在哪都将默认显示北京天安门
                isFirstLoaction = false;
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }


        }


    }

    //权限问题
    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "没有权限,请手动开启定位权限", Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 200);
            initMap();//不加这一行，即使授权了也不会定位
        } else {
            initMap();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理

        // 当不需要定位图层时关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        LocationService.locationService.stop();
        stopService(locationService);
        mLocationClient.stop();
        mMapView.onDestroy();
        mMapView = null;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }


}
