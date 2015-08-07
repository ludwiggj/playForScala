# --- !Ups

CREATE TABLE warehouses (
    id INT AUTO_INCREMENT,
    name VARCHAR(50),
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE IF EXISTS warehouses;