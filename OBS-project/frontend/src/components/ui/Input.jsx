import React from 'react';
import { cn } from '../../lib/utils.js';

export function Input({ label, error, className, id, ...props }) {
  const inputId = id || props.name;
  return (
    <div>
      {label && <label htmlFor={inputId} className="label">{label}</label>}
      <input id={inputId} className={cn('input', error && 'border-red-400 focus:ring-red-400/20', className)} {...props} />
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}

export function TextArea({ label, error, className, id, ...props }) {
  const inputId = id || props.name;
  return (
    <div>
      {label && <label htmlFor={inputId} className="label">{label}</label>}
      <textarea id={inputId} rows={3} className={cn('input', error && 'border-red-400', className)} {...props} />
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}

export function Select({ label, error, children, className, id, ...props }) {
  const inputId = id || props.name;
  return (
    <div>
      {label && <label htmlFor={inputId} className="label">{label}</label>}
      <select id={inputId} className={cn('input', error && 'border-red-400', className)} {...props}>
        {children}
      </select>
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}
