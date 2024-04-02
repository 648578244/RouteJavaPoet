package com.work.arouter_api.launcher;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import com.work.arouter_api.core.LogisticsCenter;
import com.work.arouter_api.exception.HandlerException;
import com.work.arouter_api.exception.InitException;
import com.work.arouter_api.exception.NoRouteFoundException;
import com.work.arouter_api.facade.Postcard;
import com.work.arouter_api.facade.callback.NavigationCallback;
import com.work.arouter_api.facade.service.InterceptorService;
import com.work.arouter_api.facade.service.PathReplaceService;
import com.work.arouter_api.facade.service.PretreatmentService;
import com.work.arouter_api.facade.template.ILogger;
import com.work.arouter_api.thread.DefaultPoolExecutor;
import com.work.arouter_api.utils.Constants;
import com.work.arouter_api.utils.DefaultLogger;

import org.w3c.dom.Text;

import java.util.concurrent.ThreadPoolExecutor;

public class _ARouter {
    static ILogger logger = new DefaultLogger(Constants.TAG);
    private static Context mContext;
    private volatile static boolean debuggable = false;

    private volatile static boolean hasInit = false;
    private static Handler mHandler;
    private volatile static _ARouter instance = null;

    private static InterceptorService interceptorService;


    private volatile static ThreadPoolExecutor executor = DefaultPoolExecutor.getInstance();

    public static boolean init(Application application) {
        mContext = application;
        LogisticsCenter.init(mContext, executor);
        hasInit = true;
        mHandler = new Handler(Looper.getMainLooper());
        return true;
    }

    public static _ARouter getInstance() {
        if (!hasInit) {
            throw new InitException("需要先调用ARouter.init(context)进行初始化");
        } else {
            if (instance == null) {
                synchronized (_ARouter.class) {
                    if (instance == null) {
                        instance = new _ARouter();
                    }
                }
            }
            return instance;
        }
    }

    public static boolean debuggable() {
        return debuggable;
    }

    protected Postcard build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new HandlerException("请提供参数");
        } else {
            PathReplaceService pService = navigation(PathReplaceService.class);
            if (null != pService) {
                path = pService.forString(path);
            }
        }
        return build(path, extractGroup(path), true);
    }

    private Postcard build(String path, String group, Boolean afterReplace) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(group)) {
            throw new HandlerException("请提供参数path");
        } else {
            if (!afterReplace) {
                PathReplaceService pService = navigation(PathReplaceService.class);
                if (null != pService) {
                    path = pService.forString(path);
                }
            }
            return new Postcard(path, group);
        }
    }

    static void afterInit() {
        // Trigger interceptor init, use byName.
//        interceptorService = (InterceptorService) ARouter.getInstance().build("/arouter/service/interceptor").navigation();
    }

    protected <T> T navigation(Class<? extends T> service) {
        try {
            Postcard postcard = LogisticsCenter.buildProvider(service.getName());
            if (null == postcard) {
                postcard = LogisticsCenter.buildProvider(service.getSimpleName());
            }
            if (null == postcard) {
                return null;
            }
            postcard.setContext(mContext);
            LogisticsCenter.completion(postcard);
            return (T) postcard.getProvider();
        } catch (NoRouteFoundException ex) {
//            logger.warning(Constants.TAG,ex.getMessage());
            return null;
        }

    }

    protected Object navigation(final Context context, final Postcard postcard, final int requestCode, final NavigationCallback callback) {
        PretreatmentService pretreatmentService = ARouter.getInstance().navigation(PretreatmentService.class);
        if (null != pretreatmentService && !pretreatmentService.onPretreatment(context, postcard)) {
            return null;
        }
        postcard.setContext(null == context ? mContext : context);
        try {
            LogisticsCenter.completion(postcard);
        } catch (NoRouteFoundException ex) {

        }
        return _navigation(postcard, requestCode, callback);

    }

    private Object _navigation(final Postcard postcard, final int requestCode, final NavigationCallback callback) {
        final Context currentContext = postcard.getContext();
        final Intent intent = new Intent(currentContext,postcard.getDestination());
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        runInMainThread(new Runnable() {
//            @Override
//            public void run() {
                ActivityCompat.startActivity(currentContext,intent,null);
//            }
//        });
        return null;
    }

    private void runInMainThread(Runnable runnable) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }
    /**
     * 解析入参path,得到组数据
     *
     * @param path
     */
    private String extractGroup(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new HandlerException(Constants.TAG + "请输入path 如：'/app/MainActivity'");
        }
        //截取组 如/app/MainActivity 的app
        String defaultGroup = path.substring(1, path.indexOf("/", 1));
        if (TextUtils.isEmpty(defaultGroup)) {
            throw new HandlerException(Constants.TAG + "请输入path 如：'/app/MainActivity'");
        } else {
            return defaultGroup;
        }
    }

}
