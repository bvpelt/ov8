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