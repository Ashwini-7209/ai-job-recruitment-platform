import { useState } from 'react';
import {
  Button,
  Input,
  Select,
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
  Badge,
  Avatar,
  Divider,
  IconButton,
  Skeleton,
  SkeletonText,
  SkeletonCircle,
  EmptyState,
} from '@/components/ui';

const selectOptions = [
  { value: 'candidate', label: 'Candidate' },
  { value: 'recruiter', label: 'Recruiter' },
  { value: 'admin', label: 'Administrator' },
];

export default function DesignSystemPage() {
  const [selectedRole, setSelectedRole] = useState('');
  const [loading, setLoading] = useState(false);

  const toggleLoading = () => {
    setLoading(true);
    setTimeout(() => setLoading(false), 2000);
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-8 max-w-[var(--content-max-width)] mx-auto">
      {/* Section: Typography */}
      <section>
        <h2 className="text-overline mb-4">Typography</h2>
        <Card>
          <CardContent>
            <div className="space-y-4">
              <div>
                <p className="text-display-lg text-neutral-900">Display Large</p>
                <p className="text-caption mt-1">text-display-lg / 2.25rem / Bold / -0.025em</p>
              </div>
              <div>
                <p className="text-display-md text-neutral-900">Display Medium</p>
                <p className="text-caption mt-1">text-display-md / 1.875rem / Bold / -0.02em</p>
              </div>
              <div>
                <p className="text-display-sm text-neutral-900">Display Small</p>
                <p className="text-caption mt-1">text-display-sm / 1.5rem / Semibold / -0.015em</p>
              </div>
              <Divider />
              <div>
                <p className="text-heading-lg text-neutral-900">Heading Large</p>
                <p className="text-caption mt-1">text-heading-lg / 1.25rem / Semibold</p>
              </div>
              <div>
                <p className="text-heading-md text-neutral-900">Heading Medium</p>
                <p className="text-caption mt-1">text-heading-md / 1.125rem / Semibold</p>
              </div>
              <div>
                <p className="text-heading-sm text-neutral-900">Heading Small</p>
                <p className="text-caption mt-1">text-heading-sm / 0.875rem / Semibold</p>
              </div>
              <Divider />
              <div>
                <p className="text-body-lg text-neutral-700">Body Large — The quick brown fox jumps over the lazy dog.</p>
                <p className="text-caption mt-1">text-body-lg / 1rem / Regular</p>
              </div>
              <div>
                <p className="text-body-md text-neutral-700">Body Medium — The quick brown fox jumps over the lazy dog.</p>
                <p className="text-caption mt-1">text-body-md / 0.875rem / Regular</p>
              </div>
              <div>
                <p className="text-body-sm text-neutral-700">Body Small — The quick brown fox jumps over the lazy dog.</p>
                <p className="text-caption mt-1">text-body-sm / 0.75rem / Regular</p>
              </div>
              <Divider />
              <div>
                <p className="text-label-lg text-neutral-700">Label Large</p>
                <p className="text-caption mt-1">text-label-lg / 0.875rem / Medium</p>
              </div>
              <div>
                <p className="text-label-md text-neutral-700">Label Medium</p>
                <p className="text-caption mt-1">text-label-md / 0.75rem / Medium</p>
              </div>
              <div>
                <p className="text-overline">Overline Label</p>
                <p className="text-caption mt-1">text-overline / 0.6875rem / Semibold / uppercase / 0.05em</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </section>

      {/* Section: Colors */}
      <section>
        <h2 className="text-overline mb-4">Color System</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {[
            { name: 'Primary', shades: [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950], prefix: 'primary' },
            { name: 'Secondary', shades: [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950], prefix: 'secondary' },
            { name: 'Accent', shades: [50, 100, 200, 300, 400, 500, 600, 700, 800, 900], prefix: 'accent' },
            { name: 'Success', shades: [50, 100, 200, 300, 400, 500, 600, 700, 800, 900], prefix: 'success' },
            { name: 'Warning', shades: [50, 100, 200, 300, 400, 500, 600, 700, 800, 900], prefix: 'warning' },
            { name: 'Error', shades: [50, 100, 200, 300, 400, 500, 600, 700, 800, 900], prefix: 'error' },
            { name: 'Info', shades: [50, 100, 200, 300, 400, 500, 600, 700, 800, 900], prefix: 'info' },
            { name: 'Neutral', shades: [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950], prefix: 'neutral' },
          ].map((palette) => (
            <Card key={palette.name} padding="sm">
              <CardHeader>
                <CardTitle>{palette.name}</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-1">
                  {palette.shades.map((shade) => (
                    <div
                      key={shade}
                      className="flex flex-col items-center"
                    >
                      <div
                        className={`h-8 w-8 rounded-md bg-${palette.prefix}-${shade}`}
                        title={`${palette.prefix}-${shade}`}
                      />
                      <span className="text-[10px] text-neutral-500 mt-1">{shade}</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* Section: Buttons */}
      <section>
        <h2 className="text-overline mb-4">Buttons</h2>
        <Card>
          <CardContent>
            <div className="space-y-6">
              <div>
                <p className="text-label-lg text-neutral-700 mb-3">Variants</p>
                <div className="flex flex-wrap items-center gap-3">
                  <Button variant="primary">Primary</Button>
                  <Button variant="secondary">Secondary</Button>
                  <Button variant="outline">Outline</Button>
                  <Button variant="ghost">Ghost</Button>
                  <Button variant="danger">Danger</Button>
                </div>
              </div>
              <Divider />
              <div>
                <p className="text-label-lg text-neutral-700 mb-3">Sizes</p>
                <div className="flex flex-wrap items-center gap-3">
                  <Button size="sm">Small</Button>
                  <Button size="md">Medium</Button>
                  <Button size="lg">Large</Button>
                </div>
              </div>
              <Divider />
              <div>
                <p className="text-label-lg text-neutral-700 mb-3">States</p>
                <div className="flex flex-wrap items-center gap-3">
                  <Button disabled>Disabled</Button>
                  <Button loading={loading} onClick={toggleLoading}>
                    {loading ? 'Loading...' : 'Click to Load'}
                  </Button>
                </div>
              </div>
              <Divider />
              <div>
                <p className="text-label-lg text-neutral-700 mb-3">With Icons</p>
                <div className="flex flex-wrap items-center gap-3">
                  <Button
                    icon={
                      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                        <path d="M8 3v10M3 8h10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                      </svg>
                    }
                  >
                    Add New
                  </Button>
                  <Button
                    variant="outline"
                    iconPosition="right"
                    icon={
                      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                        <path d="M3 8h10M9 4l4 4-4 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    }
                  >
                    Continue
                  </Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </section>

      {/* Section: Inputs */}
      <section>
        <h2 className="text-overline mb-4">Form Controls</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Card>
            <CardContent>
              <div className="space-y-4">
                <Input label="Email Address" placeholder="you@example.com" type="email" />
                <Input label="With Hint" placeholder="Enter your full name" hint="This will be displayed on your profile" />
                <Input label="With Error" placeholder="Enter value" error="This field is required" />
                <Input label="Disabled" placeholder="Cannot edit" disabled />
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent>
              <div className="space-y-4">
                <Select
                  label="Role"
                  options={selectOptions}
                  placeholder="Select a role"
                  value={selectedRole}
                  onChange={(e) => setSelectedRole(e.target.value)}
                />
                <Select
                  label="With Error"
                  options={selectOptions}
                  placeholder="Select one"
                  error="Please select an option"
                />
                <Select
                  label="Disabled"
                  options={selectOptions}
                  placeholder="Cannot select"
                  disabled
                />
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Section: Badges */}
      <section>
        <h2 className="text-overline mb-4">Badges</h2>
        <Card>
          <CardContent>
            <div className="space-y-4">
              <div>
                <p className="text-label-lg text-neutral-700 mb-3">Variants</p>
                <div className="flex flex-wrap items-center gap-2">
                  <Badge variant="default">Default</Badge>
                  <Badge variant="primary">Primary</Badge>
                  <Badge variant="secondary">Secondary</Badge>
                  <Badge variant="success">Success</Badge>
                  <Badge variant="warning">Warning</Badge>
                  <Badge variant="error">Error</Badge>
                  <Badge variant="info">Info</Badge>
                  <Badge variant="accent">Accent</Badge>
                </div>
              </div>
              <Divider />
              <div>
                <p className="text-label-lg text-neutral-700 mb-3">With Dots</p>
                <div className="flex flex-wrap items-center gap-2">
                  <Badge variant="success" dot>Active</Badge>
                  <Badge variant="warning" dot>Pending</Badge>
                  <Badge variant="error" dot>Inactive</Badge>
                  <Badge variant="info" dot>Processing</Badge>
                </div>
              </div>
              <Divider />
              <div>
                <p className="text-label-lg text-neutral-700 mb-3">Sizes</p>
                <div className="flex flex-wrap items-center gap-2">
                  <Badge size="sm">Small</Badge>
                  <Badge size="md">Medium</Badge>
                  <Badge size="lg">Large</Badge>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </section>

      {/* Section: Cards & Avatars */}
      <section>
        <h2 className="text-overline mb-4">Cards & Avatars</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <Card>
            <CardHeader>
              <CardTitle>Card with Header</CardTitle>
              <CardDescription>This card has a header, content, and footer.</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-body-md text-neutral-600">
                This is the card content area. It contains the main body of information.
              </p>
            </CardContent>
            <CardFooter>
              <Button size="sm" variant="outline">Cancel</Button>
              <Button size="sm">Save</Button>
            </CardFooter>
          </Card>

          <Card>
            <CardContent>
              <div className="flex items-center gap-3">
                <Avatar size="lg" initials="JD" />
                <div>
                  <p className="text-heading-sm text-neutral-900">John Doe</p>
                  <p className="text-body-sm text-neutral-500">Senior Developer</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent>
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <Avatar size="md" initials="AS" color="primary" />
                  <div className="min-w-0 flex-1">
                    <p className="text-label-lg text-neutral-900 truncate">Alice Smith</p>
                    <p className="text-body-sm text-neutral-500">Product Manager</p>
                  </div>
                  <Badge variant="success" size="sm">Active</Badge>
                </div>
                <Divider />
                <div className="flex items-center gap-3">
                  <Avatar size="md" initials="BJ" color="secondary" />
                  <div className="min-w-0 flex-1">
                    <p className="text-label-lg text-neutral-900 truncate">Bob Johnson</p>
                    <p className="text-body-sm text-neutral-500">UX Designer</p>
                  </div>
                  <Badge variant="warning" size="sm">Pending</Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Section: Avatars */}
      <section>
        <h2 className="text-overline mb-4">Avatars</h2>
        <Card>
          <CardContent>
            <div className="flex flex-wrap items-center gap-4">
              <Avatar size="xs" initials="XS" />
              <Avatar size="sm" initials="SM" color="secondary" />
              <Avatar size="md" initials="MD" color="accent" />
              <Avatar size="lg" initials="LG" color="success" />
              <Avatar size="xl" initials="XL" color="warning" />
              <Avatar size="md" initials="ER" color="error" />
              <Avatar size="md" alt="User with image" />
            </div>
          </CardContent>
        </Card>
      </section>

      {/* Section: Skeleton Loading */}
      <section>
        <h2 className="text-overline mb-4">Skeleton Loading</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Card>
            <CardContent>
              <div className="flex items-start gap-4">
                <SkeletonCircle />
                <div className="flex-1 space-y-3">
                  <SkeletonText lines={3} />
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent>
              <div className="space-y-3">
                <Skeleton className="h-32 w-full" rounded="lg" />
                <SkeletonText lines={2} />
                <Skeleton className="h-8 w-24" />
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Section: Empty State */}
      <section>
        <h2 className="text-overline mb-4">Empty State</h2>
        <Card>
          <EmptyState
            icon={
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14,2 14,8 20,8" />
              </svg>
            }
            title="No applications yet"
            description="When candidates apply to your job postings, they will appear here."
            action={<Button size="sm">Post a Job</Button>}
          />
        </Card>
      </section>

      {/* Section: Dividers */}
      <section>
        <h2 className="text-overline mb-4">Dividers</h2>
        <Card>
          <CardContent>
            <p className="text-body-md text-neutral-600">Content above</p>
            <Divider />
            <p className="text-body-md text-neutral-600">Content below</p>
            <Divider label="OR" />
            <p className="text-body-md text-neutral-600">More content</p>
          </CardContent>
        </Card>
      </section>

      {/* Section: Icon Buttons */}
      <section>
        <h2 className="text-overline mb-4">Icon Buttons</h2>
        <Card>
          <CardContent>
            <div className="flex flex-wrap items-center gap-3">
              <IconButton size="sm" aria-label="Small ghost">
                <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
                  <path d="M8 3v10M3 8h10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                </svg>
              </IconButton>
              <IconButton size="md" aria-label="Medium ghost">
                <svg width="18" height="18" viewBox="0 0 16 16" fill="none">
                  <path d="M8 3v10M3 8h10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                </svg>
              </IconButton>
              <IconButton size="lg" aria-label="Large ghost">
                <svg width="20" height="20" viewBox="0 0 16 16" fill="none">
                  <path d="M8 3v10M3 8h10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                </svg>
              </IconButton>
              <Divider orientation="vertical" className="h-6" />
              <IconButton size="md" variant="outline" aria-label="Outline">
                <svg width="18" height="18" viewBox="0 0 16 16" fill="none">
                  <path d="M11.5 3.5l-7 7M3.5 3.5l7 7" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                </svg>
              </IconButton>
              <IconButton size="md" variant="primary" aria-label="Primary">
                <svg width="18" height="18" viewBox="0 0 16 16" fill="none">
                  <path d="M8 3v10M3 8h10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                </svg>
              </IconButton>
            </div>
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
