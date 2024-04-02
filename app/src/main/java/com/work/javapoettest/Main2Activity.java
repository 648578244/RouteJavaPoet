package com.work.javapoettest;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.work.annotation.ARouter;
import com.work.annotation.model.RouterBean;
import com.work.arouter_api.core.ARouterLoadPath;
import com.work.javapoettest.test.ARouter$$Group$$order;

import java.util.Map;

@ARouter(path = "/app/Main2Activity")
public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_second);
    }
    public void jump(){
//        ARouter$$Group$$order loadGroup = new ARouter$$Group$$order();
//        Map<String, Class<? extends ARouterLoadPath>> groupMap = loadGroup.loadGroup();
//
//        Class<? extends ARouterLoadPath> clazz = groupMap.get("order");
//        try {
//            ARouterLoadPath path = clazz.newInstance();
//            Map<String, RouterBean> pathMap = path.loadPath();
//            RouterBean routerBean = pathMap.get("/app/OrderActivity");
//            if (routerBean != null){
//                Intent intent = new Intent(this,routerBean.getClazz());
//
//                startActivity(intent);
//            }
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        } catch (InstantiationException e) {
//            throw new RuntimeException(e);
//        }
    }
}