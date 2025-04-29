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
id int auto_increment,
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
);

select * from brand_base;

insert into sale_item_base (
	model, 
	brand, 
    description, 
    price, ramGb, 
    screenSizeInch, 
    storageGb, 
    color, 
    quantity
)
values (
	'Galaxy S23 Ultra', 
	'Samsung', 
	'512GB สีดำปีศาจ สภาพนางฟ้า 99% ไร้รอย แถมเคสแท้ แบตอึดสุดๆ รองรับปากกา S-Pen อุปกรณ์ครบกล่อง ประกันศูนย์เหลือ 6 เดือน ส่งฟรี',
	32900, 
	6, 
	6.7, 
	256, 
	'Phantom Black', 
	1
);

select * from sale_item_base;