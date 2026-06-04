package com.campushub.order.mapper;

import com.campushub.order.dto.OrderDetailDTO;
import com.campushub.order.dto.OrderListDTO;
import com.campushub.order.dto.OrderStatsDTO;
import com.campushub.order.entity.Order;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper {

    int insertOrder(Order order);

    long countOrdersByTaskAndHelper(@Param("taskId") Long taskId, @Param("helperId") Long helperId);

    Order selectOrderById(@Param("orderId") Long orderId);

    List<Order> selectActiveOrdersByTask(@Param("taskId") Long taskId);

    OrderDetailDTO selectOrderDetail(@Param("orderId") Long orderId, @Param("viewerId") Long viewerId);

    List<OrderListDTO> selectOrderList(
            @Param("userId") Long userId,
            @Param("role") String role,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countOrderList(
            @Param("userId") Long userId,
            @Param("role") String role,
            @Param("status") String status
    );

    OrderStatsDTO selectOrderStats(@Param("userId") Long userId);

    int updateStatusAndVersion(
            @Param("orderId") Long orderId,
            @Param("newStatus") Integer newStatus,
            @Param("currentStatus") Integer currentStatus,
            @Param("expectedVersion") Integer expectedVersion
    );

    long countCompletedByHelper(@Param("userId") Long userId);

    int cancelOrdersByTask(@Param("taskId") Long taskId);

    int completeConfirmedOrdersByTask(@Param("taskId") Long taskId);

    int cancelPendingOrdersByTask(@Param("taskId") Long taskId);
}
