# --- !Ups

CREATE TABLE warehouses (
    id INT,
    name VARCHAR(50)
);

# --- !Downs

DROP TABLE IF EXISTS warehouses;