# --- !Ups

CREATE TABLE products (
    id INT AUTO_INCREMENT,
    ean INT,
    name VARCHAR(50),
    description VARCHAR(254),
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE IF EXISTS products;