create database if not exists itb;
use itb;

-- Drop tables in reverse order of creation to avoid foreign key issues
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS sale_item_picture;
DROP TABLE IF EXISTS sale_item_base;
DROP TABLE IF EXISTS brand_base;
DROP TABLE IF EXISTS seller_picture;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS users_account;
DROP TABLE IF EXISTS buyer_account;
DROP TABLE IF EXISTS seller_account;

-- 1. Buyer
CREATE TABLE IF NOT EXISTS buyer_account (
    buyerid INT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(255) NOT NULL,
    fullname VARCHAR(255) NOT NULL,
    createdOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT ck_buyer_nickname CHECK (TRIM(nickname) <> ''),
    CONSTRAINT ck_buyer_fullname CHECK (TRIM(fullname) <> '')
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Seller
CREATE TABLE IF NOT EXISTS seller_account (
    sellerid INT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(255) NOT NULL,
    fullname VARCHAR(255) NOT NULL,
    mobile VARCHAR(255) NOT NULL,
    bankNumber VARCHAR(255) NOT NULL,
    bankName VARCHAR(255) NOT NULL,
    nationalId VARCHAR(255) NOT NULL,
    createdOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT ck_seller_nickname CHECK (TRIM(nickname) <> ''),
    CONSTRAINT ck_seller_fullname CHECK (TRIM(fullname) <> ''),
    CONSTRAINT ck_seller_mobile CHECK (TRIM(mobile) <> ''),
    CONSTRAINT ck_seller_bankNumber CHECK (TRIM(bankNumber) <> ''),
    CONSTRAINT ck_seller_bankName CHECK (TRIM(bankName) <> ''),
    CONSTRAINT ck_seller_nationalId CHECK (TRIM(nationalId) <> '')
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Users
CREATE TABLE IF NOT EXISTS users_account (
    uid INT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fullname VARCHAR(255) NOT NULL,
    buyerid INT NULL,
    sellerid INT NULL,
    isActive BOOLEAN NOT NULL DEFAULT FALSE,
    token_used VARCHAR(255),
    createdOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_buyer_user FOREIGN KEY (buyerid) REFERENCES buyer_account(buyerid) ON DELETE CASCADE,
    CONSTRAINT fk_seller_user FOREIGN KEY (sellerid) REFERENCES seller_account(sellerid) ON DELETE CASCADE,
    CONSTRAINT ck_user_nickname CHECK (TRIM(nickname) <> ''),
    CONSTRAINT ck_user_email CHECK (TRIM(email) <> ''),
    CONSTRAINT ck_user_password CHECK (TRIM(password) <> ''),
    CONSTRAINT ck_user_fullname CHECK (TRIM(fullname) <> '')
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Refresh Token
CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users_account(uid) ON DELETE CASCADE,
    CONSTRAINT ck_refresh_token CHECK (TRIM(token) <> '')
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Seller Picture
CREATE TABLE IF NOT EXISTS seller_picture (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sellerid INT NOT NULL,
    old_picture_name VARCHAR(255) NOT NULL,
    new_picture_name VARCHAR(255) NOT NULL,
    file_size_bytes INT NOT NULL,
    picture_order INT NOT NULL,
    createdOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seller_picture FOREIGN KEY (sellerid) REFERENCES seller_account(sellerid) ON DELETE CASCADE,
    CONSTRAINT ck_seller_file_size_bytes CHECK (file_size_bytes <= 2 * 1024 * 1024),
    CONSTRAINT ck_seller_old_picture_name CHECK (TRIM(old_picture_name) <> ''),
    CONSTRAINT ck_seller_new_picture_name CHECK (TRIM(new_picture_name) <> '')
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Brand
CREATE TABLE IF NOT EXISTS brand_base (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) CHARACTER SET utf8mb4 NOT NULL,
    websiteUrl VARCHAR(40) CHARACTER SET utf8mb4,
    isActive BOOLEAN,
    countryOfOrigin VARCHAR(80) CHARACTER SET utf8mb4,
    createdOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CHECK (TRIM(name) <> ''),
    CHECK (websiteUrl IS NULL OR TRIM(websiteUrl) <> ''),
    CHECK (countryOfOrigin IS NULL OR TRIM(countryOfOrigin) <> '')
);

-- 7. Sale Item
CREATE TABLE IF NOT EXISTS sale_item_base (
    id INT AUTO_INCREMENT PRIMARY KEY,
    brand_id INT,
    sellerid INT,
    model VARCHAR(60) CHARACTER SET utf8mb4 NOT NULL,
    description VARCHAR(200) CHARACTER SET utf8mb4 NOT NULL,
    price INT NOT NULL,
    ramGb INT,
    screenSizeInch DECIMAL(10,2),
    storageGb INT,
    color VARCHAR(15) CHARACTER SET utf8mb4,
    quantity INT NOT NULL,
    createdOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (brand_id) REFERENCES brand_base(id),
    FOREIGN KEY (sellerid) REFERENCES seller_account(sellerid) ON DELETE CASCADE
);

-- 8. Sale Item Picture
CREATE TABLE IF NOT EXISTS sale_item_picture (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    old_picture_name VARCHAR(255) NOT NULL,
    new_picture_name VARCHAR(255) NOT NULL,
    file_size_bytes INT NOT NULL,
    picture_order INT NOT NULL,
    createdOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sale_item_picture_item FOREIGN KEY (sale_id) REFERENCES sale_item_base(id) ON DELETE CASCADE,
    CONSTRAINT ck_file_size_bytes CHECK (file_size_bytes <= 2 * 1024 * 1024),
    CONSTRAINT ck_old_picture_name CHECK (TRIM(old_picture_name) <> ''),
    CONSTRAINT ck_new_picture_name CHECK (TRIM(new_picture_name) <> '')
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Orders
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    seller_id INT NOT NULL,
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    shipping_address VARCHAR(255) NOT NULL,
    order_note VARCHAR(255),
    order_status ENUM('COMPLETED', 'CANCELED') NOT NULL DEFAULT 'COMPLETED',
    createdOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users_account(uid) ON DELETE CASCADE,
    CONSTRAINT fk_orders_seller FOREIGN KEY (seller_id) REFERENCES seller_account(sellerid) ON DELETE CASCADE
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Order Item
CREATE TABLE IF NOT EXISTS order_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    sale_item_id INT NOT NULL,
    price INT NOT NULL,
    quantity INT NOT NULL,
    description VARCHAR(255),
    payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_sale FOREIGN KEY (sale_item_id) REFERENCES sale_item_base(id) ON DELETE CASCADE,
    CONSTRAINT ck_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_item_price CHECK (price >= 0)
) CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO brand_base (name, websiteUrl, isActive, countryOfOrigin) VALUES
('Samsung', 'https://www.samsung.com', 1, 'South Korea'),
('Apple', 'https://www.apple.com', 1, 'United States'),
('Xiaomi', 'https://www.mi.com', 1, 'China'),
('Huawei', 'https://www.huawei.com', 1, 'China'),
('OnePlus', 'https://www.oneplus.com', 1, 'China'),
('Sony', 'https://www.sony.com', 1, 'Japan'),
('LG', 'https://www.lg.com', 1, 'South Korea'),
('Nokia', 'https://www.nokia.com', 0, 'Finland'),
('Motorola', 'https://www.motorola.com', 0, 'United States'),
('OPPO', 'https://www.oppo.com', 1, 'China'),
('Vivo', 'https://www.vivo.com', 1, 'China'),
('ASUS', 'https://www.asus.com', 1, 'Taiwan'),
('Google', 'https://store.google.com', 1, 'United States'),
('Realme', 'https://www.realme.com', 1, 'China'),
('BlackBerry', 'https://www.blackberry.com', 1, 'Canada'),
('HTC', 'https://www.htc.com', 1, 'Taiwan'),
('ZTE', 'https://www.zte.com', 1, 'China'),
('Lenovo', 'https://www.lenovo.com', 1, 'China'),
('Honor', 'https://www.hihonor.com', 1, 'China'),
('Nothing', 'https://nothing.tech', 1, 'United Kingdom');

select * from brand_base;

INSERT INTO buyer_account (nickname, fullname)
VALUES ('Somchai', 'Somchai Jaidee');

INSERT INTO buyer_account (nickname, fullname)
VALUES ('Somkiat', 'Somkiat  Luckchart');

INSERT INTO seller_account (nickname, fullname, mobile, bankNumber, bankName, nationalId)
VALUES ('Somsuan', 'Somsuan Hundee', '0834567890', '0371234567', 'Bangkok Bank', '1000111100222');

INSERT INTO seller_account (nickname, fullname, mobile, bankNumber, bankName, nationalId)
VALUES ('Somsuk', 'Somsuk  Fundee', '0845678901', '2371234567', 'Siam Commercial Bank', '1000111100333');

INSERT INTO seller_account (nickname, fullname, mobile, bankNumber, bankName, nationalId)
VALUES ('Somsak', 'Somsak Saksit', '0856789012', '0373456789', 'Bangkok Bank', '1000111100444');

INSERT INTO users_account (nickname, email, password, fullname, buyerid, isActive)
VALUES ('Somchai', 'itbkk.somchai@ad.sit.kmutt.ac.th', '$argon2i$v=19$m=64,t=2,p=8$OFpneUU4YVpGek03bEoxeA$b40GGxEcupj063omldyX6w', 'Somchai Jaidee', 1, TRUE);

INSERT INTO users_account (nickname, email, password, fullname, buyerid, isActive)
VALUES ('Somkiat', 'itbkk.somkiat@ad.sit.kmutt.ac.th', '$argon2i$v=19$m=64,t=2,p=8$OFpneUU4YVpGek03bEoxeA$b40GGxEcupj063omldyX6w', 'Somkiat  Luckchart', 2, TRUE);

INSERT INTO users_account (nickname, email, password, fullname, sellerid, isActive)
VALUES ('Somsuan', 'itbkk.somsuan@ad.sit.kmutt.ac.th', '$argon2i$v=19$m=64,t=2,p=8$OFpneUU4YVpGek03bEoxeA$b40GGxEcupj063omldyX6w', 'Somsuan Hundee', 1, TRUE);

INSERT INTO users_account (nickname, email, password, fullname, sellerid, isActive)
VALUES ('Somsuk', 'itbkk.somsuk@ad.sit.kmutt.ac.th', '$argon2i$v=19$m=64,t=2,p=8$OFpneUU4YVpGek03bEoxeA$b40GGxEcupj063omldyX6w', 'Somsuk  Fundee', 2, TRUE);

INSERT INTO users_account (nickname, email, password, fullname, sellerid, isActive)
VALUES ('Somsak', 'itbkk.somsak@ad.sit.kmutt.ac.th', '$argon2i$v=19$m=64,t=2,p=8$OFpneUU4YVpGek03bEoxeA$b40GGxEcupj063omldyX6w', 'Somsak Saksit', 3, TRUE);

INSERT INTO sale_item_base (
	id, brand_id, model, description, price, ramGb, screenSizeInch, storageGb, color, quantity
) VALUES
(1, 2, 'iPhone 14 Pro Max', 'ไอโฟนเรือธงรุ่นล่าสุด มาพร้อม Dynamic Island จอใหญ่สุดในตระกูล กล้องระดับโปร', 42900, '6', '6.7', '512', 'Space Black', 5),
(2, 2, 'iPhone 14', 'ไอโฟนรุ่นใหม่ล่าสุด รองรับ 5G เร็วแรง ถ่ายภาพสวยทุกสภาพแสง', 29700, '6', '6.1', '256', 'Midnight', 8),
(3, 2, 'iPhone 13 Pro', 'ไอโฟนรุ่นโปร จอ ProMotion 120Hz กล้องระดับมืออาชีพ', 33000, '6', '6.1', '256', 'Sierra Blue', 3),
(4, 2, 'iPhone 13', 'Previous gen base model', 23100, '4', '6.1', '128', 'Pink', 10),
(5, 2, 'iPhone 12 Pro Max', '2020 flagship model', 29700, '6', '6.7', '256', 'Pacific Blue', 4),
(6, 2, 'iPhone 12', '2020 base model', 19800, '4', '6.1', '128', 'Purple', 6),
(7, 2, 'iPhone SE 2022', 'Budget-friendly model', 14190, '4', '4.7', '64', 'Starlight', 15),
(8, 2, 'iPhone 14 Plus', 'iPhone 14 Plus 128GB สี Starlight
เครื่องศูนย์ไทย โมเดล TH
แบต 100% มีกล่องครบ ประกันศูนย์ถึง พ.ย. 68
ส่งฟรี', 29700, '6', '6.7', '256', 'Blue', 7),
(9, 2, 'iPhone 13 mini', 'Compact previous gen', 19800, '4', '5.4', '128', 'Green', 5),
(10, 2, 'iPhone 12 mini', 'Compact 2020 model', 16500, '4', '5.4', '64', 'Red', 4),
(16, 1, 'Galaxy S23 Ultra', 'Samsung Galaxy S23 Ultra 512GB สีดำปีศาจ
สภาพนางฟ้า 99% ไร้รอย แถมเคสแท้ 
แบตอึดสุดๆ รองรับปากกา S-Pen
อุปกรณ์ครบกล่อง ประกันศูนย์เหลือ 6 เดือน
ส่งฟรี', 39600, null, '6.8', '512', null, 6),
(17, 1, 'Galaxy S23+', 'Premium flagship model', 33000, '8', '6.6', '256', 'Cream', 8),
(18, 1, 'Galaxy Z Fold4', 'สมาร์ทโฟนพับได้สุดล้ำ จอใหญ่เท่าแท็บเล็ต ทำงานได้หลากหลาย', 59400, '12', '7.6', '256', 'Phantom Green', 3),
(19, 1, 'Galaxy Z Flip4', 'Compact foldable', 33000, '8', '6.7', '128', 'Bora Purple', 5),
(20, 1, 'Galaxy A53 5G', 'มือถือ 5G สเปคดี กล้องเทพ แบตอึด คุ้มค่าน่าใช้', 14850, '6', '6.5', '128', 'Awesome Blue', 12),
(21, 1, 'Galaxy A33 5G', 'Budget 5G phone', 11550, '6', '6.4', '128', 'Awesome White', 15),
(22, 1, 'Galaxy S22', 'เรือธงตัวท็อปจาก Samsung พร้อม S Pen ในตัว กล้อง 200MP ซูมไกลสุด 100x', 26400, '8', '6.1', '128', 'Pink Gold', 7),
(23, 1, 'Galaxy M53', 'Mid-range performance', 14850, '6', '6.7', '128', 'Green', 9),
(24, 1, 'Galaxy A73 5G', 'Premium mid-range', 16500, '8', '6.7', '256', 'Gray', 6),
(25, 1, 'Galaxy S21 FE', 'Fan Edition model', 19800, '6', '6.4', '128', 'Olive', 8),
(31, 3, '13 Pro', 'เรือธงสเปคแรงจาก Xiaomi กล้องไลก้า ชาร์จไว 120W', 33000, '12', '6.73', '256', 'Black', 8),
(32, 3, '13T Pro', 'Xiaomi 13T Pro 12/512GB สี Meadow Green
ชิป Dimensity 9200+ เร็วแรง
กล้อง Leica ถ่ายรูปสวยขั้นเทพ
มีที่ชาร์จ 120W ครบกล่อง
จัดส่งฟรีทั่วประเทศ', 23100, '12', null
, null, 'Alpine Blue', 6),
(33, 3, 'POCO F5', 'มือถือสเปคเทพ เน้นเล่นเกม จอ 120Hz ราคาคุ้มค่า', 13200, '8', '6.67', '256', 'Carbon Black', 10),
(34, 3, 'Redmi Note 12 Pro', 'กล้องคมชัด 108MP แบตอึด ชาร์จเร็ว ราคาโดนใจ', 9900, '8', '6.67', '128', 'Sky Blue', 15),
(35, 3, '12T Pro', 'Previous flagship', 21450, '8', '6.67', '256', 'Cosmic Black', 5),
(36, 3, 'POCO X5 Pro', 'Mid-range performer', 9900, '8', '6.67', '128', 'Yellow', 12),
(37, 3, 'Redmi 12C', 'Budget friendly', 5940, '4', '6.71', '64', 'Ocean Blue', 20),
(38, 3, '12 Lite', 'Slim mid-range', 13200, '8', '6.55', '128', 'Lite Pink', 8),
(39, 3, 'POCO M5', 'Budget gaming', 7590, '6', '6.58', '128', 'Power Black', 14),
(40, 3, 'Redmi Note 11', 'Previous gen mid-range', 8250, '6', '6.43', '128', 'Star Blue', 10),
(46, 4, 'P60 Pro', 'กล้องเรือธงระดับเทพ เซ็นเซอร์ใหญ่พิเศษ ถ่ายภาพกลางคืนสวยเยี่ยม', 36300, '12', '6.67', '256', 'Rococo Pearl', 5),
(47, 4, 'Mate 50 Pro', 'เรือธงตระกูล Mate จอ OLED คมชัด ดีไซน์พรีเมียม', 42900, '8', '6.74', '256', 'Silver Black', 4),
(48, 4, 'nova 11 Pro', 'สมาร์ทโฟนดีไซน์สวย กล้องหน้าคู่ เน้นเซลฟี่ ชาร์จไว', 19800, '8', '6.78', '256', 'Green', 8),
(49, 4, 'P50 Pro', 'Previous flagship', 29700, '8', '6.6', '256', 'Cocoa Gold', 6),
(50, 4, 'nova 10', 'Stylish mid-range', 16500, '8', '6.67', '128', 'Starry Silver', 10),
(51, 4, 'Mate X3', 'Premium foldable', 66000, '12', '7.85', '512', 'Feather Gold', 3),
(52, 4, 'nova 9', 'Previous mid-range', 13200, '8', '6.57', '128', 'Starry Blue', 12),
(53, 4, 'P50 Pocket', 'Foldable fashion', 46200, '8', '6.9', '256', 'Premium Gold', 4),
(54, 4, 'nova Y70', 'Budget friendly', 9900, '4', '6.75', '128', 'Crystal Blue', 15),
(55, 4, 'Mate 40 Pro', 'Classic flagship', 26400, '8', '6.76', '256', 'Mystic Silver', 5),
(61, 12, 'ROG Phone 7', 'สมาร์ทโฟนเกมมิ่งสเปคโหด จอ 165Hz เสียงสเตอริโอคู่ แบตอึด', 33000, '16', '6.78', '512', 'Phantom Black', 4),
(62, 12, 'ROG Phone 6D', 'เกมมิ่งโฟนพลังแรง CPU Dimensity ระบายความร้อนเยี่ยม', 29700, '16', '6.78', '256', 'Space Gray', 5),
(63, 12, 'Zenfone 9', 'มือถือกะทัดรัด สเปคแรง กล้องกันสั่น ใช้ง่ายมือเดียว', 23100, '8', '5.9', '128', 'Midnight Black', 8),
(64, 12, 'ROG Phone 6', 'Previous gaming flagship', 29700, '12', '6.78', '256', 'Storm White', 6),
(65, 12, 'Zenfone 8', 'Previous compact flagship', 19800, '8', '5.9', '128', 'Obsidian Black', 7),
(66, 12, 'ROG Phone 5s', 'Gaming performance', 26400, '12', '6.78', '256', 'Phantom Black', 5),
(67, 12, 'Zenfone 8 Flip', 'Flip camera flagship', 26400, '8', '6.67', '256', 'Galactic Black', 4),
(68, 12, 'ROG Phone 5', 'Classic gaming phone', 23100, '12', '6.78', '256', 'Storm White', 6),
(69, 12, 'Zenfone 7', 'Flip camera classic', 19800, '8', '6.67', '128', 'Aurora Black', 5),
(70, 12, 'ROG Phone 3', 'Legacy gaming phone', 16500, '12', '6.59', '256', 'Black Glare', 3),
(76, 10, 'Find X6 Pro', 'กล้องเทพระดับมืออาชีพ ชิป Snapdragon 8 Gen 2 ชาร์จไว 100W', 33000, '12', '6.82', '256', 'Cosmos Black', 5),
(77, 10, 'Reno9 Pro+', 'OPPO Reno9 Pro+ 5G 256GB สี Glossy Purple
สวยสะดุดตา ใช้งานลื่นสุดๆ
แบต 4700 mAh รองรับชาร์จไว
ครบกล่อง + ใบเสร็จศูนย์
ส่งฟรี Flash Express', 23100, '12', '6.7', '256', 'Eternal Gold', 8),
(78, 10, 'Find N2 Flip', 'สมาร์ทโฟนพับได้สุดหรู จอนอกใหญ่พิเศษ กล้องคู่คมชัด', 33000, '8', '6.8', '256', 'Astral Black', 4),
(79, 10, 'Reno8 Pro', 'ดีไซน์บางเบา กล้องคมชัด ชาร์จเร็วสุด ระบบเสียงดี', 19800, '8', '6.7', '256', 'Glazed Green', 10),
(80, 10, 'Find X5 Pro', 'Previous flagship', 29700, '12', '6.7', '256', 'Ceramic White', 6),
(81, 10, 'A78', 'Mid-range performer', 9900, '8', '6.56', '128', 'Glowing Black', 15),
(82, 10, 'Reno7', 'Style focused mid-range', 13200, '8', '6.43', '128', 'Startrails Blue', 12),
(83, 10, 'Find X5 Lite', 'Previous gen lite', 14850, '8', '6.43', '128', 'Starry Black', 8),
(84, 10, 'A77', 'Budget friendly', 8250, '6', '6.56', '128', 'Ocean Blue', 20),
(85, 10, 'Reno6 Pro', 'Classic premium', 16500, '12', '6.55', '256', 'Arctic Blue', 7);

-- Update sale_item_base by setting the appropriate sellerid for each item id
UPDATE sale_item_base SET sellerid = 1 WHERE id = 1;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 2;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 3;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 4;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 5;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 6;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 7;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 8;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 9;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 10;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 16;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 17;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 18;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 19;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 20;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 21;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 22;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 23;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 24;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 25;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 31;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 32;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 33;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 34;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 35;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 36;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 37;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 38;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 39;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 40;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 46;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 47;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 48;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 49;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 50;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 51;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 52;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 53;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 54;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 55;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 61;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 62;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 63;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 64;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 65;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 66;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 67;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 68;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 69;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 70;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 76;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 77;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 78;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 79;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 80;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 81;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 82;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 83;
UPDATE sale_item_base SET sellerid = 1 WHERE id = 84;
UPDATE sale_item_base SET sellerid = 2 WHERE id = 85;

-- Verify the update (Optional)
SELECT id, sellerid, model, brand_id FROM sale_item_base WHERE sellerid IS NOT NULL ORDER BY id;

select * from sale_item_base;


-- test join relationship table
select s.model from brand_base b
join sale_item_base s on b.id = s.brand_id
where b.id = 1;

alter table sale_item_base
add unique (id);

alter table sale_item_base
modify column model varchar(60) character set utf8mb4 not null;

alter table sale_item_base
modify column color varchar(40) character set utf8mb4;

alter table sale_item_base
modify column description text character set utf8mb4 not null;

select * from sale_item_base;
