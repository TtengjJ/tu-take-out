package com.sky.service.impl;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper  setmealMapper;

    //今日数据查询
    @Override
    public BusinessDataVO getBusinessData() {
        // 查询今日有效订单数
        Integer todayOrderCount = orderMapper.getValidOrderCountByDate(LocalDate.now());
        if (todayOrderCount == null) todayOrderCount = 0;

        // 查询今日营业额
        Double todayTurnover = orderMapper.getTurnoverByDate(LocalDate.now());
        if (todayTurnover == null) todayTurnover = 0.0;

        // 查询今日新增用户数
        Long newUsers = orderMapper.getNewUserByDate(LocalDate.now());
        Integer todayUserCount = newUsers != null ? newUsers.intValue() : 0;

        // 订单完成率
        Double orderCompletionRate = 0.0;
        Integer totalOrders = orderMapper.getOrderCountByDate(LocalDate.now());
        if (totalOrders != null && totalOrders > 0) {
            Integer validOrders = orderMapper.getValidOrderCountByDate(LocalDate.now());
            orderCompletionRate = validOrders != null ? validOrders / (double) totalOrders : 0.0;
        }

        // 平均客单价
        Double unitPrice = 0.0;
        if (todayOrderCount > 0) {
            unitPrice = todayTurnover / todayOrderCount;
        }


        //封装数据
        BusinessDataVO businessDataVO = new BusinessDataVO();
        businessDataVO.setTurnover(todayTurnover);
        businessDataVO.setValidOrderCount(todayOrderCount);
        businessDataVO.setOrderCompletionRate(orderCompletionRate);
        businessDataVO.setUnitPrice(unitPrice);
        businessDataVO.setNewUsers(todayUserCount);
        return businessDataVO;
    }

    // 获取订单概览数据
    @Override
    public OrderOverViewVO getOrderOverView() {
        // 使用 OrdersPageQueryDTO 作为查询条件
        OrdersPageQueryDTO dto = new OrdersPageQueryDTO();
        // 查询所有状态的订单
        List<Orders> ordersList = orderMapper.pageQuery(dto);

        // 创建统计对象
        OrderOverViewVO orderOverViewVO = new OrderOverViewVO();

        // 统计各个状态的订单数量
        orderOverViewVO.setWaitingOrders((int) ordersList.stream().filter(o -> o.getStatus() == 2).count());
        orderOverViewVO.setDeliveredOrders((int) ordersList.stream().filter(o -> o.getStatus() == 3).count());
        orderOverViewVO.setCompletedOrders((int) ordersList.stream().filter(o -> o.getStatus() == 5).count());
        orderOverViewVO.setCancelledOrders((int) ordersList.stream().filter(o -> o.getStatus() == 6).count());
        orderOverViewVO.setAllOrders(ordersList.size());
        return orderOverViewVO;
    }

    @Override
    public DishOverViewVO getDishOverView() {
        //查询起售数量
        Integer sold = dishMapper.getDishCountByStatus(1);
        //查询停售数量
        Integer discontinued = dishMapper.getDishCountByStatus(0);

        //返回结果
        return DishOverViewVO.builder()
                .sold(sold != null ? sold : 0)
                .discontinued(discontinued != null ? discontinued : 0)
                .build();
    }

    @Override
    public SetmealOverViewVO getSetmealOverView() {
        //查询起售数量
        Integer sold = setmealMapper.getSetmealCountByStatus(1);
        //查询停售数量
        Integer discontinued = setmealMapper.getSetmealCountByStatus(0);
        //返回结果
        return SetmealOverViewVO.builder()
                .sold(sold != null ? sold : 0)
                .discontinued(discontinued != null ? discontinued : 0)
                .build();

    }

    @Override
    public BusinessDataVO getBusinessData(LocalDate date) {
        // 查询指定日期的有效订单数
        Integer orderCount = orderMapper.getValidOrderCountByDate(date);
        if (orderCount == null) orderCount = 0;

        // 查询指定日期的营业额
        Double turnover = orderMapper.getTurnoverByDate(date);
        if (turnover == null) turnover = 0.0;

        // 查询指定日期的新增用户数
        Long newUsers = orderMapper.getNewUserByDate(date);
        Integer userCount = newUsers != null ? newUsers.intValue() : 0;

        // 订单完成率
        Double orderCompletionRate = 0.0;
        Integer totalOrders = orderMapper.getOrderCountByDate(date);
        if (totalOrders != null && totalOrders > 0) {
            Integer validOrders = orderMapper.getValidOrderCountByDate(date);
            orderCompletionRate = validOrders != null ? validOrders / (double) totalOrders : 0.0;
        }

        // 平均客单价
        Double unitPrice = 0.0;
        if (orderCount > 0) {
            unitPrice = turnover / orderCount;
        }

        // 封装数据
        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(orderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(userCount)
                .build();
    }

    public BusinessDataVO getBusinessDataRange(LocalDate beginDate, LocalDate endDate) {
        // 初始化累计值
        double totalTurnover = 0.0;
        int totalOrderCount = 0;
        int totalValidOrderCount = 0;
        double totalUnitPrice = 0.0;
        int totalNewUsers = 0;

        // 计算日期范围内的天数
        long daysBetween = ChronoUnit.DAYS.between(beginDate, endDate);

        // 循环遍历日期范围，并累加每日数据
        for (int i = 0; i <= daysBetween; i++) {
            LocalDate date = beginDate.plusDays(i);
            BusinessDataVO dailyData = getBusinessData(date);

            totalTurnover += dailyData.getTurnover();
            totalOrderCount += orderMapper.getOrderCountByDate(date) ;
            totalValidOrderCount += dailyData.getValidOrderCount();
            totalUnitPrice += dailyData.getUnitPrice() * dailyData.getValidOrderCount(); // 总客单价 = 每日客单价 * 每日有效订单数
            totalNewUsers += dailyData.getNewUsers();
        }

        // 计算平均客单价
        double averageUnitPrice = totalValidOrderCount > 0 ? totalUnitPrice / totalValidOrderCount : 0.0;

        // 计算订单完成率
        double orderCompletionRate = totalOrderCount > 0 ? totalValidOrderCount / (double) totalOrderCount : 0.0;

        // 返回累计数据
        return BusinessDataVO.builder()
                .turnover(totalTurnover)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(averageUnitPrice)
                .newUsers(totalNewUsers)
                .build();
    }
}
