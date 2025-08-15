
--- subset van regeling met soortregeling, bevoegdgezag en locatie
select r.identificatie, r.tijdstipregistratie, r.eindregistratie, r.begingeldigheid, r.eindgeldigheid, r.begininwerking, s.waarde, b.naam, l.noemer, l.identificatie, l.geometrieidentificatie
from regeling r, soortregeling s, bevoegdgezag b, locatie l, regeling_regelingsgebied rr
where r.soortregeling_id = s.id and b.id = r.bevoegdgezag_id and r.id = rr.regeling_id and l.id=rr.locatie_id
limit 5;

--- locatie gegevens
select identificatie, geometrieidentificatie, locatietype, noemer
from locatie;


--- subset van regeling met soortregeling, bevoegdgezag en locatie
    select r.identificatie, r.versie, r.tijdstipregistratie, r.begingeldigheid, s.waarde, b.code, l.identificatie
    from regeling r, soortregeling s, bevoegdgezag b, locatie l, regeling_regelingsgebied rr
    where
        r.soortregeling_id = s.id and
        b.id = r.bevoegdgezag_id and
        r.id = rr.regeling_id and
        l.id=rr.locatie_id and
        r.versie > 1 and
        s.code = '/join/id/stop/regelingtype_003' --- Omgevingsplan
    ;

select count(*) as aantal, parent_group_id from locatie group by parent_group_id order by parent_group_id;
select sum(aantal) from (select count(*) as aantal, parent_group_id from locatie group by parent_group_id order by parent_group_id);

select count(*) as aantal, locatietype from locatie group by locatietype order by locatietype;
select id, identificatie, parent_group_id, minx, miny, maxx, maxy from locatie where parent_group_id in (select id from locatie where locatietype = 'GEBIEDENGROEP') order by parent_group_id;

select l1.id, l1.locatietype, l1.noemer
from locatie l1
where l1.ispons = true and
      l1.parent_group_id in (select l2.id from locatie l2 where l2.ispons = true and l2.locatietype='GEBIEDENGROEP');

select l1.id, l1.parent_group_id, l1.locatietype, l1.noemer, l2.noemer
from locatie l1, locatie l2
where l1.ispons = true and
      l1.parent_group_id = l2.id and
      l2.ispons = true and
      l2.locatietype = 'GEBIEDENGROEP';

select t.aantal, t.bevoegdgezag_id, b.code, b.naam
from
    (select count(*) as aantal, bevoegdgezag_id
     from ontwerpregeling
     group by bevoegdgezag_id) t, bevoegdgezag b
where t.bevoegdgezag_id = b.id
order by t.aantal desc;

-- unique ontwerpbesluitidentificatie ?
select aantal, ontwerpbesluitidentificatie
from
    (select count(*) as aantal, ontwerpbesluitidentificatie
     from ontwerpregeling
     group by ontwerpbesluitidentificatie)
order by aantal desc;

-- unique technischid ?
select aantal, technischid
from
    (select count(*) as aantal, technischid
     from ontwerpregeling
     group by technischid)
order by aantal desc;

-- unique expressionid ?
select aantal, expressionid
from
    (select count(*) as aantal, expressionid
     from ontwerpregeling
     group by expressionid)
order by aantal desc;

-- unique publicatieid ?
select aantal, publicatieid
from
    (select count(*) as aantal, publicatieid
     from ontwerpregeling
     group by publicatieid)
order by aantal desc;

-- postgresql
-- show size of database 'ov8'
SELECT pg_size_pretty(pg_database_size('ov8'));

-- show dead tuples
SELECT
    schemaname,
    relname,  -- tablename
    n_dead_tup,
    n_live_tup,
    round(n_dead_tup::numeric/NULLIF(n_live_tup + n_dead_tup, 0) * 100, 2) as dead_percentage
FROM pg_stat_user_tables
WHERE n_dead_tup > 0
ORDER BY n_dead_tup DESC;

-- return space to OS for table locatie
VACUUM FULL locatie;

-- show tablesize of all tables
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE schemaname NOT IN ('information_schema', 'pg_catalog')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- regular maintenance
-- Weekly maintenance script
VACUUM ANALYZE;  -- Reclaim space and update statistics
REINDEX DATABASE ov8;  -- Rebuild indexes monthly for database ov8

