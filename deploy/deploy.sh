#!/bin/bash
# ===========================================================================
# Déploiement d'Alerte
# ===========================================================================
# Récupère les images publiées, redémarre la pile, et vérifie que l'application
# répond réellement avant de déclarer le déploiement réussi.
#
# Ce script est la SEULE commande que la clé de déploiement est autorisée à exécuter
# (restriction `command=` dans ~/.ssh/authorized_keys). Un jeton dérobé chez GitHub ne
# donnerait donc pas un accès au serveur, mais uniquement le droit de redéployer.

set -euo pipefail

cd /opt/alerte

env_value() {
    grep -E "^$1=" /opt/alerte/.env | head -1 | cut -d= -f2- | sed 's/^"//; s/"$//'
}
DOMAIN=$(env_value DOMAIN)

echo "== Récupération des images =="
docker compose pull

echo "== Redémarrage de la pile =="
docker compose up -d --remove-orphans

echo "== Vérification du service =="
# /api/auth/me répond 401 sans session. Ce code prouve davantage qu'une page d'accueil
# statique : le certificat est valide, Nginx relaie bien vers l'application, et Spring
# Security a démarré. Un 502 signalerait au contraire une API absente ou en erreur.
for tentative in $(seq 1 30); do
    CODE=$(curl -s -o /dev/null -w '%{http_code}' "https://$DOMAIN/api/auth/me" || true)
    if [ "$CODE" = "401" ]; then
        echo "L'application répond (tentative $tentative)."
        docker image prune -f > /dev/null
        docker compose ps
        exit 0
    fi
    sleep 2
done

echo "ERREUR : l'application n'a pas répondu dans les 60 secondes (dernier code : $CODE)." >&2
echo "--- Derniers journaux de l'API ---" >&2
docker compose logs --tail 40 api >&2
exit 1
