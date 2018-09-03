package me.kagura.config;

import javassist.*;
import me.kagura.JJsoup;
import me.kagura.LoginInfoSerializable;
import me.kagura.impl.DefaultRedisLoginInfoSerializable;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.annotation.PostConstruct;

/**
 * 配置动态生成HttpConnectionX类
 */
@Configuration
@ComponentScan(basePackages = "me.kagura")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class BeanConfig {

    private static Class axClass;

    @PostConstruct
    public void initClass() throws NotFoundException, CannotCompileException {
        ClassPool classPool = ClassPool.getDefault();
        String canonicalName = org.jsoup.helper.HttpConnection.class.getCanonicalName();
        //将org.jsoup.helper.HttpConnection复制一份org.jsoup.helper.HttpConnectionX
        CtClass ctxClass = classPool.getAndRename(canonicalName, canonicalName + "X");
        //设置新类HttpConnectionX继承me.kagura.JJsoup
        ctxClass.setSuperclass(classPool.get(JJsoup.class.getCanonicalName()));
        CtConstructor[] constructors = ctxClass.getDeclaredConstructors();
        CtConstructor constructor = constructors[0];
        //修改HttpConnectionX的无参构造方法为public
        constructor.setModifiers(Modifier.PUBLIC);
        //删除静态方法connect(String url);
        CtMethod connect = ctxClass.getDeclaredMethod("connect");
        ctxClass.removeMethod(connect);

        //替换原execute方法
        CtMethod execute = ctxClass.getDeclaredMethod("execute");
        execute.setName("execute$");
        CtMethod executeNew = CtNewMethod.copy(execute, "execute", ctxClass, null);
        executeNew.setBody("" +
                "{" +
                "me.kagura.AopExecute.contentTypeJson(this);" +
                "me.kagura.AopExecute.logRequest(this);" +
                "long startTime = System.currentTimeMillis();" +
                "Exception exception = null;" +
                "   for (int i = 1; i <= me.kagura.AopExecute.retryCount(this); i++) {" +
                "       exception = null;" +
                "       try {" +
                "           execute$();" +
                "           me.kagura.AopExecute.followFilter(this);" +
                "           boolean isSuccess = me.kagura.AopExecute.followProcess_isSuccess(this,i);" +
                "           if (isSuccess) {" +
                "               break;" +
                "           }" +
                "       } catch (Exception e) {" +
                "           exception = e;" +
                "       }" +
                "   }" +
                "me.kagura.AopExecute.after(this,exception,startTime);" +
                "return this.res;" +
                "}");
        ctxClass.addMethod(executeNew);

        //编译并加载HttpConnectionX
        axClass = ctxClass.toClass();
    }

    @Conditional(ConditionalLoginInfoSerializable.class)
    @Bean
    public LoginInfoSerializable initDefaultLoginInfoSerializable() {
        return new DefaultRedisLoginInfoSerializable();
    }

    @Bean
    @Scope("prototype")
    public JJsoup getHttpConnection() throws IllegalAccessException, InstantiationException {
        return (JJsoup) axClass.newInstance();
    }

}

class ConditionalLoginInfoSerializable implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        try {
            Class.forName("org.springframework.data.redis.core.RedisTemplate");
        } catch (Exception e) {
            return false;
        }
        try {
            conditionContext.getBeanFactory().getBean(LoginInfoSerializable.class);
            return false;
        } catch (Exception e) {
        }
        return true;
    }
}