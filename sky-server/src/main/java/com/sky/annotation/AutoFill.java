package com.sky.annotation;

/**
 * ClassName: AutoFill
 * Package: com.sky.annotation
 * Description:
 *
 * @Author pkq
 * @Create 2024/4/3 15:34
 * @Version 1.0
 */

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行功能字段自动填充处理
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //数据库操作类型：UPDATE INSERT
    // value()是一个属性，注解的属性本质上是方法，因此必须使用括号 () 来定义
    OperationType value(); // default OperationType.INSERT;
}
