-- liquibase formatted sql
-- changeset stuart:000

CREATE TABLE geohash
(
    latitude      varchar(255) NOT NULL,
    longitude     varchar(255) NOT NULL,
    geohash       varchar(12)  NOT NULL,
    unique_prefix varchar(12)  NOT NULL,
    CONSTRAINT geohash_geohash_PK PRIMARY KEY (geohash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
