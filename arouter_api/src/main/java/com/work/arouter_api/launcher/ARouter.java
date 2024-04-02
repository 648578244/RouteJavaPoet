package com.work.arouter_api.launcher;

import android.app.Application;
import android.content.Context;

import com.work.annotation.model.RouterBean;
import com.work.arouter_api.exception.InitException;
import com.work.arouter_api.facade.Postcard;
import com.work.arouter_api.facade.callback.NavigationCallback;
import com.work.arouter_api.facade.template.ILogger;
import com.work.arouter_api.utils.Constants;

import java.util.logging.ErrorManager;

public final class ARouter {
    public static ILogger logger;
    private volatile static ARouter instance = null;
    private volatile static boolean hasInit = false;

    public static void init(Application application) {
        if (!hasInit) {
            logger = _ARouter.logger;
            hasInit = _ARouter.init(application);
            _ARouter.logger.info(Constants.TAG, "ARouter init start");
            if (hasInit) {
                _ARouter.afterInit();
            }
            _ARouter.logger.info(Constants.TAG, "ARouter init over");
        }
    }

    public static ARouter getInstance() {
        if (!hasInit) {
            throw new InitException("需要先调用ARouter.init(context)进行初始化");
        } else {
            if (instance == null) {
                synchronized (ARouter.class) {
                    if (instance == null) {
                        instance = new ARouter();
                    }
                }
            }
            return instance;
        }
    }

    public static boolean debuggable() {

        return _ARouter.debuggable();
    }

    public Postcard build(String path) {
        return _ARouter.getInstance().build(path);
    }

    public <T> T navigation(Class<? extends T> service) {
        return _ARouter.getInstance().navigation(service);
    }

    public Object navigation(Context mContext, Postcard postcard, int requestCode, NavigationCallback callback) {
        return _ARouter.getInstance().navigation(mContext, postcard, requestCode, callback);
    }
}
