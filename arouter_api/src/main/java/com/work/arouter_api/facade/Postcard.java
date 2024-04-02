package com.work.arouter_api.facade;

import android.app.Activity;
import android.content.Context;

import com.work.annotation.model.RouterBean;
import com.work.arouter_api.facade.callback.NavigationCallback;
import com.work.arouter_api.facade.template.IProvider;
import com.work.arouter_api.launcher.ARouter;

public class Postcard {
    private RouterBean.Type type;
    private String path;
    private String group;
    private Context context;
    private IProvider provider;
    private Class<?> destination;   // Destination


    public Postcard(String path, String group) {
        this.path = path;
        this.group = group;
    }

    public RouterBean.Type getType() {
        return type;
    }

    public void setType(RouterBean.Type type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
    public Class<?> getDestination() {
        return destination;
    }

    public void setDestination(Class<?> destination) {
        this.destination = destination;
    }
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public IProvider getProvider() {
        return provider;
    }

    public void setProvider(IProvider provider) {
        this.provider = provider;
    }
//    public Class<?> getDestination() {
//        return destination;
//    }
//
//    public RouterBean setDestination(Class<?> destination) {
//        this.destination = destination;
//        return this;
//    }


    public Object navigation() {
        return navigation(null);
    }

    public Object navigation(Context context) {
        return navigation(context, null);
    }

    public Object navigation(Context context, NavigationCallback callback) {
        return ARouter.getInstance().navigation(context, this, -1, callback);
    }

    public void navigation(Activity mContext, int requestCode) {
        navigation(mContext, requestCode, null);
    }

    public void navigation(Activity mContext, int requestCode, NavigationCallback callback) {
        ARouter.getInstance().navigation(mContext, this, requestCode, callback);

    }
}
