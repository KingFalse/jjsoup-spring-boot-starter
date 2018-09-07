# jjsoup-spring-boot-starter

**jjsoups** 是对jsoup的无侵入封装，使它更好的适合于网络爬虫开发[Powered by Jsoup](https://github.com/jhy/jsoup)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.kagura/jjsoup-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/me.kagura/jjsoup-spring-boot-starter) 

## 如何使用：
**在spring boot项目中引入：**
```xml
<dependency>
    <groupId>me.kagura</groupId>
    <artifactId>jjsoup-spring-boot-starter</artifactId>
    <version>0.1.4</version>
</dependency>
<!--可选，用于在@RestController中支持@JSONBodyField-->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>x.x</version>
</dependency>
<!--可选，用于支持@LoginInfoKey跟LoginInfo自动保存-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```
**快速开始：**
```java
import me.kagura.FollowProcess;
import me.kagura.JJsoup;
import me.kagura.LoginInfo;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class JJsoupTest {
    @Autowired
    JJsoup jJsoup;

    @Test
    public void Test() throws Exception {
        // 常规
        Document document = jJsoup.connect("https://github.com/KingFalse").get();
        System.err.println(document);

        // 携带Logininfo
        LoginInfo loginInfo = new LoginInfo();//随机生成UUID作为key，new LoginInfo("xxx");指定key
        // String              loginInfo.key;                一个字符串值，在序列化时作为key使用
        // Map<String, String> loginInfo.cookies;            用于存放cookie
        // Proxy               loginInfo.Proxy(Proxy proxy); 用于设置代理
        // Proxy               loginInfo.Proxy();            用于获取代理
        // Map<String, Object> loginInfo.extras;             用于存放一些自定义变量
        Connection.Response response = jJsoup.connect("https://github.com/KingFalse", loginInfo)
                .method(Connection.Method.GET)
                .execute();

        // 携带FollowProcess
        FollowProcess followProcess = new FollowProcess() {
            /**
             * 用于解析返回数据，必须重写
             * @param connection
             * @param loginInfo
             * @return
             */
            @Override
            public String doProcess(Connection connection, LoginInfo loginInfo) {
                String title = "";
                try {
                    Document responseDocument = connection.response().parse();
                    title = responseDocument.title();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return title;
            }

            /**
             * 用于处理请求时发生的异常
             * 请求时发生异常会自动重试5次
             * 5次都失败进入此方法
             * 带入最后一次的异常
             * @param connection
             * @param loginInfo
             * @param e
             * @throws Exception
             */
            @Override
            public void doException(Connection connection, LoginInfo loginInfo, Exception e) throws Exception {
                super.doException(connection, loginInfo, e);
            }

            /**
             * 用于逻辑性的请求成功判断
             * 比如接口返回200，但是返回json提示系统超时，可以return false去重试请求
             * @param connection
             * @param loginInfo
             * @return
             */
            @Override
            public boolean isSuccess(Connection connection, LoginInfo loginInfo) {
                return super.isSuccess(connection, loginInfo);
            }
        };
        response = jJsoup.connect("https://github.com/KingFalse", loginInfo, followProcess)
                .method(Connection.Method.GET)
                .execute();
        System.err.println(followProcess.result);


    }

}
```
## 依赖版本：
> * **spring boot 建议1.5以上**
> * **jsoup 1.9.1 以上(默认最新，目前1.11.3)**


## @JSONBodyField
> **请求时务必添加Content-Type: application/json;请求头**
```java
// POST发送json到RestController：{"name":"kagura"}

// 常规写法:
@RestController
class AuthController{
    @PostMapping("post")
    public ResponseEntity post(@RequestBody String body){
        JSONObject jsonObject = JSON.parseObject(body);
        System.err.println(jsonObject.getString("name"));
        return ResponseEntity.ok("OK");
    }
}

// 使用@JSONBodyField
@RestController
class AuthController {
    @PostMapping("post")
    public ResponseEntity post(
            @JSONBodyField String name //如果是请求体的根元素不用写jsonPath
//          @JSONBodyField("$.name") String name
    ) {
        System.err.println(name);
        return ResponseEntity.ok("OK");
    }
}
```
## @LoginInfoKey (LoginInfo自动获取)
**@LoginInfoKey注解用于在Controller的方法参数中指定某个参数作为LoginInfo的key，用于实现自动从redis获取对应的LoginInfo**
```java
@RestController
class AuthController {
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginInfo(LoginInfo loginInfo, @LoginInfoKey @RequestParam String traceid
    ) {
        // 此时jjsoup会根据traceid去redis中拿对应的LoginInfo对象赋值给loginInfo
        System.err.println(loginInfo.toString());
        return "OK";
    }
}
```
## LoginInfo自动保存
> * **默认情况下jjsoup自带的切面会将@Service中所有带LoginInfo类型参数的方法的@AfterReturning跟AfterThrowing时自动将LoginInfo序列化到redis**
> * **如果不需要序列化时请使用@LoginInfoSolidify(false)，加在对应的方法或者类名上**

## 关于LoginInfo自动存取
> * **jjsoup自带了一个redis序列化实现，您只需要添加spring-boot-starter-data-redis即可**
> * **如果您的项目没有使用redis进行缓存，或者您想自定义缓存策略时可以实现me.kagura.LoginInfoSerializable接口并添加@Component即可**

## 统一初始化
> * **用于需要给所有的请求设置属性的时候**
> * **比如要爬取的网站很容易超时，则需要对每个请求设置超时时间**
```java
import me.kagura.InitConnection;
import org.jsoup.Connection;

@Component
class initJsoup implements InitConnection {

    @Override
    public void init(Connection connection) {
        connection.proxy("127.0.0.1", 8888);
        connection.timeout(50000);
        connection.followRedirects(true);
        connection.maxBodySize(1024 * 10);
    }
}
```

## 统一过滤器
> * **用于请求执行后统一处理**
> * **比如将请求结果输出到控制台，或者上传OSS等需求**
```java
@Component
class OSSFilter implements FollowFilter {

    @Override
    public void doFilter(Connection connection, LoginInfo loginInfo) {
        if (loginInfo == null) {
            return;
        }
        System.err.println(connection.response().body());
    }
}
```

## 特点
> * **自动重试**
> * **自动设置Content-Type: application/json;**
> * **自动存取LoginInfo**
> * **@JSONBodyField json请求解析更优雅**

## 欢迎加微信
![Work on it](/Wechat.jpeg)

![Work on it](/nichousha.png)
