package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * ClassName: AutoFillAspect
 * Package: com.sky.aspect
 * Description:
 *
 * @Author pkq
 * @Create 2024/4/3 15:43
 * @Version 1.0
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Before("@annotation(com.sky.annotation.AutoFill)")
    public void autoFill(JoinPoint joinPoint) throws NoSuchFieldException, IllegalAccessException {
        //0.获取方法参数
        Object entity = joinPoint.getArgs()[0];
        //1.获取方法签名
        MethodSignature signature=(MethodSignature)joinPoint.getSignature();
        //2.根据方法签名获取方法Method对象
        Method method = signature.getMethod();
        //3.根据方法Method对象，获取方法身上的注解@AutoFill
        AutoFill annotation = method.getAnnotation(AutoFill.class);
        //4.根据注解的对象，获取它里面的value属性值
        OperationType value = annotation.value();
        //5.根据属性值来判断是添加还是更新，以此区分是填充4个值还是2个值
//        if (value==OperationType.INSERT){
//            //添加操作 4个属性
//            //5.1获取参数的字节码对象
//            Class clazz = arg.getClass();
//            //5.2获取属性
//            Field createTime = clazz.getDeclaredField("createTime");
//            Field updateTime = clazz.getDeclaredField("updateTime");
//            Field createUser = clazz.getDeclaredField("createUser");
//            Field updateUser = clazz.getDeclaredField("updateUser");
//            //暴力反射
//            createTime.setAccessible(true);
//            updateTime.setAccessible(true);
//            createUser.setAccessible(true);
//            updateUser.setAccessible(true);
//            //给属性赋值
//            createTime.set(arg, LocalDateTime.now());
//            updateTime.set(arg,LocalDateTime.now());
//            createUser.set(arg, BaseContext.getCurrentId());
//            updateUser.set(arg,BaseContext.getCurrentId());
//
//
//        }else{
//            //修改操作，2个属性值
//            //5.1获取参数的字节码对象
//            Class clazz = arg.getClass();
//            //5.2获取属性
//            Field updateTime = clazz.getDeclaredField("updateTime");
//            Field updateUser = clazz.getDeclaredField("updateUser");
//            //暴力反射
//            updateTime.setAccessible(true);
//            updateUser.setAccessible(true);
//            //给属性赋值
//            updateTime.set(arg,LocalDateTime.now());
//            updateUser.set(arg,BaseContext.getCurrentId());
//        }
//        //6.通过反射来获取对应的4个属性或者2个属性
//
//        //7.通过反射直接给他们赋值

        if(value==OperationType.INSERT){
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                setCreateTime.invoke(entity, LocalDateTime.now());
                setUpdateTime.invoke(entity, LocalDateTime.now());
                setCreateUser.invoke(entity, BaseContext.getCurrentId());
                setUpdateUser.invoke(entity, BaseContext.getCurrentId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(value==OperationType.UPDATE){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                setUpdateTime.invoke(entity, LocalDateTime.now());
                setUpdateUser.invoke(entity, BaseContext.getCurrentId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }




    }
}


