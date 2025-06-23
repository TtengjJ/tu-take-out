package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/setmeal")
@RestController("adminSetmealController")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    //分页查询
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("分页查询：{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    //新增
    @PostMapping
    @CacheEvict(value = "setmealCache", key="#setmealDTO.categoryId")//清除缓存
    public Result<SetmealVO> save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        SetmealVO setmealVO = setmealService.save(setmealDTO);
        return Result.success(setmealVO);
    }

    //修改
    @PutMapping
    @CacheEvict(value = "setmealCache", allEntries = true)//清除所有缓存
    public Result<SetmealVO> update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐：{}", setmealDTO);
        SetmealVO setmealVO = setmealService.update(setmealDTO);
        return Result.success(setmealVO);
    }

    //批量删除
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)//清除所有缓存
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐：{}", ids);
        setmealService.delete(ids);
        return Result.success("删除成功");
    }
    //停售起售
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache", allEntries = true)//清除所有缓存
    public Result<String> updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("停售起售套餐：{}", id);
        setmealService.updateStatus(status, id);
        return Result.success("修改成功");
    }

    //根据id查询
    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐：{}", id);
        SetmealVO setmealVO =  setmealService.dishgetById(id);
        return Result.success(setmealVO);
    }


}
