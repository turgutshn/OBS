import React from 'react';
import { cn } from '../../lib/utils.js';

const TONES = {
  default: 'bg-slate-100 text-slate-700',
  success: 'bg-emerald-100 text-emerald-700',
  warning: 'bg-amber-100 text-amber-700',
  danger: 'bg-red-100 text-red-700',
  info: 'bg-sky-100 text-sky-700',
  brand: 'bg-brand-100 text-brand-700',
};

export function Badge({ tone = 'default', className, children }) {
  return <span className={cn('badge', TONES[tone] ?? TONES.default, className)}>{children}</span>;
}
