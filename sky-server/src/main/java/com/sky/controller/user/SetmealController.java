package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;


    //根据分类id查询套餐列表
    @GetMapping("/list")
    public Result<List<Setmeal>> list(Long categoryId){
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        return Result.success(setmealService.list(setmeal));
    }

    //根据套餐id查询包含的菜品
    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> detail(@PathVariable Long id) {
        List<DishItemVO> dishItemVOS = setmealService.getById(id);
        return Result.success(dishItemVOS);
    }
}
