import { type HTMLAttributes } from 'react';

type AvatarSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

interface AvatarProps extends HTMLAttributes<HTMLDivElement> {
  src?: string;
  alt?: string;
  size?: AvatarSize;
  initials?: string;
  color?: 'primary' | 'secondary' | 'success' | 'warning' | 'error';
}

const sizeStyles: Record<AvatarSize, string> = {
  xs: 'h-6 w-6 text-xs',
  sm: 'h-8 w-8 text-xs',
  md: 'h-10 w-10 text-sm',
  lg: 'h-12 w-12 text-base',
  xl: 'h-16 w-16 text-lg',
};

const colorStyles: Record<string, string> = {
  primary: 'bg-secondary-100 text-secondary-700',
  secondary: 'bg-secondary-100 text-secondary-700',
  success: 'bg-success-100 text-success-700',
  warning: 'bg-warning-100 text-warning-700',
  error: 'bg-error-100 text-error-700',
};

function Avatar({
  src,
  alt = '',
  size = 'md',
  initials,
  color = 'primary',
  className = '',
  ...props
}: AvatarProps) {
  const initialsText = initials ?? alt.charAt(0).toUpperCase();

  return (
    <div
      className={[
        'relative inline-flex shrink-0 items-center justify-center rounded-full',
        sizeStyles[size],
        !src ? colorStyles[color] : '',
        'font-medium select-none',
        className,
      ].join(' ')}
      {...props}
    >
      {src ? (
        <img
          src={src}
          alt={alt}
          className="h-full w-full rounded-full object-cover"
        />
      ) : (
        <span aria-hidden="true">{initialsText}</span>
      )}
    </div>
  );
}

export { Avatar, type AvatarProps, type AvatarSize };
