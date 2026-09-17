import { lazy, Suspense } from 'react';
import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';
import { SidebarProvider } from '@/components/layout';
import { CandidateLayout } from '@/components/candidate';
import { RecruiterLayout } from '@/components/recruiter';
import { AdminLayout } from '@/components/admin';
import { useAuth } from '@/contexts';
import type { AuthRole } from '@/types/auth';

const LandingPage = lazy(() => import('@/pages/LandingPage'));
const NotFoundPage = lazy(() => import('@/pages/NotFoundPage'));
const NotificationsPage = lazy(() => import('@/pages/NotificationsPage'));
const LoginPage = lazy(() => import('@/pages/auth/LoginPage'));
const RoleSelectionPage = lazy(() => import('@/pages/auth/RoleSelectionPage'));
const RegisterCandidatePage = lazy(() => import('@/pages/auth/RegisterCandidatePage'));
const RegisterRecruiterPage = lazy(() => import('@/pages/auth/RegisterRecruiterPage'));
const ForgotPasswordPage = lazy(() => import('@/pages/auth/ForgotPasswordPage'));
const ResetPasswordPage = lazy(() => import('@/pages/auth/ResetPasswordPage'));
const ChangePasswordPage = lazy(() => import('@/pages/auth/ChangePasswordPage'));

const CandidateIndex = lazy(() => import('@/pages/candidate/CandidateIndex'));
const CandidateDashboardPage = lazy(() => import('@/pages/candidate/CandidateDashboardPage'));
const CandidateProfilePage = lazy(() => import('@/pages/candidate/CandidateProfilePage'));
const CandidateResumePage = lazy(() => import('@/pages/candidate/CandidateResumePage'));
const CandidateJobSearchPage = lazy(() => import('@/pages/candidate/CandidateJobSearchPage'));
const CandidateJobDetailPage = lazy(() => import('@/pages/candidate/CandidateJobDetailPage'));
const CandidateApplicationsPage = lazy(() => import('@/pages/candidate/CandidateApplicationsPage'));
const CandidateApplicationDetailPage = lazy(() => import('@/pages/candidate/CandidateApplicationDetailPage'));
const CandidateInterviewsPage = lazy(() => import('@/pages/candidate/CandidateInterviewsPage'));
const SavedJobsPage = lazy(() => import('@/pages/candidate/SavedJobsPage'));
const CandidateJobAlertsPage = lazy(() => import('@/pages/candidate/CandidateJobAlertsPage'));
const NotificationPreferencesPage = lazy(() => import('@/pages/candidate/NotificationPreferencesPage'));
const CandidateAnalyticsPage = lazy(() => import('@/pages/candidate/CandidateAnalyticsPage'));
const CareerAssistantPage = lazy(() => import('@/pages/candidate/CareerAssistantPage'));
const SkillGapPage = lazy(() => import('@/pages/candidate/SkillGapPage'));

const RecruiterIndex = lazy(() => import('@/pages/recruiter/RecruiterIndex'));
const RecruiterDashboardPage = lazy(() => import('@/pages/recruiter/RecruiterDashboardPage'));
const RecruiterProfilePage = lazy(() => import('@/pages/recruiter/RecruiterProfilePage'));
const RecruiterJobsPage = lazy(() => import('@/pages/recruiter/RecruiterJobsPage'));
const RecruiterJobCreatePage = lazy(() => import('@/pages/recruiter/RecruiterJobCreatePage'));
const RecruiterJobDetailPage = lazy(() => import('@/pages/recruiter/RecruiterJobDetailPage'));
const RecruiterApplicationsPage = lazy(() => import('@/pages/recruiter/RecruiterApplicationsPage'));
const RecruiterApplicationDetailPage = lazy(() => import('@/pages/recruiter/RecruiterApplicationDetailPage'));
const RecruiterInterviewsPage = lazy(() => import('@/pages/recruiter/RecruiterInterviewsPage'));
const RecruiterAnalyticsPage = lazy(() => import('@/pages/recruiter/RecruiterAnalyticsPage'));
const RecruiterCandidateSearchPage = lazy(() => import('@/pages/recruiter/RecruiterCandidateSearchPage'));

