\c numportal
SET ROLE num;

CREATE TABLE num.flyway_schema_history (
                                           installed_rank integer NOT NULL,
                                           version character varying(50),
                                           description character varying(200) NOT NULL,
                                           type character varying(20) NOT NULL,
                                           script character varying(1000) NOT NULL,
                                           checksum integer,
                                           installed_by character varying(100) NOT NULL,
                                           installed_on timestamp without time zone DEFAULT now() NOT NULL,
                                           execution_time integer NOT NULL,
                                           success boolean NOT NULL
);


ALTER TABLE num.flyway_schema_history OWNER TO num;

ALTER TABLE ONLY num.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


CREATE INDEX flyway_schema_history_s_idx ON num.flyway_schema_history USING btree (success);


CREATE TABLE num.organization
(
    id          serial PRIMARY KEY,
    name        varchar(50) UNIQUE NOT NULL,
    description varchar(250)
);

CREATE TABLE num.user_details
(
    user_id         varchar(250) PRIMARY KEY,
    approved        boolean NOT NULL,
    organization_id integer references num.organization (id) ON DELETE NO ACTION ON UPDATE NO ACTION

);

INSERT INTO num.user_details
VALUES ('24041063-b809-4df5-9cf3-65f8a99226fa', true, null);
