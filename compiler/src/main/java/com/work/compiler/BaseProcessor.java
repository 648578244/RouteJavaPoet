package com.work.compiler;

import com.work.compiler.utils.Constants;
import com.work.compiler.utils.EmptyUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public  abstract  class BaseProcessor extends AbstractProcessor {
    /**
     * 操作ELement工具类（类，函数，属性都是element）
     */
     Elements elementsUtils;
    /**
     * type(类信息)工具类宝航用于操作TypeMirror的工具方法
     */
     Types typesUtils;
    /**
     * 用来报告错误，警告和其他提示信息
     */
     Messager messager;
    /**
     * 文件生成器 类/资源 filter用来创建新的类文件 class文件以及辅助文件
     */
     Filer filer;

     String moduleName;
     String packageNameForAPT;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementsUtils = processingEnvironment.getElementUtils();
        typesUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        Map<String, String> options = processingEnv.getOptions();
        if (!EmptyUtils.isEmpty(options)) {
            moduleName = options.get(Constants.MODULE_NAME);
            packageNameForAPT = options.get(Constants.APT_PACKAGE);
            messager.printMessage(Diagnostic.Kind.NOTE, "moduleName>>>" + moduleName);
            messager.printMessage(Diagnostic.Kind.NOTE, "packageName>>>" + packageNameForAPT);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    //接收Build.gradle传递过来的参数
    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {{
            this.add(Constants.MODULE_NAME);
            this.add(Constants.APT_PACKAGE);
        }};
    }
}
