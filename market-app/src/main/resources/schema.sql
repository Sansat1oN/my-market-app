CREATE TABLE IF NOT EXISTS customers
(
    username      VARCHAR(255) PRIMARY KEY,
    password_hash VARCHAR(60)  NOT NULL
);

CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255)  NOT NULL,
    description VARCHAR(1000),
    img_path    VARCHAR(255),
    price       BIGINT        NOT NULL
);

CREATE TABLE IF NOT EXISTS cart_items
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL REFERENCES customers (username),
    item_id  BIGINT NOT NULL REFERENCES items (id),
    count    INT NOT NULL,
    UNIQUE (username, item_id)
);

CREATE TABLE IF NOT EXISTS orders
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL REFERENCES customers (username)
);

CREATE TABLE IF NOT EXISTS order_items
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders (id),
    item_id  BIGINT NOT NULL REFERENCES items (id),
    price    BIGINT NOT NULL,
    count    INT    NOT NULL
);
