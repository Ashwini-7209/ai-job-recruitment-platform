import { type HTMLAttributes } from 'react';

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  padding?: 'none' | 'sm' | 'md' | 'lg';
  bordered?: boolean;
}

const paddingStyles: Record<string, string> = {
  none: '',
  sm: 'p-4',
  md: 'p-5',
  lg: 'p-6',
};

function Card({
  padding = 'md',
  bordered = true,
  className = '',
  children,
  ...props
}: CardProps) {
  return (
    <div
      className={[
        'rounded-xl bg-white',
        bordered ? 'ring-1 ring-neutral-200' : '',
        paddingStyles[padding],
        className,
      ].join(' ')}
      {...props}
    >
      {children}
    </div>
  );
}

interface CardHeaderProps extends HTMLAttributes<HTMLDivElement> {}

function CardHeader({ className = '', children, ...props }: CardHeaderProps) {
  return (
    <div
      className={['pb-4 border-b border-neutral-100', className].join(' ')}
      {...props}
    >
      {children}
    </div>
  );
}

interface CardTitleProps extends HTMLAttributes<HTMLHeadingElement> {}

function CardTitle({ className = '', children, ...props }: CardTitleProps) {
  return (
    <h3
      className={['text-heading-md text-neutral-900', className].join(' ')}
      {...props}
    >
      {children}
    </h3>
  );
}

interface CardDescriptionProps extends HTMLAttributes<HTMLParagraphElement> {}

function CardDescription({ className = '', children, ...props }: CardDescriptionProps) {
  return (
    <p
      className={['text-body-sm text-neutral-500 mt-1', className].join(' ')}
      {...props}
    >
      {children}
    </p>
  );
}

interface CardContentProps extends HTMLAttributes<HTMLDivElement> {}

function CardContent({ className = '', children, ...props }: CardContentProps) {
  return (
    <div className={['pt-4', className].join(' ')} {...props}>
      {children}
    </div>
  );
}

interface CardFooterProps extends HTMLAttributes<HTMLDivElement> {}

function CardFooter({ className = '', children, ...props }: CardFooterProps) {
  return (
    <div
      className={['flex items-center gap-2 pt-4 border-t border-neutral-100', className].join(' ')}
      {...props}
    >
      {children}
    </div>
  );
}

export { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter, type CardProps };
