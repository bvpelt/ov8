drop table if exists bevoegdgezag;
drop view if exists locatie_geometrie_view;
drop table if exists locatie;
drop table if exists regeling;
drop table if exists regeling_opvolgervan;
drop table if exists regeling_regelingsgebied;
drop table if exists soortregeling;
drop table if exists ontwerpregeling;
drop table if exists soortstap;
drop table if exists procedurestap;
drop table if exists geo;
drop table if exists ontwerplocatie;
drop table if exists ontwerpregeling_regelingsgebied;
drop table if exists procedureverloop;
drop table if exists procedurestap;

delete from flyway_schema_history where installed_rank > 0;