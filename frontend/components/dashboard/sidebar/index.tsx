"use client";

import * as React from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuth, type UserType } from "@/contexts/AuthContext";

import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuBadge,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
} from "@/components/ui/sidebar";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { cn } from "@/lib/utils";
import AtomIcon from "@/components/icons/atom";
import BracketsIcon from "@/components/icons/brackets";
import ProcessorIcon from "@/components/icons/proccesor";
import CuteRobotIcon from "@/components/icons/cute-robot";
import EmailIcon from "@/components/icons/email";
import MonkeyIcon from "@/components/icons/monkey";
import DotsVerticalIcon from "@/components/icons/dots-vertical";
import { Bullet } from "@/components/ui/bullet";
import LockIcon from "@/components/icons/lock";
import Image from "next/image";
import { useIsV0 } from "@/lib/v0-context";
import PlusIcon from "@/components/icons/plus";

// Fonction pour obtenir l'avatar de l'utilisateur
function getUserAvatar(user: { email?: string; neq?: string; nom?: string }): string {
  const avatars = [
    "/avatars/user_krimson.png",
    "/avatars/user_mati.png",
    "/avatars/user_pek.png",
    "/avatars/user_joyboy.png",
  ];
  
  // Utiliser l'email ou NEQ pour sélectionner un avatar de manière cohérente
  const identifier = user.email || user.neq || user.nom || "";
  if (!identifier) return avatars[0];
  
  // Hash simple pour toujours obtenir le même avatar pour le même utilisateur
  let hash = 0;
  for (let i = 0; i < identifier.length; i++) {
    hash = identifier.charCodeAt(i) + ((hash << 5) - hash);
  }
  
  return avatars[Math.abs(hash) % avatars.length];
}

// Fonction pour obtenir les items du menu selon le type d'utilisateur
function getMenuItems(userType: UserType) {
  const baseItems = [
    {
      title: "Vue d'ensemble",
      url: "/",
      icon: BracketsIcon,
      isActive: false,
    },
    {
      title: "Notifications",
      url: "/notifications",
      icon: EmailIcon,
      isActive: false,
    },
  ];

  switch (userType) {
    case "RESIDENT":
      return [
        ...baseItems,
        {
          title: "Mes travaux",
          url: "/residents",
          icon: AtomIcon,
          isActive: false,
        },
        {
          title: "Signaler un problème",
          url: "/residents/signaler",
          icon: PlusIcon,
          isActive: false,
        },
      ];

    case "PRESTATAIRE":
      return [
        ...baseItems,
        {
          title: "Problèmes disponibles",
          url: "/prestataires",
          icon: ProcessorIcon,
          isActive: false,
        },
        {
          title: "Mes projets",
          url: "/prestataires/projets",
          icon: ProcessorIcon,
          isActive: false,
        },
      ];

    case "STPM":
      return [
        ...baseItems,
        {
          title: "Problèmes",
          url: "/stpm",
          icon: CuteRobotIcon,
          isActive: false,
        },
        {
          title: "Candidatures",
          url: "/stpm/candidatures",
          icon: CuteRobotIcon,
          isActive: false,
        },
      ];

    default:
      return baseItems;
  }
}

// This is sample data for the sidebar
const data = {
  navMain: [
    {
      title: "Outils",
      items: [
        {
          title: "Vue d'ensemble",
          url: "/",
          icon: BracketsIcon,
          isActive: true,
        },
        {
          title: "Résidents",
          url: "/residents",
          icon: AtomIcon,
          isActive: false,
        },
        {
          title: "Prestataires",
          url: "/prestataires",
          icon: ProcessorIcon,
          isActive: false,
        },
        {
          title: "STPM",
          url: "/stpm",
          icon: CuteRobotIcon,
          isActive: false,
        },
        {
          title: "Notifications",
          url: "/notifications",
          icon: EmailIcon,
          isActive: false,
        },
      ],
    },
  ],
  desktop: {
    title: "MaVille (En ligne)",
    status: "online",
  },
  user: {
    name: "ADMIN",
    email: "admin@maville.mtl",
    avatar: "/avatars/user_krimson.png", // Default avatar
  },
};

