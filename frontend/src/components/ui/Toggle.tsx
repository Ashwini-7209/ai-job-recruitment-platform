import { type InputHTMLAttributes, forwardRef, useId } from 'react';

interface ToggleProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type' | 'size'> {
  label?: string;
  error?: string;
  hint?: string;
  size?: 'sm' | 'md' | 'lg';
}

const Toggle = forwardRef<HTMLInputElement, ToggleProps>(
  ({ label, error, hint, size = 'md', className = '', id: propId, ...props }, ref) => {
    const autoId = useId();
    const id = propId ?? autoId;

    const sizeStyles = {
      sm: 'w-9 h-5',
      md: 'w-11 h-6',
      lg: 'w-14 h-7',
    };

    const dotSizeStyles = {
      sm: 'w-3.5 h-3.5',
      md: 'w-5 h-5',
      lg: 'w-6 h-6',
    };

    const translateStyles = {
      sm: 'peer-checked:translate-x-4',
      md: 'peer-checked:translate-x-5',
      lg: 'peer-checked:translate-x-7',
    };

    return (
      <div className="w-full">
        {label && (
          <label
            htmlFor={id}
            className="block text-label-md text-neutral-700 mb-1.5"
          >
            {label}
          </label>
        )}
        <div className="flex items-center gap-3">
          <label className="relative inline-flex cursor-pointer items-center">
            <input
              ref={ref}
              id={id}
              type="checkbox"
              className="peer sr-only"
              {...props}
            />
            <div
              className={[
                'rounded-full bg-neutral-200 transition-colors duration-200 ease-in-out',
                'peer-checked:bg-primary-600 peer-focus-visible:ring-2 peer-focus-visible:ring-primary-500/20',
                'peer-disabled:opacity-50 peer-disabled:cursor-not-allowed',
                sizeStyles[size],
                props.disabled ? 'opacity-50 cursor-not-allowed' : '',
                className,
              ].join(' ')}
            />
            <div
              className={[
                'absolute left-0.5 top-1/2 -translate-y-1/2 rounded-full bg-white shadow-sm transition-transform duration-200 ease-in-out',
                'peer-checked:translate-x-full',
                dotSizeStyles[size],
                translateStyles[size],
              ].join(' ')}
            />
          </label>
          {props.checked !== undefined && (
            <span className="text-sm text-neutral-600">
              {props.checked ? 'Enabled' : 'Disabled'}
            </span>
          )}
        </div>
        {error && (
          <p className="mt-1 text-body-sm text-error-600">{error}</p>
        )}
        {!error && hint && (
          <p className="mt-1 text-body-sm text-neutral-500">{hint}</p>
        )}
      </div>
    );
  },
);

Toggle.displayName = 'Toggle';

export { Toggle, type ToggleProps };
