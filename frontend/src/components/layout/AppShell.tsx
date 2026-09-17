import { useEffect, useRef, type ReactNode } from 'react';
import { useSidebar } from './SidebarContext';
import { useIsMobile } from '@/hooks/useMediaQuery';
import { SidebarBrand } from './SidebarParts';
import { IconButton } from '@/components/ui/IconButton';

interface AppShellProps {
  sidebar: ReactNode;
  topBar?: ReactNode;
  children: ReactNode;
}

function MobileOverlay({ onClick }: { onClick: () => void }) {
  return (
    <div
      className="fixed inset-0 z-[var(--z-modal-backdrop)] bg-primary-900/30 backdrop-blur-sm lg:hidden"
      onClick={onClick}
      aria-hidden="true"
    />
  );
}

function SidebarMobile({ children, onClose }: { children: ReactNode; onClose: () => void }) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [onClose]);

  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => { document.body.style.overflow = ''; };
  }, []);

  return (
    <>
      <MobileOverlay onClick={onClose} />
      <div
        ref={ref}
        className="fixed inset-y-0 left-0 z-[var(--z-modal)] flex w-[var(--sidebar-width)] flex-col bg-white shadow-xl lg:hidden"
        role="dialog"
        aria-modal="true"
        aria-label="Sidebar"
      >
        <div className="flex items-center justify-between px-4 py-4 border-b border-neutral-200">
          <SidebarBrand title="HireFlow" subtitle="AI Recruitment" />
          <IconButton onClick={onClose} aria-label="Close sidebar">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M15 5L5 15M5 5l10 10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </IconButton>
        </div>
        <div className="flex-1 overflow-y-auto px-3 py-4">
          {children}
        </div>
      </div>
    </>
  );
}

function AppShell({ sidebar, topBar, children }: AppShellProps) {
  const { isOpen, isMobileOpen, toggle, closeMobile } = useSidebar();
  const isMobile = useIsMobile();

  return (
    <div className="flex h-screen overflow-hidden bg-neutral-50">
      {/* Desktop sidebar */}
      <aside
        className={[
          'hidden lg:flex lg:flex-col lg:fixed lg:inset-y-0 lg:z-[var(--z-fixed)]',
          'bg-white border-r border-neutral-200',
          'transition-[width] duration-200 ease-in-out',
          isOpen ? 'lg:w-[var(--sidebar-width)]' : 'lg:w-[var(--sidebar-collapsed-width)]',
        ].join(' ')}
        aria-label="Sidebar"
      >
        <div className="flex flex-col h-full">
          <div className="flex items-center justify-between border-b border-neutral-200">
            <SidebarBrand
              title="HireFlow"
              subtitle={isOpen ? 'AI Recruitment' : undefined}
              collapsed={!isOpen}
            />
            <div className="pr-3">
              <IconButton
                size="sm"
                onClick={toggle}
                aria-label={isOpen ? 'Collapse sidebar' : 'Expand sidebar'}
              >
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" className={[
                  'transition-transform duration-200',
                  isOpen ? '' : 'rotate-180',
                ].join(' ')}>
                  <path d="M10 4l-4 4 4 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </IconButton>
            </div>
          </div>
          <div className="flex-1 overflow-y-auto px-3 py-4">
            {sidebar}
          </div>
        </div>
      </aside>

      {/* Mobile sidebar */}
      {isMobile && isMobileOpen && (
        <SidebarMobile onClose={closeMobile}>
          {sidebar}
        </SidebarMobile>
      )}

      {/* Main content */}
      <div className={[
        'flex flex-col flex-1 min-w-0',
        'transition-[margin] duration-200 ease-in-out',
        !isMobile && isOpen ? 'lg:ml-[var(--sidebar-width)]' : '',
        !isMobile && !isOpen ? 'lg:ml-[var(--sidebar-collapsed-width)]' : '',
      ].join(' ')}>
        {topBar && <div className="sticky top-0 z-[var(--z-sticky)]">{topBar}</div>}
        <main className="flex-1 overflow-y-auto">
          {children}
        </main>
      </div>
    </div>
  );
}

export { AppShell, type AppShellProps };
