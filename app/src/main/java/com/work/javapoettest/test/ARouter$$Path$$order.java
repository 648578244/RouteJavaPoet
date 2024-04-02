package com.work.javapoettest.test;

import com.work.annotation.model.RouterBean;
import com.work.arouter_api.core.ARouterLoadPath;
import com.work.order.OrderActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟ARouter 路由器的组文件 对应的路径
 */
public class ARouter$$Path$$order implements ARouterLoadPath {
    @Override
    public Map<String, RouterBean> loadPath() {

        Map<String, RouterBean> pathMap = new HashMap<>();
        pathMap.put("/order/OrderActivity", RouterBean.create(RouterBean.Type.ACTIVITY, OrderActivity.class, "/order/OrderActivity", "order"));
        return pathMap;
    }
}
