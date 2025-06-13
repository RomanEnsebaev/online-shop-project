CREATE TABLE users (
    user_id     SERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    TEXT         NOT NULL,         
    email       VARCHAR(120) UNIQUE,
    role        VARCHAR(10)  NOT NULL
                CHECK (role IN ('ADMIN','USER')),
    created_at  TIMESTAMP    DEFAULT NOW(),
    deleted_at  TIMESTAMP    NULL
);

CREATE TABLE products (
    product_id  SERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    description TEXT,
    price       NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    stock_qty   INTEGER       NOT NULL CHECK (stock_qty >= 0),
    active      BOOLEAN       DEFAULT TRUE,
    created_at  TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE carts (
    cart_id      SERIAL PRIMARY KEY,
    user_id      INTEGER NOT NULL
                 REFERENCES users(user_id) ON DELETE CASCADE,
    created_at   TIMESTAMP DEFAULT NOW()
);

alter table carts alter column user_id drop not null;
alter table carts add column session_id VARCHAR(64); 

CREATE TABLE cart_items (
    cart_id     INTEGER NOT NULL
                REFERENCES carts(cart_id)    ON DELETE CASCADE,
    product_id  INTEGER NOT NULL
                REFERENCES products(product_id),
    qty         INTEGER NOT NULL CHECK (qty > 0),
    PRIMARY KEY (cart_id, product_id)
);

CREATE TABLE orders (
    order_id    SERIAL PRIMARY KEY,
    user_id     INTEGER NOT NULL
                REFERENCES users(user_id),
    total       NUMERIC(12,2) NOT NULL CHECK (total >= 0),
    created_at  TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE order_items (
    order_id    INTEGER NOT NULL
                REFERENCES orders(order_id)  ON DELETE CASCADE,
    product_id  INTEGER NOT NULL
                REFERENCES products(product_id),
    price_at_purchase NUMERIC(10,2) NOT NULL CHECK (price_at_purchase >= 0),
    qty         INTEGER NOT NULL CHECK (qty > 0),
    PRIMARY KEY (order_id, product_id)
);

CREATE INDEX idx_users_username       ON users(username);
CREATE INDEX idx_products_active      ON products(active);
CREATE INDEX idx_cart_items_cart      ON cart_items(cart_id);
CREATE INDEX idx_order_user_created   ON orders(user_id, created_at DESC);


