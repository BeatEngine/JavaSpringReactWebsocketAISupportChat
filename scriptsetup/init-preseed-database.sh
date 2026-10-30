# Create the database APPLICATION_DB owner POSTGRES_USER
ddlvar="CREATE DATABASE $APPLICATION_DB OWNER $POSTGRES_USER;"
state=$(psql -c "$ddlvar" -h localhost -p 5432 -U postgres 2>&1)
echo "INIT DB: $state"
