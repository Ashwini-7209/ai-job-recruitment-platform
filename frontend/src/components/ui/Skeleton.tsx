interface SkeletonProps {
  className?: string;
  rounded?: 'sm' | 'md' | 'lg' | 'full';
}

function Skeleton({ className = '', rounded = 'md' }: SkeletonProps) {
  const roundedStyles: Record<string, string> = {
    sm: 'rounded-sm',
    md: 'rounded-md',
    lg: 'rounded-lg',
    full: 'rounded-full',
  };

  return (
    <div
      role="status"
      aria-label="Loading"
      className={[
        'animate-pulse bg-neutral-200',
        roundedStyles[rounded],
        className,
      ].join(' ')}
    >
      <span className="sr-only">Loading...</span>
    </div>
  );
}

function SkeletonText({ lines = 1, className = '' }: { lines?: number; className?: string }) {
  return (
    <div className={['space-y-2', className].join(' ')}>
      {Array.from({ length: lines }).map((_, i) => (
        <Skeleton
          key={i}
          className={['h-4', i === lines - 1 ? 'w-3/4' : 'w-full'].join(' ')}
        />
      ))}
    </div>
  );
}

function SkeletonCircle({ className = '' }: { className?: string }) {
  return <Skeleton className={['h-10 w-10', className].join(' ')} rounded="full" />;
}

function SkeletonCard({ className = '' }: { className?: string }) {
  return (
    <div className={['rounded-xl bg-white p-5 ring-1 ring-neutral-200', className].join(' ')}>
      <div className="flex items-start gap-4">
        <SkeletonCircle />
        <div className="flex-1 space-y-3">
          <Skeleton className="h-4 w-1/3" />
          <Skeleton className="h-3 w-2/3" />
          <Skeleton className="h-3 w-1/2" />
        </div>
      </div>
    </div>
  );
}

export { Skeleton, SkeletonText, SkeletonCircle, SkeletonCard, type SkeletonProps };
