package com.sky.service.impl;


import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断当前商品在购物车中是否存在
        // (userid和setmealid或dish（dish_flavor)查)
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //用户id
        Long userId= BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart>list= shoppingCartMapper.list(shoppingCart);

        //如果存在则修改数量
        if(!list.isEmpty()){
            ShoppingCart cart=list.getFirst();
            cart.setNumber(cart.getNumber()+1);//数量加1
            shoppingCartMapper.updateNumberById(cart);
        }
        else{
            //如果不存在则新增
            Long dishId= shoppingCartDTO.getDishId();
            //判断是菜品还是套餐
            if(dishId!=null){
                //菜品,查询
                Dish dish=dishMapper.getById(dishId);
                //除拷贝过的都要set
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }
            else{
                //套餐
                Long setmealId= shoppingCartDTO.getSetmealId();
                Setmeal setmeal= setmealMapper.selectById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    //查询购物车
    @Override
    public List<ShoppingCart> showShoppingCart() {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        shoppingCartMapper.list( shoppingCart);
        return shoppingCartMapper.list(shoppingCart);
    }

    @Override
    public void deleteShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.clearById(userId);
    }

    @Override
    public void deleteShoppingCartById(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart=new ShoppingCart();
         //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //将传入的数据转为购物车属性
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //根据传入数据查询购物车
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        //拿到当前要删除的商品
        ShoppingCart cart = shoppingCarts.getFirst();
        //判断当前商品数量
        if  ( cart.getNumber() > 1) {
            cart.setNumber(cart.getNumber() - 1);
            shoppingCartMapper.updateNumberById(cart);
        }
        else {
            shoppingCartMapper.deleteByDTO(shoppingCart);
        }
    }
}
