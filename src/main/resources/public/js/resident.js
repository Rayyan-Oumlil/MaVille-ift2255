// JavaScript pour la page Résident

// Gérer le formulaire de signalement
document.getElementById('probleme-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const email = document.getElementById('email').value;
    const rue = document.getElementById('rue').value;
    const quartier = document.getElementById('quartier').value;
    const type = document.getElementById('type').value;
    const description = document.getElementById('description').value;
    
    // Validation
    if (!email || !rue || !quartier || !type || !description) {
        showAlert('Veuillez remplir tous les champs obligatoires.', 'warning');
        return;
    }
    
    // Afficher un loader
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Envoi...';
    
    try {
        const response = await apiCall('/residents/problemes', 'POST', {
            email: email,
            rue: rue,
            quartier: quartier,
            typeProbleme: type,
            description: description
        });
        
        showAlert('Problème signalé avec succès! Vous recevrez des notifications sur ce problème.', 'success');
        document.getElementById('probleme-form').reset();
    } catch (error) {
        showAlert('Erreur lors du signalement: ' + error.message, 'danger');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
});

// Charger les travaux
async function loadTravaux() {
    const container = document.getElementById('travaux-container');
    if (!container) return;
    
    try {
        const travaux = await apiCall('/residents/travaux');
        
        if (travaux.length === 0) {
            container.innerHTML = '<div class="alert alert-info">Aucun travail trouvé.</div>';
            return;
        }
        
        let html = '<div class="table-responsive"><table class="table table-hover">';
        html += '<thead><tr><th>Localisation</th><th>Type</th><th>Statut</th><th>Date Début</th><th>Date Fin</th></tr></thead>';
        html += '<tbody>';
        
        travaux.forEach(travail => {
            html += `<tr>
                <td>${travail.localisation || 'N/A'}</td>
                <td>${travail.typeTravaux || 'N/A'}</td>
                <td>${formatStatus(travail.statut || 'EN_ATTENTE')}</td>
                <td>${formatDate(travail.dateDebutPrevue)}</td>
                <td>${formatDate(travail.dateFinPrevue)}</td>
            </tr>`;
        });
        
        html += '</tbody></table></div>';
        container.innerHTML = html;
    } catch (error) {
        container.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// Charger les notifications
async function loadNotifications() {
    const container = document.getElementById('notifications-container');
    if (!container) return;
    
    // Récupérer l'email depuis le formulaire ou utiliser un email par défaut
    const emailInput = document.getElementById('email');
    const email = emailInput?.value || 'marie.tremblay@email.com';
    
    try {
        const notifications = await apiCall(`/residents/${encodeURIComponent(email)}/notifications`);
        
        const countBadge = document.getElementById('notification-count');
        if (countBadge) {
            const unreadCount = notifications.filter(n => !n.lu).length;
            countBadge.textContent = unreadCount;
            countBadge.style.display = unreadCount > 0 ? 'inline' : 'none';
        }
        
        if (notifications.length === 0) {
            container.innerHTML = '<div class="alert alert-info"><i class="bi bi-info-circle"></i> Aucune notification.</div>';
            return;
        }
        
        // Trier par date (plus récentes en premier)
        notifications.sort((a, b) => {
            const dateA = new Date(a.dateCreation || 0);
            const dateB = new Date(b.dateCreation || 0);
            return dateB - dateA;
        });
        
        let html = '';
        notifications.forEach(notif => {
            const badgeClass = notif.lu ? 'bg-secondary' : 'bg-primary';
            const alertClass = notif.lu ? 'alert-light border' : 'alert-primary';
            html += `<div class="alert ${alertClass} mb-2">
                <div class="d-flex justify-content-between align-items-start">
                    <div class="flex-grow-1">
                        <span class="badge ${badgeClass} me-2">${notif.lu ? 'Lu' : 'Nouveau'}</span>
                        <strong>${notif.message || 'Notification'}</strong>
                        <br><small class="text-muted"><i class="bi bi-clock"></i> ${formatDate(notif.dateCreation)}</small>
                        ${notif.quartier ? `<br><small class="text-muted"><i class="bi bi-geo-alt"></i> ${notif.quartier}</small>` : ''}
                    </div>
                </div>
            </div>`;
        });
        
        container.innerHTML = html;
    } catch (error) {
        container.innerHTML = `<div class="alert alert-danger"><i class="bi bi-exclamation-triangle"></i> Erreur: ${error.message}</div>`;
    }
}

// Recherche de travaux
document.getElementById('search-travaux')?.addEventListener('input', (e) => {
    const searchTerm = e.target.value.toLowerCase();
    const rows = document.querySelectorAll('#travaux-container tbody tr');
    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
});

// Charger les données quand on change d'onglet
document.querySelectorAll('[data-bs-toggle="tab"]').forEach(tab => {
    tab.addEventListener('shown.bs.tab', (e) => {
        if (e.target.id === 'travaux-tab') {
            loadTravaux();
        } else if (e.target.id === 'notifications-tab') {
            loadNotifications();
        }
    });
});

// Charger les notifications au chargement si l'onglet est actif
if (document.getElementById('notifications-tab')?.classList.contains('active')) {
    loadNotifications();
}

