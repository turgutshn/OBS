import React from 'react';
import { cn } from '../../lib/utils.js';

export function Card({ className, children }) {
  return <div className={cn('card', className)}>{children}</div>;
}

export function CardHeader({ title, subtitle, action }) {
  return (
    <div className="flex items-start justify-between border-b border-slate-100 px-6 py-4">
      <div>
        <h3 className="text-base font-semibold text-slate-900">{title}</h3>
        {subtitle && <p className="text-sm text-slate-500">{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}

export function CardBody({ className, children }) {
  return <div className={cn('card-body', className)}>{children}</div>;
}
