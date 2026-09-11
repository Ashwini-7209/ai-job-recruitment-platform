import { type ButtonHTMLAttributes, forwardRef } from 'react';

type IconButtonSize = 'sm' | 'md' | 'lg';

interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  size?: IconButtonSize;
  variant?: 'ghost' | 'outline' | 'primary';
}

const sizeStyles: Record<IconButtonSize, string> = {
  sm: 'h-7 w-7',
  md: 'h-9 w-9',
  lg: 'h-10 w-10',
};

const variantStyles: Record<string, string> = {
  ghost: 'text-neutral-500 hover:text-neutral-700 hover:bg-neutral-100 active:bg-neutral-200',
  outline: 'text-neutral-600 ring-1 ring-inset ring-neutral-300 hover:bg-neutral-50 active:bg-neutral-100',
  primary: 'text-white bg-primary-600 hover:bg-primary-700 active:bg-primary-800 shadow-sm',
};

const IconButton = forwardRef<HTMLButtonElement, IconButtonProps>(
  ({ size = 'md', variant = 'ghost', className = '', children, ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={[
          'inline-flex items-center justify-center rounded-lg',
          'transition-colors duration-150 ease-in-out',
          'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600',
          'disabled:opacity-50 disabled:cursor-not-allowed',
          sizeStyles[size],
          variantStyles[variant],
          className,
        ].join(' ')}
        {...props}
      >
        {children}
      </button>
    );
  },
);

IconButton.displayName = 'IconButton';

export { IconButton, type IconButtonProps, type IconButtonSize };
