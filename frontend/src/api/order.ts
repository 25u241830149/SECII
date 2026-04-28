import { request } from '@/api/request'
import type { GrabOrderPayload, OrderDetail, OrderRole, OrderItem, PageResponse } from '@/types'

interface OrderListParams {
  page?: number
  size?: number
  userId?: number
  role?: OrderRole
  status?: string
}

export function grabOrder(data: GrabOrderPayload) {
  return request.post<null>('/orders/grab', data)
}

export function getOrderDetail(orderId: number) {
  return request.get<OrderDetail>(`/orders/${orderId}`)
}

export function confirmOrder(orderId: number) {
  return request.post<null>(`/orders/${orderId}/confirm`)
}

export function completeOrder(orderId: number) {
  return request.post<null>(`/orders/${orderId}/complete`)
}

export function cancelOrder(orderId: number, userId: number) {
  return request.post<null>(`/orders/${orderId}/cancel`, null, { params: { userId } })
}

export function getOrderList(params: OrderListParams) {
  return request.get<PageResponse<OrderItem>>('/orders', { params })
}