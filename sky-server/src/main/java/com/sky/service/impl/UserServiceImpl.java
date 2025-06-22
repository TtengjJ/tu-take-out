package com.sky.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sky.entity.User;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    //微信服务接口
    public static final String wx_login="https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(String code) {

        // 参数校验
        if (code == null || code.isEmpty()) {
            throw new RuntimeException("登录code不能为空");
        }

        String openid = getOpenid(code);
        if(openid==null){
             throw new RuntimeException("登录失败");
         }
        //判断是否为新用户
        //查询数据库
        User user=userMapper.getUserByOpenId(openid);

        //如果是新用户，则创建用户
        if(user==null){
            user=User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回用户信息
        return user;
    }

    private String getOpenid(String code) {
        //获得openid
        Map<String,String> map=new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type","authorization_code");
        log.info("微信登录请求参数：{}", map);

        String json= HttpClientUtil.doGet(wx_login,map);
        log.info("微信登录返回结果：{}", json);
        //判断openid是否存在
        //json解析
        JSONObject jsonObject= JSON.parseObject(json);

        // 判断是否存在错误
        if (jsonObject.getInteger("errcode") != null) {
            log.error("微信登录失败：{}", jsonObject.getString("errmsg"));
            return null;
        }


        String openid = jsonObject.getString("openid");

        if (openid == null || openid.isEmpty()) {
            log.error("获取openid失败");
            return null;
        }
        return openid;
    }
}
