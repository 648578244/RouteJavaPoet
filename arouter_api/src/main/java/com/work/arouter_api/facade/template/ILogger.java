package com.work.arouter_api.facade.template;

public interface ILogger {
    void info(String tag,String message);
    void error(String tag, String message);

    String getDefaultTag();

    void warning(String tag, String s);
}
