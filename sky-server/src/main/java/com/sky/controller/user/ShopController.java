package com.sky.controller.user;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
public class ShopController {

    public final static String SHOP_STATUS = "SHOP:STATUS";
    public static final Integer DEFAULT_STATUS = 0; // 默认店铺状态为关闭

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);
        if (status == null){
            status = DEFAULT_STATUS;
            redisTemplate.opsForValue().set(SHOP_STATUS,status);
        }
        log.info("获取店铺状态：{}",status==0?"打样中":"营业中");
        return Result.success( status);
    }
}
