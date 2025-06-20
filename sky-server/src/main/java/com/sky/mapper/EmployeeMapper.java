package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper // 确保添加了 @Mapper 注解
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    //新增员工
    void insert(Employee employee);

    Page<Employee> selectPage(EmployeePageQueryDTO employeePageQueryDTO);

    void updateStatus(Employee employee);

    void updateById(Employee employee);

    Employee selectById(Long id);
}