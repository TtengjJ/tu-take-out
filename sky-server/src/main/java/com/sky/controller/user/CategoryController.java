package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //查询菜品的分类
    @GetMapping("/list")
    @Cacheable(value = "categoryCache", key = "'category:' + #type", unless = "#result.data == null")
    public Result<List<Category>> list(Integer type){
        log.info("查询分类");
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }

}
