package com.sky.mapper;


import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    Orders getByNumber(String number);

    List<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    // 根据id查询订单详情
    Orders getById(Long id);

    void update(Orders orders);
}
