import React from 'react';
import { cn } from '../../lib/utils.js';

const VARIANTS = {
  primary: 'btn-primary',
  secondary: 'btn-secondary',
  ghost: 'btn-ghost',
  danger: 'btn-danger',
};

export function Button({ variant = 'primary', className, children, ...props }) {
  return (
    <button className={cn(VARIANTS[variant] ?? VARIANTS.primary, className)} {...props}>
      {children}
    </button>
  );
}
