# Онлайн-магазин на Spring MVC

Простое веб-приложение «Интернет-магазин» на чистом Spring Framework 6.2.2 с аутентификацией Spring Security, хранением данных в PostgreSQL и доступом к БД через чистый JDBC.
Конфигурация произведена через Java-аннотации

## Технологии

- **Java 17+**
- **Spring Framework 6.2.2** (MVC, Security)
- **JDBC** (без JdbcTemplate и ORM)
- **Thymeleaf** (`src/main/resources/templates`)
- **Log4j2** (`src/main/resources/log4j2.xml`)
- **PostgreSQL 13+**
- **Tomcat 11**
- **Maven 3.8+**
- **JUnit 5 + Mockito** для модульных тестов

---

## Предварительные требования

- Установлены **Java 17+** и **Maven 3.8+**
- Установлен и запущен **PostgreSQL 13+**
- Установлен и настроен **Apache Tomcat 11**
- Доступ к консоли/терминалу и Git

---

## Настройка базы данных

### 1. Создать базу данных
    psql -U postgres
    CREATE DATABASE online_shop;
    \q

### 2. Выполнить SQL-скрипт создания схемы
    psql -U postgres -d online_shop -f db/schema.sql

---

## Сборка и запуск приложения

### 1. Клонировать репозиторий
    git clone https://https://github.com/RomanEnsebaev/online-shop-project/tree/master
    cd online-shop-project

### 2. Настроить параметры БД
      jdbc.url=jdbc:postgresql://localhost:5432/onlineShop
      jdbc.user="ваш пользователь"
      jdbc.pass="ваш пароль"
Параметры базы данных хранятся в файле src/main/resources/application.properties  



### 3. Собрать WAR-файл
    mvn clean package
В каталоге target/ появится online-shop.war.

### 4. Развернуть в Tomcat
    cp target/online-shop.war $TOMCAT_HOME/webapps/
    $TOMCAT_HOME/bin/startup.sh
Приложение доступно по адресу http://localhost:8080/online-shop/.

### 5. Запустить тесты
    mvn test

## Использование

1. Откройте в браузере:
    http://localhost:8080/online-shop/login

2. Зарегистрируйтесь и войдите.
После авторизации откроется домашняя страница со списком товаров.

3. Добавьте товар в корзину.
На иконке корзины отобразится количество — кликните и оформите заказ.

4. Перейдите в раздел «Мои заказы» и подтвердите покупку.

