CREATE DATABASE user_accounts;
USE user_accounts;


CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    pass VARCHAR(255),
    username VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    date_of_birth DATE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    verification_code VARCHAR(255),
    gender ENUM('MALE', 'FEMALE'),
    reset_token VARCHAR(255),
    reset_token_expiry DATETIME,
    token_expiration DATETIME,
    updated_at DATETIME
);

CREATE TABLE emergency_contacts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


CREATE TABLE authorities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    authority VARCHAR(255) NOT NULL
);
