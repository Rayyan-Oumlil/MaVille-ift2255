/**
 * API Client for MaVille Backend
 * Base URL: http://localhost:7000/api
 */

import { reportError } from './error-handler';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:7000/api';

export interface ApiError {
  status: string;
  message: string;
  code: number;
  path: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  page: number;
  pageSize: number; // Backend utilise pageSize
  size?: number; // Alias pour compatibilité
  totalItems: number; // Backend utilise totalItems
  total?: number; // Alias pour compatibilité
  totalPages: number;
  hasNext?: boolean;
  hasPrevious?: boolean;
}

// Types for MaVille entities
export interface Probleme {
  id: number;
  lieu: string;
  description: string;
  type: string;
  priorite: string;
  declarant: string;
  date: string;
}

export interface Travail {
  id: string;
  source: string;
  titre: string;
  description?: string;
  lieu: string;
  quartier: string;
  type: string;
  date_debut?: string;
  date_fin?: string;
  cout?: number;
  statut: string;
}

export interface Candidature {
  id: number;
  prestataire: string;
  statut: string;
  description: string;
  cout?: number;
  dateDebut?: string;
  dateFin?: string;
}

export interface Projet {
  id: number;
  description: string;
  statut: string;
  localisation: string;
  dateDebut?: string;
  dateFin?: string;
  cout?: number;
  prestataire?: {
    numeroEntreprise: string;
    nomEntreprise: string;
  };
}

export interface Notification {
  id?: string;
  message: string;
  lu: boolean;
  date: string;
  type: string;
  projetId?: number;
}

// API Client functions
async function fetchAPI<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });

    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
      let errorDetails: any = null;
      
      // Cloner la réponse pour pouvoir la lire plusieurs fois si nécessaire
      const responseClone = response.clone();
      
      try {
        // Essayer de lire comme JSON d'abord
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
          errorDetails = await response.json();
          if (errorDetails && typeof errorDetails === 'object') {
            if (errorDetails.message) {
              errorMessage = String(errorDetails.message);
            } else if (errorDetails.error) {
              errorMessage = String(errorDetails.error);
            } else if (Object.keys(errorDetails).length > 0) {
              // Si l'objet n'est pas vide mais n'a pas de message, utiliser une représentation
              errorMessage = JSON.stringify(errorDetails);
            }
          }
        } else {
          // Si ce n'est pas du JSON, lire comme texte
          const text = await responseClone.text();
          if (text && text.trim()) {
            errorMessage = text.trim();
          }
        }
      } catch (e) {
        // Si la lecture échoue, essayer de lire comme texte
        try {
          const text = await responseClone.text();
          if (text && text.trim()) {
            errorMessage = text.trim();
          }
        } catch (e2) {
          // Si tout échoue, utiliser le message par défaut
          if (response.status === 500) {
            errorMessage = 'Erreur interne du serveur. Vérifiez les logs du backend.';
          } else if (response.status === 404) {
            errorMessage = `Endpoint non trouvé: ${endpoint}`;
          } else if (response.status === 0 || response.status >= 500) {
            errorMessage = 'Erreur serveur. Vérifiez que le backend est démarré et accessible.';
          }
        }
      }
      
      const error = new Error(errorMessage);
      
      console.error('API Error:', {
        url,
        status: response.status,
        statusText: response.statusText,
        endpoint,
        errorDetails: errorDetails || 'Aucun détail disponible',
        errorMessage,
      });
      
      // Reporter l'erreur à Sentry si configuré
      reportError(error, {
        url,
        status: response.status,
        endpoint,
        errorDetails,
      });
      
      throw error;
    }

    // Vérifier que la réponse est bien du JSON avant de parser
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return response.json();
    } else {
      // Si ce n'est pas du JSON, essayer de parser quand même ou retourner le texte
      const text = await response.text();
      try {
        return JSON.parse(text);
      } catch {
        throw new Error(`Réponse inattendue du serveur: ${text.substring(0, 100)}`);
      }
    }
  } catch (error) {
    // Gérer les erreurs réseau (CORS, connexion refusée, etc.)
    if (error instanceof TypeError) {
      if (error.message.includes('fetch') || error.message.includes('Failed to fetch')) {
        const isLocalhost = url.includes('localhost') || url.includes('127.0.0.1');
        const errorMessage = isLocalhost
          ? 'Impossible de se connecter au serveur. Vérifiez que le backend est démarré sur http://localhost:7000'
          : 'Impossible de se connecter au serveur. Vérifiez que NEXT_PUBLIC_API_URL est configuré dans Vercel.';
        const networkError = new Error(errorMessage);
        console.error('Network Error:', {
          url,
          message: error.message,
          hint: isLocalhost 
            ? 'Vérifiez que le backend est démarré sur http://localhost:7000'
            : 'Configurez NEXT_PUBLIC_API_URL dans Vercel avec l\'URL Cloud Run',
        });
        reportError(networkError, { url, originalError: error.message });
        throw networkError;
      }
    }
    // Si c'est déjà une Error avec un message, la relancer
    if (error instanceof Error) {
      reportError(error, { url });
      throw error;
    }
    // Sinon, créer une nouvelle Error
    const unknownError = new Error(`Erreur inconnue: ${String(error)}`);
    reportError(unknownError, { url, originalError: String(error) });
    throw unknownError;
  }
}

// Health & Info
export async function getHealth() {
  return fetchAPI<{ status: string; message: string; version: string }>('/health');
}

