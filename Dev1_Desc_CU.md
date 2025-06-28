Dev1_Desc_CU

Acteurs : 
    Acteur principal :
        Residents : habitants de la ville, majoritairement les utilisateurs, inlus l AgentSTPM et le Prestataire.
    Acteurs secondaires : 
        AgentSTPM : Il reçoit les plaintes des residents et décide si elles sont valides ou non, depend des formulaires des residents
        Prestataire : Il effectue les réparations ou interventions demandées suite à une plainte approuvée.

Cas :
Rechercher des travaux :
Cette fonction permet au resident de chercher des travaux specifiques par quartier ou par type (routier, gaz, signalisation, etc.). C’est un acces rapide a l’information ciblee.

Consulter les travaux :
Le resident peut visualiser les projets planifies ou en cours dans sa ville. L’affichage peut etre filtre par quartier ou par type de travaux. Cela lui permet de s’informer et d’anticiper les impacts sur sa mobilite.

Signaler un probleme routier a la ville : Les residents ont comme role d utiliser l appli pour signaler un problème ou une anomalie dans la region et remplire un formulaire qui le decrit.

Affecter une priorite a un probleme : l AgentSTPM a comme role d analyser les plaintes soumises par les residents et peut les mettre en ordre de priorite

Accepter ou refuser des projets de travaux : Si la plainte est approuve par l Agent, elle est assignee a un prestataire responsable des reparations (plus haut niveau)

Mettre a jour les informations d'un projet :
Une fois accepte, le prestataire peut ;
modifier la desc, changer la date de fin, mettre a jour le statut du projet

Consulter la liste des problèmes routiers :
Permet au prestataire de voir les problemes en attente d’intervention. Les resultats peuvent etre filtres par type, quartier ou date de debut. C’est l’entree vers le processus de proposition de projets.

Soumettre une canditature a un projet : Le prestataire confirme donc que les reparations vont debuter ou sont pris en compte par la municipalite

Envoyer notification : Le resident recois une notification pour lui confirmer le fait que sa demande a bien ete prise en compte.

Scenario/Description : 
Le resident ouvre l application, il va ensuite se diriger vers 'signaler un probleme routier' ou il va donc remplire un formulaire qui contient toutes les informations necessaires pour aider a la resolution du probleme (type de probleme, localisation...) il click sur un bouton permettant d envoyer le formulaire qui sera pris en compte pas un AgentSTMP ayant comme travail d'accepter ou de refuser des projets de travaux soummis et ensuite changer la priorite des plaintes pour qu il puisse ensuite l envoyer a ses superieurs (prestataires) vont finamlemenet confirmer le tout (avec les municipaliter par exemple) ou sinon consulter la liste des problemes routiers ou il les modifier encore plus et le resident sera notfie quand sa demande est prise en compte.

Extension : 
Localisation incorrecte
Infos manquantes
Photo flou
Probleme deja attribue 

Preconditions :
L'Utilisateur a acces a l'application
Agent authentifies
Une plainte doit avoir ete soumise
Droit d'acces elevee pour l Agent

Postconditions :
Les plaintes sont stockes dans le systeme
Approbation attendu par Agent/Prestataire
