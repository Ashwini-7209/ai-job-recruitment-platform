import { useState, useEffect } from 'react';
import { Button, Card, CardContent, Badge, EmptyState, Skeleton } from '@/components/ui';
import { jobAlertService, type JobAlert } from '@/services/jobAlert.service';
import CreateEditAlertForm from '@/components/candidate/CreateEditAlertForm';

export default function CandidateJobAlertsPage() {
  const [alerts, setAlerts] = useState<JobAlert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingAlert, setEditingAlert] = useState<JobAlert | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  const fetchAlerts = async (pageNum: number = 0) => {
    try {
      setLoading(true);
      setError(null);
      const response = await jobAlertService.getAlerts(pageNum, 20);
      if (response.success && response.data) {
        if (pageNum === 0) {
          setAlerts(response.data.content);
        } else {
          setAlerts(prev => [...prev, ...response.data!.content]);
        }
        setHasMore(!response.data.last);
      }
    } catch (err) {
      setError('Failed to load job alerts');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlerts();
  }, []);

  const handleCreateAlert = async (data: any) => {
    try {
      const response = await jobAlertService.createAlert(data);
      if (response.success) {
        setShowCreateForm(false);
        fetchAlerts();
      }
    } catch (err: any) {
      throw err;
    }
  };

  const handleUpdateAlert = async (data: any) => {
    if (!editingAlert) return;
    try {
      const response = await jobAlertService.updateAlert(editingAlert.id, data);
      if (response.success) {
        setEditingAlert(null);
        fetchAlerts();
      }
    } catch (err: any) {
      throw err;
    }
  };

  const handleDeleteAlert = async (alertId: number) => {
    if (!confirm('Are you sure you want to delete this alert?')) return;
    try {
      const response = await jobAlertService.deleteAlert(alertId);
      if (response.success) {
        fetchAlerts();
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleToggleStatus = async (alertId: number) => {
    try {
      const response = await jobAlertService.toggleAlertStatus(alertId);
      if (response.success) {
        fetchAlerts();
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to toggle alert status');
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '-';
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (showCreateForm || editingAlert) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            onClick={() => {
              setShowCreateForm(false);
              setEditingAlert(null);
            }}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M19 12H5M12 19l-7-7 7-7" />
            </svg>
          </Button>
          <h1 className="text-heading-lg text-neutral-900">
            {editingAlert ? 'Edit Job Alert' : 'Create Job Alert'}
          </h1>
        </div>
        <Card>
          <CardContent className="p-6">
            <CreateEditAlertForm
              initialData={editingAlert}
              onSubmit={editingAlert ? handleUpdateAlert : handleCreateAlert}
              onCancel={() => {
                setShowCreateForm(false);
                setEditingAlert(null);
              }}
            />
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-heading-lg text-neutral-900">Job Alerts</h1>
        <Button onClick={() => setShowCreateForm(true)}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M12 5v14M5 12h14" />
          </svg>
          Create Alert
        </Button>
      </div>

      {error && (
        <div className="rounded-lg bg-error-50 p-4 text-body-sm text-error-700">
          {error}
        </div>
      )}

      {loading && alerts.length === 0 ? (
        <div className="space-y-4">
          {[1, 2, 3].map(i => (
            <Card key={i}>
              <CardContent className="p-6">
                <Skeleton className="h-6 w-48 mb-4" />
                <Skeleton className="h-4 w-full mb-2" />
                <Skeleton className="h-4 w-3/4" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : alerts.length === 0 ? (
        <Card>
          <CardContent className="p-12">
            <EmptyState
              icon={
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                  <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                </svg>
              }
              title="No job alerts yet"
              description="Create alerts to get notified when new jobs match your preferences."
              action={
                <Button onClick={() => setShowCreateForm(true)}>Create Your First Alert</Button>
              }
            />
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {alerts.map(alert => (
            <Card key={alert.id}>
              <CardContent className="p-6">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-heading-sm text-neutral-900 truncate">{alert.name}</h3>
                      <Badge variant={alert.active ? 'success' : 'default'} size="sm">
                        {alert.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </div>
                    <div className="flex flex-wrap gap-2 text-body-sm text-neutral-600 mb-3">
                      {alert.keywords && (
                        <span className="flex items-center gap-1">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <circle cx="11" cy="11" r="8" />
                            <line x1="21" y1="21" x2="16.65" y2="16.65" />
                          </svg>
                          {alert.keywords}
                        </span>
                      )}
                      {alert.location && (
                        <span className="flex items-center gap-1">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                            <circle cx="12" cy="10" r="3" />
                          </svg>
                          {alert.location}
                        </span>
                      )}
                      {alert.workplaceType && (
                        <Badge variant="secondary" size="sm">{alert.workplaceType}</Badge>
                      )}
                      {alert.employmentType && (
                        <Badge variant="secondary" size="sm">{alert.employmentType}</Badge>
                      )}
                      {alert.minimumExperience !== undefined && alert.minimumExperience !== null && (
                        <span className="flex items-center gap-1">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
                            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
                          </svg>
                          {alert.minimumExperience}+ years
                        </span>
                      )}
                    </div>
                    {alert.skills && (
                      <div className="flex flex-wrap gap-1 mb-3">
                        {alert.skills.split(',').map((skill, i) => (
                          <Badge key={i} variant="default" size="sm">{skill.trim()}</Badge>
                        ))}
                      </div>
                    )}
                    <div className="flex gap-4 text-caption text-neutral-500">
                      <span>Created {formatDate(alert.createdAt)}</span>
                      {alert.lastTriggeredAt && (
                        <span>Last triggered {formatDate(alert.lastTriggeredAt)}</span>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleToggleStatus(alert.id)}
                    >
                      {alert.active ? 'Disable' : 'Enable'}
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setEditingAlert(alert)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDeleteAlert(alert.id)}
                      className="text-error-600 hover:text-error-700"
                    >
                      Delete
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
                  const nextPage = page + 1;
                  setPage(nextPage);
                  fetchAlerts(nextPage);
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
