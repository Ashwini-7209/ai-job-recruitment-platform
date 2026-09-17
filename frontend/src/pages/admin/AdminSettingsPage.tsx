import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts';
import { Card, CardContent, Button, Input, Badge } from '@/components/ui';

const roleColors: Record<string, 'default' | 'primary' | 'success' | 'info'> = {
  ADMIN: 'info',
  RECRUITER: 'success',
  CANDIDATE: 'primary',
};

export default function AdminSettingsPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    if (!window.confirm('Are you sure you want to sign out?')) return;
    await logout();
    navigate('/login');
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-heading-lg text-neutral-900">Settings</h1>
        <Button variant="outline" onClick={() => navigate('/admin/dashboard')}>
          Back to Dashboard
        </Button>
      </div>

      <Card>
        <CardContent className="space-y-4">
          <h2 className="text-heading-sm text-neutral-900">Profile</h2>

          <div className="space-y-2">
            <label className="text-body-sm font-medium text-neutral-700">Full Name</label>
            <Input disabled value={user?.fullName ?? ''} />
          </div>

          <div className="space-y-2">
            <label className="text-body-sm font-medium text-neutral-700">Email</label>
            <Input disabled type="email" value={user?.email ?? ''} />
          </div>

          <div className="space-y-2">
            <label className="text-body-sm font-medium text-neutral-700">Role</label>
            <div>
              <Badge variant={roleColors[user?.role ?? ''] ?? 'default'}>
                {user?.role ?? 'N/A'}
              </Badge>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="space-y-4">
          <h2 className="text-heading-sm text-neutral-900">Security</h2>
          <p className="text-body-sm text-neutral-500">Manage your account security settings.</p>
          <Link
            to="/change-password"
            className="inline-flex items-center gap-2 text-body-sm font-medium text-secondary-600 hover:text-secondary-700 transition-colors"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 1 0-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 0 0 2.25-2.25v-6.75a2.25 2.25 0 0 0-2.25-2.25H6.75a2.25 2.25 0 0 0-2.25 2.25v6.75a2.25 2.25 0 0 0 2.25 2.25Z" />
            </svg>
            Change Password
          </Link>
        </CardContent>
      </Card>

      <Card className="border-error-200">
        <CardContent className="space-y-4">
          <h2 className="text-heading-sm text-error-600">Danger Zone</h2>
          <p className="text-body-sm text-neutral-600">
            Signing out will end your current session.
          </p>
          <Button variant="danger" onClick={handleLogout}>
            Sign Out
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
