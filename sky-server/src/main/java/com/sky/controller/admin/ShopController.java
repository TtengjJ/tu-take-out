package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {

    public final static String SHOP_STATUS = "SHOP:STATUS";
    public static final Integer DEFAULT_STATUS = 0; // 默认店铺状态为关闭

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;


    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺状态，状态：{}",status==0?"关闭":"开启");
        redisTemplate.opsForValue().set(SHOP_STATUS,status);
        return Result.success();
    }

    @GetMapping("/status")
    public Result<Integer> getStatus( ){
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);
        if (status == null) {
            status = DEFAULT_STATUS;
            redisTemplate.opsForValue().set(SHOP_STATUS,status);
        }
        log.info("获取店铺状态：{}",status==0?"关闭":"开启");

        return Result.success( status);
    }
}
