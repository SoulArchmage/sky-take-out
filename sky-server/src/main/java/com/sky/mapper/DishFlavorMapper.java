package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * ClassName: DishFlavorMapper
 * Package: com.sky.mapper
 * Description:
 *
 * @Author pkq
 * @Create 2024-04-04 14:04
 * @Version 1.0
 */
@Mapper
public interface DishFlavorMapper {
    @Insert("insert into dish_flavor(id, dish_id, name, value) values (default,#{dishId},#{name},#{value})")
    void insert(DishFlavor dishFlavor);
}

