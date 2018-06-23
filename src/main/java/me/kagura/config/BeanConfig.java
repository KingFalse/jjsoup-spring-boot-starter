package me.kagura.config;

import javassist.*;
import me.kagura.HttpConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

/**
 * 配置动态生成HttpConnectionX类
 */
@Configuration
@ComponentScan(basePackages = "me.kagura")
public class BeanConfig {

    private static Class axClass;

    @PostConstruct
    public void initClass() throws NotFoundException, CannotCompileException {
        ClassPool classPool = ClassPool.getDefault();
        String canonicalName = org.jsoup.helper.HttpConnection.class.getCanonicalName();
        //将org.jsoup.helper.HttpConnection复制一份org.jsoup.helper.HttpConnectionX
        CtClass ctxClass = classPool.getAndRename(canonicalName, canonicalName + "X");
        //设置新类HttpConnectionX继承抽象类com.example.proxyjsoup.HttpConnection
        ctxClass.setSuperclass(classPool.get(HttpConnection.class.getCanonicalName()));
        CtConstructor[] constructors = ctxClass.getDeclaredConstructors();
        CtConstructor constructor = constructors[0];
        //修改HttpConnectionX的无参构造方法为public
        constructor.setModifiers(Modifier.PUBLIC);
        //删除静态方法connect(String url);
        CtMethod connect = ctxClass.getDeclaredMethod("connect");
        ctxClass.removeMethod(connect);
        //编译并加载HttpConnectionX
        axClass = ctxClass.toClass();
    }

    @Bean
    @Scope("prototype")
    public HttpConnection getHttpConnection() throws IllegalAccessException, InstantiationException {
        return (HttpConnection) axClass.newInstance();
    }

}
