package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.WeChatProperties;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

@Component
@Slf4j
public class WeChatPayUtil {

    private static final String JSAPI = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
    private static final String REFUNDS = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

    @Autowired
    private WeChatProperties weChatProperties;

    private CloseableHttpClient httpClient;
    private PrivateKey privateKey;

    // 固定配置，用于测试
    private static final String TEST_PRIVATE_KEY =
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCtXDGyh7EdP7v0"
                    + "2HIp2eZYkVs2giIcKHD5zpNrXUyF0OrHjkRVVzb85WGm5s0AII75yKs8fdGXLPYb"
                    + "2frNzXt2GmyRVP9zk9HrU+vQwCwaufmrR3SpqBN+t6LgR+GT+CulG203e+H53d7I"
                    + "c5NQqz3nBM9/OxIlCvHZrSqkTNCbylGyDqVGQ8QCWZvPGBVh7p7WQ4OHZeM+0aR5"
                    + "L+6myQYmarqSVPrHXe3CZzxp4EL+YVm4MnN5BBZaGJNv7az5yE4QUQvPM5hFBLzH"
                    + "FOfOhA3iKyO2VMpG2ZxNj0ISwcREkh8WhI1CUI2Zv+Y6FTXqnZXz2nMHcBUr8X5v"
                    + "YeohK+7dAgMBAAECggEAZCqDQZNhcxQwZzQmLWtQUJzFGJ66o2VgQEYx+rHnM8HG"
                    + "p+1CiDCyvwkyJ7yEQYX09svcP9fgGYGz0BRXDgJ0RGxlPQrGFdGxKzxqI9oVvfW2"
                    + "pXg7k7sRTL4qKZ+vqG9qQQZVXs4NP9ASNpuGcR5YNv2JRbhuwHPz5KCz1KJJ5AnX"
                    + "Qxk2ZQqP+ANmnpzReHE3viL7jQ1BRSoEWGOqUjUUhnb+bv3kps2jAM9Mo6IXAIPq"
                    + "NF5R8NVFh1+tdgYJGF9XQrNDv5NAIXKhg+C3WAX6XBhfMGrAT9Sf3EnnNAYTd+5U"
                    + "RzD8uuxgkXR6NYVycd22OHmqi8G6oAo5e2xjy92TCQKBgQD2Z3Sg3uV6QN1D0hBR"
                    + "P2gVjk2q9TWjgS7ZKJ9ZqBXz8vNydAF0zsZvKdnY9yj+nIHKvYH1Cd8c6s86j0KD"
                    + "J0hV3UnW4QqPiCo2RY3S2fRS5t4Gc1J9iC9F4Pj0rrp5RsqWl9AOE5s4iU2LwZez"
                    + "ErciZ4kfquvFBwHXcnEobQe/7wKBgQCz+C3+GBXjY1ZmK86qaGGHJYBZ7ZqRbKmZ"
                    + "VeOyJ3IxEQeU1kRFvdXP9QNQfkvGcTl0WcY1txSEwxZWQZjLMY0oQ9AZzPbCZz3B"
                    + "rUOmqJIYOh/SaKmcpBlVXCK0vRGFdJ0XlJJvvYJ9dQRJ5fOuRyWX2XRPt4BP8OTs"
                    + "iLvoAfFlwwKBgH+FQVK9rUi8pure8NCy7lX/YluvArBJX5QEfGxvPh0NF4M3lzqF"
                    + "OpZ7oX6cb0kNoIYL6JjmGpXpJGzBXaDqQb9JAGRFBq+yxsuZ5z9ziQjbGGKGqXsQ"
                    + "L0uDvxkLV7UqE5xpYFQqdj5QA7k7oB0pDfWgO4aBaZKOI7EDsXgEYYbZAoGAS2Vs"
                    + "sCPa1BQ3PcyQxD0cihfa8HnuXpb+CQeK6JKFmS2zebK3YHXb5E2RWCXeGFJBG+qF"
                    + "7f5kGZ1L5K8M+PvUaF4XM7CohIYH1hJs+FYqUxB1qI9GQ7g5Bni8vXEzV8F3Urq+"
                    + "GGu5xLFyBlVjtL5WlhapT3t8Hs6QyHUXj4H/9zkCgYEA6pu9qoQUxDjW0AfB+R89"
                    + "M8rMphQI8RHvf5WqQQ1ePVCd8N6VO/GKDRYIzICUQSDhR3uhNLGNFItcCtoCb7Zx"
                    + "kLOhM0A6/9F3FHb4vdh2D3UwBpLj43FydQ1smz5OcxDzJ2g5v1jjzP7b4kNyKS0U"
                    + "hpPy+5Zs0NZrHD8FnPR3GL4=";

