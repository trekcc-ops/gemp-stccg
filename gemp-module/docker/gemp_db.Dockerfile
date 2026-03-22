FROM mariadb:lts AS mariadb

COPY ./gemp-module/docker/database_script.sql /docker-entrypoint-initdb.d