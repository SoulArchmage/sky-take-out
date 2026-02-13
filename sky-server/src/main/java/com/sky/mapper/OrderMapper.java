package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderSubmitVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders (number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, remark, user_name, phone, address, consignee, cancel_reason, rejection_reason, cancel_time, estimated_delivery_time, delivery_status, delivery_time, pack_amount, tableware_number, tableware_status)" +
            "VALUES (#{number}, #{status}, #{userId}, #{addressBookId}, #{orderTime}, #{checkoutTime}, #{payMethod}, #{payStatus}, #{amount}, #{remark}, #{userName}, #{phone}, #{address}, #{consignee}, #{cancelReason}, #{rejectionReason}, #{cancelTime}, #{estimatedDeliveryTime}, #{deliveryStatus}, #{deliveryTime}, #{packAmount}, #{tablewareNumber}, #{tablewareStatus});")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Orders order);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{orderId}")
    Orders getById(Long orderId);

    @Update("update orders set status = #{status}, cancel_reason = #{cancelReason}, cancel_time = #{cancelTime}, pay_status = #{payStatus} where id = #{id}")
    void update(Orders order);

    @Select("select count(*) from orders where status = #{status}")
    Integer countStatue(Integer status);

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    void updateBatch(List<Orders> ordersList);


}
