package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理异常（地址簿，购物车）
        //地址簿
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //购物车
        Long userID=BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userID);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入一条数据
        Orders orders = new Orders();
        //订单属性拷贝
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);//待付款
        orders.setUserId(BaseContext.getCurrentId()); //用户id,从当前线程中获取
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//订单号,使用当前时间戳
        orders.setPayStatus(Orders.UN_PAID);//未支付
        orders.setPhone(addressBook.getPhone());//电话,DTO中不包含
        orders.setConsignee(addressBook.getConsignee());//收货人,addressBook中包含
        orderMapper.insert(orders);
        //向订单详情表插入多条数据
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(item, orderDetail);
            orderDetail.setOrderId(orders.getId());//订单id,从订单表xml中返回
            return orderDetail;
                }).toList();
        orderDetailMapper.insertBatch(orderDetailList);
        //清空购物车
        shoppingCartMapper.clearById(userID);
        //返回订单提交结果VO
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();
    }
}
