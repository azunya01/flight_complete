package com.sky.test;



import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.http.HttpClient;

@SpringBootTest
public class HttpClientTest {


    @Test
    public void testGet() throws IOException {
         CloseableHttpClient aDefault = HttpClients.createDefault();
         HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");

        final CloseableHttpResponse response = aDefault.execute(httpGet);

        final int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("状态码："+statusCode);
        HttpEntity entity = response.getEntity();
        final String string = EntityUtils.toString(entity);
        System.out.println("服务端的数据为："+string);

        response.close();
        aDefault.close();

    }

    @Test
    public void testPost() throws IOException {
        CloseableHttpClient aDefault = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", "admin");
        jsonObject.addProperty("password", "123456");
        StringEntity stringEntity = new StringEntity(jsonObject.toString());
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        final CloseableHttpResponse execute = aDefault.execute(httpPost);
        int statusCode = execute.getStatusLine().getStatusCode();
        System.out.println("响应码为："+statusCode);
        HttpEntity entity = execute.getEntity();
        String string = EntityUtils.toString(entity);
        System.out.println("响应数据为："+string);
        execute.close();
        aDefault.close();
    }
}
