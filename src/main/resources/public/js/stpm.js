// JavaScript pour la page STPM

let candidatureEnCours = null;
let problemeEnCours = null;

// Charger les statistiques
async function loadStats() {
    try {
        // Charger les problèmes
        const problemes = await apiCall('/prestataires/problemes');
        document.getElementById('stat-problemes').textContent = problemes.length || 0;
        
        // Charger les candidatures
        const candidatures = await apiCall('/stpm/candidatures');
        const enAttente = candidatures.filter(c => c.statut === 'SOUMISE').length;
        document.getElementById('stat-candidatures').textContent = enAttente;
        
        // Charger les projets
        // Note: Il faudrait un endpoint pour ça, pour l'instant on met 0
        document.getElementById('stat-projets').textContent = '5'; // Valeur par défaut
        
        // Notifications STPM
        // Note: Il faudrait un endpoint pour les notifications STPM
        document.getElementById('stat-notifications').textContent = '0';
    } catch (error) {
        console.error('Erreur chargement stats:', error);
    }
}

// Charger les candidatures
async function loadCandidatures() {
    const container = document.getElementById('candidatures-container');
    if (!container) return;
    
    try {
        const candidatures = await apiCall('/stpm/candidatures');
        const enAttente = candidatures.filter(c => c.statut === 'SOUMISE');
        
        if (enAttente.length === 0) {
            container.innerHTML = '<div class="alert alert-success">Aucune candidature en attente. Tout est à jour!</div>';
            return;
        }
        
        let html = '<div class="table-responsive"><table class="table table-hover">';
        html += '<thead><tr><th>ID</th><th>Prestataire</th><th>Problème(s)</th><th>Coût</th><th>Dates</th><th>Description</th><th>Action</th></tr></thead>';
        html += '<tbody>';
        
        enAttente.forEach(candidature => {
            const dateDebut = formatDate(candidature.dateDebut);
            const dateFin = formatDate(candidature.dateFin);
            
            html += `<tr>
                <td><strong>#${candidature.id}</strong></td>
                <td>${candidature.prestataire?.nomEntreprise || 'N/A'}</td>
                <td>${candidature.problemesVises?.join(', ') || 'N/A'}</td>
                <td>$${candidature.cout || '0'}</td>
                <td>${dateDebut} → ${dateFin}</td>
                <td>${candidature.description || 'N/A'}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="ouvrirModalValidation(${candidature.id})">
                        <i class="bi bi-check-circle"></i> Valider
                    </button>
                </td>
            </tr>`;
        });
        
        html += '</tbody></table></div>';
        container.innerHTML = html;
        
        // Mettre à jour les stats
        loadStats();
    } catch (error) {
        container.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// Ouvrir modal de validation
async function ouvrirModalValidation(candidatureId) {
    try {
        const candidatures = await apiCall('/stpm/candidatures');
        candidatureEnCours = candidatures.find(c => c.id === candidatureId);
        
        if (!candidatureEnCours) {
            showAlert('Candidature introuvable', 'danger');
            return;
        }
        
        const info = `
            <strong>Candidature #${candidatureEnCours.id}</strong><br>
            Prestataire: ${candidatureEnCours.prestataire?.nomEntreprise || 'N/A'}<br>
            Coût: $${candidatureEnCours.cout || '0'}<br>
            Dates: ${formatDate(candidatureEnCours.dateDebut)} → ${formatDate(candidatureEnCours.dateFin)}<br>
            Description: ${candidatureEnCours.description || 'N/A'}
        `;
        
        document.getElementById('modal-candidature-info').innerHTML = info;
        
        const modal = new bootstrap.Modal(document.getElementById('validationModal'));
        modal.show();
        
        // Afficher/masquer le champ motif selon la décision
        document.getElementById('decision-validation').addEventListener('change', (e) => {
            document.getElementById('motif-container').style.display = 
                e.target.value === 'refuser' ? 'block' : 'none';
        });
    } catch (error) {
        showAlert('Erreur: ' + error.message, 'danger');
    }
}

// Valider une candidature
async function validerCandidature() {
    if (!candidatureEnCours) return;
    
    const decision = document.getElementById('decision-validation').value;
    const accepter = decision === 'accepter';
    const motif = document.getElementById('motif-refus').value;
    
    try {
        const response = await apiCall(
            `/stpm/candidatures/${candidatureEnCours.id}/valider`,
            'PUT',
            {
                accepter: accepter,
                motif: accepter ? null : motif
            }
        );
        
        showAlert(response.message || 'Candidature traitée avec succès!', 'success');
        
        // Fermer le modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('validationModal'));
        modal.hide();
        
        // Recharger les candidatures
        loadCandidatures();
    } catch (error) {
        showAlert('Erreur: ' + error.message, 'danger');
    }
}

// Charger les problèmes
async function loadProblemes() {
    const container = document.getElementById('problemes-container');
    if (!container) return;
    
    try {
        const problemes = await apiCall('/prestataires/problemes');
        
        if (problemes.length === 0) {
            container.innerHTML = '<div class="alert alert-info">Aucun problème trouvé.</div>';
            return;
        }
        
        let html = '<div class="table-responsive"><table class="table table-hover">';
        html += '<thead><tr><th>ID</th><th>Lieu</th><th>Type</th><th>Priorité</th><th>Description</th><th>Date</th><th>Action</th></tr></thead>';
        html += '<tbody>';
        
        problemes.forEach(probleme => {
            html += `<tr>
                <td><strong>#${probleme.id}</strong></td>
                <td>${probleme.lieu || 'N/A'}</td>
                <td>${probleme.typeProbleme || 'N/A'}</td>
                <td>${formatPriority(probleme.priorite || 'MOYENNE')}</td>
                <td>${probleme.description || 'N/A'}</td>
                <td>${formatDate(probleme.dateSignalement)}</td>
                <td>
                    <button class="btn btn-sm btn-warning" onclick="ouvrirModalPriorite(${probleme.id})">
                        <i class="bi bi-pencil"></i> Modifier
                    </button>
                </td>
            </tr>`;
        });
        
        html += '</tbody></table></div>';
        container.innerHTML = html;
    } catch (error) {
        container.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// Ouvrir modal de priorité
async function ouvrirModalPriorite(problemeId) {
    try {
        const problemes = await apiCall('/prestataires/problemes');
        problemeEnCours = problemes.find(p => p.id === problemeId);
        
        if (!problemeEnCours) {
            showAlert('Problème introuvable', 'danger');
            return;
        }
        
        const info = `
            <strong>Problème #${problemeEnCours.id}</strong><br>
            Lieu: ${problemeEnCours.lieu || 'N/A'}<br>
            Type: ${problemeEnCours.typeProbleme || 'N/A'}<br>
            Priorité actuelle: ${formatPriority(problemeEnCours.priorite || 'MOYENNE')}<br>
            Description: ${problemeEnCours.description || 'N/A'}
        `;
        
        document.getElementById('modal-probleme-info').innerHTML = info;
        document.getElementById('nouvelle-priorite').value = problemeEnCours.priorite || 'MOYENNE';
        
        const modal = new bootstrap.Modal(document.getElementById('prioriteModal'));
        modal.show();
    } catch (error) {
        showAlert('Erreur: ' + error.message, 'danger');
    }
}

// Modifier la priorité
async function modifierPriorite() {
    if (!problemeEnCours) return;
    
    const nouvellePriorite = document.getElementById('nouvelle-priorite').value;
    
    try {
        const response = await apiCall(
            `/stpm/problemes/${problemeEnCours.id}/priorite`,
            'PUT',
            {
                priorite: nouvellePriorite
            }
        );
        
        showAlert('Priorité modifiée avec succès!', 'success');
        
        // Fermer le modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('prioriteModal'));
        modal.hide();
        
        // Recharger les problèmes
        loadProblemes();
    } catch (error) {
        showAlert('Erreur: ' + error.message, 'danger');
    }
}

// Charger les notifications STPM
async function loadNotifications() {
    const container = document.getElementById('notifications-container');
    if (!container) return;
    
    try {
        // Note: Il faudrait un endpoint spécifique pour les notifications STPM
        // Pour l'instant, on affiche un message
        container.innerHTML = '<div class="alert alert-info">Fonctionnalité en développement. Les notifications STPM seront disponibles prochainement.</div>';
    } catch (error) {
        container.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// Recherche de problèmes
document.getElementById('filter-problemes')?.addEventListener('input', (e) => {
    const searchTerm = e.target.value.toLowerCase();
    const rows = document.querySelectorAll('#problemes-container tbody tr');
    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
});

// Charger les données selon l'onglet actif
document.querySelectorAll('[data-bs-toggle="tab"]').forEach(tab => {
    tab.addEventListener('shown.bs.tab', (e) => {
        if (e.target.id === 'candidatures-tab') {
            loadCandidatures();
        } else if (e.target.id === 'problemes-tab') {
            loadProblemes();
        } else if (e.target.id === 'notifications-tab') {
            loadNotifications();
        }
    });
});

// Charger les stats et candidatures au démarrage
document.addEventListener('DOMContentLoaded', () => {
    loadStats();
    if (document.getElementById('candidatures-tab')?.classList.contains('active')) {
        loadCandidatures();
    }
});

