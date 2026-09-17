import type { ReactNode } from 'react';

interface TopBarProps {
  title?: string;
  breadcrumbs?: { label: string; href?: string }[];
  actions?: ReactNode;
  leftSlot?: ReactNode;
}

function TopBar({ title, breadcrumbs, actions, leftSlot }: TopBarProps) {
  return (
    <header className="sticky top-0 z-[var(--z-sticky)] flex h-[var(--header-height)] items-center gap-2 sm:gap-4 border-b border-neutral-200/80 bg-white/95 backdrop-blur-sm px-3 sm:px-6">
      <div className="flex items-center gap-2 sm:gap-3 flex-1 min-w-0">
        {leftSlot}
        <div className="min-w-0 flex-1">
          {breadcrumbs && breadcrumbs.length > 0 && (
            <nav className="hidden sm:flex items-center gap-1 text-body-sm text-neutral-500 mb-0.5" aria-label="Breadcrumb">
              {breadcrumbs.map((crumb, i) => (
                <span key={i} className="flex items-center gap-1">
                  {i > 0 && <span className="text-neutral-300">/</span>}
                  {crumb.href ? (
                    <a href={crumb.href} className="hover:text-neutral-700 transition-colors">
                      {crumb.label}
                    </a>
                  ) : (
                    <span className="text-neutral-700 font-medium">{crumb.label}</span>
                  )}
                </span>
              ))}
            </nav>
          )}
          {title && (
            <h1 className="text-heading-sm sm:text-heading-md text-primary-900 truncate">{title}</h1>
          )}
        </div>
      </div>
      {actions && (
        <div className="flex items-center gap-1.5 sm:gap-2 shrink-0">
          {actions}
        </div>
      )}
    </header>
  );
}

export { TopBar, type TopBarProps };
