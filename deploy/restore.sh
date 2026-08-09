#!/bin/bash
# ===========================================================================
# Restauration de la base de données d'Alerte
# ===========================================================================
# Restaure une archive produite par backup.sh.
#
#   /opt/alerte/restore.sh /opt/alerte/backups/alertmns_2026-08-09_030000.sql.gz
#
# ATTENTION : l'archive contient des instructions de suppression (--clean), donc la
# restauration ÉCRASE les données présentes. Le script demande confirmation.

set -euo pipefail

CONTAINER=alerte-postgres-1

if [ $# -ne 1 ]; then
    echo "Usage : $0 <archive.sql.gz>" >&2
    exit 1
fi

ARCHIVE="$1"

if [ ! -s "$ARCHIVE" ]; then
    echo "ERREUR : archive introuvable ou vide : $ARCHIVE" >&2
    exit 1
fi

# Lecture ciblée plutôt qu'un `source` du fichier : le .env n'est pas un script shell,
# et certaines de ses valeurs contiennent des espaces (voir backup.sh).
env_value() {
    grep -E "^$1=" /opt/alerte/.env | head -1 | cut -d= -f2- | sed 's/^"//; s/"$//'
}

POSTGRES_DB=$(env_value POSTGRES_DB)
POSTGRES_USER=$(env_value POSTGRES_USER)
POSTGRES_PASSWORD=$(env_value POSTGRES_PASSWORD)

echo "Restauration de $ARCHIVE dans la base « $POSTGRES_DB »."
echo "Les données actuelles seront écrasées."
read -r -p "Confirmer (oui/non) ? " REPONSE
if [ "$REPONSE" != "oui" ]; then
    echo "Restauration annulée."
    exit 0
fi

# L'application est arrêtée pendant l'opération : elle écrirait sinon dans une base en
# cours de reconstruction, et Hibernate échouerait sur des tables momentanément absentes.
echo "Arrêt de l'application…"
docker compose -f /opt/alerte/compose.yaml stop api

gunzip -c "$ARCHIVE" \
    | docker exec -i -e PGPASSWORD="$POSTGRES_PASSWORD" "$CONTAINER" \
        psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"

echo "Redémarrage de l'application…"
docker compose -f /opt/alerte/compose.yaml start api

echo "Restauration terminée."
