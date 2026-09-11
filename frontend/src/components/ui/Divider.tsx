import { type HTMLAttributes } from 'react';

interface DividerProps extends HTMLAttributes<HTMLHRElement> {
  orientation?: 'horizontal' | 'vertical';
  label?: string;
}

function Divider({
  orientation = 'horizontal',
  label,
  className = '',
  ...props
}: DividerProps) {
  if (orientation === 'vertical') {
    return (
      <div
        role="separator"
        aria-orientation="vertical"
        className={[
          'inline-block h-full w-px bg-neutral-200',
          className,
        ].join(' ')}
        {...(props as HTMLAttributes<HTMLDivElement>)}
      />
    );
  }

  if (label) {
    return (
      <div className={['relative flex items-center py-2', className].join(' ')} {...props}>
        <div className="flex-grow border-t border-neutral-200" aria-hidden="true" />
        <span className="mx-3 text-caption shrink-0">{label}</span>
        <div className="flex-grow border-t border-neutral-200" aria-hidden="true" />
      </div>
    );
  }

  return (
    <hr
      role="separator"
      className={['border-t border-neutral-200', className].join(' ')}
      {...props}
    />
  );
}

export { Divider, type DividerProps };
