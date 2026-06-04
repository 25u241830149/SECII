import { apiGet, apiPost } from './request'
import type {
  EntityId,
  GrabOrderRequest,
  OrderDetailDTO,
  OrderListDTO,
  OrderListQuery,
  OrderStatsDTO,
  PageResponse,
} from '@/types'

export function grabOrder(payload: GrabOrderRequest) {
  return apiPost<OrderDetailDTO, GrabOrderRequest>('/orders/grab', payload)
}

export function confirmOrder(orderId: EntityId) {
  return apiPost<OrderDetailDTO>(`/orders/${orderId}/confirm`)
}

export function rejectOrder(orderId: EntityId) {
  return apiPost<OrderDetailDTO>(`/orders/${orderId}/reject`)
}

export function abandonOrder(orderId: EntityId) {
  return apiPost<OrderDetailDTO>(`/orders/${orderId}/abandon`)
}

export function completeOrder(orderId: EntityId) {
  return apiPost<OrderDetailDTO>(`/orders/${orderId}/complete`)
}

export function cancelOrder(orderId: EntityId) {
  return apiPost<OrderDetailDTO>(`/orders/${orderId}/cancel`)
}

export function getOrders(query: OrderListQuery) {
  return apiGet<PageResponse<OrderListDTO>>('/orders', { params: query })
}

export function getOrderStats(userId: EntityId) {
  return apiGet<OrderStatsDTO>('/orders/summary/stats', { params: { userId } })
}

export function getOrderDetail(orderId: EntityId) {
  return apiGet<OrderDetailDTO>(`/orders/${orderId}`)
}
