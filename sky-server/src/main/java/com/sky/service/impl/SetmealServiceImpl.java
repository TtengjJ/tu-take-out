package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    //根据套餐id查询包含的菜品
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }

    //根据分类id查询套餐列表
    @Override
    public List<DishItemVO> getById(Long id) {
        return setmealMapper.getById(id);
    }


    //admin分页查询
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //分页
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        //查询
        Page<SetmealVO> setmealVOPage = (Page<SetmealVO>) setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(setmealVOPage.getTotal(), setmealVOPage);
    }

    @Override
    public SetmealVO save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        //获取生成的套餐id
        Long setmealId = setmeal.getId();

        // 插入套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(dish -> {
                dish.setSetmealId(setmealId);
                setmealMapper.insertSetmealDish(dish);
            });
        }
        //返回VO对象
        SetmealVO setmealVO= new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    @Override
    public SetmealVO update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.updateById(setmeal);
        //获取生成的套餐id
        Long setmealId = setmeal.getId();
        //删除原有的套餐和菜品的关联关系
        setmealMapper.deleteSetmealDish(setmealId);
        // 插入套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(dish -> {
                dish.setSetmealId(setmealId);
                setmealMapper.insertSetmealDish(dish);
            });
        }
        //返回VO对象
        SetmealVO setmealVO= new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    //根据id查套餐信息
    @Override
    public SetmealVO dishgetById(Long id) {
        SetmealVO setmealVO = new SetmealVO();
        Setmeal setmeal = setmealMapper.selectById(id);
        BeanUtils.copyProperties(setmeal,setmealVO);
        //查询套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealMapper.selectSetmealDish(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public void delete(List<Long> ids) {
        //判断套餐是否启用
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.selectById(id);
            if (setmeal.getStatus() == 1) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除套餐
        setmealMapper.deleteBatchIds(ids);
        //循环删除套餐和菜品的关联关系
        for (Long id : ids) {
            setmealMapper.deleteSetmealDish(id);
        }
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        setmealMapper.updateStatus(status,id);
    }
}
