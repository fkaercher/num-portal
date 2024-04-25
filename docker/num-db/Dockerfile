FROM postgres:16.2

ADD db-setup/numportal.sql /docker-entrypoint-initdb.d/
ADD db-setup/numportal-attachment.sql /docker-entrypoint-initdb.d/
