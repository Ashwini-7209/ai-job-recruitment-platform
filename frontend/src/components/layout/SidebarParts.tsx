import { type ReactNode } from 'react';

interface SidebarBrandProps {
  logo?: ReactNode;
  title: string;
  subtitle?: string;
  collapsed?: boolean;
}

function SidebarBrand({ logo, title, subtitle, collapsed }: SidebarBrandProps) {
  return (
    <div className="flex items-center gap-3 px-4 py-4">
      {logo ?? (
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-secondary-500">
          <svg className="h-4 w-4 text-white" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 14.15v4.25c0 1.094-.787 2.036-1.872 2.18-2.087.277-4.216.42-6.378.42s-4.291-.143-6.378-.42c-1.085-.144-1.872-1.086-1.872-2.18v-4.25m16.5 0a2.18 2.18 0 0 0 .75-1.661V8.706c0-1.081-.768-2.015-1.837-2.175a48.114 48.114 0 0 0-3.413-.387m4.5 8.006c-.194.165-.42.295-.673.38A23.978 23.978 0 0 1 12 15.75c-2.648 0-5.195-.429-7.577-1.22a2.016 2.016 0 0 1-.673-.38m0 0A2.18 2.18 0 0 1 3 12.489V8.706c0-1.081.768-2.015 1.837-2.175a48.111 48.111 0 0 1 3.413-.387m7.5 0V5.25A2.25 2.25 0 0 0 13.5 3h-3a2.25 2.25 0 0 0-2.25 2.25v.894m7.5 0a48.667 48.667 0 0 0-7.5 0" />
          </svg>
        </div>
      )}
      {!collapsed && (
        <div className="min-w-0">
          <p className="text-sm font-semibold text-primary-900 truncate">{title}</p>
          {subtitle && (
            <p className="text-xs text-neutral-500 truncate">{subtitle}</p>
          )}
        </div>
      )}
    </div>
  );
}

interface SidebarNavItemProps {
  icon: ReactNode;
  label: string;
  href?: string;
  active?: boolean;
  onClick?: () => void;
  collapsed?: boolean;
}

function SidebarNavItem({ icon, label, href = '#', active, onClick, collapsed }: SidebarNavItemProps) {
  return (
    <a
      href={href}
      onClick={(e) => {
        e.preventDefault();
        onClick?.();
      }}
      className={[
        'group flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium',
        'transition-all duration-150',
        active
          ? 'bg-secondary-50 text-secondary-600'
          : 'text-neutral-600 hover:bg-neutral-50 hover:text-primary-900',
        collapsed ? 'justify-center px-2' : '',
      ].join(' ')}
      title={collapsed ? label : undefined}
    >
      <span className={[
        'shrink-0',
        active ? 'text-secondary-500' : 'text-neutral-400 group-hover:text-neutral-600',
      ].join(' ')}>
        {icon}
      </span>
      {!collapsed && <span className="truncate">{label}</span>}
    </a>
  );
}

interface SidebarNavGroupProps {
  label?: string;
  children: ReactNode;
  collapsed?: boolean;
}

function SidebarNavGroup({ label, children, collapsed }: SidebarNavGroupProps) {
  return (
    <div className="mt-6">
      {label && !collapsed && (
        <p className="px-3 mb-1.5 text-overline">{label}</p>
      )}
      <nav className="space-y-0.5">
        {children}
      </nav>
    </div>
  );
}

interface SidebarFooterProps {
  children: ReactNode;
}

function SidebarFooter({ children }: SidebarFooterProps) {
  return (
    <div className="mt-auto border-t border-neutral-200 pt-4 px-3">
      {children}
    </div>
  );
}

export { SidebarBrand, SidebarNavItem, SidebarNavGroup, SidebarFooter };
