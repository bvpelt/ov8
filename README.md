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

```bash
# Save all regelingen
curl http://localhost:8080/save/regelingen

# Get history for known regelingen
curl http://localhost:8080/save/regelingenhistorie

# Get ontwerp regelingen
curl http://localhost:8080/save/ontwerp

# Get geometrieen
curl http://localhost:8080/save/geometrie
```
Check results
```sql
select count(*) from regeling;
--- # 1 1502 REGELINGEN
--- # 2 1985 REGELINGEN

select count(*) from ontwerpregeling;
--- # 1  437 ONTWERPREGELINGEN

select count(*) from locatie;
--- # 1 2072 locaties

select count(*) from ontwerplocatie;
--- 173

select count(*) from geo;
--- # 1 1121
```