import { useState, useEffect } from 'react';
import { Button, Card, CardContent, Input, Select, Badge, EmptyState, Skeleton } from '@/components/ui';
import { adminService, type AdminUser } from '@/services/admin.service';

const roleOptions = [
  { value: '', label: 'All Roles' },
  { value: 'CANDIDATE', label: 'Candidate' },
  { value: 'RECRUITER', label: 'Recruiter' },
  { value: 'ADMIN', label: 'Admin' },
];

const statusOptions = [
  { value: '', label: 'All Status' },
  { value: 'true', label: 'Active' },
  { value: 'false', label: 'Inactive' },
];

const roleBadgeColors: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'error'> = {
  CANDIDATE: 'primary',
  RECRUITER: 'success',
  ADMIN: 'info',
};

export default function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [totalElements, setTotalElements] = useState(0);

  const fetchUsers = async (pageNum: number = 0, reset: boolean = false) => {
    try {
      setLoading(true);
      setError(null);
      const response = await adminService.getUsers({
        q: search || undefined,
        role: roleFilter || undefined,
        enabled: statusFilter ? statusFilter === 'true' : undefined,
        page: pageNum,
        size: 20,
      });
      if (response.success && response.data) {
        if (reset || pageNum === 0) {
          setUsers(response.data.content);
        } else {
          setUsers(prev => [...prev, ...response.data!.content]);
        }
        setHasMore(!response.data.last);
        setTotalElements(response.data.totalElements);
      }
    } catch (err) {
      setError('Failed to load users');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers(0, true);
  }, [search, roleFilter, statusFilter]);

  const handleToggleStatus = async (userId: number, currentEnabled: boolean) => {
    if (!confirm(`Are you sure you want to ${currentEnabled ? 'disable' : 'enable'} this user?`)) return;
    try {
      const response = await adminService.updateUserStatus(userId, !currentEnabled);
      if (response.success) {
        fetchUsers(0, true);
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update user status');
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-heading-lg text-neutral-900">User Management</h1>
          <p className="text-body-sm text-neutral-500 mt-1">{totalElements} total users</p>
        </div>
      </div>

      {error && (
        <div className="rounded-lg bg-error-50 p-4 text-body-sm text-error-700">{error}</div>
      )}

      <Card>
        <CardContent className="p-4">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <Input
                placeholder="Search by name or email..."
                value={search}
                onChange={e => setSearch(e.target.value)}
                leftIcon={
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="11" cy="11" r="8" />
                    <line x1="21" y1="21" x2="16.65" y2="16.65" />
                  </svg>
                }
              />
            </div>
            <Select
              options={roleOptions}
              value={roleFilter}
              onChange={e => setRoleFilter(e.target.value)}
              className="w-full sm:w-40"
            />
            <Select
              options={statusOptions}
              value={statusFilter}
              onChange={e => setStatusFilter(e.target.value)}
              className="w-full sm:w-40"
            />
          </div>
        </CardContent>
      </Card>

      {loading && users.length === 0 ? (
        <div className="space-y-4">
          {[1, 2, 3, 4, 5].map(i => (
            <Card key={i}>
              <CardContent className="p-4">
                <div className="flex items-center gap-4">
                  <Skeleton className="h-10 w-10 rounded-full" />
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-5 w-48" />
                    <Skeleton className="h-4 w-32" />
                  </div>
                  <Skeleton className="h-8 w-20" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : users.length === 0 ? (
        <Card>
          <CardContent className="p-12">
            <EmptyState
              icon={
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
                </svg>
              }
              title="No users found"
              description="Try adjusting your search or filters."
            />
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {users.map(user => (
            <Card key={user.id}>
              <CardContent className="p-4">
                <div className="flex items-center gap-4">
                  <div className="flex-shrink-0 w-10 h-10 rounded-full bg-primary-100 flex items-center justify-center">
                    <span className="text-body-sm font-medium text-primary-700">
                      {user.fullName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)}
                    </span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <p className="text-body-lg font-medium text-neutral-900 truncate">{user.fullName}</p>
                      <Badge variant={roleBadgeColors[user.role]} size="sm">{user.role}</Badge>
                      {!user.enabled && <Badge variant="error" size="sm">Inactive</Badge>}
                    </div>
                    <p className="text-body-sm text-neutral-500 truncate">{user.email}</p>
                  </div>
                  <div className="hidden sm:flex items-center gap-4 text-caption text-neutral-500">
                    {user.role === 'CANDIDATE' && <span>{user.applicationCount ?? 0} applications</span>}
                    {user.role === 'RECRUITER' && <span>{user.jobCount ?? 0} jobs</span>}
                    <span>Joined {formatDate(user.createdAt)}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      variant={user.enabled ? 'outline' : 'primary'}
                      size="sm"
                      onClick={() => handleToggleStatus(user.id, user.enabled)}
                    >
                      {user.enabled ? 'Disable' : 'Enable'}
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}

          {hasMore && (
            <div className="flex justify-center pt-4">
              <Button
                variant="outline"
                onClick={() => {
                  setPage(prev => prev + 1);
                  fetchUsers(page + 1);
                }}
                loading={loading}
              >
                Load More
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
