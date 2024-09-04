FROM mariadb:10.5 AS MariaDB

COPY ./gemp-module/database_script.sql /docker-entrypoint-initdb.d
COPY ./initial_user_setup.sql /docker-entrypoint-initdb.d
