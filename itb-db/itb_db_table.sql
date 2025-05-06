create database if not exists itb;
use itb;

show databases;

drop table if exists sale_item_base;
drop table if exists brand_base;

create table if not exists brand_base (
id int auto_increment,
name varchar(30) character set utf8mb4 not null,
websiteUrl varchar(40) character set utf8mb4,
isActive boolean,
countryOfOrigin varchar(80) character set utf8mb4,
createdOn datetime not null default current_timestamp,
updatedOn datetime not null default current_timestamp on update current_timestamp,

primary key brand_base(id),
check (trim(name) <> ''),
check (websiteUrl is null or trim(websiteUrl) <> ''),
check (countryOfOrigin is null or trim(countryOfOrigin) <> '')
);

create table if not exists sale_item_base (
id int,
brand_id int,
model varchar(60) character set utf8mb4 not null,
description varchar(200) character set utf8mb4 not null,
price int not null,
ramGb int,
screenSizeInch decimal(10,2),
storageGb int,
color varchar(15) character set utf8mb4,
quantity int not null default 1,
createdOn datetime not null default current_timestamp,
updatedOn datetime not null default current_timestamp on update current_timestamp,

primary key sale_item_base(id),
foreign key sale_item_base(brand_id) references brand_base(id),
check (trim(model) <> ''),
check (trim(description) <> ''),
check (color is null or trim(color) <> '')
);

show tables;