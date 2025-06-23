package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;


public interface SetmealService {
    List<Setmeal> list(Setmeal setmeal);

    List<DishItemVO> getById(Long id);


    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    SetmealVO save(SetmealDTO setmealDTO);

    SetmealVO update(SetmealDTO setmealDTO);

    SetmealVO dishgetById(Long id);

    void delete(List<Long> ids);

    void updateStatus(Integer status, Long id);
}
