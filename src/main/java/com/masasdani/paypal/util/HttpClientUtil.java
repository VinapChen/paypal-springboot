package com.masasdani.paypal.util;

import java.io.IOException;

import net.sf.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * @author wangbowen
 * @since 18-4-3
 */

public class HttpClientUtil {
    /**
     * post请求
     *
     * @param url
     * @param jsonObject object
     * @return json object
     */
    public static JSONObject doPost(String url, JSONObject jsonObject) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        JSONObject response = null;

        try {
            StringEntity s = new StringEntity(jsonObject.toString());
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");
            post.setEntity(s);
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = res.getEntity();
                String result = EntityUtils.toString(entity);
                response = JSONObject.fromObject(result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        System.out.println(response.toString());
        return response;
    }
}
