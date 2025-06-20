package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeDTO employeeSaveDTO);

    PageResult getPage(EmployeePageQueryDTO employeePageQueryDTO);

    void updateStatus(Integer status, Long id);

    void update(EmployeeDTO employeeUpdateDTO);

    Employee getById(Long id);
}
