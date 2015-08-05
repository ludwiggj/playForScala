# --- !Ups

CREATE TABLE stock_items (
    id INT,
    product_id INT,
    warehouse_id INT,
    quantity INT)
;

# --- !Downs

DROP TABLE IF EXISTS stock_items;