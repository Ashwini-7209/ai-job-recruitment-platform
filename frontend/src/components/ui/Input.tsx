import { type InputHTMLAttributes, forwardRef, useId } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  hint?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, hint, leftIcon, rightIcon, className = '', id: propId, ...props }, ref) => {
    const autoId = useId();
    const id = propId ?? autoId;

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
        <div className="relative">
          {leftIcon && (
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-neutral-400">
              {leftIcon}
            </div>
          )}
          <input
            ref={ref}
            id={id}
            className={[
              'block w-full rounded-lg border bg-white px-3.5 py-2 text-sm text-neutral-900',
              'placeholder:text-neutral-400',
              'transition-all duration-150 ease-in-out',
              'focus:outline-none focus:ring-2 focus:ring-offset-0',
              error
                ? 'border-error-300 focus:border-error-500 focus:ring-error-500/20'
                : 'border-neutral-300 focus:border-secondary-500 focus:ring-secondary-500/20',
              leftIcon ? 'pl-10' : '',
              rightIcon ? 'pr-10' : '',
              props.disabled ? 'bg-neutral-50 text-neutral-500 cursor-not-allowed' : '',
              className,
            ].join(' ')}
            {...props}
          />
          {rightIcon && (
            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3 text-neutral-400">
              {rightIcon}
            </div>
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

Input.displayName = 'Input';

export { Input, type InputProps };
