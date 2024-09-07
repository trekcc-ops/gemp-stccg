FROM mariadb:10.5 AS MariaDB

COPY ./gemp-module/docker/database_script.sql /docker-entrypoint-initdb.d
COPY ./gemp-module/docker/initial_user_setup.sql /docker-entrypoint-initdb.d
