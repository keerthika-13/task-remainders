CREATE DATABASE IF NOT EXISTS task_remainder;
USE task_remainder;

CREATE TABLE IF NOT EXISTS customers (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) DEFAULT 'active',
    PRIMARY KEY (id)
);

INSERT INTO customers (name, email, status)
VALUES
    ('Alice Johnson', 'alice@example.com', 'active'),
    ('Bob Smith', 'bob@example.com', 'active'),
    ('Carla Davis', 'carla@example.com', 'inactive')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    status = VALUES(status);
