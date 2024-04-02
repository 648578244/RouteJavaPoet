package com.work.javapoettest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import com.work.arouter_api.launcher.ARouter;

import java.util.Map;

@com.work.annotation.ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       Button btnInit = (Button) findViewById(R.id.btn_init);
       Button btnClick = (Button) findViewById(R.id.btn_click);
       btnInit.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               ARouter.init(getApplication());
           }
       });
       btnClick.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               ARouter.getInstance()
                       .build("/order/OrderActivity")
                       .navigation(MainActivity.this, 666);
           }
       });
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