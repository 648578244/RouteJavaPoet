package com.work.arouter_api.core;

import static com.work.arouter_api.utils.Constants.AROUTER_SP_CACHE_KEY;
import static com.work.arouter_api.utils.Constants.AROUTER_SP_KEY_MAP;
import static com.work.arouter_api.utils.Constants.DOT;
import static com.work.arouter_api.utils.Constants.ROUTE_ROOT_PAKCAGE;
import static com.work.arouter_api.utils.Constants.SDK_NAME;
import static com.work.arouter_api.utils.Constants.SEPARATOR;
import static com.work.arouter_api.utils.Constants.SUFFIX_GROUP;

import android.content.Context;
import android.content.SharedPreferences;

import com.work.annotation.model.RouterBean;
import com.work.arouter_api.exception.NoRouteFoundException;
import com.work.arouter_api.facade.Postcard;
import com.work.arouter_api.launcher.ARouter;
import com.work.arouter_api.utils.ClassUtils;
import com.work.arouter_api.utils.Constants;
import com.work.arouter_api.utils.PackageUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

public class LogisticsCenter {
    private static Context mContext;
    static ThreadPoolExecutor executor;

    public synchronized static void init(Context context, ThreadPoolExecutor tpe) {
        mContext = context;
        executor = tpe;
        try {
            Set<String> routerMap;
            if (ARouter.debuggable() || PackageUtils.isNewVersion(context)) {
                //通过指定包名，扫描包下面包含的所有的ClassName
                routerMap = ClassUtils.getFileNameByPackageName(mContext, ROUTE_ROOT_PAKCAGE);
                if (!routerMap.isEmpty()) {
                    SharedPreferences.Editor edit = context.getSharedPreferences(AROUTER_SP_CACHE_KEY, Context.MODE_PRIVATE).edit();
                    edit.putStringSet(AROUTER_SP_KEY_MAP, routerMap).apply();
                }
                PackageUtils.updateVersion(context);
            } else {
                routerMap = new HashSet<>(context.getSharedPreferences(AROUTER_SP_CACHE_KEY, Context.MODE_PRIVATE).getStringSet(AROUTER_SP_KEY_MAP, new HashSet<String>()));
            }
            for (String className : routerMap) {
                String path = ROUTE_ROOT_PAKCAGE+DOT+SDK_NAME+SEPARATOR+SUFFIX_GROUP;
                if (className.startsWith(path)){
                    Warehouse.groupsIndex.putAll(((ARouterLoadGroup)(Class.forName(className).getConstructor().newInstance())).loadGroup());
                }
            }
        }catch (Exception e){

        }

    }

    public static Postcard buildProvider(String serviceName) {
        RouterBean bean = Warehouse.providersIndex.get(serviceName);
        if (null == bean) {
            return null;
        } else {
            return new Postcard(bean.getPath(), bean.getGroup());
        }
    }

    public synchronized static void completion(Postcard postcard) {
        if (null == postcard) {
            throw new NoRouteFoundException(Constants.TAG + "No postcard!");
        }
        RouterBean bean = Warehouse.routes.get(postcard.getPath());
        if (bean == null) {
            try {
                addRouteGroupDynamic(postcard.getGroup(), null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            completion(postcard);
        }else{
            postcard.setDestination(bean.getClazz());
            postcard.setType(bean.getType());
            postcard.setPath(bean.getPath());
            postcard.setGroup(bean.getGroup());

        }
    }

    public synchronized static void addRouteGroupDynamic(String groupName, ARouterLoadGroup group) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Warehouse.routes = Warehouse.groupsIndex.get(groupName).getConstructor().newInstance().loadPath();
    }
}
