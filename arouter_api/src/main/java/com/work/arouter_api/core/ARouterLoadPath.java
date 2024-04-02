package com.work.arouter_api.core;

import com.work.annotation.model.RouterBean;

import java.util.Map;

/**
 * 路由组group对应的详细Path加载数据接口
 * 比如APP分组对应有哪些类需要加载
 */
public interface ARouterLoadPath {
    /**
     * 加载路由组Group中的Path详细数据
     * 比如 “app” 分组下的信息：
     * @return key："/app/MainActivity",value:MainActivity信息封装到RouterBean
     */
    Map<String, RouterBean> loadPath();
}
