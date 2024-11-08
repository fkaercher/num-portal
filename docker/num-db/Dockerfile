FROM postgres:16.2

ADD db-setup/numportal.sql /docker-entrypoint-initdb.d/00-numportal.sql
ADD docker/num-db/add-admin.sql /docker-entrypoint-initdb.d/01-add-admin.sql

ADD db-setup/numportal-attachment.sql /docker-entrypoint-initdb.d/
