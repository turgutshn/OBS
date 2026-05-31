import { api } from './client.js';

export const authApi = {
  login: (username, password) => api.post('/auth/login', { username, password }).then(r => r.data),
  logout: (refreshToken) => api.post('/auth/logout', { refreshToken }).then(r => r.data),
  me: () => api.get('/auth/me').then(r => r.data),
  changePassword: (currentPassword, newPassword) =>
    api.post('/auth/change-password', { currentPassword, newPassword }).then(r => r.data),
};

export const studentsApi = {
  list: (params) => api.get('/admin/students', { params }).then(r => r.data),
  get: (id) => api.get(`/admin/students/${id}`).then(r => r.data),
  create: (payload) => api.post('/admin/students', payload).then(r => r.data),
  update: (id, payload) => api.put(`/admin/students/${id}`, payload).then(r => r.data),
  delete: (id) => api.delete(`/admin/students/${id}`).then(r => r.data),
  enrollments: (id) => api.get(`/admin/students/${id}/enrollments`).then(r => r.data),
  fees: (id) => api.get(`/admin/students/${id}/fees`).then(r => r.data),
  activate: (id) => api.post(`/admin/students/${id}/activate`).then(r => r.data),
  deactivate: (id) => api.post(`/admin/students/${id}/deactivate`).then(r => r.data),
};

export const teachersApi = {
  list: (params) => api.get('/admin/teachers', { params }).then(r => r.data),
  get: (id) => api.get(`/admin/teachers/${id}`).then(r => r.data),
  create: (payload) => api.post('/admin/teachers', payload).then(r => r.data),
  update: (id, payload) => api.put(`/admin/teachers/${id}`, payload).then(r => r.data),
  delete: (id) => api.delete(`/admin/teachers/${id}`).then(r => r.data),
};

export const coursesApi = {
  list: (params) => api.get('/admin/courses', { params }).then(r => r.data),
  get: (id) => api.get(`/admin/courses/${id}`).then(r => r.data),
  create: (payload) => api.post('/admin/courses', payload).then(r => r.data),
  update: (id, payload) => api.put(`/admin/courses/${id}`, payload).then(r => r.data),
  delete: (id) => api.delete(`/admin/courses/${id}`).then(r => r.data),
  assignTeacher: (id, teacherId) =>
    api.post(`/admin/courses/${id}/assign-teacher/${teacherId}`).then(r => r.data),
  enrollments: (id) => api.get(`/admin/courses/${id}/enrollments`).then(r => r.data),
};

export const enrollmentsApi = {
  enroll: (payload) => api.post('/admin/enrollments', payload).then(r => r.data),
  drop: (id) => api.post(`/admin/enrollments/${id}/drop`).then(r => r.data),
  delete: (id) => api.delete(`/admin/enrollments/${id}`).then(r => r.data),
  grade: (id, payload) => api.put(`/teacher/enrollments/${id}/grade`, payload).then(r => r.data),
  mine: () => api.get('/student/enrollments').then(r => r.data),
};

export const feesApi = {
  list: (params) => api.get('/admin/fees', { params }).then(r => r.data),
  assess: (payload) => api.post('/admin/fees', payload).then(r => r.data),
  pay: (id, payload) => api.post(`/admin/fees/${id}/payments`, payload).then(r => r.data),
  waive: (id) => api.post(`/admin/fees/${id}/waive`).then(r => r.data),
  delete: (id) => api.delete(`/admin/fees/${id}`).then(r => r.data),
  mine: () => api.get('/student/fees').then(r => r.data),
};

export const reportsApi = {
  summary: () => api.get('/admin/reports/summary').then(r => r.data),
  transcript: (studentId) => api.get(`/admin/reports/transcript/${studentId}`).then(r => r.data),
  myTranscript: () => api.get('/student/transcript').then(r => r.data),
  auditLogs: (params) => api.get('/admin/audit-logs', { params }).then(r => r.data),
};
