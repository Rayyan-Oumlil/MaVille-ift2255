import { ArrowUp, ArrowDown } from "lucide-react"

interface StatCardProps {
  label: string
  value: string
  description: string
  trend?: "up" | "down"
  badge?: string
  badgeType?: "success" | "warning" | "info"
  icon?: string
}

export default function StatCard({ label, value, description, trend, badge, badgeType = "info", icon }: StatCardProps) {
  const trendColor = trend === "up" ? "text-blue-400" : trend === "down" ? "text-orange-400" : ""

  const badgeColors = {
    success: "bg-blue-500/20 text-blue-400 border-blue-500/50",
    warning: "bg-orange-500/20 text-orange-400 border-orange-500/50",
    info: "bg-blue-500/20 text-blue-400 border-blue-500/50",
  }

  return (
    <div className="bg-card border border-border rounded-sm p-4 hover:border-primary/50 transition-colors">
      {/* Title with blue square bracket */}
      <div className="flex items-start gap-2 mb-3">
        <div className="text-primary text-lg font-bold">[</div>
        <div className="flex-1">
          <h3 className="text-sm font-bold text-foreground uppercase tracking-wider">{label}</h3>
        </div>
      </div>

      {/* Value */}
      <div className="mb-2">
        <div className="text-3xl font-bold text-white mb-1">{value}</div>
        <p className="text-xs text-muted-foreground uppercase">{description}</p>
      </div>

      {/* Footer with trend and badge */}
      <div className="flex items-center justify-between">
        {trend && (
          <div className="flex items-center gap-1">
            {trend === "up" ? (
              <ArrowUp className={`w-5 h-5 ${trendColor}`} />
            ) : (
              <ArrowDown className={`w-5 h-5 ${trendColor}`} />
            )}
            {trend === "up" && <ArrowUp className={`w-5 h-5 ${trendColor}`} />}
          </div>
        )}

        {badge && (
          <div className={`text-xs font-bold px-3 py-1 rounded-full border ${badgeColors[badgeType]}`}>{badge}</div>
        )}
      </div>
    </div>
  )
}
