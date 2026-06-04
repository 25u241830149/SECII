package com.campushub.message.service;

import com.campushub.order.event.OrderCancelledEvent;
import com.campushub.order.event.OrderCompletedEvent;
import com.campushub.order.event.OrderConfirmedEvent;
import com.campushub.order.event.OrderCreatedEvent;
import com.campushub.order.event.OrderRejectedEvent;
import com.campushub.order.event.OrderAbandonedEvent;
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
        if (event.teamUp()) {
            messageService.notifyUser(
                    event.posterId(),
                    MessageCodecs.TYPE_ORDER,
                    "收到新的组队申请",
                    "需求“" + event.taskTitle() + "”收到新的加入申请，请及时处理。"
            );
            messageService.notifyUser(
                    event.helperId(),
                    MessageCodecs.TYPE_ORDER,
                    "组队申请已提交",
                    "你已提交加入“" + event.taskTitle() + "”的申请，等待发起人确认。"
            );
            return;
        }
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
        if (event.teamUp()) {
            messageService.notifyUser(
                    event.helperId(),
                    MessageCodecs.TYPE_ORDER,
                    "组队申请已通过",
                    "发起人已通过你加入“" + event.taskTitle() + "”的申请。"
            );
            return;
        }
        messageService.notifyUser(
                event.helperId(),
                MessageCodecs.TYPE_ORDER,
                "订单已确认",
                "发布者已确认订单 #" + event.orderId() + "，可以开始履约。"
        );
    }

    @EventListener
    public void onOrderRejected(OrderRejectedEvent event) {
        messageService.notifyUser(
                event.helperId(),
                MessageCodecs.TYPE_ORDER,
                event.teamUp() ? "组队申请未通过" : "接单申请未通过",
                event.teamUp()
                        ? "你加入“" + event.taskTitle() + "”的申请未通过。"
                        : "你对需求“" + event.taskTitle() + "”的接单申请已被发布者拒绝。"
        );
    }

    @EventListener
    public void onOrderAbandoned(OrderAbandonedEvent event) {
        messageService.notifyUser(
                event.posterId(),
                MessageCodecs.TYPE_ORDER,
                event.teamUp() ? "有同学退出组队" : "接单者已放弃",
                event.teamUp()
                        ? "需求“" + event.taskTitle() + "”的一名同学已撤回申请或退出队伍，组队进度已更新。"
                        : "需求“" + event.taskTitle() + "”的接单者已放弃，任务已重新开放。"
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
                "需求已取消",
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
