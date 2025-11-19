package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Objects;


//自定义切面，实现公共字段填充
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //切入点
    //在执行增改方法时，自动填充公共字段
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autofillPointCut() {}

    //前置通知
    @Before("autofillPointCut()")
    public void  autofill(JoinPoint joinPoint) {
        log.info("执行了公共字段填充");
        //获取方法签名,获取方法参数，转型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取方法上的注解,获取数据库操作类型
        OperationType operationType = signature.getMethod().getAnnotation(AutoFill.class).value();
        //获取方法参数
        Object[] args = joinPoint.getArgs();
        //获取实体类对象
        Object entity = args[0];

        //获取当前用户id
         Long userId = BaseContext.getCurrentId();
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        //根据操作类型，填充公共字段
        if (Objects.requireNonNull(operationType) == OperationType.INSERT) {
            try {
                //反射机制获取实体类中的特定方法
                Method setCreateTime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //invoke反射：运行时动态调用实体对象的方法
                //调用setCreateTime方法，填充创建时间
                setCreateTime.invoke(entity, now);
                //调用setCreateUser方法，填充创建人
                setCreateUser.invoke(entity, userId);
                //调用setUpdateTime方法，填充更新时间
                setUpdateTime.invoke(entity, now);
                //调用setUpdateUser方法，填充更新人
                setUpdateUser.invoke(entity, userId);
            } catch (Exception e) {
                log.error("自动填充字段时发生异常", e);
            }
        }
        else if (OperationType.UPDATE.equals(operationType)) {
            try {
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //调用setUpdateTime方法，填充更新时间
                setUpdateTime.invoke(entity, now);
                //调用setUpdateUser方法，填充更新人
                setUpdateUser.invoke(entity, userId);
            } catch (Exception e) {
                log.error("自动填充字段时发生异常", e);
            }

        }

    }
}
