"use client"

import { useState, useMemo } from "react"
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts"
import { useApiQuery } from "@/hooks/use-api-query"
import * as api from "@/lib/api"

// Generate chart data from API data
function generateChartData(problems: any[], projects: any[], applications: any[], period: "week" | "month" | "year") {
  const now = new Date()
  const days = period === "week" ? 7 : period === "month" ? 30 : 12 // 12 months for year
  const data: { date: string; problems: number; projects: number; applications: number }[] = []

  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(now)
    if (period === "week" || period === "month") {
      date.setDate(date.getDate() - i)
    } else {
      date.setMonth(date.getMonth() - i)
    }
    
    let dateStr: string
    if (period === "week") {
      dateStr = `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}`
    } else if (period === "month") {
      dateStr = `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}`
    } else {
      dateStr = date.toLocaleDateString("fr-FR", { month: "short" })
    }

    // Count items for this date
    const problemsCount = problems.filter(p => {
      if (!p.date) return false
      try {
        const pDate = new Date(p.date)
        if (period === "week" || period === "month") {
          return pDate.toDateString() === date.toDateString()
        } else {
          return pDate.getMonth() === date.getMonth() && pDate.getFullYear() === date.getFullYear()
        }
      } catch {
        return false
      }
    }).length

    const projectsCount = projects.filter(p => {
      if (!p.date_debut) return false
      try {
        const pDate = new Date(p.date_debut)
        if (period === "week" || period === "month") {
          return pDate.toDateString() === date.toDateString()
        } else {
          return pDate.getMonth() === date.getMonth() && pDate.getFullYear() === date.getFullYear()
        }
      } catch {
        return false
      }
    }).length

    // Applications - distribute evenly for now since they don't have dates
    const applicationsCount = period === "week" 
      ? Math.floor(applications.length / 7)
      : period === "month"
      ? Math.floor(applications.length / 30)
      : Math.floor(applications.length / 12)

    data.push({
      date: dateStr,
      problems: problemsCount,
      projects: projectsCount,
      applications: applicationsCount,
    })
  }

  return data
}

export default function ActivityChart() {
  const [period, setPeriod] = useState<"week" | "month" | "year">("week")

  const { data: problemsData, isLoading: problemsLoading } = useApiQuery(
    ["problemes", "chart"],
    () => api.getStpmProblemes({ page: 0, size: 1000 }),
    {
      staleTime: 60 * 1000, // 1 minute
    }
  )

  const { data: applicationsData, isLoading: applicationsLoading } = useApiQuery(
    ["candidatures", "chart"],
    () => api.getStpmCandidatures({ page: 0, size: 1000 }),
    {
      staleTime: 60 * 1000,
    }
  )

  const { data: travauxData, isLoading: travauxLoading } = useApiQuery(
    ["travaux", "chart"],
    () => api.getResidentsTravaux({ page: 0, size: 1000 }),
    {
      staleTime: 60 * 1000,
    }
  )

  const chartData = useMemo(() => {
    return generateChartData(
      problemsData?.data || [],
      travauxData?.data || [],
      applicationsData?.data || [],
      period
    )
  }, [problemsData, travauxData, applicationsData, period])

  const loading = problemsLoading || applicationsLoading || travauxLoading

  return (
    <div className="bg-card border border-border rounded-sm p-6">
      {/* Header with tabs */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 sm:gap-0 mb-6">
        <h2 className="text-base sm:text-lg font-bold text-foreground uppercase">VUE D'ENSEMBLE DE L'ACTIVITÉ</h2>
        <div className="flex gap-1 sm:gap-2 w-full sm:w-auto">
          {(["week", "month", "year"] as const).map((p) => (
            <button
              key={p}
              onClick={() => setPeriod(p)}
              className={`flex-1 sm:flex-none px-3 sm:px-4 py-2 text-xs font-bold uppercase transition-colors ${
                period === p
                  ? "text-white bg-primary/20 border-b-2 border-primary"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              {p === "week" ? "SEMAINE" : p === "month" ? "MOIS" : "ANNÉE"}
            </button>
          ))}
        </div>
      </div>

      {/* Legend */}
      <div className="flex gap-6 mb-6">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 bg-blue-400 rounded-full"></div>
          <span className="text-xs text-muted-foreground uppercase">PROBLÈMES</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 bg-blue-400 rounded-full"></div>
          <span className="text-xs text-muted-foreground uppercase">PROJETS</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 bg-orange-400 rounded-full"></div>
          <span className="text-xs text-muted-foreground uppercase">CANDIDATURES</span>
        </div>
      </div>

      {/* Chart */}
      {loading ? (
        <div className="h-[300px] flex items-center justify-center">
          <p className="text-sm text-muted-foreground">Chargement des données du graphique...</p>
        </div>
      ) : (
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" stroke="#333333" verticalPoints={[]} />
          <XAxis dataKey="date" stroke="#666666" style={{ fontSize: "12px" }} />
          <YAxis stroke="#666666" style={{ fontSize: "12px" }} />
          <Tooltip
            contentStyle={{
              backgroundColor: "#1e1e1e",
              border: "1px solid #333333",
              borderRadius: "4px",
            }}
            labelStyle={{ color: "#ffffff" }}
          />
          <Line
            type="monotone"
            dataKey="problems"
            stroke="#00ff88"
            strokeWidth={2}
            dot={false}
            isAnimationActive={false}
          />
          <Line
            type="monotone"
            dataKey="projects"
            stroke="#00d4ff"
            strokeWidth={2}
            dot={false}
            isAnimationActive={false}
          />
          <Line
            type="monotone"
            dataKey="applications"
            stroke="#ff6b35"
            strokeWidth={2}
            dot={false}
            isAnimationActive={false}
          />
        </LineChart>
      </ResponsiveContainer>
      )}
    </div>
  )
}
