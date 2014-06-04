Конвертация html в json
================

Настройки
---------

Файл config.properties

- *host* - host сервера с БД;
- *port* - порт сервера с БД. значение по умолчанию для postgresql - **5432**;
- *db_name* - название БД;
- *user_name* - имя пользователя БД;
- *user_pass* - пароль пользователя БД;

Работа с БД
-----------

Накат БД:

```
sudo -u postgres psql < create_db.sql
```

Откат БД:

```
sudo -u postgres psql < drop_db.sql
```

Дамп БД:

```
sudo -u postgres pg_dump search_db > dump
```

Если скрипт генерации БД падает, возможно, нужно установить кодировку:

```
sudo locale-gen ru_RU.CP1251
```

, затем перепнуть postgres:

```
sudo service postgresql restart
```

Сборка приложения
-----------------

1. В консоли набрать следующую команду (если лень ставить maven, то можно попробовать собрать приложение средствами IDE):

  ```
  mvn clean package
  ```
2. Переместить в папку, где будет запускаться приложение, файл `target/search-html-to-json-jar-with-dependencies.jar`. Переименовать его в `search-html-to-json.jar` (ну или поправить скрипт run.sh).

3. Поместить в ту же папку файлы config.properties, create_db.sql, drop_db.sql, run.sh.

Альтернативный вариант получения собранного приложения
------------------------------------------------------

1. Распаковать архив search.tar.gz.

2. Порадоваться

Запуск приложения
-----------------

1. Указать в скрипте путь к файлу конфигурации.

2. Запустить:

  ```
  ./run.sh
  ```

3. Если скрипт не запускается:

  ```
  sudo chmod -R 777 run.sh
  chmod +x run.sh
  ```
4. Если коннект к бд не проходит - [wiki](http://help.ubuntu.ru/wiki/%D1%80%D1%83%D0%BA%D0%BE%D0%B2%D0%BE%D0%B4%D1%81%D1%82%D0%B2%D0%BE_%D0%BF%D0%BE_ubuntu_server/%D0%B1%D0%B0%D0%B7%D1%8B_%D0%B4%D0%B0%D0%BD%D0%BD%D1%8B%D1%85/postgresql), смотреть раздел *Настройка*.

