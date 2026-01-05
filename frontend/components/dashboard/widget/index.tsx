"use client";

import React, { useState, useEffect } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import TVNoise from "@/components/ui/tv-noise";
import type { WidgetData } from "@/types/dashboard";

interface WidgetProps {
  widgetData: WidgetData;
}

export default function Widget({ widgetData }: WidgetProps) {
  const [currentTime, setCurrentTime] = useState(new Date());
  const montrealTimeZone = "America/Montreal";

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString("en-US", {
      timeZone: montrealTimeZone,
      hour12: true,
      hour: "numeric",
      minute: "2-digit",
    });
  };

  const formatDate = (date: Date) => {
    const dayOfWeek = date.toLocaleDateString("en-US", {
      timeZone: montrealTimeZone,
      weekday: "long",
    });
    const restOfDate = date.toLocaleDateString("en-US", {
      timeZone: montrealTimeZone,
      year: "numeric",
      month: "long",
      day: "numeric",
    });
    return { dayOfWeek, restOfDate };
  };

  const getTimezoneOffset = (date: Date) => {
    // Use Intl.DateTimeFormat to get timezone offset
    // Format the date in Montreal timezone and extract offset
    const formatter = new Intl.DateTimeFormat("en", {
      timeZone: montrealTimeZone,
      timeZoneName: "shortOffset",
    });
    
    const parts = formatter.formatToParts(date);
    const offsetPart = parts.find((part) => part.type === "timeZoneName");
    
    if (offsetPart && offsetPart.value) {
      // Extract offset from string like "GMT-5" or "GMT+1"
      const match = offsetPart.value.match(/GMT([+-])(\d+)/);
      if (match) {
        const sign = match[1];
        const hours = match[2];
        return `${sign}${hours}`;
      }
    }
    
    // Fallback: calculate using date difference
    // Get UTC time components
    const utcHours = date.getUTCHours();
    const utcMinutes = date.getUTCMinutes();
    
    // Get Montreal time components
    const montrealFormatter = new Intl.DateTimeFormat("en", {
      timeZone: montrealTimeZone,
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
    });
    const montrealParts = montrealFormatter.formatToParts(date);
    const montrealHour = parseInt(montrealParts.find((p) => p.type === "hour")?.value || "0");
    const montrealMinute = parseInt(montrealParts.find((p) => p.type === "minute")?.value || "0");
    
    // Calculate offset
    const utcTotalMinutes = utcHours * 60 + utcMinutes;
    const montrealTotalMinutes = montrealHour * 60 + montrealMinute;
    let offsetMinutes = montrealTotalMinutes - utcTotalMinutes;
    
    // Handle day boundary
    if (offsetMinutes > 12 * 60) offsetMinutes -= 24 * 60;
    if (offsetMinutes < -12 * 60) offsetMinutes += 24 * 60;
    
    const offsetHours = Math.round(offsetMinutes / 60);
    return offsetHours >= 0 ? `+${offsetHours}` : `${offsetHours}`;
  };

  const dateInfo = formatDate(currentTime);
  const timezoneOffset = getTimezoneOffset(currentTime);

  return (
    <Card className="w-full aspect-[2] relative overflow-hidden">
      <TVNoise opacity={0.3} intensity={0.2} speed={40} />
      <CardContent className="!bg-accent flex-1 flex flex-col justify-between text-sm font-medium uppercase relative z-20 text-black">
        <div className="flex justify-between items-center">
          <span className="opacity-70 text-black">{dateInfo.dayOfWeek}</span>
          <span className="text-black">{dateInfo.restOfDate}</span>
        </div>
        <div className="text-center">
          <div className="text-5xl font-display text-black" suppressHydrationWarning>
            {formatTime(currentTime)}
          </div>
        </div>

        <div className="flex justify-between items-center">
          <span className="opacity-70 text-black">Montréal</span>
          <span className="text-black">Québec, Canada</span>

          <Badge variant="secondary" className="bg-accent text-black border-black/20">
            UTC{timezoneOffset}
          </Badge>
        </div>
      </CardContent>
    </Card>
  );
}
