import React from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  GraduationCap,
  BookOpen,
  ClipboardList,
  Wallet,
  FileBarChart,
  ScrollText,
  LogOut,
  KeyRound,
  UserCircle2,
} from 'lucide-react';
import { useAuth } from '../../auth/AuthContext.jsx';
import { cn } from '../../lib/utils.js';

const ADMIN_NAV = [
  { to: '/admin', label: 'Genel Bakış', icon: LayoutDashboard, end: true },
  { to: '/admin/students', label: 'Öğrenciler', icon: GraduationCap },
  { to: '/admin/teachers', label: 'Öğretmenler', icon: Users },
  { to: '/admin/courses', label: 'Dersler', icon: BookOpen },
  { to: '/admin/enrollments', label: 'Ders Atama / Kayıt', icon: ClipboardList },
  { to: '/admin/fees', label: 'Katkı Payı', icon: Wallet },
  { to: '/admin/reports', label: 'Raporlar', icon: FileBarChart },
  { to: '/admin/audit', label: 'Denetim Kayıtları', icon: ScrollText },
];

const STUDENT_NAV = [
  { to: '/student', label: 'Panel', icon: LayoutDashboard, end: true },
  { to: '/student/courses', label: 'Derslerim', icon: BookOpen },
  { to: '/student/transcript', label: 'Transkript', icon: ScrollText },
  { to: '/student/fees', label: 'Katkı Payı', icon: Wallet },
];

export function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const nav = user?.role === 'STUDENT' ? STUDENT_NAV : ADMIN_NAV;
  const portalTitle = user?.role === 'STUDENT' ? 'Öğrenci Paneli' : 'Yönetim Paneli';

  return (
    <div className="flex h-screen bg-slate-50">
      <aside className="hidden w-64 flex-col border-r border-slate-200 bg-white md:flex">
        <div className="flex h-16 items-center gap-2 border-b border-slate-100 px-6">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-white">
            T
          </div>
          <div>
            <div className="text-sm font-semibold text-slate-900">OBS</div>
            <div className="text-xs text-slate-500">{portalTitle}</div>
          </div>
        </div>
        <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-4">
          {nav.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-brand-50 text-brand-700'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
                )
              }
            >
              <item.icon size={18} />
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="border-t border-slate-100 p-4">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-600">
              <UserCircle2 size={20} />
            </div>
            <div className="flex-1 overflow-hidden">
              <div className="truncate text-sm font-medium text-slate-900">{user?.fullName || user?.username}</div>
              <div className="truncate text-xs text-slate-500">{user?.role}</div>
            </div>
          </div>
          <div className="mt-3 flex gap-2">
            <Link to="/account/password" className="btn-secondary flex-1" title="Şifre değiştir">
              <KeyRound size={14} />
            </Link>
            <button
              onClick={() => logout().then(() => navigate('/login'))}
              className="btn-danger flex-1"
              title="Çıkış yap"
            >
              <LogOut size={14} />
              Çıkış
            </button>
          </div>
        </div>
      </aside>
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-6">
          <div className="text-sm text-slate-500">
            Hoş geldiniz, <span className="font-medium text-slate-900">{user?.fullName || user?.username}</span>
          </div>
          <div className="text-xs text-slate-400">{new Date().toLocaleDateString('tr-TR')}</div>
        </header>
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
