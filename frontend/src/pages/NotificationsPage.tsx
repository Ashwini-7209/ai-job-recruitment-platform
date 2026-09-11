import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, Button, Badge, EmptyState, Skeleton } from '@/components/ui';
import { notificationService, type Notification, type NotificationType } from '@/services/notification.service';

function formatTimeAgo(dateString: string): string {
  const now = new Date();
  const date = new Date(dateString);
  const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (seconds < 60) return 'Just now';
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d ago`;
  return date.toLocaleDateString();
}

function getNotificationIcon(type: NotificationType): string {
  switch (type) {
    case 'APPLICATION_RECEIVED':
    case 'APPLICATION_STATUS_CHANGED':
      return 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z';
    case 'INTERVIEW_SCHEDULED':
    case 'INTERVIEW_RESCHEDULED':
      return 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z';
    case 'INTERVIEW_CANCELLED':
      return 'M6 18L18 6M6 6l12 12';
    case 'JOB_RECOMMENDATION':
      return 'M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z';
    default:
      return 'M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9';
  }
}

function getNotificationColor(type: NotificationType): string {
  switch (type) {
    case 'APPLICATION_RECEIVED':
    case 'APPLICATION_STATUS_CHANGED':
      return 'text-primary-600 bg-primary-50';
    case 'INTERVIEW_SCHEDULED':
    case 'INTERVIEW_RESCHEDULED':
      return 'text-success-600 bg-success-50';
    case 'INTERVIEW_CANCELLED':
      return 'text-error-600 bg-error-50';
    case 'JOB_RECOMMENDATION':
      return 'text-accent-600 bg-accent-50';
    default:
      return 'text-neutral-500 bg-neutral-100';
  }
}

function getNotificationBadgeVariant(type: NotificationType): 'primary' | 'success' | 'error' | 'accent' | 'default' {
  switch (type) {
    case 'APPLICATION_RECEIVED':
    case 'APPLICATION_STATUS_CHANGED':
      return 'primary';
    case 'INTERVIEW_SCHEDULED':
    case 'INTERVIEW_RESCHEDULED':
      return 'success';
    case 'INTERVIEW_CANCELLED':
      return 'error';
    case 'JOB_RECOMMENDATION':
      return 'accent';
    default:
      return 'default';
  }
}

function getNotificationTypeLabel(type: NotificationType): string {
  switch (type) {
    case 'APPLICATION_RECEIVED':
      return 'Application';
    case 'APPLICATION_STATUS_CHANGED':
      return 'Status Update';
    case 'INTERVIEW_SCHEDULED':
      return 'Interview';
    case 'INTERVIEW_RESCHEDULED':
      return 'Rescheduled';
    case 'INTERVIEW_CANCELLED':
      return 'Cancelled';
    case 'JOB_RECOMMENDATION':
      return 'Recommendation';
    default:
      return 'Notification';
  }
}

export default function NotificationsPage() {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [filter, setFilter] = useState<'all' | 'unread'>('all');

  const fetchNotifications = useCallback(async (pageNum: number, append = false) => {
    setIsLoading(true);
    try {
      const result = await notificationService.getNotifications({ page: pageNum, size: 20 });
      if (append) {
        setNotifications((prev) => [...prev, ...result.content]);
      } else {
        setNotifications(result.content);
      }
      setHasMore(!result.last);
    } catch {
      // Error handled silently
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchNotifications(0);
  }, [fetchNotifications]);

  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch {
      // Error handled silently
    }
  };

  const handleMarkAsRead = async (id: number) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
    } catch {
      // Error handled silently
    }
  };

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    fetchNotifications(nextPage, true);
  };

  const handleNotificationClick = (notification: Notification) => {
    handleMarkAsRead(notification.id);
    if (notification.entityId && notification.entityType) {
      if (notification.entityType === 'APPLICATION') {
        navigate(`/candidate/applications/${notification.entityId}`);
      } else if (notification.entityType === 'INTERVIEW') {
        navigate('/candidate/interviews');
      }
    }
  };

  const filteredNotifications = filter === 'unread'
    ? notifications.filter((n) => !n.read)
    : notifications;

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <div className="p-4 sm:p-6 max-w-3xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-heading-lg text-neutral-900">Notifications</h1>
          <p className="text-body-sm text-neutral-500 mt-1">
            Stay updated on your applications and interviews
          </p>
        </div>
        <div className="flex items-center gap-2">
          {unreadCount > 0 && (
            <Button variant="ghost" size="sm" onClick={handleMarkAllAsRead}>
              Mark all read
            </Button>
          )}
        </div>
      </div>

      <div className="flex gap-2">
        <button
          onClick={() => setFilter('all')}
          className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-colors ${
            filter === 'all'
              ? 'bg-primary-50 text-primary-700'
              : 'text-neutral-500 hover:bg-neutral-100'
          }`}
        >
          All
        </button>
        <button
          onClick={() => setFilter('unread')}
          className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-colors ${
            filter === 'unread'
              ? 'bg-primary-50 text-primary-700'
              : 'text-neutral-500 hover:bg-neutral-100'
          }`}
        >
          Unread {unreadCount > 0 && `(${unreadCount})`}
        </button>
      </div>

      <Card>
        <CardContent className="p-0">
          {isLoading && notifications.length === 0 ? (
            <div className="divide-y divide-neutral-100">
              {Array.from({ length: 5 }).map((_, i) => (
                <div key={i} className="flex items-start gap-3 px-4 py-4">
                  <Skeleton className="h-8 w-8 rounded-lg shrink-0" />
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-4 w-3/4" />
                    <Skeleton className="h-3 w-full" />
                    <Skeleton className="h-3 w-1/4" />
                  </div>
                </div>
              ))}
            </div>
          ) : filteredNotifications.length === 0 ? (
            <div className="py-12">
              <EmptyState
                icon={
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                    <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                  </svg>
                }
                title={filter === 'unread' ? 'No unread notifications' : 'No notifications yet'}
                description={
                  filter === 'unread'
                    ? 'All caught up! Check back later for updates.'
                    : 'Notifications about your applications and interviews will appear here.'
                }
              />
            </div>
          ) : (
            <div className="divide-y divide-neutral-100">
              {filteredNotifications.map((notification) => (
                <button
                  key={notification.id}
                  onClick={() => handleNotificationClick(notification)}
                  className={`flex w-full items-start gap-3 px-4 py-4 text-left transition-colors hover:bg-neutral-50 ${
                    !notification.read ? 'bg-primary-50/20' : ''
                  }`}
                >
                  <div className={`mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${getNotificationColor(notification.type)}`}>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d={getNotificationIcon(notification.type)} />
                    </svg>
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-start justify-between gap-2">
                      <p className={`text-sm ${!notification.read ? 'font-medium text-neutral-900' : 'text-neutral-700'}`}>
                        {notification.title}
                      </p>
                      <Badge variant={getNotificationBadgeVariant(notification.type)} size="sm">
                        {getNotificationTypeLabel(notification.type)}
                      </Badge>
                    </div>
                    <p className="mt-1 text-sm text-neutral-500">
                      {notification.message}
                    </p>
                    <p className="mt-1.5 text-xs text-neutral-400">
                      {formatTimeAgo(notification.createdAt)}
                    </p>
                  </div>
                  {!notification.read && (
                    <div className="mt-2 h-2 w-2 shrink-0 rounded-full bg-primary-500" />
                  )}
                </button>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {hasMore && !isLoading && filteredNotifications.length > 0 && (
        <div className="text-center">
          <Button variant="outline" onClick={handleLoadMore}>
            Load more
          </Button>
        </div>
      )}
    </div>
  );
}
