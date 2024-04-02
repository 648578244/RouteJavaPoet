package com.work.compiler;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.sun.org.apache.xpath.internal.operations.Mod;
import com.work.annotation.ARouter;
import com.work.annotation.model.RouterBean;
import com.work.compiler.utils.Constants;
import com.work.compiler.utils.EmptyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * ARouter注解处理器
 */

@AutoService(Processor.class)
@SupportedAnnotationTypes({Constants.AROUTER_ANNOTATION_TYOES})//支持的注解类型
//@SupportedSourceVersion(SourceVersion.RELEASE_8)//接受的编译版本
//@SupportedOptions({Constants.MODULE_NAME, Constants.APT_PACKAGE})//接收Build.gradle传递过来的参数
public class ARouterPorcessor extends BaseProcessor {
    //key：组名“app",value:“app”组的路由路径”ARouter$$Path$$app.class“
    private Map<String, List<RouterBean>> tempPathMap = new HashMap<>();
    private Map<String, String> tempGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        if (EmptyUtils.isEmpty(moduleName) || EmptyUtils.isEmpty(packageNameForAPT)) {
            throw new RuntimeException("注解处理器需要的参数modulename或者pacgageName为空，请在对应的build.gradle配置参数");
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //一旦有类上面使用了ARouter注解
        if (!EmptyUtils.isEmpty(annotations)) {
            //获取所有被@ARouter注解的元素集合
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);
            if (!EmptyUtils.isEmpty(elements)) {
                //解析元素
                try {
                    paseElements(elements);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 解析所有被@ARouter注解的元素集合
     *
     * @param elements
     */
    private void paseElements(Set<? extends Element> elements) throws IOException {
        //通过Element工具类获取Activity类型
        TypeElement activityType = elementsUtils.getTypeElement(Constants.ACTIVITY);
        //显示类的信息
        TypeMirror activityMirror = activityType.asType();
        for (Element element : elements) {
            //获取每个元素的类信息
            TypeMirror elementMirror = element.asType();
            messager.printMessage(Diagnostic.Kind.NOTE, "遍历的元素信息为：" + elementMirror.toString());
            //获取每个类上的@ARouter注解，对应的path值
            ARouter aRouter = element.getAnnotation(ARouter.class);
            //路由详细信息，封装到实体类
            RouterBean bean = new RouterBean.Builder()
                    .setGroup(aRouter.group())
                    .setPath(aRouter.path())
                    .setElement(element)
                    .build();

            //高级判断，说明ARputer的注解只能用在类之上，只用在Activity
            if (typesUtils.isSubtype(elementMirror, activityMirror)) {
                bean.setType(RouterBean.Type.ACTIVITY);
            } else {
                throw new RuntimeException("@ARouter注解目前仅限用于Activity上");
            }
            //赋值临时map存储以上信息，用来遍历时生成代码
            valueOfPathMap(bean);
        }
        TypeElement groupLoadType = elementsUtils.getTypeElement(Constants.AROUTER_GROUP);
        TypeElement pathLoadType = elementsUtils.getTypeElement(Constants.AROUTER_PATH);
        //1.生成路由的详细path类文件，如：ARouter$$Path$$app
        createPathFile(pathLoadType);

        //2.生成路由组Group类文件，如：ARouter$$Group$$app
        createGroupFile(groupLoadType, pathLoadType);

    }

    /**
     * 生成路由组Group对应的详细Path文件，如ARouter$$Path$$order
     *
     * @param pathLoadType ARouterLoadPath 接口信息
     */
    private void createPathFile(TypeElement pathLoadType) throws IOException {
        if (EmptyUtils.isEmpty(tempPathMap)) return;

        for (Map.Entry<String, List<RouterBean>> entry : tempPathMap.entrySet()) {
            //构造方法的返回值 Map<String, RouterBean>
            ParameterizedTypeName methodReturns = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(RouterBean.class));
            //方法体构造
            // @Override
            // public Map<String, RouterBean> loadPath() {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturns);
            //不循环部分Map<String, RouterBean> pathMap = new HashMap<>();
            methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()", Map.class, String.class, RouterBean.class, Constants.PATH_PARAMETER_NAME, HashMap.class);

            List<RouterBean> pathList = entry.getValue();
            for (RouterBean bean : pathList) {
                //方法内容循环部分
                //pathMap.put("/order/OrderActivity", RouterBean.create(RouterBean.Type.ACTIVITY, OrderActivity.class, "/order/OrderActivity", "order"));
                methodBuilder.addStatement("$N.put($S,$T.create($T.$L,$T.class,$S,$S))",
                        Constants.PATH_PARAMETER_NAME,
                        bean.getPath(),
                        RouterBean.class,
                        RouterBean.Type.class,
                        bean.getType(),
                        bean.getElement(),
                        bean.getPath(),
                        bean.getGroup());
            }
            //遍历后 return pathMap
            methodBuilder.addStatement("return $N", Constants.PATH_PARAMETER_NAME);

            //生成类文件，如：ARouter$$Path$$order
            String finalClassName = Constants.PATH_FILE_NAME + entry.getKey();

            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件为：" + packageNameForAPT + "." + finalClassName);

            JavaFile.builder(packageNameForAPT, TypeSpec.classBuilder(finalClassName)
                            .addSuperinterface(ClassName.get(pathLoadType))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(methodBuilder.build())
                            .build())
                    .build()
                    .writeTo(filer);
            tempGroupMap.put(entry.getKey(), finalClassName);
        }

    }

    /**
     * 生成路由组Group文件，如ARouter$$Group$$order
     *
     * @param groupLoadType ARouterLoadGroup 接口信息
     * @param pathLoadType  ARouterLoadPath 接口信息
     */
    private void createGroupFile(TypeElement groupLoadType, TypeElement pathLoadType) throws IOException {
        if (EmptyUtils.isEmpty(tempPathMap) || EmptyUtils.isEmpty(tempGroupMap)) return;
        //Map<String, Class<? extends ARouterLoadPath>>
        ParameterizedTypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathLoadType))));

        //方法配置    @Override
        //public Map<String, Class<? extends ARouterLoadPath>> loadGroup() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.GROUP_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturns);
        //不循环部分初始化HashMap
        //Map<String,Class<? extends  ARouterLoadPath>> groupMap = new HashMap<>();
        methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathLoadType))),
                Constants.GROUP_PARAMETER_NAME,
                HashMap.class);
        //循环添加数据
        for (Map.Entry<String,String> entry :tempGroupMap.entrySet()){
            //groupMap.put("order",ARouter$$Path$$order.class);
            methodBuilder.addStatement("$N.put($S,$T.class)",
                    Constants.GROUP_PARAMETER_NAME,
                    entry.getKey(),
                    ClassName.get(packageNameForAPT,entry.getValue()));
        }
        //return groupMap;
        methodBuilder.addStatement("return $N",Constants.GROUP_PARAMETER_NAME);

        //生成类文件
        String finalClassName = Constants.GROUP_FILE_NAME +moduleName;
        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Group类文件为：" + packageNameForAPT + "." + finalClassName);
        JavaFile.builder(packageNameForAPT,TypeSpec.classBuilder(finalClassName)
                .addSuperinterface(ClassName.get(groupLoadType))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodBuilder.build()).build()).build().writeTo(filer);
    }

    /**
     * 赋值临时map存储，用来存储路由组Group对应的详细Path类对象，生成路由路径类文件时遍历
     *
     * @param bean
     */
    private void valueOfPathMap(RouterBean bean) {
        if (chechRouterPath(bean)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean >>>" + bean.toString());
            List<RouterBean> routerBeans = tempPathMap.get(bean.getGroup());
            if (EmptyUtils.isEmpty(routerBeans)) {
                routerBeans = new ArrayList<>();
                routerBeans.add(bean);
                tempPathMap.put(bean.getGroup(), routerBeans);
            } else {
                boolean b = routerBeans.stream().anyMatch(obj -> bean.getPath().equalsIgnoreCase(obj.getPath()));
                if (!b) {
                    routerBeans.add(bean);
                }

            }
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范，如：/app/MainActivity");
        }

    }

    private boolean chechRouterPath(RouterBean bean) {
        String group = bean.getGroup();
        String path = bean.getPath();
        if (!EmptyUtils.isEmpty(group)) {
            bean.setGroup(group);
            return true;
        }
        //@ARouter注解的path值，必须要以/开头（模仿阿里的ARouter路透架构）
        if (EmptyUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范，如：/app/MainActivity");
            return false;

        }
        //比如开发者代码为path="/MainActivity"
        if (path.lastIndexOf("/") == 0) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范，如：/app/MainActivity");
            return false;

        }
        //从第一个/ 到第二个/ 中间截取组名
        String finalGroup = path.substring(1, path.indexOf("/", 1));
        //如果开发者代码为path="/MainActivity/MainActivity/MainActivity"
        if (finalGroup.contains("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范，如：/app/MainActivity");
            return false;
        }
        if (EmptyUtils.isEmpty(finalGroup) && !finalGroup.equals(moduleName)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解Group值必须和当前子模块名相同");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }

        return true;
    }
}
