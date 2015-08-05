# --- !Ups

CREATE TABLE products (
    id INT,
    ean INT,
    name VARCHAR(50),
    description VARCHAR(254)
);

# --- !Downs

DROP TABLE IF EXISTS products;