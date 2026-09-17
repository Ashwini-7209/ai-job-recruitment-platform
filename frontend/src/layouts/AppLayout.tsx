import { Outlet, useLocation } from 'react-router-dom';
import { AppShell, SidebarNavItem, SidebarNavGroup, SidebarFooter, TopBar, useSidebar } from '@/components/layout';
import { useIsMobile } from '@/hooks/useMediaQuery';
import { Avatar, IconButton } from '@/components/ui';

const mainNavItems = [
  {
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
        <polyline points="9,22 9,12 15,12 15,22" />
      </svg>
    ),
    label: 'Dashboard',
    active: true,
  },
  {
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
        <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
      </svg>
    ),
    label: 'Jobs',
  },
  {
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
        <circle cx="9" cy="7" r="4" />
        <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
        <path d="M16 3.13a4 4 0 0 1 0 7.75" />
      </svg>
    ),
    label: 'Candidates',
  },
  {
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14,2 14,8 20,8" />
        <line x1="16" y1="13" x2="8" y2="13" />
        <line x1="16" y1="17" x2="8" y2="17" />
      </svg>
    ),
    label: 'Applications',
  },
  {
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2L2 7l10 5 10-5-10-5z" />
        <path d="M2 17l10 5 10-5" />
        <path d="M2 12l10 5 10-5" />
      </svg>
    ),
    label: 'Companies',
  },
];

const secondaryNavItems = [
  {
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="3" />
        <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
      </svg>
    ),
    label: 'Settings',
  },
  {
    icon: (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="10" />
        <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
        <line x1="12" y1="17" x2="12.01" y2="17" />
      </svg>
    ),
    label: 'Help & Support',
  },
];

function SidebarContent() {
  return (
    <>
      <SidebarNavGroup label="Main">
        {mainNavItems.map((item) => (
          <SidebarNavItem key={item.label} {...item} />
        ))}
      </SidebarNavGroup>
      <SidebarNavGroup label="System">
        {secondaryNavItems.map((item) => (
          <SidebarNavItem key={item.label} {...item} />
        ))}
      </SidebarNavGroup>
      <SidebarFooter>
        <div className="flex items-center gap-3">
          <Avatar size="sm" initials="AU" />
          <div className="min-w-0 flex-1 hidden lg:block">
            <p className="text-sm font-medium text-neutral-900 truncate">Admin User</p>
            <p className="text-xs text-neutral-500 truncate">admin@jobrecruit.com</p>
          </div>
        </div>
      </SidebarFooter>
    </>
  );
}

export default function AppLayout() {
  const isMobile = useIsMobile();
  const { openMobile } = useSidebar();
  const location = useLocation();

  const titleMap: Record<string, string> = {
    '/recruiter': 'Recruiter Dashboard',
    '/recruiter/jobs': 'Manage Jobs',
    '/recruiter/applications': 'Applications',
    '/recruiter/interviews': 'Interviews',
    '/recruiter/candidates': 'Candidate Search',
    '/recruiter/analytics': 'Analytics',
    '/recruiter/profile': 'Profile',
    '/candidate': 'Candidate Dashboard',
    '/candidate/jobs': 'Browse Jobs',
    '/candidate/applications': 'My Applications',
    '/candidate/interviews': 'Interviews',
    '/candidate/saved-jobs': 'Saved Jobs',
    '/candidate/resume': 'Resume',
    '/candidate/ai': 'AI Career Assistant',
    '/candidate/analytics': 'Analytics',
    '/candidate/preferences': 'Preferences',
    '/admin': 'Admin Dashboard',
    '/admin/users': 'User Management',
    '/admin/jobs': 'Job Management',
    '/admin/applications': 'Applications',
    '/admin/analytics': 'Analytics',
    '/admin/audit-logs': 'Audit Logs',
  };

  const pageTitle = titleMap[location.pathname] || 'Job Portal';

  const topBarActions = (
    <>
      <IconButton aria-label="Search">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
      </IconButton>
      <IconButton aria-label="Notifications">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
      </IconButton>
      <div className="hidden sm:block h-6 w-px bg-neutral-200" />
      <Avatar size="sm" initials="AU" />
    </>
  );

  return (
    <AppShell
      sidebar={<SidebarContent />}
      topBar={
        <TopBar
          leftSlot={isMobile ? (
            <IconButton onClick={openMobile} aria-label="Menu" className="lg:hidden">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M3 5h14M3 10h14M3 15h14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
              </svg>
            </IconButton>
          ) : undefined}
          title={pageTitle}
          actions={topBarActions}
        />
      }
    >
      <Outlet />
    </AppShell>
  );
}
