package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
public class DishController {

    //redis缓存
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private DishService dishService;

    //根据分类id查询菜品
    @GetMapping("/list")
    @Cacheable(value = "dishCache", key = "#categoryId")
    public Result<List<DishVO>> list(Long categoryId){
//        //构造redis的key
//        String key = "dish_" + categoryId;
//        //从redis中获取数据
//        List<DishVO> dishVOS = (List<DishVO>) redisTemplate.opsForValue().get(key);
//        //判断redis中是否存在数据
//        if(dishVOS != null&& !dishVOS.isEmpty()){
//            //存在则在redis中直接返回
//            log.info("从redis中获取数据");
//            return Result.success(dishVOS);
//        }

        Dish dish=new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);

        //不存在则从数据库中查询数据
        log.info("根据分类id查询菜品");
        List<DishVO> dishVOS = dishService.list(dish);
        //将查询到的数据存入redis中
        //redisTemplate.opsForValue().set(key,dishVOS);

        return Result.success(dishVOS);
    }

}
