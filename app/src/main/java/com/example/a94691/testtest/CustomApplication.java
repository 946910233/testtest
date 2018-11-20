package com.example.a94691.testtest;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

//全局变量的位置有点像小程序的app，登录账户密码存放的位置
public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(this);

    }
}
