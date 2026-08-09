#!/bin/bash
# ===========================================================================
# Sauvegarde de la base de données d'Alerte
# ===========================================================================
# Produit une archive compressée horodatée dans /opt/alerte/backups, et supprime
# celles qui dépassent la durée de rétention.
#
# Installation :  sudo cp backup.sh /opt/alerte/ && chmod +x /opt/alerte/backup.sh
# Exécution   :   /opt/alerte/backup.sh
# Automatique :   voir la tâche planifiée décrite en fin de fichier.

# -e : toute commande en échec interrompt le script.
# -u : une variable non définie est une erreur, plutôt qu'une chaîne vide.
# -o pipefail : un échec au milieu d'un tube n'est pas masqué par le succès du dernier
#               maillon — sans cela, un pg_dump en échec suivi d'un gzip réussi produirait
#               une archive valide et vide, et le script signalerait un succès.
set -euo pipefail

BACKUP_DIR=/opt/alerte/backups
RETENTION_DAYS=14
CONTAINER=alerte-postgres-1

STAMP=$(date +%Y-%m-%d_%H%M%S)
FILE="$BACKUP_DIR/alertmns_$STAMP.sql.gz"

mkdir -p "$BACKUP_DIR"

# Identifiants lus dans le fichier de configuration de la pile : ils ne sont écrits qu'à un
# seul endroit, et ce script ne les duplique pas.
#
# La lecture est ciblée, clé par clé, et non un `source` du fichier entier. Motif : le .env
# n'est pas un script shell. Une valeur comme `ORGANISATION_NAME=Metz Numeric School` y est
# parfaitement valide pour Docker Compose, qui prend tout ce qui suit le `=` ; interprétée
# par le shell, elle devient une affectation suivie d'une commande inexistante.
env_value() {
    grep -E "^$1=" /opt/alerte/.env | head -1 | cut -d= -f2- | sed 's/^"//; s/"$//'
}

POSTGRES_DB=$(env_value POSTGRES_DB)
POSTGRES_USER=$(env_value POSTGRES_USER)
POSTGRES_PASSWORD=$(env_value POSTGRES_PASSWORD)

# pg_dump s'exécute DANS le conteneur : aucun client PostgreSQL n'est requis sur l'hôte,
# et la version de l'outil correspond nécessairement à celle du serveur — un pg_dump plus
# ancien que la base refuserait de s'exécuter.
docker exec -e PGPASSWORD="$POSTGRES_PASSWORD" "$CONTAINER" \
    pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists \
    | gzip > "$FILE"

# Une sauvegarde vide est un échec silencieux : mieux vaut aucune archive qu'une archive
# inutilisable, qui donnerait un faux sentiment de sécurité jusqu'au jour de la restauration.
if [ ! -s "$FILE" ]; then
    echo "ERREUR : sauvegarde vide, fichier supprimé." >&2
    rm -f "$FILE"
    exit 1
fi

# Rotation : au-delà de la rétention, les archives sont supprimées.
find "$BACKUP_DIR" -name 'alertmns_*.sql.gz' -mtime +"$RETENTION_DAYS" -delete

echo "Sauvegarde effectuée : $FILE ($(du -h "$FILE" | cut -f1))"

# ===========================================================================
# Automatisation
# ===========================================================================
# Sauvegarde quotidienne à 3 h du matin, journal dans /opt/alerte/backups/backup.log :
#
#   crontab -e
#   0 3 * * * /opt/alerte/backup.sh >> /opt/alerte/backups/backup.log 2>&1
