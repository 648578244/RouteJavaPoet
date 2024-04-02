package com.work.arouter_api.core;

import com.work.annotation.model.RouterBean;

import java.util.HashMap;
import java.util.Map;

public class Warehouse {
    static Map<String, RouterBean> providersIndex = new HashMap<>();
    static Map<String, Class<? extends ARouterLoadPath>> groupsIndex = new HashMap<>();

    static Map<String, RouterBean> routes = new HashMap<>();


}
