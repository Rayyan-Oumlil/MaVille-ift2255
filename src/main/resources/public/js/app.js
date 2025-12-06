// MaVille Frontend JavaScript
// Communique avec l'API Javalin

const API_BASE_URL = '/api';

// Fonction utilitaire pour les appels API
async function apiCall(endpoint, method = 'GET', body = null) {
    try {
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            }
        };
        
        if (body) {
            options.body = JSON.stringify(body);
        }
        
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// Charger les statistiques sur la page d'accueil
async function loadStats() {
    try {
        // Charger les problèmes
        const problemes = await apiCall('/prestataires/problemes');
        document.getElementById('stat-problemes').textContent = problemes.length || 0;
        
        // Charger les projets (si endpoint existe)
        // document.getElementById('stat-projets').textContent = projets.length || 0;
        
        // Charger les candidatures (si endpoint existe)
        // document.getElementById('stat-candidatures').textContent = candidatures.length || 0;
        
        // Stats par défaut pour l'instant
        document.getElementById('stat-projets').textContent = '5';
        document.getElementById('stat-candidatures').textContent = '5';
        document.getElementById('stat-residents').textContent = '5';
        
    } catch (error) {
        console.error('Erreur lors du chargement des stats:', error);
        // Afficher des valeurs par défaut en cas d'erreur
        document.getElementById('stat-problemes').textContent = '0';
        document.getElementById('stat-projets').textContent = '0';
        document.getElementById('stat-candidatures').textContent = '0';
        document.getElementById('stat-residents').textContent = '0';
    }
}

// Afficher un message d'alerte
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const container = document.querySelector('.container') || document.body;
    container.insertBefore(alertDiv, container.firstChild);
    
    // Auto-dismiss après 5 secondes
    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

// Formater une date
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-CA', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

// Formater une priorité
function formatPriority(priorite) {
    const priorities = {
        'FAIBLE': { text: 'Faible', class: 'priority-faible' },
        'MOYENNE': { text: 'Moyenne', class: 'priority-moyenne' },
        'ELEVEE': { text: 'Élevée', class: 'priority-elevee' },
        'URGENTE': { text: 'Urgente', class: 'priority-urgente' }
    };
    
    const priority = priorities[priorite] || { text: priorite, class: '' };
    return `<span class="${priority.class}"><strong>${priority.text}</strong></span>`;
}

// Formater un statut
function formatStatus(statut) {
    const statuses = {
        'EN_ATTENTE': { text: 'En attente', class: 'status-en-attente' },
        'EN_COURS': { text: 'En cours', class: 'status-en-cours' },
        'TERMINE': { text: 'Terminé', class: 'status-termine' },
        'SUSPENDU': { text: 'Suspendu', class: 'status-suspendu' }
    };
    
    const status = statuses[statut] || { text: statut, class: 'bg-secondary' };
    return `<span class="badge ${status.class}">${status.text}</span>`;
}

// Charger les stats au chargement de la page d'accueil
if (document.getElementById('stats-container')) {
    document.addEventListener('DOMContentLoaded', loadStats);
}

// Export pour utilisation dans d'autres fichiers
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { apiCall, showAlert, formatDate, formatPriority, formatStatus };
}

