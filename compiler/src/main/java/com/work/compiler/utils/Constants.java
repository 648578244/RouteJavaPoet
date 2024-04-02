package com.work.compiler.utils;

public class Constants {
    /**
     * 注解处理器支持的注解类型
     */
    public static final String AROUTER_ANNOTATION_TYOES = "com.work.annotation.ARouter";

    //每个字模块的模块名
    public static final String MODULE_NAME = "moduleName";

    //用于存放APT生成的类文件
    public static final String APT_PACKAGE = "packageNameForAPT";
    public static final String STRING = "java.lang.String";
    public static final String ACTIVITY = "android.app.Activity";
    public static final String BASE_PACKAGE = "com.work.arouter_api";
    public static final String AROUTER_GROUP = BASE_PACKAGE + ".core.ARouterLoadGroup";
    public static final String AROUTER_PATH = BASE_PACKAGE + ".core.ARouterLoadPath";
    public static final String PATH_METHOD_NAME = "loadPath";
    public static final String PATH_PARAMETER_NAME = "pathMap";
    public static final String PATH_FILE_NAME = "ARouter$$Path$$";
    public static final String GROUP_METHOD_NAME = "loadGroup";
    public static final String GROUP_PARAMETER_NAME = "groupMap";
    public static final String GROUP_FILE_NAME = "ARouter$$Group$$";


}
