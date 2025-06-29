package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDate;

public interface WorkSpaceService {
    BusinessDataVO getBusinessData();

    OrderOverViewVO getOrderOverView();

    DishOverViewVO getDishOverView();

    SetmealOverViewVO getSetmealOverView();

    // 新增支持日期参数的方法
    BusinessDataVO getBusinessData(LocalDate date);

    BusinessDataVO getBusinessDataRange(LocalDate beginDate, LocalDate endDate);
}
