import React from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { Login } from './pages/Login.jsx';
import { ChangePassword } from './pages/ChangePassword.jsx';
import { AppLayout } from './components/layout/AppLayout.jsx';
import { ProtectedRoute } from './auth/ProtectedRoute.jsx';
import { AdminDashboard } from './pages/admin/Dashboard.jsx';
import { AdminStudents } from './pages/admin/Students.jsx';
import { AdminTeachers } from './pages/admin/Teachers.jsx';
import { AdminCourses } from './pages/admin/Courses.jsx';
import { AdminEnrollments } from './pages/admin/Enrollments.jsx';
import { AdminFees } from './pages/admin/Fees.jsx';
import { AdminReports } from './pages/admin/Reports.jsx';
import { AdminTranscript } from './pages/admin/Transcript.jsx';
import { AdminAuditLogs } from './pages/admin/AuditLogs.jsx';
import { StudentDashboard } from './pages/student/Dashboard.jsx';
import { StudentCourses } from './pages/student/Courses.jsx';
import { StudentTranscript } from './pages/student/Transcript.jsx';
import { StudentFees } from './pages/student/Fees.jsx';
import { useAuth } from './auth/AuthContext.jsx';

function RoleRoot() {
  const { user, isAuthenticated, bootstrapping } = useAuth();
  if (bootstrapping) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role === 'STUDENT') return <Navigate to="/student" replace />;
  return <Navigate to="/admin" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<RoleRoot />} />

      <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
        <Route path="/account/password" element={<ChangePassword />} />

        <Route path="/admin" element={
          <ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
        } />
        <Route path="/admin/students" element={
          <ProtectedRoute roles={['ADMIN']}><AdminStudents /></ProtectedRoute>
        } />
        <Route path="/admin/teachers" element={
          <ProtectedRoute roles={['ADMIN']}><AdminTeachers /></ProtectedRoute>
        } />
        <Route path="/admin/courses" element={
          <ProtectedRoute roles={['ADMIN']}><AdminCourses /></ProtectedRoute>
        } />
        <Route path="/admin/enrollments" element={
          <ProtectedRoute roles={['ADMIN']}><AdminEnrollments /></ProtectedRoute>
        } />
        <Route path="/admin/fees" element={
          <ProtectedRoute roles={['ADMIN']}><AdminFees /></ProtectedRoute>
        } />
        <Route path="/admin/reports" element={
          <ProtectedRoute roles={['ADMIN']}><AdminReports /></ProtectedRoute>
        } />
        <Route path="/admin/transcript/:studentId" element={
          <ProtectedRoute roles={['ADMIN']}><AdminTranscript /></ProtectedRoute>
        } />
        <Route path="/admin/audit" element={
          <ProtectedRoute roles={['ADMIN']}><AdminAuditLogs /></ProtectedRoute>
        } />

        <Route path="/student" element={
          <ProtectedRoute roles={['STUDENT']}><StudentDashboard /></ProtectedRoute>
        } />
        <Route path="/student/courses" element={
          <ProtectedRoute roles={['STUDENT']}><StudentCourses /></ProtectedRoute>
        } />
        <Route path="/student/transcript" element={
          <ProtectedRoute roles={['STUDENT']}><StudentTranscript /></ProtectedRoute>
        } />
        <Route path="/student/fees" element={
          <ProtectedRoute roles={['STUDENT']}><StudentFees /></ProtectedRoute>
        } />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
