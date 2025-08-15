CREATE OR REPLACE VIEW locatie_geometrie_view AS
SELECT l.geometrieidentificatie,
       l.locatietype,
       l.noemer,
       l.ispons,
       l.versie,
       l.begininwerking,
       l.begingeldigheid,
       l.eindgeldigheid,
       l.tijdstipregistratie,
       l.eindregistratie,
       g.geometrie
FROM public.locatie AS l
         INNER JOIN
     public.geo AS g ON l.geometrieidentificatie = g.geoid;