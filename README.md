# ov8
Loading data


## Database access

```sql
GRANT ALL PRIVILEGES ON SCHEMA public TO testuser;
GRANT ALL ON ALL TABLES IN SCHEMA public TO testuser;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO testuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO testuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO testuser;
```

test access

```bash
psql -U testuser -W ov8
```

## Postgis

Installing postgis https://trac.osgeo.org/postgis/wiki/UsersWikiPostGIS3UbuntuPGSQLApt

On a postgresql-17 installation on ubuntu

```bash
sudo apt install postgresql-17-postgis-3
sudo -u postgres psql
```

```sql

CREATE DATABASE ov8;
ALTER DATABASE ov8 SET search_path=public,postgis,contrib;
\connect ov8;

CREATE SCHEMA postgis;

CREATE EXTENSION postgis SCHEMA postgis;
SELECT postgis_full_version();
```

# Running
After building app

```
# Save all regelingen
http://localhost:8080/stream/save
http://localhost:8080/save/regelingen

# Get history for known regelingen
http://localhost:8080/history/proces
http://localhost:8080/save/regelingenhistorie

# Get ontwerp regelingen
http://localhost:8080/stream/saveontwerp
http://localhost:8080/save/ontwerp

# Get geometrieen
http://localhost:8080/geo/download
http://localhost:8080/save/geometrie

select count(*) from regeling;
--- # 1 1480 REGELINGEN
--- # 2 1972 REGELINGEN

select count(*) from ontwerpregeling;
--- # 1  430 ONTWERPREGELINGEN

select count(*) from locatie;
--- # 1 1983 locaties

select count(*) from geo;
--- # 1 1006
```