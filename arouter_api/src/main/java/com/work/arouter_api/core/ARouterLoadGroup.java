package com.work.arouter_api.core;

import java.util.Map;

/**
 * 路由组Group对味提供加载数据接口
 */
public interface ARouterLoadGroup {
    Map<String,Class<? extends ARouterLoadPath>> loadGroup();
}
