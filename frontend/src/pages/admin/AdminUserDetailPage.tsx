import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, CardContent, Button, Badge, Select, Skeleton } from '@/components/ui';
import { adminService, type AdminUser } from '@/services/admin.service';
import { useAuth } from '@/contexts';

const roleBadgeColors: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'error'> = {
  CANDIDATE: 'primary',
  RECRUITER: 'success',
  ADMIN: 'info',
};

const roleOptions = [
  { value: 'CANDIDATE', label: 'Candidate' },
  { value: 'RECRUITER', label: 'Recruiter' },
  { value: 'ADMIN', label: 'Admin' },
];

export default function AdminUserDetailPage() {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const { user: currentUser } = useAuth();

  const [userData, setUserData] = useState<AdminUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusLoading, setStatusLoading] = useState(false);
  const [roleLoading, setRoleLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [showStatusConfirm, setShowStatusConfirm] = useState(false);
  const [showRoleConfirm, setShowRoleConfirm] = useState(false);
  const [pendingRole, setPendingRole] = useState<string>('');
  const timeouts = useRef<ReturnType<typeof setTimeout>[]>([]);

  useEffect(() => {
    return () => timeouts.current.forEach(clearTimeout);
  }, []);
  const fetchUser = useCallback(async () => {
    if (!userId) return;
    try {
      setLoading(true);
      setError(null);
      const response = await adminService.getUser(Number(userId));
      if (response.success && response.data) {
        setUserData(response.data);
      } else {
        setError('User not found');
      }
    } catch (err) {
      setError('Failed to load user details');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handleStatusToggle = async () => {
    if (!userData) return;
    setStatusLoading(true);
    try {
      const response = await adminService.updateUserStatus(userData.id, !userData.enabled);
      if (response.success && response.data) {
        setUserData(response.data);
        setSuccessMessage(`User ${response.data.enabled ? 'enabled' : 'disabled'} successfully`);
        setShowStatusConfirm(false);
        timeouts.current.push(setTimeout(() => setSuccessMessage(null), 3000));
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update user status');
      timeouts.current.push(setTimeout(() => setError(null), 3000));
    } finally {
      setStatusLoading(false);
    }
  };

  const handleRoleChange = async () => {
    if (!userData || !pendingRole) return;
    setRoleLoading(true);
    try {
      const response = await adminService.updateUserRole(userData.id, pendingRole);
      if (response.success && response.data) {
        setUserData(response.data);
        setSuccessMessage('User role updated successfully');
        setShowRoleConfirm(false);
        setPendingRole('');
        timeouts.current.push(setTimeout(() => setSuccessMessage(null), 3000));
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update user role');
      timeouts.current.push(setTimeout(() => setError(null), 3000));
    } finally {
      setRoleLoading(false);
    }
  };

  const isSelf = currentUser?.id === Number(userId);

  if (loading) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
        <div className="flex items-center gap-3">
          <Skeleton className="h-10 w-10" />
          <Skeleton className="h-8 w-48" />
        </div>
        <Card>
          <CardContent className="p-6">
            <div className="space-y-6">
              <div className="flex items-center gap-4">
                <Skeleton className="h-16 w-16" rounded="full" />
                <div className="space-y-2">
                  <Skeleton className="h-6 w-48" />
                  <Skeleton className="h-4 w-32" />
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Skeleton className="h-16 w-full" />
                <Skeleton className="h-16 w-full" />
                <Skeleton className="h-16 w-full" />
                <Skeleton className="h-16 w-full" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (error && !userData) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
        <div className="flex items-center gap-3">
          <Button variant="ghost" onClick={() => navigate('/admin/users')} size="sm">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M19 12H5M12 19l-7-7 7-7" />
            </svg>
            Back
          </Button>
        </div>
        <Card>
          <CardContent className="p-12 text-center">
            <p className="text-body-lg text-error-600">{error}</p>
            <Button variant="outline" onClick={fetchUser} className="mt-4">
              Try Again
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <Button variant="ghost" onClick={() => navigate('/admin/users')} size="sm">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
          Back to Users
        </Button>
      </div>

      {successMessage && (
        <div className="rounded-lg bg-success-50 p-4 text-body-sm text-success-700 flex items-center gap-2">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M20 6L9 17l-5-5" />
          </svg>
          {successMessage}
        </div>
      )}

      {error && (
        <div className="rounded-lg bg-error-50 p-4 text-body-sm text-error-700 flex items-center gap-2">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <path d="M15 9l-6 6M9 9l6 6" />
          </svg>
          {error}
        </div>
      )}

      {userData && (
        <>
          <Card>
            <CardContent className="p-6">
              <div className="flex flex-col sm:flex-row items-start gap-6">
                <div className="h-20 w-20 rounded-full bg-secondary-100 flex items-center justify-center flex-shrink-0">
                   <span className="text-heading-lg font-semibold text-secondary-700">
                     {(userData.fullName || '').split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)}
                  </span>
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex flex-wrap items-center gap-3">
                    <h1 className="text-heading-lg text-neutral-900">{userData.fullName}</h1>
                    <Badge variant={roleBadgeColors[userData.role]} size="md">{userData.role}</Badge>
                    <Badge variant={userData.enabled ? 'success' : 'error'} size="md" dot>
                      {userData.enabled ? 'Active' : 'Inactive'}
                    </Badge>
                  </div>
                  <p className="text-body-md text-neutral-500 mt-1">{userData.email}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <Card>
              <CardContent className="p-6">
                <h3 className="text-label-lg text-neutral-900 mb-4">Account Information</h3>
                <div className="space-y-4">
                  <div>
                    <p className="text-body-sm text-neutral-500">Full Name</p>
                    <p className="text-body-md text-neutral-900">{userData.fullName}</p>
                  </div>
                  <div>
                    <p className="text-body-sm text-neutral-500">Email</p>
                    <p className="text-body-md text-neutral-900">{userData.email}</p>
                  </div>
                  <div>
                    <p className="text-body-sm text-neutral-500">Role</p>
                    <div className="flex items-center gap-2 mt-1">
                      <Badge variant={roleBadgeColors[userData.role]} size="md">{userData.role}</Badge>
                    </div>
                  </div>
                  <div>
                    <p className="text-body-sm text-neutral-500">Account Status</p>
                    <div className="flex items-center gap-2 mt-1">
                      <Badge variant={userData.enabled ? 'success' : 'error'} size="md" dot>
                        {userData.enabled ? 'Active' : 'Inactive'}
                      </Badge>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-6">
                <h3 className="text-label-lg text-neutral-900 mb-4">Activity & Dates</h3>
                <div className="space-y-4">
                  <div>
                    <p className="text-body-sm text-neutral-500">Registered</p>
                    <p className="text-body-md text-neutral-900">{formatDate(userData.createdAt)}</p>
                  </div>
                  <div>
                    <p className="text-body-sm text-neutral-500">Last Updated</p>
                    <p className="text-body-md text-neutral-900">{formatDate(userData.updatedAt)}</p>
                  </div>
                  {userData.role === 'CANDIDATE' && (
                    <div>
                      <p className="text-body-sm text-neutral-500">Applications Submitted</p>
                      <p className="text-body-md text-neutral-900">{userData.applicationCount ?? 0}</p>
                    </div>
                  )}
                  {userData.role === 'RECRUITER' && (
                    <div>
                      <p className="text-body-sm text-neutral-500">Jobs Posted</p>
                      <p className="text-body-md text-neutral-900">{userData.jobCount ?? 0}</p>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardContent className="p-6">
              <h3 className="text-label-lg text-neutral-900 mb-4">Manage User</h3>
              <div className="space-y-6">
                <div>
                  <p className="text-body-sm text-neutral-500 mb-3">Account Status</p>
                  {isSelf ? (
                    <p className="text-body-sm text-neutral-500 italic">
                      You cannot disable your own account.
                    </p>
                  ) : (
                    <Button
                      variant={userData.enabled ? 'danger' : 'primary'}
                      onClick={() => setShowStatusConfirm(true)}
                      loading={statusLoading}
                    >
                      {userData.enabled ? 'Disable Account' : 'Enable Account'}
                    </Button>
                  )}
                </div>

                <div className="border-t border-neutral-100 pt-6">
                  <p className="text-body-sm text-neutral-500 mb-3">Role</p>
                  {isSelf ? (
                    <p className="text-body-sm text-neutral-500 italic">
                      You cannot change your own role.
                    </p>
                  ) : userData.role === 'ADMIN' ? (
                    <div className="space-y-3">
                      <div className="flex items-center gap-2 p-3 rounded-lg bg-warning-50 text-warning-700 text-body-sm">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
                        </svg>
                        Warning: Changing the last admin's role may restrict administrative access.
                      </div>
                      <Select
                        options={roleOptions}
                        value={userData.role}
                        onChange={e => {
                          setPendingRole(e.target.value);
                          setShowRoleConfirm(true);
                        }}
                        className="w-full sm:w-48"
                      />
                    </div>
                  ) : (
                    <Select
                      options={roleOptions}
                      value={userData.role}
                      onChange={e => {
                        setPendingRole(e.target.value);
                        setShowRoleConfirm(true);
                      }}
                      className="w-full sm:w-48"
                    />
                  )}
                </div>
              </div>
            </CardContent>
          </Card>
        </>
      )}

      {showStatusConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-neutral-900/50 backdrop-blur-sm">
          <Card className="w-full max-w-md mx-4" padding="lg">
            <div className="space-y-4">
              <h3 className="text-heading-md text-neutral-900">Confirm Status Change</h3>
              <p className="text-body-md text-neutral-600">
                Are you sure you want to {userData?.enabled ? 'disable' : 'enable'} this user's account?
                {userData?.enabled && ' They will no longer be able to log in.'}
              </p>
              <div className="flex items-center gap-3 justify-end">
                <Button
                  variant="ghost"
                  onClick={() => setShowStatusConfirm(false)}
                  disabled={statusLoading}
                >
                  Cancel
                </Button>
                <Button
                  variant={userData?.enabled ? 'danger' : 'primary'}
                  onClick={handleStatusToggle}
                  loading={statusLoading}
                >
                  {userData?.enabled ? 'Disable' : 'Enable'}
                </Button>
              </div>
            </div>
          </Card>
        </div>
      )}

      {showRoleConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-neutral-900/50 backdrop-blur-sm">
          <Card className="w-full max-w-md mx-4" padding="lg">
            <div className="space-y-4">
              <h3 className="text-heading-md text-neutral-900">Confirm Role Change</h3>
              <p className="text-body-md text-neutral-600">
                Are you sure you want to change <strong>{userData?.fullName}</strong>'s role from{' '}
                <Badge variant={roleBadgeColors[userData?.role || '']} size="sm">{userData?.role}</Badge> to{' '}
                <Badge variant={roleBadgeColors[pendingRole]} size="sm">{pendingRole}</Badge>?
              </p>
              <div className="flex items-center gap-3 justify-end">
                <Button
                  variant="ghost"
                  onClick={() => {
                    setShowRoleConfirm(false);
                    setPendingRole('');
                  }}
                  disabled={roleLoading}
                >
                  Cancel
                </Button>
                <Button
                  variant="primary"
                  onClick={handleRoleChange}
                  loading={roleLoading}
                >
                  Change Role
                </Button>
              </div>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}
