-- Assuming sellerid 3 and 4 exist in seller_account table.
USE itb;

-- Force Activate Account
UPDATE users_account SET isAcitve = 1;

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
