create database itb;
use itb;

drop table brand_base;
drop table sale_item_base;

create table brand_base (
id int auto_increment,
name varchar(30) character set utf8 not null,
websiteUrl varchar(40) character set utf8,
isActive boolean,
countryOfOrigin varchar(80) character set utf8,
createdOn datetime not null default current_timestamp,
updatedOn datetime not null default current_timestamp on update current_timestamp,

primary key brand_base(id),
check (trim(name) <> ''),
check (websiteUrl is null or trim(websiteUrl) <> ''),
check (countryOfOrigin is null or trim(countryOfOrigin) <> '')
);

create table sale_item_base (
id int,
model varchar(60) character set utf8 not null,
brand varchar(60) character set utf8 not null,
description varchar(200) character set utf8 not null,
price int not null,
ramGb int,
screenSizeInch decimal(10,2),
storageGb int,
color varchar(15) character set utf8,
quantity int not null default 1,
createdOn datetime not null default current_timestamp,
updatedOn datetime not null default current_timestamp on update current_timestamp,

primary key sale_item_base(id),
check (trim(model) <> ''),
check (trim(description) <> ''),
check (color is null or trim(color) <> '')
);

insert into brand_base(
	name,
    websiteUrl, 
    isActive, 
    countryOfOrigin
)
values (
	'Samsung', 
    'https://www.samsung.com', 
    TRUE, 
    'South Korea'
),
(
	'Apple',
	'https://www.apple.com',
	TRUE,
	'United States'
),
(
	'Xiaomi',
	'https://www.mi.com',
	TRUE,
	'China'
),
(
	'Huawei',
	'https://www.huawei.com',
	TRUE,
	'China'
),
(
	'OnePlus',
	'https://www.oneplus.com',
	TRUE,
	'China'
),
(
	'Sony',
	'https://www.sony.com',
	TRUE,
	'Japan'
),
(
	'LG',
	'https://www.lg.com',
	TRUE,
	'South Korea'
),
(
	'Xiaomi',
	'https://www.mi.com',
	TRUE,
	'China'
),
(
	'Nokia',
	'https://www.nokia.com',
	FALSE,
	'Finland'
),
(
	'Motorola',
	'https://www.motorola.com',
	FALSE,
	'United States'
),
(
	'OPPO',
	'https://www.oppo.com',
	TRUE,
	'China'
),
(
	'Vivo',
	'https://www.vivo.com',
	TRUE,
	'China'
),
(
	'ASUS',
	'https://www.asus.com',
	TRUE,
	'Taiwan'
),
(
	'Google',
	'https://store.google.com',
	TRUE,
	'United States'
),
(
	'Realme',
	'https://www.realme.com',
	TRUE,
	'China'
),
(
	'BlackBerry',
	'https://www.blackberry.com',
	TRUE,
	'Canada'
),
(
	'HTC',
	'https://www.htc.com',
	TRUE,
	'Taiwan'
),
(
	'ZTE',
	'https://www.zte.com',
	TRUE,
	'China'
),
(
	'Lenovo',
	'https://www.lenovo.com',
	TRUE,
	'China'
),
(
	'Honor',
	'https://www.hihonor.com',
	TRUE,
	'China'
),
(
	'Nothing',
	'https://nothing.tech',
	TRUE,
	'United Kingdom'
);

select * from brand_base;

insert into sale_item_base (
	id,
	model, 
	brand, 
    description, 
    price, 
	ramGb,
    screenSizeInch, 
    storageGb, 
    color, 
    quantity
)
values (
	1,
	'iPhone 14 Pro Max', 
	'Apple', 
	'ไอโฟนเรือธงรุ่นล่าสุด มาพร้อม Dynamic Island จอใหญ่สุดในตระกูล กล้องระดับโปร',
	42900, 
	6,
	6.7, 
	512, 
	'Space Black', 
	5
),
(
	2,
	'iPhone 14', 
	'Apple', 
	'ไอโฟนรุ่นใหม่ล่าสุด รองรับ 5G เร็วแรง ถ่ายภาพสวยทุกสภาพแสง',
	29700, 
	6,
	6.1, 
	256, 
	'Midnight', 
	8
),
(
	3,
	'iPhone 13 Pro', 
	'Apple', 
	'ไอโฟนรุ่นโปร จอ ProMotion 120Hz กลอ้ งระดับมืออาชีพ',
	33000, 
	6,
	6.1, 
	256, 
	'Sierra Blue', 
	3
),
(
	7,
	'iPhone SE 2022', 
	'Apple', 
	'ไอโฟนรุ่นโปร จอ ProMotion 120Hz กลอ้ งระดับมืออาชีพ',
	14190, 
	4,
	4.7, 
	64, 
	'Starlight', 
	15
),
(
	8,
	'iPhone 14 Plus', 
	'Apple', 
	'iPhone 14 Plus 128GB สี Starlight เครื่องศูนย์ไทยโมเดล TH แบต 100% มีกล่องครบ ประกันศูนย์ถึง พ.ย. 68 ส่งฟรี',
	29700, 
	6,
	6.7, 
	256, 
	'Blue', 
	7
),
(
	16,
	'Galaxy S23 Ultra', 
	'Samsung', 
	'Samsung Galaxy S23 Ultra 512GB สีดำาปีศาจ สภาพนางฟ้า 99% ไร้รอย แถมเคสแท้ แบตอึดสุดๆ รองรับปากกา S-Pen',
	32900, 
	null,
	7.6, 
	256, 
	null, 
	1
);

select * from sale_item_base;

