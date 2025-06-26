package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WorkSpaceService workSpaceService;

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

    //导出报表数据
    @Override
    public void exportData(HttpServletResponse response) {
        //查询数据库，30天内的运营数据
        LocalDate beginDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().minusDays(1);//昨天

        //概览数据
        BusinessDataVO businessDataVO= workSpaceService.getBusinessData(endDate);

        //写入excel文件
        //基于模版创建工作簿
        InputStream in= this.getClass().getClassLoader().getResourceAsStream("template/business_report_template.xlsx");
        try {
            XSSFWorkbook excel = null;
            if (in != null) {
                excel = new XSSFWorkbook(in);
            }
            else
            {
                log.error("模板文件未找到");
                throw new RuntimeException("模板文件未找到");
            }
            //填充数据
            if (excel != null) {
                //获取sheet标签页
                XSSFSheet sheet = excel.getSheet("Sheet1");

                //填充概览数据
                sheet.getRow(1).getCell(1).setCellValue("时间：" + beginDate + " 至 " + endDate);
                XSSFRow row = sheet.getRow(3);
                row.getCell(2).setCellValue(businessDataVO.getTurnover()); //营业额
                row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate() * 100 + "%"); //订单完成率
                row.getCell(6).setCellValue(businessDataVO.getNewUsers()); //新增用户

                row = sheet.getRow(4);
                //有效订单数
                row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
                //平均客单价
                row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

                //明细数据
                for(int i = 0; i < 30; i++) {
                    LocalDate date = beginDate.plusDays(i);
                    //获取当天营业数据
                    BusinessDataVO dailyData = workSpaceService.getBusinessData(LocalDate.now().minusDays(i + 30));
                    //填充，从第八行
                    row= sheet.getRow(7 + i);
                    row.getCell(1).setCellValue(date.toString()); //日期
                    row.getCell(2).setCellValue(dailyData.getTurnover()); //营业额
                    row.getCell(3).setCellValue(dailyData.getValidOrderCount()); //有效订单数
                    row.getCell(4).setCellValue(dailyData.getOrderCompletionRate() * 100 + "%"); //订单完成率
                    row.getCell(5).setCellValue(dailyData.getUnitPrice()); //平均客单价
                    row.getCell(6).setCellValue(dailyData.getNewUsers()); //新增用户

                }
                //通过输出流下载文件到浏览器
                ServletOutputStream out = response.getOutputStream();
                excel.write(out);

                //关闭资源
                out.close();
                excel.close();


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
