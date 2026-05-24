package com.campushub.order.mapper;

import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.dto.OrderListDTO;
import com.campushub.order.entity.Order;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper {

    int insertOrder(Order order);

    Order selectOrderById(@Param("orderId") Long orderId);

    OrderDetailDTO selectOrderDetail(@Param("orderId") Long orderId);

    List<OrderListDTO> selectOrderList(
            @Param("userId") Long userId,
            @Param("role") String role,
            @Param("status") Integer status,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countOrderList(
            @Param("userId") Long userId,
            @Param("role") String role,
            @Param("status") Integer status
    );

    int updateStatusAndVersion(
            @Param("orderId") Long orderId,
            @Param("newStatus") Integer newStatus,
            @Param("currentStatus") Integer currentStatus,
            @Param("expectedVersion") Integer expectedVersion
    );

    long countCompletedByHelper(@Param("userId") Long userId);
}