const AdminIndex = lazy(() => import('@/pages/admin/AdminIndex'));
const AdminDashboardPage = lazy(() => import('@/pages/admin/AdminDashboardPage'));
const AdminUsersPage = lazy(() => import('@/pages/admin/AdminUsersPage'));
const AdminJobsPage = lazy(() => import('@/pages/admin/AdminJobsPage'));
const AdminApplicationsPage = lazy(() => import('@/pages/admin/AdminApplicationsPage'));
const AdminAuditLogsPage = lazy(() => import('@/pages/admin/AdminAuditLogsPage'));
const AdminAnalyticsPage = lazy(() => import('@/pages/admin/AdminAnalyticsPage'));
const AdminUserDetailPage = lazy(() => import('@/pages/admin/AdminUserDetailPage'));
const AdminJobDetailPage = lazy(() => import('@/pages/admin/AdminJobDetailPage'));
const AdminSettingsPage = lazy(() => import('@/pages/admin/AdminSettingsPage'));

function SuspenseWrapper({ children }: { children: React.ReactNode }) {
  return (
    <Suspense
      fallback={
        <div className="flex h-screen items-center justify-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-secondary-200 border-t-secondary-600" />
        </div>
      }
    >
      {children}
    </Suspense>
  );
}

function ProtectedRoute({
  children,
  allowedRoles,
}: {
  children: React.ReactNode;
  allowedRoles?: AuthRole[];
}) {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-secondary-200 border-t-secondary-600" />
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    const redirectMap: Record<AuthRole, string> = {
      CANDIDATE: '/candidate/dashboard',
      RECRUITER: '/recruiter/dashboard',
      ADMIN: '/admin/dashboard',
    };
    return <Navigate to={redirectMap[user.role]} replace />;
  }

  return <>{children}</>;
}

function GuestRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-secondary-200 border-t-secondary-600" />
      </div>
    );
  }

  if (isAuthenticated && user) {
    const redirectMap: Record<AuthRole, string> = {
      CANDIDATE: '/candidate/dashboard',
      RECRUITER: '/recruiter/dashboard',
      ADMIN: '/admin/dashboard',
    };
    return <Navigate to={redirectMap[user.role]} replace />;
  }

  return <>{children}</>;
}

