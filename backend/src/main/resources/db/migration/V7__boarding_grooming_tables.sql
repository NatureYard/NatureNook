create table if not exists boarding_order (
    id bigserial primary key,
    store_id bigint not null references store(id),
    member_id bigint not null references member(id),
    pet_id bigint not null references pet_profile(id),
    cage_no varchar(32),
    check_in_time timestamp not null,
    planned_check_out_time timestamp not null,
    actual_check_out_time timestamp,
    status varchar(32) not null default 'CHECKED_IN',
    total_fee numeric(12,2) not null default 0,
    remark varchar(255),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists boarding_daily_record (
    id bigserial primary key,
    boarding_order_id bigint not null references boarding_order(id),
    record_date date not null,
    feed_items jsonb,
    health_note varchar(255),
    exception_note varchar(255),
    staff_id bigint references staff(id),
    created_at timestamp not null default current_timestamp
);

create table if not exists boarding_exception_record (
    id bigserial primary key,
    boarding_order_id bigint not null references boarding_order(id),
    exception_type varchar(64) not null,
    description varchar(255) not null,
    staff_id bigint references staff(id),
    created_at timestamp not null default current_timestamp
);

create table if not exists grooming_order (
    id bigserial primary key,
    store_id bigint not null references store(id),
    member_id bigint not null references member(id),
    pet_id bigint not null references pet_profile(id),
    service_items jsonb,
    staff_id bigint references staff(id),
    scheduled_at timestamp,
    started_at timestamp,
    completed_at timestamp,
    status varchar(32) not null default 'BOOKED',
    total_fee numeric(12,2) not null default 0,
    photo_urls jsonb,
    remark varchar(255),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists grooming_service_record (
    id bigserial primary key,
    grooming_order_id bigint not null references grooming_order(id),
    service_name varchar(128) not null,
    note varchar(255),
    created_at timestamp not null default current_timestamp
);
