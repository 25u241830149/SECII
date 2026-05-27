import { apiPost } from './request'
import type { UploadResultDTO } from '@/types'

const uploadFile = (url: string, file: File, extra?: Record<string, string>) => {
  const formData = new FormData()
  formData.append('file', file)

  Object.entries(extra || {}).forEach(([key, value]) => {
    formData.append(key, value)
  })

  return apiPost<UploadResultDTO, FormData>(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function uploadAvatar(file: File) {
  return uploadFile('/upload/avatar', file)
}

export function uploadStudentCard(file: File, studentId: string) {
  return uploadFile('/upload/student-card', file, { studentId })
}

export function uploadTaskImage(file: File) {
  return uploadFile('/upload/task-image', file)
}
