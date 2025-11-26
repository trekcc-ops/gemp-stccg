FROM mariadb:lts AS mariadb

COPY ./gemp-module/docker/database_script.sql /docker-entrypoint-initdb.d
COPY ./gemp-module/docker/initial_user_setup.sql /docker-entrypoint-initdb.d
