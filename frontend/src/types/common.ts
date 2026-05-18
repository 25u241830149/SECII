export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  success?: boolean
  timestamp?: string
  requestId?: string
}

export interface PageResponse<T = unknown> {
  records: T[]
  total: number
  page: number
  size: number
  pages?: number
  hasNext?: boolean
  hasPrevious?: boolean
}

export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  sort?: string
}

export interface SelectOption<T extends string | number = string> {
  label: string
  value: T
  disabled?: boolean
}

export type EntityId = number
export type Nullable<T> = T | null
