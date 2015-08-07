# --- !Ups

CREATE TABLE stock_items (
    id INT AUTO_INCREMENT,
    product_id INT,
    warehouse_id INT,
    quantity INT,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE IF EXISTS stock_items;