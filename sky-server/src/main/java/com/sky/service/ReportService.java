package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    TurnoverReportVO turnoverReport(LocalDate beginDate, LocalDate endDate);

    UserReportVO userReport(LocalDate begin, LocalDate end);

    OrderReportVO ordersReport(LocalDate begin, LocalDate end);

    SalesTop10ReportVO salesTop10Report(LocalDate begin, LocalDate end);
}
