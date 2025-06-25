package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Autowired
    private WeChatPayUtil weChatPayUtil;

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

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 根据订单号查询订单
        String orderNumber = ordersPaymentDTO.getOrderNumber();

        //查询订单
        Orders orders = orderMapper.getByNumber(orderNumber);

        // 调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                orderNumber,  // 商户订单号
                orders.getAmount(), // 支付金额
                "苍穹外卖订单", // 商品描述
                orders.getUserId().toString() // 用户openid,需要从数据库获取
        );

        // 封装返回结果
        OrderPaymentVO vo = OrderPaymentVO.builder()
                .nonceStr(jsonObject.getString("nonceStr"))
                .paySign(jsonObject.getString("paySign"))
                .timeStamp(jsonObject.getString("timeStamp"))
                .signType(jsonObject.getString("signType"))
                .packageStr(jsonObject.getString("package"))
                .build();

        return vo;
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //查询订单
        Page<Orders> ordersPage = (Page<Orders>) orderMapper.pageQuery(ordersPageQueryDTO);
        List<Orders> ordersList = ordersPage.getResult();

        // 构建OrderVO列表，包含订单详情
        List<OrderVO> orderVOList = ordersList.stream().map(orders -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);

            // 查询订单详情
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
            orderVO.setOrderDetailList(orderDetails);

            return orderVO;
        }).toList();
        return new PageResult(ordersPage.getTotal(),orderVOList);
    }

    // 查询订单详情
    @Override
    public OrderVO getOrderDetail(Long id) {
        //查询订单
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            return null; //或抛出异常
        }
        //查询订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        //构建返回对象
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    //取消订单
    @Override
    public void cancelOrder(Long id) {
        //查询订单
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException("订单不存在");
        }

        //更新订单状态为已取消
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);
    }


    //各个状态的订单数量统计
    @Override
    public OrderStatisticsVO statistics() {

        // 使用 OrdersPageQueryDTO 作为查询条件
        OrdersPageQueryDTO dto = new OrdersPageQueryDTO();
        // 查询所有状态的订单
        List<Orders> ordersList = orderMapper.pageQuery(dto);

        // 创建统计对象
        OrderStatisticsVO statisticsVO = new OrderStatisticsVO();

        // 使用 stream 统计各状态订单数量
        statisticsVO.setToBeConfirmed((int) ordersList.stream()
                .filter(order -> Orders.TO_BE_CONFIRMED.equals(order.getStatus()))
                .count());

        statisticsVO.setConfirmed((int) ordersList.stream()
                .filter(order -> Orders.CONFIRMED.equals(order.getStatus()))
                .count());

        statisticsVO.setDeliveryInProgress((int) ordersList.stream()
                .filter(order -> Orders.DELIVERY_IN_PROGRESS.equals(order.getStatus()))
                .count());


        return statisticsVO;
    }

    //接单
    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);//订单不存在
        }

        //判断订单状态是否可以接单
        if (!Objects.equals(orders.getStatus(), Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);//订单状态不允许接单
        }

        //更新订单状态为已接单
        //new Orders对象,清晰表明只更新状态
        Orders ordersToUpdate = new Orders();
        ordersToUpdate.setId(ordersConfirmDTO.getId());
        ordersToUpdate.setStatus(Orders.CONFIRMED);
        orderMapper.update(ordersToUpdate);
    }

    @Override
    public void conRejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND); //订单不存在
        }
        //判断订单状态是否可以拒单
        if (!Objects.equals(orders.getStatus(), Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR); //订单状态不允许拒单
        }

        //更新订单状态为已拒单
        Orders ordersToUpdate = new Orders();
        ordersToUpdate.setId(ordersRejectionDTO.getId());
        ordersToUpdate.setStatus(Orders.CANCELLED); //将状态设置为已取消
        ordersToUpdate.setRemark(ordersRejectionDTO.getRejectionReason()); //设置拒单原因
        orderMapper.update(ordersToUpdate);
    }


    //派送订单
    @Override
    public void deliveryOrder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND); //订单不存在
        }
        //判断订单状态是否可以派送
        if (!Objects.equals(orders.getStatus(), Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR); //订单状态不允许派送
        }
        //更新订单状态为派送中
        Orders ordersToUpdate = new Orders();
        ordersToUpdate.setId(id);
        ordersToUpdate.setStatus(Orders.DELIVERY_IN_PROGRESS); //将状态设置为派送中
        orderMapper.update(ordersToUpdate);
    }

    @Override
    public void completeOrder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND); //订单不存在
        }
        //判断订单状态是否可以完成
        if (!Objects.equals(orders.getStatus(), Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR); //订单状态不允许完成
        }
        //更新订单状态为已完成
        Orders ordersToUpdate = new Orders();
        ordersToUpdate.setId(id);
        ordersToUpdate.setStatus(Orders.COMPLETED); //将状态设置为已完成
        orderMapper.update(ordersToUpdate);
    }

    //重复订单
    @Override
    public void repetition(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND); //订单不存在
        }

        // 查询原订单明细
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        //清空购物车
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.clearById(userId);


        // 将订单明细转换为购物车项目
        List<ShoppingCart> shoppingCartList = orderDetails.stream().map(detail -> {
            ShoppingCart cart = new ShoppingCart();
            // 复制基本信息
            BeanUtils.copyProperties(detail, cart);
            // 设置用户ID和创建时间
            cart.setUserId(BaseContext.getCurrentId());
            cart.setCreateTime(LocalDateTime.now());
            return cart;
        }).collect(Collectors.toList());

        // 批量插入购物车
        shoppingCartMapper.insertBatch(shoppingCartList);


    }

    //催单
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND); //订单不存在
        }
        //判断订单状态
        if (!Objects.equals(orders.getStatus(), Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR); //订单状态不允许提醒
        }

    }
}
