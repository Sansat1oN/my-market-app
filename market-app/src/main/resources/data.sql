MERGE INTO customers (username, password_hash) KEY (username)
VALUES ('buyer1', '$2b$10$S1UbCP2vseWrsMhWN/mXEuXMkb32X04zb/1przm7IfZz2Q2TOajxC'),
       ('buyer2', '$2b$10$2Z28jz5Rd5rwff49o7AS9OPAh79ZpMZpmpo/bzE74cuglWK8IZXd6');

INSERT INTO items (title, description, img_path, price)
VALUES ('Молоко ЛЕНТА пастеризованное', 'Молоко питьевое пастеризованное 2,5%, 900 мл', 'images/milk.jpg', 50),
       ('Кефир ПРОСТОКВАШИНО', 'Кефир 2,5%, без змж, 930 г', 'images/kefir.jpg', 95),
       ('Сметана ПРОСТОКВАШИНО', 'Сметана 15%, без змж, 300 г', 'images/sour-cream.jpg', 95),
       ('Творог ПРОСТОКВАШИНО', 'Творог 2%, без змж, 200 г', 'images/cottage-cheese.jpg', 120),
       ('Масса творожная РОСТАГРОЭКСПОРТ Особая', 'Масса творожная с изюмом 23%, без змж, 180 г', 'images/curd-mass.jpg', 90),
       ('Йогурт TEOS Греческий натуральный', 'Йогурт греческий 2%, без змж, 250 г', 'images/yogurt.jpg', 80),
       ('Сыр БЕЛЕБЕЕВСКИЙ', 'Сыр полутвёрдый 45%, без змж, 190 г', 'images/cheese.jpg', 200),
       ('Масло ЛЕНТА сливочное', 'Масло сладкосливочное 82,5%, высший сорт, без змж, 180 г', 'images/butter.jpg', 130),
       ('Сливки ДОМИК В ДЕРЕВНЕ', 'Сливки стерилизованные 20%, без змж, 200 г', 'images/cream.jpg', 80),
       ('Простокваша НАШЕЙ ДОЙКИ', 'Простокваша термостатная 4%, без змж', 'images/prostokvasha.jpg', 85);
