-- create db
CREATE DATABASE search_db ENCODING 'win1251'
   lc_ctype='ru_RU.CP1251'
   lc_collate='ru_RU.CP1251'
  TEMPLATE template0;

-- create user
CREATE USER search_user WITH password 'search';

GRANT ALL privileges ON DATABASE search_db TO search_user;

-- create tables
\c search_db

CREATE SEQUENCE news_ids;
CREATE SEQUENCE statistic_ids;

CREATE TABLE news (
    id INTEGER PRIMARY KEY DEFAULT NEXTVAL('news_ids'), 
    news_text text, 
    data_type text,
    json text UNIQUE);

CREATE TABLE statistic (
    id INTEGER PRIMARY KEY DEFAULT NEXTVAL('statistic_ids'), 
    source text UNIQUE,
    news_number INTEGER);

CREATE OR REPLACE FUNCTION update_stats(i INTEGER, s text) RETURNS void AS $$
    DECLARE
        stat INTEGER;
    BEGIN
      SELECT news_number
      INTO stat
      FROM statistic
      WHERE source = s;

      IF stat IS NOT NULL THEN    
          UPDATE statistic
          SET news_number = stat + i
          WHERE source = s;
      ELSE
          INSERT INTO statistic(source, news_number)
          VALUES (s, i);
      END IF;
    END;
$$ LANGUAGE plpgsql;

GRANT ALL PRIVILEGES ON news TO search_user;
GRANT ALL PRIVILEGES ON news_ids TO search_user;
GRANT ALL PRIVILEGES ON statistic TO search_user;
GRANT ALL PRIVILEGES ON statistic_ids TO search_user;

