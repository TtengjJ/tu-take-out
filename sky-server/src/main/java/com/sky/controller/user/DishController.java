package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    //根据分类id查询菜品
    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId){
        log.info("根据分类id查询菜品");
        List<DishVO> list = dishService.list(categoryId);
        return Result.success(list);
    }


}
