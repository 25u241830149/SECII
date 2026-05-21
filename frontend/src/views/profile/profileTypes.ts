import type { Component } from 'vue'

import type { OrderStatus, TaskCategory, TaskStatus } from '@/types'

export interface ProfileStatCard {
  label: string
  value: string | number
  icon?: Component
  tone: 'blue' | 'green' | 'orange' | 'red' | 'purple'
  helper?: string
}

export interface ProfileTaskItem {
  id: number
  title: string
  category: TaskCategory
  image: string
  publisher: string
  location: string
  reward: number
  status: TaskStatus
  deadline: string
  createdAt: string
  collectedAt?: string
  applicants?: number
  capacity?: number
}

export interface ProfileOrderItem {
  id: number
  title: string
  category: TaskCategory
  image: string
  publisher: string
  location: string
  reward: number
  status: OrderStatus
  acceptedAt: string
  deadline: string
}

export interface CreditRecordItem {
  id: number
  time: string
  reason: string
  delta: number
  scoreAfter: number
}

