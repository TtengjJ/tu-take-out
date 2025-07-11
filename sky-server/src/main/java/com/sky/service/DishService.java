package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DishService {
    /**
     * 新增菜品和对应的口味
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品批量删除
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品和对应的口味数据
     */
    DishDTO getByIdWithFlavor(Long id);

    /**
     * 修改菜品和对应的口味
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 菜品起售停售
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据分类id查询菜品
     */
    List<DishVO> list(Dish dish);


    List<DishVO> getlist(Long categoryId);
}
