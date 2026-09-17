import { Outlet, useNavigate } from 'react-router-dom';
import { AppShell, TopBar, useSidebar } from '@/components/layout';
import { useIsMobile } from '@/hooks/useMediaQuery';
import { IconButton } from '@/components/ui/IconButton';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { useAuth } from '@/contexts';
import { NotificationBell } from '@/components/notifications';
import AdminSidebar from './AdminSidebar';

export default function AdminLayout() {
  const isMobile = useIsMobile();
  const { openMobile, closeMobile } = useSidebar();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleSidebarNavigate = () => {
    if (isMobile) closeMobile();
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const initials = user?.fullName
    ? user.fullName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
    : 'SA';

  const topBarActions = (
    <>
      <IconButton aria-label="Search">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
      </IconButton>
      <NotificationBell />
      <div className="hidden sm:block h-6 w-px bg-neutral-200" />
      <button
        onClick={() => navigate('/admin/settings')}
        className="flex items-center gap-2 rounded-lg p-1 hover:bg-neutral-100 transition-colors"
      >
        <Avatar size="sm" initials={initials} color="secondary" />
        <span className="hidden md:block text-sm font-medium text-neutral-700">
          {user?.fullName || 'Admin'}
        </span>
      </button>
      <Button variant="ghost" size="sm" onClick={handleLogout}>
        Sign out
      </Button>
    </>
  );

  return (
    <AppShell
      sidebar={<AdminSidebar onNavigate={handleSidebarNavigate} />}
      topBar={
        <TopBar
          leftSlot={isMobile ? (
            <IconButton onClick={openMobile} aria-label="Menu" className="lg:hidden">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M3 5h14M3 10h14M3 15h14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
              </svg>
            </IconButton>
          ) : undefined}
          actions={topBarActions}
        />
      }
    >
      <Outlet />
    </AppShell>
  );
}
