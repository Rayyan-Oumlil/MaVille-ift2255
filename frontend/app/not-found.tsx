import React from "react";
import Image from "next/image";
import DashboardPageLayout from "@/components/dashboard/layout";
import CuteRobotIcon from "@/components/icons/cute-robot";

export default function NotFound() {
  return (
    <DashboardPageLayout
      header={{
        title: "Page introuvable",
        description: "Page en construction",
        icon: CuteRobotIcon,
      }}
    >
      <div className="flex flex-col items-center justify-center gap-10 flex-1">
        <picture className="w-1/4 aspect-square grayscale opacity-50">
          <Image
            src="/assets/bot_greenprint.gif"
            alt="Statut de sécurité"
            width={1000}
            height={1000}
            quality={90}
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
            className="size-full object-contain"
          />
        </picture>

        <div className="flex flex-col items-center justify-center gap-2">
          <h1 className="text-xl font-bold uppercase text-muted-foreground">
            Page introuvable
          </h1>
          <p className="text-sm max-w-sm text-center text-muted-foreground text-balance">
            Cette page n'existe pas encore ou est en cours de développement.
          </p>
        </div>
      </div>
    </DashboardPageLayout>
  );
}
