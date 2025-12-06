// JavaScript pour la page Prestataire

// Charger les problèmes disponibles
async function loadProblemes() {
    const container = document.getElementById('problemes-container');
    if (!container) return;
    
    try {
        const problemes = await apiCall('/prestataires/problemes');
        
        if (problemes.length === 0) {
            container.innerHTML = '<div class="alert alert-info">Aucun problème disponible.</div>';
            return;
        }
        
        let html = '<div class="table-responsive"><table class="table table-hover">';
        html += '<thead><tr><th>ID</th><th>Lieu</th><th>Type</th><th>Priorité</th><th>Description</th><th>Date</th></tr></thead>';
        html += '<tbody>';
        
        problemes.forEach(probleme => {
            html += `<tr>
                <td><strong>#${probleme.id}</strong></td>
                <td>${probleme.lieu || 'N/A'}</td>
                <td>${probleme.typeProbleme || 'N/A'}</td>
                <td>${formatPriority(probleme.priorite || 'MOYENNE')}</td>
                <td>${probleme.description || 'N/A'}</td>
                <td>${formatDate(probleme.dateSignalement)}</td>
            </tr>`;
        });
        
        html += '</tbody></table></div>';
        container.innerHTML = html;
    } catch (error) {
        container.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// Charger mes projets
async function loadMesProjets() {
    const container = document.getElementById('mes-projets-container');
    const neq = document.getElementById('neq-projets').value;
    
    if (!neq) {
        showAlert('Veuillez entrer votre NEQ', 'warning');
        return;
    }
    
    try {
        const projets = await apiCall(`/prestataires/${encodeURIComponent(neq)}/projets`);
        
        if (projets.length === 0) {
            container.innerHTML = '<div class="alert alert-info">Aucun projet trouvé pour ce NEQ.</div>';
            return;
        }
        
        let html = '<div class="table-responsive"><table class="table table-hover">';
        html += '<thead><tr><th>ID</th><th>Localisation</th><th>Statut</th><th>Date Début</th><th>Date Fin</th><th>Coût</th></tr></thead>';
        html += '<tbody>';
        
        projets.forEach(projet => {
            html += `<tr>
                <td><strong>#${projet.id}</strong></td>
                <td>${projet.localisation || 'N/A'}</td>
                <td>${formatStatus(projet.statut || 'EN_ATTENTE')}</td>
                <td>${formatDate(projet.dateDebutPrevue)}</td>
                <td>${formatDate(projet.dateFinPrevue)}</td>
                <td>$${projet.coutEstime || '0'}</td>
            </tr>`;
        });
        
        html += '</tbody></table></div>';
        container.innerHTML = html;
    } catch (error) {
        container.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// Gérer le formulaire de candidature
document.getElementById('candidature-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const neq = document.getElementById('neq').value;
    const problemeId = parseInt(document.getElementById('probleme-id').value);
    const description = document.getElementById('description-candidature').value;
    const cout = parseFloat(document.getElementById('cout').value);
    const dateDebut = document.getElementById('date-debut').value;
    const dateFin = document.getElementById('date-fin').value;
    
    try {
        const response = await apiCall('/prestataires/candidatures', 'POST', {
            neq: neq,
            problemesVises: [problemeId],
            description: description,
            cout: cout,
            dateDebut: dateDebut,
            dateFin: dateFin
        });
        
        showAlert('Candidature soumise avec succès!', 'success');
        document.getElementById('candidature-form').reset();
    } catch (error) {
        showAlert('Erreur lors de la soumission: ' + error.message, 'danger');
    }
});

// Filtres
document.getElementById('filter-quartier')?.addEventListener('input', filterProblemes);
document.getElementById('filter-type')?.addEventListener('change', filterProblemes);

function filterProblemes() {
    const quartierFilter = document.getElementById('filter-quartier').value.toLowerCase();
    const typeFilter = document.getElementById('filter-type').value;
    const rows = document.querySelectorAll('#problemes-container tbody tr');
    
    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        const typeMatch = !typeFilter || row.textContent.includes(typeFilter);
        const quartierMatch = !quartierFilter || text.includes(quartierFilter);
        row.style.display = (typeMatch && quartierMatch) ? '' : 'none';
    });
}

// Charger les problèmes au chargement
if (document.getElementById('problemes-tab')?.classList.contains('active')) {
    loadProblemes();
}

