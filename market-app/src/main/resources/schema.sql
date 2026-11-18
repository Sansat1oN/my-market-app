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
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL UNIQUE REFERENCES items (id),
    count   INT    NOT NULL
);

CREATE TABLE IF NOT EXISTS orders
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS order_items
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders (id),
    item_id  BIGINT NOT NULL REFERENCES items (id),
    price    BIGINT NOT NULL,
    count    INT    NOT NULL
);
