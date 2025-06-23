package com.sky.mapper;


import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    //动态条件查询
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    //更新购物车
    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    //添加购物车数据
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) " +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id = #{userId}")
    void clearById(Long userId);

    void deleteByDTO(ShoppingCart shoppingCart);
}
