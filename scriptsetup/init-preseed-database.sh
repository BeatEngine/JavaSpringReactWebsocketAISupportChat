# Create the database APPLICATION_DB owner POSTGRES_USER
ddlvar="CREATE DATABASE $APPLICATION_DB OWNER $POSTGRES_USER;"
set -e
psql -c "$ddlvar" -h localhost -p 5432 -U postgres