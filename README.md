# Описание
- Веб-приложение «Витрина интернет-магазина» на Spring Boot
- Витрина товаров, карточка товара, корзина, оформление заказа, список заказов
- Учебный проект 5-го спринта

## Стек

- Java 21
- Spring Boot 3.5.5
- Spring Web MVC, Thymeleaf
- Spring Data JPA, Hibernate
- H2 (в памяти), Liquibase
- Maven
- Встроенный Tomcat
- JUnit 5
- Docker

## Сборка

```
mvn clean package
```

После сборки executable jar лежит в `target/my-market-app-0.0.1-SNAPSHOT.jar`


## Тесты

```
mvn test
```

- Юнит-тесты сервисов на моках без контекста Spring, юнит-тесты контроллеров на моках через `@WebMvcTest`, тесты репозиториев через `@DataJpaTest`, интеграционные тесты контроллеров через `@SpringBootTest`
- Тесты каждого вида наследуют общий базовый класс, поэтому контекст Spring поднимается три раза на весь прогон
- Таблицы и товары накатываются миграциями Liquibase при старте контекста


## Запуск и использование

```
java -jar target/my-market-app-0.0.1-SNAPSHOT.jar
```

- Приложение - встроенный Tomcat на порту 8080, открывается по адресу `http://localhost:8080`
- База данных H2 в памяти, после остановки приложения корзина и заказы не сохраняются
- Товары загружаются при старте из файла `src/main/resources/db/changelog/items.csv`, картинки лежат в `src/main/resources/images`


## Запуск в Docker

Сначала собрать jar, затем образ:

```
mvn clean package
docker build -t my-market-app .
docker run -p 8080:8080 my-market-app
```

Приложение открывается по адресу `http://localhost:8080`


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
