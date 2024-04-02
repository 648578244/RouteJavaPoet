package com.work.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)//作用的目标再类上
@Retention(RetentionPolicy.CLASS)//在编译期
public @interface ARouter {
    String path();
    String group() default "";
}
