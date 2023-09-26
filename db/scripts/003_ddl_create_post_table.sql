CREATE TABLE IF NOT EXISTS post(
    id serial PRIMARY KEY,
    name TEXT,
    link TEXT UNIQUE,
    text TEXT,
    created TIMESTAMP
);