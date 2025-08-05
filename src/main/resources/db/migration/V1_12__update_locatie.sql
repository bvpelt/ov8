-- Add the new columns to the 'locatie' table.
-- The 'text' type in PostgreSQL is equivalent to a 'string' and is suitable for storing varying length character data.
ALTER TABLE locatie
    ADD COLUMN status TEXT,
     ADD COLUMN ontwerpbesluitid TEXT,
    ADD COLUMN technischid TEXT;

-- Create non-unique indexes on the newly added columns.
-- These indexes will improve the performance of queries that filter or sort by these columns.
-- The naming convention 'idx_<table>_<column>' is used for clarity.
CREATE INDEX idx_locatie_ontwerpbesluitid ON locatie (ontwerpbesluitid);
CREATE INDEX idx_locatie_technischid ON locatie (technischid);
