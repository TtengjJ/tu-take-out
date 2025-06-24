package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.*;

import java.util.List;

public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);


    // 订单支付
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO getOrderDetail(Long id);

    void cancelOrder(Long id);

    void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);

    //各个状态的订单数量统计
    OrderStatisticsVO statistics();

    void conRejectOrder(OrdersRejectionDTO ordersRejectionDTO);

    void deliveryOrder(Long id);

    void completeOrder(Long id);
}
