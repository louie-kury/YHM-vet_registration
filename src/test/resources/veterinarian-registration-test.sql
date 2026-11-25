create table veterinarian (
    id bigint auto_increment primary key,
    uuid char(36) not null,
    account_uuid char(36) not null,
    name varchar(50) not null,
    major varchar(100) not null,
    specialty varchar(100) not null,
    license_no varchar(100) not null,
    is_available tinyint not null,
    status varchar(20) not null,
    status_reason varchar(255),
    created_at datetime not null,
    updated_at datetime not null
);

create table vet_hospital_affiliation (
    id bigint auto_increment primary key,
    uuid char(36) not null,
    vet_id bigint not null,
    hospital_id bigint not null,
    started_at datetime not null,
    ended_at datetime,
    current_flag tinyint,
    constraint ck_affiliation_test_failure check (hospital_id <> 30)
);