const router = createBrowserRouter([
  {
    path: '/',
    element: <SuspenseWrapper><LandingPage /></SuspenseWrapper>,
  },
  {
    path: '/login',
    element: (
      <GuestRoute>
        <SuspenseWrapper><LoginPage /></SuspenseWrapper>
      </GuestRoute>
    ),
  },
  {
    path: '/register',
    element: (
      <GuestRoute>
        <SuspenseWrapper><RoleSelectionPage /></SuspenseWrapper>
      </GuestRoute>
    ),
  },
  {
    path: '/register/candidate',
    element: (
      <GuestRoute>
        <SuspenseWrapper><RegisterCandidatePage /></SuspenseWrapper>
      </GuestRoute>
    ),
  },
  {
    path: '/register/recruiter',
    element: (
      <GuestRoute>
        <SuspenseWrapper><RegisterRecruiterPage /></SuspenseWrapper>
      </GuestRoute>
    ),
  },
  {
    path: '/forgot-password',
    element: <SuspenseWrapper><ForgotPasswordPage /></SuspenseWrapper>,
  },
  {
    path: '/reset-password',
    element: <SuspenseWrapper><ResetPasswordPage /></SuspenseWrapper>,
  },
  {
    path: '/change-password',
    element: (
      <ProtectedRoute allowedRoles={['CANDIDATE', 'RECRUITER', 'ADMIN']}>
        <SuspenseWrapper><ChangePasswordPage /></SuspenseWrapper>
      </ProtectedRoute>
    ),
  },
  {
    path: '/notifications',
    element: (
      <ProtectedRoute allowedRoles={['CANDIDATE', 'RECRUITER', 'ADMIN']}>
        <SuspenseWrapper><NotificationsPage /></SuspenseWrapper>
      </ProtectedRoute>
    ),
  },
  {
    path: '/candidate',
    element: (
      <ProtectedRoute allowedRoles={['CANDIDATE']}>
        <SidebarProvider>
          <CandidateLayout />
        </SidebarProvider>
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <SuspenseWrapper><CandidateIndex /></SuspenseWrapper> },
      { path: 'dashboard', element: <SuspenseWrapper><CandidateDashboardPage /></SuspenseWrapper> },
      { path: 'profile', element: <SuspenseWrapper><CandidateProfilePage /></SuspenseWrapper> },
      { path: 'resume', element: <SuspenseWrapper><CandidateResumePage /></SuspenseWrapper> },
      { path: 'jobs', element: <SuspenseWrapper><CandidateJobSearchPage /></SuspenseWrapper> },
      { path: 'jobs/:jobId', element: <SuspenseWrapper><CandidateJobDetailPage /></SuspenseWrapper> },
      { path: 'saved', element: <SuspenseWrapper><SavedJobsPage /></SuspenseWrapper> },
      { path: 'applications', element: <SuspenseWrapper><CandidateApplicationsPage /></SuspenseWrapper> },
      { path: 'applications/:applicationId', element: <SuspenseWrapper><CandidateApplicationDetailPage /></SuspenseWrapper> },
      { path: 'interviews', element: <SuspenseWrapper><CandidateInterviewsPage /></SuspenseWrapper> },
      { path: 'job-alerts', element: <SuspenseWrapper><CandidateJobAlertsPage /></SuspenseWrapper> },
      { path: 'notifications', element: <SuspenseWrapper><NotificationPreferencesPage /></SuspenseWrapper> },
      { path: 'analytics', element: <SuspenseWrapper><CandidateAnalyticsPage /></SuspenseWrapper> },
      { path: 'ai', element: <SuspenseWrapper><CareerAssistantPage /></SuspenseWrapper> },
      { path: 'skill-gap/:jobId', element: <SuspenseWrapper><SkillGapPage /></SuspenseWrapper> },
    ],
  },
  {
    path: '/recruiter',
    element: (
      <ProtectedRoute allowedRoles={['RECRUITER']}>
        <SidebarProvider>
          <RecruiterLayout />
        </SidebarProvider>
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <SuspenseWrapper><RecruiterIndex /></SuspenseWrapper> },
      { path: 'dashboard', element: <SuspenseWrapper><RecruiterDashboardPage /></SuspenseWrapper> },
      { path: 'profile', element: <SuspenseWrapper><RecruiterProfilePage /></SuspenseWrapper> },
      { path: 'jobs', element: <SuspenseWrapper><RecruiterJobsPage /></SuspenseWrapper> },
      { path: 'jobs/new', element: <SuspenseWrapper><RecruiterJobCreatePage /></SuspenseWrapper> },
      { path: 'jobs/:jobId', element: <SuspenseWrapper><RecruiterJobDetailPage /></SuspenseWrapper> },
      { path: 'applications', element: <SuspenseWrapper><RecruiterApplicationsPage /></SuspenseWrapper> },
      { path: 'applications/:applicationId', element: <SuspenseWrapper><RecruiterApplicationDetailPage /></SuspenseWrapper> },
      { path: 'candidates', element: <SuspenseWrapper><RecruiterCandidateSearchPage /></SuspenseWrapper> },
      { path: 'interviews', element: <SuspenseWrapper><RecruiterInterviewsPage /></SuspenseWrapper> },
      { path: 'analytics', element: <SuspenseWrapper><RecruiterAnalyticsPage /></SuspenseWrapper> },
    ],
  },
  {
    path: '/admin',
    element: (
      <ProtectedRoute allowedRoles={['ADMIN']}>
        <SidebarProvider>
          <AdminLayout />
        </SidebarProvider>
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <SuspenseWrapper><AdminIndex /></SuspenseWrapper> },
      { path: 'dashboard', element: <SuspenseWrapper><AdminDashboardPage /></SuspenseWrapper> },
      { path: 'users', element: <SuspenseWrapper><AdminUsersPage /></SuspenseWrapper> },
      { path: 'users/:userId', element: <SuspenseWrapper><AdminUserDetailPage /></SuspenseWrapper> },
      { path: 'jobs', element: <SuspenseWrapper><AdminJobsPage /></SuspenseWrapper> },
      { path: 'jobs/:jobId', element: <SuspenseWrapper><AdminJobDetailPage /></SuspenseWrapper> },
      { path: 'applications', element: <SuspenseWrapper><AdminApplicationsPage /></SuspenseWrapper> },
      { path: 'logs', element: <SuspenseWrapper><AdminAuditLogsPage /></SuspenseWrapper> },
      { path: 'analytics', element: <SuspenseWrapper><AdminAnalyticsPage /></SuspenseWrapper> },
      { path: 'settings', element: <SuspenseWrapper><AdminSettingsPage /></SuspenseWrapper> },
    ],
  },
  {
    path: '*',
    element: <SuspenseWrapper><NotFoundPage /></SuspenseWrapper>,
  },
]);

export default function AppRouter() {
  return <RouterProvider router={router} />;
}
