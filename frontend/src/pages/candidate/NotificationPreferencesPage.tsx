import { useState, useEffect } from 'react';
import { Card, CardContent, Skeleton } from '@/components/ui';
import { Toggle } from '@/components/ui/Toggle';
import {
  notificationPreferenceService,
  type NotificationPreference,
} from '@/services/notificationPreference.service';

export default function NotificationPreferencesPage() {
  const [preferences, setPreferences] = useState<NotificationPreference | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    fetchPreferences();
  }, []);

  const fetchPreferences = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await notificationPreferenceService.getPreferences();
      if (response.success && response.data) {
        setPreferences(response.data);
      }
    } catch (err) {
      setError('Failed to load notification preferences');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = async (key: keyof NotificationPreference) => {
    if (!preferences) return;

    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await notificationPreferenceService.updatePreferences({
        [key]: !preferences[key],
      });
      if (response.success && response.data) {
        setPreferences(response.data);
        setSuccess('Preferences updated successfully');
        setTimeout(() => setSuccess(null), 3000);
      }
    } catch (err) {
      setError('Failed to update preferences');
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <h1 className="text-heading-lg text-neutral-900">Notification Preferences</h1>
        <Card>
          <CardContent className="p-6 space-y-6">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="flex items-center justify-between">
                <div className="space-y-2">
                  <Skeleton className="h-5 w-48" />
                  <Skeleton className="h-4 w-64" />
                </div>
                <Skeleton className="h-6 w-11 rounded-full" />
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-heading-lg text-neutral-900">Notification Preferences</h1>

      {error && (
        <div className="rounded-lg bg-error-50 p-4 text-body-sm text-error-700">
          {error}
        </div>
      )}

      {success && (
        <div className="rounded-lg bg-success-50 p-4 text-body-sm text-success-700">
          {success}
        </div>
      )}

      <Card>
        <CardContent className="p-6">
          <div className="space-y-8">
            <div className="flex items-center justify-between gap-4">
              <div>
                <h3 className="text-body-lg font-medium text-neutral-900">Application Updates</h3>
                <p className="text-body-sm text-neutral-500 mt-1">
                  Receive notifications about your job application status changes.
                </p>
              </div>
              <Toggle
                checked={preferences?.applicationStatus ?? true}
                onChange={() => handleToggle('applicationStatus')}
                disabled={saving}
              />
            </div>

            <div className="border-t border-neutral-100" />

            <div className="flex items-center justify-between gap-4">
              <div>
                <h3 className="text-body-lg font-medium text-neutral-900">Interview Updates</h3>
                <p className="text-body-sm text-neutral-500 mt-1">
                  Receive notifications about interview scheduling, rescheduling, and cancellations.
                </p>
              </div>
              <Toggle
                checked={preferences?.interviewUpdates ?? true}
                onChange={() => handleToggle('interviewUpdates')}
                disabled={saving}
              />
            </div>

            <div className="border-t border-neutral-100" />

            <div className="flex items-center justify-between gap-4">
              <div>
                <h3 className="text-body-lg font-medium text-neutral-900">Job Alerts</h3>
                <p className="text-body-sm text-neutral-500 mt-1">
                  Receive notifications when new jobs match your saved job alerts.
                </p>
              </div>
              <Toggle
                checked={preferences?.jobAlerts ?? true}
                onChange={() => handleToggle('jobAlerts')}
                disabled={saving}
              />
            </div>

            <div className="border-t border-neutral-100" />

            <div className="flex items-center justify-between gap-4">
              <div>
                <h3 className="text-body-lg font-medium text-neutral-900">System Notifications</h3>
                <p className="text-body-sm text-neutral-500 mt-1">
                  Receive general platform notifications and job recommendations.
                </p>
              </div>
              <Toggle
                checked={preferences?.systemNotifications ?? true}
                onChange={() => handleToggle('systemNotifications')}
                disabled={saving}
              />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
