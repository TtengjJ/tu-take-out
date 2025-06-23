package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);
    /**
     * 根据条件查询套餐
     */
    List<Setmeal> list(Setmeal setmeal);

    //根据分类id查询套餐列表
    List<DishItemVO> getById(Long id);


    //分页查询
    List<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    @AutoFill(OperationType.INSERT)
    void insertSetmealDish(SetmealDish dish);

    @AutoFill(OperationType.UPDATE)
    void updateById(Setmeal setmeal);

    void deleteSetmealDish(Long setmealId);

    @Select("select * from setmeal where id = #{id}")
    Setmeal selectById(Long id);

    List<SetmealDish> selectSetmealDish(Long id);


    void deleteBatchIds(List<Long> ids);

    void updateStatus(Integer status, Long id);
}