// Residents
export async function getResidentsTravaux(params?: {
  quartier?: string;
  type?: string;
  page?: number;
  size?: number;
}) {
  const queryParams = new URLSearchParams();
  if (params?.quartier) queryParams.append('quartier', params.quartier);
  if (params?.type) queryParams.append('type', params.type);
  if (params?.page !== undefined) queryParams.append('page', params.page.toString());
  if (params?.size !== undefined) queryParams.append('size', params.size.toString());
  
  const query = queryParams.toString();
  return fetchAPI<PaginatedResponse<Travail>>(`/residents/travaux${query ? `?${query}` : ''}`);
}

export async function signalerProbleme(data: {
  residentId: string;
  lieu: string;
  description: string;
  type: string;
}) {
  return fetchAPI<{ success: boolean; message: string; problemeId: number; quartierAbonnement: string }>(
    '/residents/problemes',
    {
      method: 'POST',
      body: JSON.stringify(data),
    }
  );
}

export async function getResidentNotifications(email: string) {
  return fetchAPI<{ notifications: Notification[]; total: number; non_lues: number }>(
    `/residents/${encodeURIComponent(email)}/notifications`
  );
}

export interface Preferences {
  notificationsEmail: boolean
  notificationsQuartier: boolean
  notificationsType: string[]
}

export async function getResidentPreferences(email: string) {
  return fetchAPI<{ success: boolean; preferences: Preferences }>(
    `/residents/${encodeURIComponent(email)}/preferences`
  )
}

export async function updateResidentPreferences(email: string, preferences: Partial<Preferences>) {
  return fetchAPI<{ success: boolean; message: string; preferences: Preferences }>(
    `/residents/${encodeURIComponent(email)}/preferences`,
    {
      method: 'PUT',
      body: JSON.stringify(preferences),
    }
  )
}

// Providers
export async function getPrestatairesProblemes(params?: {
  quartier?: string;
  type?: string;
  page?: number;
  size?: number;
}) {
  const queryParams = new URLSearchParams();
  if (params?.quartier) queryParams.append('quartier', params.quartier);
  if (params?.type) queryParams.append('type', params.type);
  if (params?.page !== undefined) queryParams.append('page', params.page.toString());
  if (params?.size !== undefined) queryParams.append('size', params.size.toString());
  
  const query = queryParams.toString();
  return fetchAPI<PaginatedResponse<Probleme>>(`/prestataires/problemes${query ? `?${query}` : ''}`);
}

export async function soumettreCandidature(data: {
  prestataireId: string;
  description: string;
  dateDebut: string;
  dateFin: string;
  cout: number;
  problemesVises: number[];
}) {
  return fetchAPI<{ success: boolean; message: string; candidatureId: number }>(
    '/prestataires/candidatures',
    {
      method: 'POST',
      body: JSON.stringify(data),
    }
  );
}

export async function getPrestataireProjets(neq: string) {
  return fetchAPI<{ projets: Projet[]; total: number }>(
    `/prestataires/${encodeURIComponent(neq)}/projets`
  );
}

export async function getPrestataireNotifications(neq: string) {
  return fetchAPI<{ notifications: Notification[]; total: number }>(
    `/prestataires/${encodeURIComponent(neq)}/notifications`
  );
}

// STPM
export async function getStpmCandidatures(params?: { page?: number; size?: number }) {
  const queryParams = new URLSearchParams();
  if (params?.page !== undefined) queryParams.append('page', params.page.toString());
  if (params?.size !== undefined) queryParams.append('size', params.size.toString());
  
  const query = queryParams.toString();
  return fetchAPI<PaginatedResponse<Candidature>>(`/stpm/candidatures${query ? `?${query}` : ''}`);
}

export async function validerCandidature(id: number, accepter: boolean) {
  return fetchAPI<{ success: boolean; message: string; projetId?: number }>(
    `/stpm/candidatures/${id}/valider`,
    {
      method: 'PUT',
      body: JSON.stringify({ accepter }),
    }
  );
}

export async function getStpmProblemes(params?: { page?: number; size?: number }) {
  const queryParams = new URLSearchParams();
  if (params?.page !== undefined) queryParams.append('page', params.page.toString());
  if (params?.size !== undefined) queryParams.append('size', params.size.toString());
  
  const query = queryParams.toString();
  return fetchAPI<PaginatedResponse<Probleme>>(`/stpm/problemes${query ? `?${query}` : ''}`);
}

export async function modifierPrioriteProbleme(id: number, priorite: 'FAIBLE' | 'MOYENNE' | 'ELEVEE') {
  return fetchAPI<{ success: boolean; message: string }>(
    `/stpm/problemes/${id}/priorite`,
    {
      method: 'PUT',
      body: JSON.stringify({ priorite }),
    }
  );
}

export async function getStpmNotifications() {
  return fetchAPI<{ notifications: Notification[]; total: number }>('/stpm/notifications');
}

export async function clearAllStpmNotifications() {
  return fetchAPI<{ success: boolean; message: string; count: number }>(
    '/stpm/notifications',
    { method: 'DELETE' }
  );
}

export async function clearAllResidentNotifications(email: string) {
  return fetchAPI<{ success: boolean; message: string; count: number }>(
    `/residents/${encodeURIComponent(email)}/notifications`,
    { method: 'DELETE' }
  );
}

export async function clearAllPrestataireNotifications(neq: string) {
  return fetchAPI<{ success: boolean; message: string; count: number }>(
    `/prestataires/${encodeURIComponent(neq)}/notifications`,
    { method: 'DELETE' }
  );
}

// Montreal
export async function getMontrealTravaux() {
  return fetchAPI<{ travaux: Travail[]; total: number }>('/montreal/travaux');
}
