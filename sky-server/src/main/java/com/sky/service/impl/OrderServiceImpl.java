package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO) {
        //填充用户id
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        //设置分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        //分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        //将Orders对象转换为OrderVO对象并返回
        List<OrderVO> orderVOList = getOrderVOList(page, false);
        return new PageResult(page.getTotal(), orderVOList);
    }


    @Override
    public OrderVO detail(Long orderId) {

        //先查询订单信息
        Orders orders = orderMapper.getById(orderId);

        //再查询订单明细
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

        //封装入OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;
    }

    @Override
    public void userCancelById(Long orderId) throws Exception {

        //根据id查询订单
        Orders order = orderMapper.getById(orderId);

        //校验订单是否存在
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态不为待付款或待接单时不允许用户退款
        if (order.getStatus() > Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

//        //订单状态为待接单时取消，需要进行退款
//        if (order.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    order.getNumber(), //商户订单号
//                    order.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额
//
//            //支付状态修改为退款
//            order.setPayStatus(Orders.REFUND);
//        }

        //更新订单状态、取消原因、取消时间并更新到数据库
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("用户取消");
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void repetition(Long orderId) {
        //查询当前用户id
        Long userId = BaseContext.getCurrentId();

        //根据id查询当前订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

        //将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetails.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            //将订单详情里的菜品信息复制到购物车对象中并补全缺少的信息
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        //将购物车对象批量添加到数据库
        shoppingCartList.forEach(x -> shoppingCartMapper.insert(x));
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置分页，并进行分页条件查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        //将Orders对象转换为OrderVO对象并返回
        List<OrderVO> orderVOList = getOrderVOList(page, true);
        return new PageResult(page.getTotal(), orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page, boolean isAdmin) {
        List<OrderVO> orderVOList = new ArrayList<>();

        //查询订单明细，并封装入OrderVO进行响应
        if (page != null && !page.isEmpty()) {
            for (Orders order : page) {
                //封装入OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);

                //查询订单明细
                Long orderId = order.getId(); //订单id
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);

                //如果是管理端的查询请求，就拼接菜品信息字符串(格式：菜品名字*菜品数量; )
                if (isAdmin) {
                    StringBuffer orderDishes = new StringBuffer();
                    for (OrderDetail orderDetail : orderDetailList) {
                        orderDishes.append(orderDetail.getName()).append('*').append(orderDetail.getNumber()).append("; ");
                        orderVO.setOrderDishes(orderDishes.toString());
                    }
                } else {
                    //如果是用户端的查询请求，就把订单明细封装入OrderVO
                    orderVO.setOrderDetailList(orderDetailList);
                }
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    @Override
    public OrderStatisticsVO statistics() {
        //根据状态，分别查询出待接单、待派送、派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatue(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatue(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatue(Orders.DELIVERY_IN_PROGRESS);

        //将查询出的数据封装到OrderStaticticsVO中并返回
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        //根据id查询订单
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());

        //只有订单存在且状态为待接单时才可以拒单
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Integer payStatue = orders.getPayStatus(); //支付状态
        //若用户已支付，则需要退款
//        if (payStatue.equals(Orders.PAID)) {
//            //调用微信支付退款接口
//            String refund = weChatPayUtil.refund(
//                    orders.getNumber(), //商户订单号
//                    orders.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额
//            log.info("申请退款：{}", refund);
//
//            //支付状态修改为退款
//            orders.setPayStatus(Orders.REFUND);
//        }

        //根据订单id更新订单状态、拒单原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


}
