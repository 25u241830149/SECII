package com.campushub.message.service;

import com.campushub.order.event.OrderCancelledEvent;
import com.campushub.order.event.OrderCompletedEvent;
import com.campushub.order.event.OrderConfirmedEvent;
import com.campushub.order.event.OrderCreatedEvent;
import com.campushub.review.event.ReviewCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final MessageService messageService;

    public NotificationService(MessageService messageService) {
        this.messageService = messageService;
    }

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        messageService.notifyUser(
                event.posterId(),
                MessageCodecs.TYPE_ORDER,
                "你的任务收到抢单",
                "订单 #" + event.orderId() + " 已创建，请确认接单。"
        );
        messageService.notifyUser(
                event.helperId(),
                MessageCodecs.TYPE_ORDER,
                "抢单成功",
                "你已成功抢单，等待发布者确认。"
        );
    }

    @EventListener
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        messageService.notifyUser(
                event.helperId(),
                MessageCodecs.TYPE_ORDER,
                "订单已确认",
                "发布者已确认订单 #" + event.orderId() + "，可以开始履约。"
        );
    }

    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {
        messageService.notifyUser(
                event.posterId(),
                MessageCodecs.TYPE_REVIEW,
                "订单已完成，请评价",
                "订单 #" + event.orderId() + " 已完成，请为接单同学留下评价。"
        );
        messageService.notifyUser(
                event.helperId(),
                MessageCodecs.TYPE_REVIEW,
                "订单已完成，请评价",
                "订单 #" + event.orderId() + " 已完成，请为发布者留下评价。"
        );
    }

    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        messageService.notifyIfDifferent(
                event.helperId(),
                event.posterId(),
                MessageCodecs.TYPE_ORDER,
                "订单已取消",
                "你参与的需求“" + event.taskTitle() + "”已由发布者取消。"
        );
    }

    @EventListener
    public void onReviewCreated(ReviewCreatedEvent event) {
        messageService.notifyIfDifferent(
                event.targetUserId(),
                event.reviewerId(),
                MessageCodecs.TYPE_REVIEW,
                "你收到一条新评价",
                "订单 #" + event.orderId() + " 收到 " + event.rating() + " 星评价。"
        );
    }
}
