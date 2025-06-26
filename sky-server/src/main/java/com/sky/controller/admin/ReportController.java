package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;


@RestController
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取营业额报表
     *
     */
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, LocalDate end) {
        // 调用服务层方法获取营业额报表数据
        log.info("获取营业额报表数据，开始日期：{}，结束日期：{}", begin, end);
        TurnoverReportVO turnoverReport = reportService.turnoverReport(begin, end);
        return Result.success(turnoverReport);

    }

    /**
     * 新增和总计用户数量
     *
     */
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, LocalDate end) {
        // 调用服务层方法获取用户报表数据
        log.info("获取用户报表数据，开始日期：{}，结束日期：{}", begin, end);
        UserReportVO userReport = reportService.userReport(begin,end);
        return Result.success(userReport);
    }

    /*
      订单统计
     */
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, LocalDate end){
        // 调用服务层方法获取订单报表数据
        log.info("获取订单报表数据，开始日期：{}，结束日期：{}", begin, end);
        OrderReportVO orderReport = reportService.ordersReport(begin, end);
        return Result.success(orderReport);
    }

    //销量排名统计
     @GetMapping("/top10")
    public Result<SalesTop10ReportVO> salesTop10Report(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin, LocalDate end){
        // 调用服务层方法获取销量排名报表数据
         log.info("获取销量排名报表数据，开始日期：{}，结束日期：{}", begin, end);
        SalesTop10ReportVO salesTop10Report = reportService.salesTop10Report(begin, end);
        return Result.success(salesTop10Report);
     }

        /**
        * 导出报表
         */
     @GetMapping("/export")
     public void export(HttpServletResponse response){
        log.info("导出报表数据");
         reportService.exportData(response);
     }
}
