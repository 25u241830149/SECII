import { apiGet } from './request'
import type { EntityId, OrderDTO, OrderStatus, PageQuery, PageResponse } from '@/types'

export interface OrderQuery extends PageQuery {
  userId: EntityId
  role?: 'poster' | 'helper'
  status?: OrderStatus
}

export function getOrders(query: OrderQuery) {
  return apiGet<PageResponse<OrderDTO>>('/orders', {
    params: query,
  })
}

