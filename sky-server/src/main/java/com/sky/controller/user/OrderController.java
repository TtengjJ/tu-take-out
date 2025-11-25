package com.sky.controller.user;


import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;


@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;


    //用户下单
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单，订单信息：{}",ordersSubmitDTO);
        return Result.success(orderService.submit(ordersSubmitDTO));
    }

    // 订单支付
    @PutMapping("/payment")
    @CacheEvict(value = {"orderCache", "orderDetailCache"}, allEntries = true)
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        return Result.success(orderPaymentVO);
    }

    //查询历史订单
    @GetMapping("/historyOrders")
    @Cacheable(value = "orderCache", key = " 'orderHistory:' + #ordersPageQueryDTO.userId", unless = "#result.data == null")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询历史订单：{}", ordersPageQueryDTO);
        // 设置当前用户ID
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    //查询订单详情，数据封装不对可能导致页面没有信息
    @GetMapping("/orderDetail/{id}")
    @Cacheable(value = "orderDetailCache", key = "#id")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        log.info("查询订单详情，订单ID：{}", id);
        OrderVO orderVO= orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    //取消订单
    @PutMapping("/cancel/{id}")
    @CacheEvict(value = {"orderCache", "orderDetailCache"}, key = "#id")
    public Result<String> cancelOrder(@PathVariable Long id) {
        log.info("取消订单，订单ID：{}", id);
        // 调用服务层取消订单
        orderService.cancelOrder(id);
        return Result.success("订单已取消");
    }

    //再来一单
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id) {
        log.info("再来一单，订单ID：{}", id);
        orderService.repetition(id);
        return Result.success();
    }

    //催单
    @GetMapping("/reminder/{id}")
    @CacheEvict(value = "orderCache", allEntries = true)
    public Result reminder(@PathVariable Long id) {
        log.info("用户催单，订单ID：{}", id);
        orderService.reminder(id);
        return Result.success();
    }


}
