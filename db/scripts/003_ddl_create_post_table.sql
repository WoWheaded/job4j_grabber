CREATE TABLE IF NOT EXISTS post(
    id serial PRIMARY KEY,
    name TEXT,
    text TEXT,
    link TEXT UNIQUE,
    created TIMESTAMP
);