-- oppervlakte voor locaties
select l.noemer, ST_AREA(g.geometrie)/1000000 from locatie l, geo g where l.geometrieidentificatie = g.geoid and l.noemer is not null order by l.noemer;

-- show geometrie
-- as string
select geoid, ST_AsText(geometrie) from geo where id = 1;
select geoid, geometrie from geo where id = 1;

select geoid, ST_NRings(geometrie)
from geo
where ST_GeometryType(geometrie) = 'ST_MultiPolygon'
order by ST_NRings(geometrie) DESC;

select noemer, geoid, ST_NRings(geometrie), ST_Area(geometrie) /1000000
from geo, locatie
where ST_GeometryType(geometrie) = 'ST_MultiPolygon' AND
    geoid = geometrieidentificatie
order by ST_NRings(geometrie) DESC;

-- welke geometrieen overlappen
select g1.id, g2.id
from geo g1, geo g2
where g1.id <> g2.id and ST_Intersects(g1.geometrie, g2.geometrie)
order by g1.id, g2.id;


select o.identificatie, o.versie, pvl.bekendop,  pvl.ontvangenop, ps.id, ps.voltooidop, ps.actor, s.id, s.waarde, s.code
        from ontwerpregeling o, procedureverloop pvl, procedurestap ps, soortstap s
        where pvl.ontwerpregeling_id = o.id and
            ps.procedureverloop_id = pvl.id and
            s.id = ps.soortstap_id
        order by
            o.identificatie,
            o.versie,
            ps.voltooidop,
            ps.id;

--
-- aggregeer bounding box van gebieden in een gebiedengroep
--
with box as (
    SELECT l.id as id, BOX2d(ST_Union(g.geometrie)) AS bounding_box
    FROM locatie l
             JOIN locatie lg ON lg.parent_group_id = l.id
             JOIN geo g ON g.geoid = lg.geometrieidentificatie

    WHERE l.locatietype = 'GEBIEDENGROEP'
      AND lg.locatietype = 'GEBIED'
    group by l.id )
select box.id,
       st_xmin(box.bounding_box) as xmin,
       st_xmax(box.bounding_box) as xmax,
       st_ymin(box.bounding_box) as ymin,
       st_ymax(box.bounding_box) as ymax
from box;

--
-- update boundingbox van gebiedengroepen
WITH box AS (
    SELECT l.id AS id,
           st_xmin(BOX2D(ST_Union(g.geometrie))) AS xmin,
           st_xmax(BOX2D(ST_Union(g.geometrie))) AS xmax,
           st_ymin(BOX2D(ST_Union(g.geometrie))) AS ymin,
           st_ymax(BOX2D(ST_Union(g.geometrie))) AS ymax
    FROM locatie l
             JOIN locatie lg ON lg.parent_group_id = l.id
             JOIN geo g ON g.geoid = lg.geometrieidentificatie
    WHERE l.locatietype = 'GEBIEDENGROEP'
      AND lg.locatietype = 'GEBIED'
    GROUP BY l.id
)
UPDATE locatie
SET minx = box.xmin,
    maxx = box.xmax,
    miny = box.ymin,
    maxy = box.ymax
FROM box
WHERE locatie.id = box.id;

-- select bounding box
--
WITH box AS (
    SELECT groep.id AS id,
           ST_XMin(bbox) AS xmin,
           ST_XMax(bbox) AS xmax,
           ST_YMin(bbox) AS ymin,
           ST_YMax(bbox) AS ymax
    FROM (
             SELECT l.id, BOX2D(ST_Union(g.geometrie)) AS bbox
             FROM locatie l
                      JOIN locatie lg ON lg.parent_group_id = l.id
                      JOIN geo g ON g.geoid = lg.geometrieidentificatie
             WHERE l.locatietype = 'GEBIEDENGROEP'
               AND lg.locatietype = 'GEBIED'
             GROUP BY l.id
         ) AS groep
)
UPDATE locatie
SET minx = box.xmin,
    maxx = box.xmax,
    miny = box.ymin,
    maxy = box.ymax
FROM locatie l, box
WHERE locatie.id = box.id;