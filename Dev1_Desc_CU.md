Dev1_Desc_CU

Acteurs : 
    Acteur principal :
        Residents : habitants de la ville, majoritairement les utilisateurs, inlus l AgentSTPM et le Prestataire.
    Acteurs secondaires : 
        AgentSTPM : Il reçoit les plaintes des residents et décide si elles sont valides ou non, depend des formulaires des residents
        Prestataire : Il effectue les réparations ou interventions demandées suite à une plainte approuvée.

Cas :
Soumettre une plainte : Les residents ont comme role d utiliser l appli pour signaler un problème ou une anomalie dans la region et remplire un formulaire qui le decrit.

Approbation d une plainte : l AgentSTPM a comme role d analyser les plaintes soumises par les residents et doit les approuver ou rejeter selon plusieurs facteurs.

Attribuer la plainte : Si la plainte est approuve par l Agent, elle est assignee a un prestataire responsable des reparations (plus haut niveau)

Confirmation de reparations : Le prestataire confirme donc que les reparations vont debuter ou sont pris en compte par la municipalite

Envoyer une notification : Le resident recois une notification pour lui confirmer le fait que sa demande a bien ete prise en compte.

Scenario/Description : 
Le resident ouvre l application, il va ensuite se diriger vers Soumettre une plainte ou il va donc remplire un formulaire qui contient toutes les informations necessaires pour aider a la resolution du probleme (type de probleme, localisation...) il click sur un bouton permettant d envoyer le formulaire qui sera pris en compte pas un agent ayant comme travail de confirmer la legitimiter de la plainte pour qu il puisse ensuite l envoyer a ses superieurs (prestataires) vont finamlemenet confirmer le tout (avec les municipaliter par exemple) et le resident sera notfie.

Extension : 
Localisation incorrecte
Infos manquantes
Photo flou
Probleme deja attribue 

Preconditions :
Agent authentifies
Une plainte doit avoir ete soumise
Droit d'acces elevee pour l Agent

Postconditions :
Les plaintes sont stockes dans le systeme
Approbation attendu par Agent/Prestataire
