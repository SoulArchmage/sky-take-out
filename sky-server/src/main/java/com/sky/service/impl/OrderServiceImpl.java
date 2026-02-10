package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    public void reminder(Long id) {
        //orderMapper.reminder(id);
    }

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 1、处理业务异常
        // 1.1、校验地址簿是否存在
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new IllegalArgumentException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 1.2、校验购物车是否为空
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(addressBook.getUserId())
                .build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new IllegalArgumentException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // 2、向订单表插入 1 条数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        // 除了 DTO 中包含的字段，其他部分字段需要手动设置
        // 如取消原因等不需要设置，因为当前业务是下单，而不是取消
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(addressBook.getUserId());
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now());
        orderMapper.insert(order);
        // 3、向订单明细表插入 n 条数据
        // 使用 ArrayList 来存储订单明细，避免频繁的扩容操作
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart shoppingCartItem : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCartItem, orderDetail);
            // 设置订单明细的订单 ID
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }
        // 批量插入订单明细
        orderDetailMapper.insertBatch(orderDetailList);
        // 4、清空购物车数据
        shoppingCartMapper.delete(ShoppingCart.builder().userId(addressBook.getUserId()).build());
        // 5、返回 VO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
        return orderSubmitVO;
    }
}
