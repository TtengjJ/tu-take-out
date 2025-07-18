package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.util.Objects;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 进行md5加密，然后再进行比对
        //password =DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (Objects.equals(employee.getStatus(), StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    //新增员工
    @Override
    public void save(EmployeeDTO employeeSaveDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeSaveDTO, employee);
        //设置状态为1
        employee.setStatus(StatusConstant.ENABLE);
        //设置密码为123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //保存
        employeeMapper.insert(employee);
    }


    //分页查询
    @Override
    public PageResult getPage(EmployeePageQueryDTO employeePageQueryDTO) {
        //1、查询分页数据
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<Employee> employeePage = employeeMapper.selectPage(employeePageQueryDTO);

        //2、封装返回结果
        return new PageResult(employeePage.getTotal(), employeePage.getResult());

    }

    @Override
    public void updateStatus(Integer status, Long id) {
        //构造器将需要的构造出来即可
        Employee employee= Employee.builder()
                .id(id)
                .status(status)
                //.updateTime(LocalDateTime.now())
                //.updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.updateStatus(employee);
    }

    @Override
    public void update(EmployeeDTO employeeUpdateDTO) {
        Employee employee = new Employee();
        //拷贝属性
        BeanUtils.copyProperties(employeeUpdateDTO, employee);
        employeeMapper.updateById(employee);
    }

    @Override
    public Employee getById(Long id) {
        return employeeMapper.selectById(id);
    }


}