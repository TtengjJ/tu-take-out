package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    // 添加购物车
    void add(ShoppingCartDTO shoppingCartDTO);

    //查看购物车
    List<ShoppingCart> showShoppingCart();

    void deleteShoppingCart();

    void deleteShoppingCartById(ShoppingCartDTO shoppingCartDTO);
}
