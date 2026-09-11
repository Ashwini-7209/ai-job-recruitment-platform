import { useLocation, useNavigate } from 'react-router-dom';
import { SidebarNavItem, SidebarNavGroup, SidebarFooter } from '@/components/layout';
import { Avatar } from '@/components/ui';

const mainNavItems = [
  {
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="3" width="7" height="7" rx="1" />
        <rect x="14" y="3" width="7" height="7" rx="1" />
        <rect x="14" y="14" width="7" height="7" rx="1" />
        <rect x="3" y="14" width="7" height="7" rx="1" />
      </svg>
    ),
    label: 'Overview',
    href: '/recruiter/dashboard',
  },
  {
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
        <line x1="16" y1="2" x2="16" y2="6" />
        <line x1="8" y1="2" x2="8" y2="6" />
        <line x1="3" y1="10" x2="21" y2="10" />
      </svg>
    ),
    label: 'Interviews',
    href: '/recruiter/interviews',
  },
];

const secondaryNavItems = [
  {
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M18 20V10" />
        <path d="M12 20V4" />
        <path d="M6 20v-6" />
      </svg>
    ),
    label: 'Analytics',
    href: '/recruiter/analytics',
  },
];

interface RecruiterSidebarProps {
  onNavigate?: () => void;
}

export default function RecruiterSidebar({ onNavigate }: RecruiterSidebarProps) {
  const location = useLocation();
  const navigate = useNavigate();

  const handleNav = (href: string) => {
    navigate(href);
    onNavigate?.();
  };

  const isActive = (href: string) => location.pathname === href;

  return (
    <>
      <SidebarNavGroup>
        {mainNavItems.map((item) => (
          <SidebarNavItem
            key={item.href}
            icon={item.icon}
            label={item.label}
            active={isActive(item.href)}
            onClick={() => handleNav(item.href)}
          />
        ))}
      </SidebarNavGroup>
      <SidebarNavGroup label="Recruiting Tools">
        {secondaryNavItems.map((item) => (
          <SidebarNavItem
            key={item.href}
            icon={item.icon}
            label={item.label}
            active={isActive(item.href)}
            onClick={() => handleNav(item.href)}
          />
        ))}
      </SidebarNavGroup>
      <SidebarFooter>
        <div className="flex items-center gap-3">
          <Avatar size="sm" initials="DC" color="secondary" />
          <div className="min-w-0 flex-1 hidden lg:block">
            <p className="text-sm font-medium text-neutral-900 truncate">Demo Company</p>
            <p className="text-xs text-neutral-500 truncate">Recruiter Account</p>
          </div>
        </div>
      </SidebarFooter>
    </>
  );
}
