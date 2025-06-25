package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/workspace")
@Slf4j
public class WorkSpaceController {

    @Autowired
    private WorkSpaceService workSpaceService;

    /**
     * 获取工作台数据
     * 包括今日有效订单数、今日营业额、今日新增用户数、订单完成率、平均客单价
     */
    @GetMapping("/businessData")
    public Result<BusinessDataVO> getBusinessData() {
        log.info("获取工作台数据");
        BusinessDataVO businessData = workSpaceService.getBusinessData();
        return Result.success(businessData);
    }

    // 获取订单概览数据
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> getOrderOverView() {
        log.info("获取订单概览数据");
        OrderOverViewVO orderOverView = workSpaceService.getOrderOverView();
        return Result.success(orderOverView);
    }

    //菜品总览
     @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> getDishOverView() {
        log.info("获取菜品总览数据");
        DishOverViewVO dishOverView = workSpaceService.getDishOverView();
        return Result.success(dishOverView);
    }

    // 获取套餐总览数据
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> getSetmealOverView() {
        log.info("获取套餐总览数据");
        SetmealOverViewVO setmealOverView = workSpaceService.getSetmealOverView();
        return Result.success(setmealOverView);
    }


}
