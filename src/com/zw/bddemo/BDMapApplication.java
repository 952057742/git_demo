package com.zw.bddemo;

import com.baidu.mapapi.SDKInitializer;

import android.app.Application;

public class BDMapApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		// ��ʹ�� SDK �����֮ǰ��ʼ�� context ��Ϣ������ ApplicationContext
		SDKInitializer.initialize(this);
	}
}
