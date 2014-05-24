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

Сборка приложения
-----------------

1. В консоли набрать следующую команду (если лень ставить maven, то можно попробовать собрать приложение средствами IDE):

  ```
  mvn clean package
  ```
2. Переместить в папку, где будет запускать приложение, файл `target/search-html-to-json-jar-with-dependencies.jar`. Переименовать его в `search-html-to-json.jar` (ну или поправить скрипт).

3. Поместить в ту же папку файлы config.properties, create_db.sql, drop_db.sql, run.sh.

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
