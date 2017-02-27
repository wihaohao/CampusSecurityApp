package com.campussecurity.app;

import android.app.Activity;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.campussecurity.app.login.model.User;
import com.hao.common.exception.ApiException;
import com.hao.common.manager.AppManager;
import com.hao.common.rx.RxBus;
import com.hao.common.rx.RxEvent;
import com.hao.common.utils.UmengUtil;
import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * @Package com.campussecurity.app
 * @作 用:
 * @创 建 人: linguoding 邮箱：linggoudingg@gmail.com
 * @日 期: 2017年02月13日  15:25
 */


public class App extends MultiDexApplication implements AppManager.Delegate {
    private RefWatcher mRefWatcher;
    public User cacheUser;
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        if (AppManager.isInOtherProcess(this)) {
            Log.e("App", "enter the other process!");
            return;
        }

        // 初始化内存泄露检测库
        mRefWatcher = LeakCanary.install(this);
        // 初始化应用程序管理器
        AppManager.getInstance().init(BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug"), this);

        // 初始化友盟 SDK
        UmengUtil.initSdk();
        RxBus.toObservable(RxEvent.AppEnterForegroundEvent.class).subscribe(appEnterForegroundEvent -> appEnterForeground());
        RxBus.toObservable(RxEvent.AppEnterBackgroundEvent.class).subscribe(appEnterBackgroundEvent -> appEnterBackground());

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void refWatcherWatchFragment(Fragment fragment) {
        mRefWatcher.watch(fragment);
    }

    @Override
    public boolean isActivityNotContainFragment(Activity activity) {
        return true;
    }


    @Override
    public void handleServerException(ApiException apiException) {
        Logger.i("处理网络请求异常");
    }

    private void appEnterForeground() {
        Logger.i("应用进入前台");
    }

    private void appEnterBackground() {
        Logger.i("应用进入后台");
    }
}
