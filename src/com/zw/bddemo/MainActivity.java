package com.zw.bddemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import com.baidu.location.BDGeofence;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private LocationClient locationClient;
	private LocationMode locationMode;
	public MyLocationListener myListener = new MyLocationListener();
	MapView mapView;
	BaiduMap baiduMap;

	boolean isFirstLoc = true;
	private Context context;
	private BitmapDescriptor bitmapDescriptor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initMapViews();

		context = MainActivity.this;
		
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mReceiver = new SDKReceiver();
		registerReceiver(mReceiver, iFilter);
		
		addCustomElementsDemo();
		refesh();
	}
	
	List<LatLng> points = null;
	Polyline polyline = null;
	Marker markerA;
	public void refesh(){
		final Handler handler = new Handler(){
			double x = 0.02;
			@Override
			public void handleMessage(Message msg) {
				
				LatLng p1 = new LatLng(22.57923, 113.95923+x);
				points.add(p1);
				polyline.setPoints(points);
				markerA.setPosition(p1);
				x=x+0.02;
			}
		};
		
		TimerTask mTask = new TimerTask() {
			
			@Override
			public void run() {
				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);
			}
		};
		Timer timer = new Timer(true);
		timer.schedule(mTask, 1000,1500);
	}
	
	private void addCustomElementsDemo(){
		//添加折线
		LatLng p1 = new LatLng(22.57923,113.95923);
		LatLng p2 = new LatLng(22.54923,113.99923);
		LatLng p3 = new LatLng(22.57923,113.93923);
		List<LatLng> points = new ArrayList<LatLng>();
		points.add(p1);
		points.add(p2);
		points.add(p3); 
		OverlayOptions ooPolyLine = new PolylineOptions().width(10)
				.color(0xAAFF0000).points(points);
		baiduMap.addOverlay(ooPolyLine);
		
		// 添加弧线
		OverlayOptions ooArc = new ArcOptions().color(0xAA00FF00).width(4)
				.points(p1, p2, p3);
		baiduMap.addOverlay(ooArc);
		
		// 添加点
		LatLng llDot = new LatLng(22.57925, 112.93923);
		OverlayOptions ooDot = new DotOptions().center(llDot).radius(6)
				.color(0xFF0000FF);
		baiduMap.addOverlay(ooDot);
		
		LatLng llText = new LatLng(22.51925, 112.93923);
		OverlayOptions ooText = new TextOptions().bgColor(0xAAFFFF00)
				.fontSize(24).fontColor(0xFFFF00FF).text("百度地图").rotate(-30)
				.position(llText);
		baiduMap.addOverlay(ooText);
	}

	public class SDKReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				showToast("key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置");
			} else if (s
					.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				showToast("网络出错");
			}
		}
	}

	private void showToast(String str) {
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	private SDKReceiver mReceiver;

	private void initMapViews() {
		mapView = (MapView) findViewById(R.id.bmapView);
		baiduMap = mapView.getMap();
		baiduMap.setMyLocationEnabled(true);
		locationClient = new LocationClient(this);
		locationClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(1000);
		locationClient.setLocOption(option);
		locationClient.start();
		//mapView.showZoomControls(true);
		
	}

	

	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || mapView == null) {
				return;
			}
			MyLocationData locationData = new MyLocationData.Builder()
					.accuracy(location.getRadius()).direction(100)
					.latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			baiduMap.setMyLocationData(locationData);
			
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				mapView.refreshDrawableState();
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				baiduMap.animateMapStatus(u);
				initInfoWindow(ll,location.getAddrStr()+location.getCity()+location.getNetworkLocationType());
			}
		}

	}

	private void initInfoWindow(LatLng ll,String str) {
		View view = LayoutInflater.from(context).inflate(R.layout.pop_layout, null);
		TextView t1 = (TextView) view.findViewById(R.id.location_tips);
		t1.setText("纬度："+ll.latitude+"经度："+ll.longitude+"地址："+str);
		InfoWindow infoWindow = new InfoWindow(view, ll, 0);
		baiduMap.showInfoWindow(infoWindow);
		showGeoFence(ll);
	}
	
	private void showGeoFence(LatLng ll){
		BDGeofence.Builder builder = new BDGeofence.Builder();
		builder.setCircularRegion(ll.longitude, ll.latitude, 2);
		
	}
	
	@Override
	protected void onPause() {
		mapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		locationClient.stop();
		baiduMap.setMyLocationEnabled(false);
		mapView.onDestroy();
		mapView = null;
		super.onDestroy();
	}
}
