create table hospital (
    id bigint primary key,
    uuid char(36) not null,
    name varchar(100) not null,
    road_address varchar(255),
    lot_address varchar(255),
    tel varchar(20)
);

insert into hospital (id, uuid, name, road_address, lot_address, tel) values
    (30, '12345678-1234-4234-a234-123456789030', '나래동물병원', '부산 바다로 30', null, null),
    (20, '12345678-1234-4234-a234-123456789020', '가온동물병원', '서울 별길 20', '서울 별동 20', '02-555-0020'),
    (10, '12345678-1234-4234-a234-123456789010', '가온동물병원', '서울 달길 10', '서울 달동 10', '02-555-0010');
