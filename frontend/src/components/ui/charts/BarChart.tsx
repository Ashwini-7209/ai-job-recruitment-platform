interface BarChartProps {
  data: Array<{ label: string; value: number }>;
  height?: number;
}

export function BarChart({ data, height = 200 }: BarChartProps) {
  if (!data || data.length === 0) {
    return <div className="text-center text-gray-500 py-8">No data available</div>;
  }

  const maxValue = Math.max(...data.map(d => d.value), 1);

  return (
    <div className="w-full" style={{ height }}>
      <div className="flex items-end justify-between h-full gap-1">
        {data.map((item, index) => {
          const barHeight = (item.value / maxValue) * 100;
          return (
            <div key={index} className="flex flex-col items-center flex-1 h-full justify-end">
              <span className="text-xs text-gray-600 mb-1">{item.value}</span>
              <div
                className="w-full bg-secondary-500 rounded-t"
                style={{ height: `${barHeight}%`, minHeight: '2px' }}
              />
              <span className="text-xs text-gray-500 mt-1 text-center truncate w-full" title={item.label}>
                {item.label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