    @PostConstruct
    public void init() {
        try {
            // 初始化私钥
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(TEST_PRIVATE_KEY));
            this.privateKey = kf.generatePrivate(keySpec);

            // 创建支付客户端
            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant("1234567890", // 商户号
                            "ABCDEFGHIJKLMN", // 商户序列号
                            this.privateKey)
                    .withValidator(response -> true); // 暂时不验证响应签名

            this.httpClient = builder.build();
            log.info("微信支付客户端初始化成功");
        } catch (Exception e) {
            log.error("微信支付客户端初始化失败", e);
            throw new RuntimeException("微信支付初始化失败", e);
        }
    }

    private String post(String url, String body) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpPost.addHeader("Wechatpay-Serial", "ABCDEFGHIJKLMN");
        httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    private String get(String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        httpGet.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpGet.addHeader("Wechatpay-Serial", "ABCDEFGHIJKLMN");

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    private String jsapi(String orderNum, BigDecimal total, String description, String openid) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appid", "wx123456789");
        jsonObject.put("mchid", "1234567890");
        jsonObject.put("description", description);
        jsonObject.put("out_trade_no", orderNum);
        jsonObject.put("notify_url", "https://your-domain.com/notify");

        JSONObject amount = new JSONObject();
        amount.put("total", total.multiply(new BigDecimal(100)).intValue());
        amount.put("currency", "CNY");
        jsonObject.put("amount", amount);

        JSONObject payer = new JSONObject();
        payer.put("openid", openid);
        jsonObject.put("payer", payer);

        return post(JSAPI, jsonObject.toJSONString());
    }

    public JSONObject pay(String orderNum, BigDecimal total, String description, String openid) throws Exception {
        String response = jsapi(orderNum, total, description, openid);
        JSONObject result = JSON.parseObject(response);

        String prepayId = result.getString("prepay_id");
        if (prepayId != null) {
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            String nonceStr = RandomStringUtils.randomAlphanumeric(32);

            String message = String.format("%s\n%s\n%s\n%s\n",
                    "wx123456789",
                    timeStamp,
                    nonceStr,
                    "prepay_id=" + prepayId);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            String sign = Base64.getEncoder().encodeToString(signature.sign());

            JSONObject payParams = new JSONObject();
            payParams.put("timeStamp", timeStamp);
            payParams.put("nonceStr", nonceStr);
            payParams.put("package", "prepay_id=" + prepayId);
            payParams.put("signType", "RSA");
            payParams.put("paySign", sign);

            return payParams;
        }
        return result;
    }

    public String refund(String outTradeNo, String outRefundNo, BigDecimal refund, BigDecimal total) throws Exception {
        JSONObject request = new JSONObject();
        request.put("out_trade_no", outTradeNo);
        request.put("out_refund_no", outRefundNo);

        JSONObject amount = new JSONObject();
        amount.put("refund", refund.multiply(new BigDecimal(100)).intValue());
        amount.put("total", total.multiply(new BigDecimal(100)).intValue());
        amount.put("currency", "CNY");
        request.put("amount", amount);

        request.put("notify_url", "https://your-domain.com/refund/notify");

        return post(REFUNDS, request.toJSONString());
    }
}