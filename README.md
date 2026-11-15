# Описание
- Проект из веб-приложения «Витрина интернет-магазина» и RESTful-сервиса платежей
- Витрина товаров, карточка товара, корзина, оформление заказа, список заказов
- Кеширование товаров в Redis, получение баланса и оплата через сервис платежей
- Учебный проект 7-го спринта

## Стек

- Java 21
- Spring Boot 3.5.5
- Spring WebFlux, Thymeleaf
- Spring Data R2DBC
- Spring Data Redis
- H2
- Redis
- OpenAPI Generator
- Maven
- Встроенный Netty
- JUnit 5
- Docker

## Сборка

```
mvn clean package
```

После сборки executable jar лежат в:

- `market-app/target/market-app-0.0.1-SNAPSHOT.jar`
- `payment-service/target/payment-service-0.0.1-SNAPSHOT.jar`


## Тесты

```
mvn test
```

- Тесты каждого вида наследуют общий базовый класс для кеширования контекстов Spring
- Таблицы и товары создаются скриптами `schema.sql` и `data.sql` при старте контекста, у тестов репозиториев своя база в памяти
- В интеграционных тестах витрины используется Embedded Redis на порту 6370, внешний Redis не нужен


## Запуск и использование

Запустить Redis и затем оба приложения в отдельных терминалах:

```
docker run -d --name my-market-redis -p 6379:6379 redis:7.4.2-bookworm
java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar
java -jar market-app/target/market-app-0.0.1-SNAPSHOT.jar
```

- Витрина открывается по адресу `http://localhost:8080`, сервис платежей работает на порту 8081
- Начальный баланс в сервисе платежей: 1000 рублей
- База данных H2 в памяти, после остановки приложения корзина и заказы не сохраняются
- Таблицы создаются при старте из `market-app/src/main/resources/schema.sql`, товары загружаются из `market-app/src/main/resources/data.sql`, картинки лежат в `market-app/src/main/resources/images`
- Товары кешируются в Redis на 2 минуты


## Запуск в Docker Compose

Jar собираются внутри образов, заранее собирать их не нужно:

```
docker compose up --build
```

Остановить все контейнеры:

```
docker compose down
```

## Сервис платежей

- `GET /balance` - получить текущий баланс
- `POST /payments` с JSON `{"amount": 100}` - списать сумму заказа и получить остаток


## Страницы

Товары:

- `GET /items?search=&sort=NO&pageNumber=1&pageSize=5` - витрина товаров с поиском, сортировкой и пагинацией
- `POST /items?id=&action=` - изменить количество товара в корзине со страницы витрины
- `GET /items/{id}` - страница товара
- `POST /items/{id}?action=` - изменить количество товара в корзине со страницы товара
- `GET /images/{name}` - картинка товара

Корзина:

- `GET /cart/items` - корзина
- `POST /cart/items?id=&action=` - изменить количество товара или удалить его из корзины

Заказы:

- `POST /buy` - оформить заказ из корзины
- `GET /orders` - список заказов
- `GET /orders/{id}` - страница заказа
