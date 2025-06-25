package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    private static List<LocalDate> getRangeDates(LocalDate beginDate, LocalDate endDate) {
        // 参数校验
        if (beginDate == null || endDate == null || endDate.isBefore(beginDate)) {
            throw new IllegalArgumentException("日期参数错误");
        }

        // 获取指定日期范围内的所有日期放到dateList
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate currentDate = beginDate;
        while (!currentDate.isAfter(endDate)) {
            dateList.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        return dateList;
    }

    //指定日期范围内的营业额报表
    @Override
    public TurnoverReportVO turnoverReport(LocalDate beginDate, LocalDate endDate) {
        //获取指定日期范围内的所有日期放到dateList
        List<LocalDate> dateList = getRangeDates(beginDate, endDate);


        //查询指定日期范围内的营业额
        //遍历dateList
        List<Double> turnoverList = dateList.stream()
                .map(date ->{
                    //查询当天营业额
                    Double turnover = orderMapper.getTurnoverByDate(date);
                    //如果查询结果为null，则设置为0.0
                    return turnover != null ? turnover : 0.0;
                })
                .toList();

        //构建返回对象
        // 取出dateList中的日期，转换为字符串列表
        return TurnoverReportVO.builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .turnoverList(turnoverList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .build();
    }



    @Override
    public UserReportVO userReport(LocalDate beginDate, LocalDate endDate) {
        //获取指定日期范围内的所有日期放到dateList
        List<LocalDate> dateList = getRangeDates(beginDate, endDate);
        // 获取每天的用户总量
        List<Integer> totalUserList = dateList.stream()
                .map(date -> {
                    Long total = orderMapper.getUserTotalByDate(date);
                    return total != null ? total.intValue() : 0;
                })
                .toList();

        // 获取每天的新增用户数
        List<Integer> newUserList = dateList.stream()
                .map(date -> {
                    Long newUser = orderMapper.getNewUserByDate(date);
                    return newUser != null ? newUser.intValue() : 0;
                })
                .toList();

        return UserReportVO.builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .totalUserList(totalUserList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .newUserList(newUserList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .build();

    }

    //订单统计
    @Override
    public OrderReportVO ordersReport(LocalDate begin, LocalDate end) {
        // 获取指定日期范围内的所有日期放到dateList
        List<LocalDate> dateList = getRangeDates(begin, end);
        // 获取每日订单数和有效订单数
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;

        for (LocalDate date : dateList) {
            Integer orderCount = orderMapper.getOrderCountByDate(date);
            Integer validCount = orderMapper.getValidOrderCountByDate(date);

            orderCount= orderCount != null ? orderCount : 0;
            validCount = validCount != null ? validCount : 0;

            orderCountList.add(orderCount);
            validOrderCountList.add(validCount);

            totalOrderCount += orderCount;
            validOrderCount += validCount;
        }

        Double orderCompletionRate = totalOrderCount == 0 ? 0.0
                : (double) validOrderCount / totalOrderCount * 100;

        return OrderReportVO.builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .orderCountList(orderCountList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .validOrderCountList(validOrderCountList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    //销量排名统计
    @Override
    public SalesTop10ReportVO salesTop10Report(LocalDate begin, LocalDate end) {
        // 不需要获取指定日期dateList
        // 获取销量排名前10的商品
        List<GoodsSalesDTO> salesItems = orderMapper.salesTop10Report(begin, end);

        //构建返回结果
        String nameList = salesItems.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.joining(","));

        String numberList = salesItems.stream()
                .map(item -> String.valueOf(item.getNumber()))
                .collect(Collectors.joining(","));

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }
}