export function DashboardSidebar({
  className,
  ...props
}: React.ComponentProps<typeof Sidebar>) {
  const isV0 = useIsV0();
  const { userType, user, logout } = useAuth();
  const pathname = usePathname();
  const router = useRouter();

  // Obtenir les items du menu selon le type d'utilisateur
  const menuItems = getMenuItems(userType).map(item => {
    // Pour la page d'accueil, on vérifie l'égalité exacte
    if (item.url === "/") {
      return { ...item, isActive: pathname === "/" };
    }
    // Pour les autres pages, on vérifie l'égalité exacte ou si le pathname commence par l'URL suivie d'un "/"
    return {
      ...item,
      isActive: pathname === item.url || pathname.startsWith(item.url + "/")
    };
  });
  const sidebarData = {
    ...data,
    navMain: [
      {
        title: "Outils",
        items: menuItems,
      },
    ],
  };

  return (
    <Sidebar {...props} className={cn("py-sides", className)}>
      <SidebarHeader className="rounded-t-lg flex gap-3 flex-row rounded-b-none">
        <div className="flex overflow-clip size-12 shrink-0 items-center justify-center rounded bg-sidebar-primary-foreground/10 transition-colors group-hover:bg-sidebar-primary text-sidebar-primary-foreground">
          <MonkeyIcon className="size-10 group-hover:scale-[1.7] origin-top-left transition-transform" />
        </div>
        <div className="grid flex-1 text-left text-sm leading-tight">
          <span className="text-2xl font-display">MAVILLE</span>
          <span className="text-xs uppercase">Gestion des Travaux Publics</span>
        </div>
      </SidebarHeader>

      <SidebarContent>
        {sidebarData.navMain.map((group, i) => (
          <SidebarGroup
            className={cn(i === 0 && "rounded-t-none")}
            key={group.title}
          >
            <SidebarGroupLabel>
              <Bullet className="mr-2" />
              {group.title}
            </SidebarGroupLabel>
            <SidebarGroupContent>
              <SidebarMenu>
                {group.items.map((item) => (
                  <SidebarMenuItem
                    key={item.title}
                    className={cn(
                      item.locked && "pointer-events-none opacity-50",
                      isV0 && "pointer-events-none"
                    )}
                    data-disabled={item.locked}
                  >
                    <SidebarMenuButton
                      asChild={!item.locked}
                      isActive={item.isActive}
                      disabled={item.locked}
                      className={cn(
                        "disabled:cursor-not-allowed",
                        item.locked && "pointer-events-none"
                      )}
                    >
                      {item.locked ? (
                        <div className="flex items-center gap-3 w-full">
                          <item.icon className="size-5" />
                          <span>{item.title}</span>
                        </div>
                      ) : (
                        <Link href={item.url} className="flex items-center gap-3 w-full">
                          <item.icon className="size-5" />
                          <span>{item.title}</span>
                        </Link>
                      )}
                    </SidebarMenuButton>
                    {item.locked && (
                      <SidebarMenuBadge>
                        <LockIcon className="size-5 block" />
                      </SidebarMenuBadge>
                    )}
                  </SidebarMenuItem>
                ))}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        ))}
      </SidebarContent>

      <SidebarFooter className="p-0">
        <SidebarGroup>
          <SidebarGroupLabel>
            <Bullet className="mr-2" />
            Utilisateur
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              <SidebarMenuItem>
                <Popover>
                  <PopoverTrigger className="flex gap-0.5 w-full group cursor-pointer">
                    <div className="shrink-0 flex size-14 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground overflow-clip">
                      <Image
                        src={user ? getUserAvatar(user) : data.user.avatar}
                        alt={user?.nom || data.user.name}
                        width={120}
                        height={120}
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <div className="group/item pl-3 pr-1.5 pt-2 pb-1.5 flex-1 flex bg-sidebar-accent hover:bg-sidebar-accent-active/75 items-center rounded group-data-[state=open]:bg-sidebar-accent-active group-data-[state=open]:hover:bg-sidebar-accent-active group-data-[state=open]:text-sidebar-accent-foreground">
                      <div className="grid flex-1 text-left text-sm leading-tight">
                        <span className="truncate text-xl font-display">
                          {user?.nom || data.user.name}
                        </span>
                        <span className="truncate text-xs uppercase opacity-50 group-hover/item:opacity-100">
                          {user?.email || user?.neq || data.user.email}
                        </span>
                      </div>
                      <DotsVerticalIcon className="ml-auto size-4" />
                    </div>
                  </PopoverTrigger>
                  <PopoverContent
                    className="w-56 p-0"
                    side="bottom"
                    align="end"
                    sideOffset={4}
                  >
                    <div className="flex flex-col">
                      <button className="flex items-center px-4 py-2 text-sm hover:bg-accent">
                        <MonkeyIcon className="mr-2 h-4 w-4" />
                        Compte
                      </button>
                      <button 
                        onClick={() => {
                          logout();
                          // Redirection immédiate avec window.location pour éviter les délais
                          window.location.href = "/login";
                        }}
                        className="flex items-center px-4 py-2 text-sm hover:bg-accent text-red-500"
                      >
                        <LockIcon className="mr-2 h-4 w-4" />
                        Déconnexion
                      </button>
                    </div>
                  </PopoverContent>
                </Popover>
              </SidebarMenuItem>
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarFooter>

      <SidebarRail />
    </Sidebar>
  );
}
