-- create db
CREATE DATABASE search_db;

-- create user
CREATE USER search_user WITH password 'search';

GRANT ALL privileges ON DATABASE search_db TO search_user;

-- create table
\c search_db

CREATE SEQUENCE news_ids;

CREATE TABLE news (
    id INTEGER PRIMARY KEY DEFAULT NEXTVAL('news_ids'), 
    news_text text, 
    data_type text,
    json text UNIQUE);

GRANT ALL PRIVILEGES ON news TO search_user;
GRANT ALL PRIVILEGES ON news_ids TO search_user;

