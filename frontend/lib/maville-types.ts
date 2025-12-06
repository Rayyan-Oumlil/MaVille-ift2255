/**
 * TypeScript types for MaVille dashboard data
 */

export interface MaVilleStat {
  label: string;
  value: string | number;
  description: string;
  trend?: "up" | "down";
  badge?: string;
  badgeType?: "success" | "warning" | "info";
}

export interface MaVilleProbleme {
  id: number;
  lieu: string;
  description: string;
  type: string;
  priorite: "FAIBLE" | "MOYENNE" | "ELEVEE";
  declarant: string;
  date: string;
}

export interface MaVilleCandidature {
  id: number;
  prestataire: string;
  statut: "SOUMISE" | "APPROUVEE" | "REJETEE";
  description: string;
  cout?: number;
  dateDebut?: string;
  dateFin?: string;
}

export interface MaVilleProjet {
  id: number;
  description: string;
  statut: "PLANIFIE" | "EN_COURS" | "TERMINE" | "ANNULE";
  localisation: string;
  dateDebut?: string;
  dateFin?: string;
  cout?: number;
  prestataire?: {
    numeroEntreprise: string;
    nomEntreprise: string;
  };
}

export interface MaVilleNotification {
  id?: string;
  message: string;
  lu: boolean;
  date: string;
  type: string;
  projetId?: number;
}

export interface ActivityDataPoint {
  date: string;
  problems: number;
  projects: number;
  applications: number;
}
