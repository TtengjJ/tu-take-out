package com.sky.controller.user;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     */
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车 -> 商品信息:{}",shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        log.info("查询购物车");
        List<ShoppingCart> shoppingCarts= shoppingCartService.showShoppingCart();
        return Result.success(shoppingCarts);
    }

    //清空购物车
    @DeleteMapping("/clean")
    public Result delete() {
        log.info("清空购物车");
        shoppingCartService.deleteShoppingCart();
        return Result.success();
    }

    //删除购物车中的商品
    @PostMapping("/sub")
    public Result delete(@RequestBody ShoppingCartDTO listshoppingCartDTO) {
        log.info("删除购物车中的商品 -> 商品信息:{}", listshoppingCartDTO);
        shoppingCartService.deleteShoppingCartById(listshoppingCartDTO);
        return Result.success();
    }
}
