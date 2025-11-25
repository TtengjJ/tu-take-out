package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;
    //订单搜索
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索");
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    //各个状态的订单数量统计
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        log.info("各个状态的订单数量统计");
        OrderStatisticsVO statisticsVO = orderService.statistics();
        return Result.success(statisticsVO);
    }

    //查询订单详情
    @GetMapping("/details/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        log.info("查询订单详情，订单ID：{}", id);
        OrderVO orderSubmitVO = orderService.getOrderDetail(id);
        return Result.success(orderSubmitVO);
    }

    //取消订单
    @PutMapping("/cancel")
    @CacheEvict(value = {"orderCache", "orderDetailCache"}, allEntries = true)
    public Result<String> cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("取消订单，订单ID：{}，取消原因：{}", ordersCancelDTO.getId(), ordersCancelDTO.getCancelReason());
        orderService.cancelOrder(ordersCancelDTO.getId());
        return Result.success("订单取消成功");
    }

    //接单，ordersConfirmDTO只包含必要的字段：订单id和状态
    @PutMapping("/confirm")
    @CacheEvict(value = {"orderCache", "orderDetailCache"}, allEntries = true)
    public Result<String> confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单，订单ID：{}", ordersConfirmDTO.getId());
        orderService.confirmOrder(ordersConfirmDTO);
        return Result.success("订单接单成功");
    }

    //拒单
    @PutMapping("/rejection")
    @CacheEvict(value = {"orderCache", "orderDetailCache"}, allEntries = true)
    public Result<String> rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("拒单，订单ID：{}", ordersRejectionDTO.getId());
        orderService.conRejectOrder(ordersRejectionDTO);
        return Result.success("订单拒单成功");
    }

    //派送
    @PutMapping("/delivery/{id}")
    @CacheEvict(value = {"orderCache", "orderDetailCache"}, allEntries = true)
    public Result<String> deliveryOrder(@PathVariable Long id) {
        log.info("派送订单，订单ID：{}", id);
        orderService.deliveryOrder(id);
        return Result.success("订单派送成功");
    }

    //完成订单
    @PutMapping("/complete/{id}")
    @CacheEvict(value = {"orderCache", "orderDetailCache"}, allEntries = true)
    public Result<String> completeOrder(@PathVariable Long id) {
        log.info("完成订单，订单ID：{}", id);
        orderService.completeOrder(id);
        return Result.success("订单已完成");
    }




}
