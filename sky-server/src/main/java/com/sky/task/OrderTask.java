package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;


    //处理超时订单
    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
    public void handleTimeoutOrders() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        //查询订单状态status=1（待付款）且下单时间超过15分钟的订单
        //将这些订单的状态改为6（已取消）
        orderMapper.getByStatusAndTimeout(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15))
                .forEach(order -> {
                    order.setStatus(Orders.CANCELLED); // 设置状态为已取消
                    order.setCancelReason("订单超时未支付，已自动取消");
                    order.setCancelTime(LocalDateTime.now()); // 设置取消时间为当前时间
                    orderMapper.update(order);
                });
    }
    //处理派送中订单
    //每天凌晨一点触发
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void handleDeliveryOrders() {
        log.info("定时处理派送中订单：{}", LocalDateTime.now());
        //查询订单状态status=4（派送中）且派送时间超过30分钟的订单
        //将这些订单的状态改为5（已完成）
        orderMapper.getByStatusAndTimeout(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusMinutes(50))
                .forEach(order -> {
                    order.setStatus(Orders.COMPLETED); // 设置状态为已完成
                    order.setDeliveryTime(LocalDateTime.now()); // 设置派送完成时间为当前时间
                    orderMapper.update(order);
                });
    }
}
