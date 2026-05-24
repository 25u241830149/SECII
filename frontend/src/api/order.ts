import { apiGet, apiPost } from './request'
import type {
  EntityId,
  GrabOrderRequest,
  OrderDetailDTO,
  OrderListDTO,
  OrderListQuery,
  PageResponse,
} from '@/types'

export function grabOrder(payload: GrabOrderRequest) {
  return apiPost<OrderDetailDTO, GrabOrderRequest>('/orders/grab', payload)
}

export function confirmOrder(orderId: EntityId) {
  return apiPost<OrderDetailDTO>(`/orders/${orderId}/confirm`)
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

export function getOrderDetail(orderId: EntityId) {
  return apiGet<OrderDetailDTO>(`/orders/${orderId}`)
}
