create table if not exists store (
    id bigserial primary key,
    code varchar(32) not null unique,
    name varchar(128) not null,
    city varchar(64),
    address varchar(255),
    business_hours jsonb,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists staff (
    id bigserial primary key,
    store_id bigint references store(id),
    name varchar(64) not null,
    phone varchar(32) not null unique,
    role_code varchar(32) not null,
    status varchar(32) not null default 'ACTIVE',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists member (
    id bigserial primary key,
    store_id bigint references store(id),
    name varchar(64) not null,
    phone varchar(32) not null unique,
    level varchar(32) not null default 'NORMAL',
    face_bound boolean not null default false,
    risk_tag varchar(32) not null default 'NORMAL',
    wx_openid varchar(64),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists pet_profile (
    id bigserial primary key,
    member_id bigint not null references member(id),
    name varchar(64) not null,
    species varchar(32) not null,
    breed varchar(64),
    gender varchar(16),
    birthday date,
    weight numeric(8,2),
    notes jsonb,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists membership_card (
    id bigserial primary key,
    member_id bigint not null references member(id),
    card_type varchar(32) not null,
    store_id bigint references store(id),
    status varchar(32) not null,
    valid_from timestamp not null,
    valid_to timestamp not null,
    face_bound boolean not null default false,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists reservation (
    id bigserial primary key,
    member_id bigint not null references member(id),
    store_id bigint not null references store(id),
    pet_id bigint references pet_profile(id),
    reservation_type varchar(32) not null,
    reservation_date date not null,
    time_slot varchar(32) not null,
    status varchar(32) not null,
    amount numeric(12,2) not null default 0,
    source varchar(32) not null default 'CUSTOMER_MINI',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists customer_order (
    id bigserial primary key,
    member_id bigint not null references member(id),
    store_id bigint not null references store(id),
    reservation_id bigint references reservation(id),
    order_no varchar(64) not null unique,
    order_type varchar(32) not null,
    status varchar(32) not null,
    payable_amount numeric(12,2) not null default 0,
    paid_amount numeric(12,2) not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists gate_device (
    id bigserial primary key,
    store_id bigint not null references store(id),
    code varchar(64) not null unique,
    name varchar(128) not null,
    device_type varchar(32) not null default 'FACE_GATE',
    status varchar(32) not null default 'ONLINE',
    last_seen_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists pass_entitlement (
    id bigserial primary key,
    member_id bigint not null references member(id),
    store_id bigint not null references store(id),
    source_type varchar(32) not null,
    source_id bigint not null,
    status varchar(32) not null,
    valid_from timestamp not null,
    valid_to timestamp not null,
    reentry_policy varchar(32) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists entry_exit_record (
    id bigserial primary key,
    member_id bigint references member(id),
    store_id bigint not null references store(id),
    gate_device_id bigint references gate_device(id),
    direction varchar(16) not null,
    result varchar(32) not null,
    risk_flag boolean not null default false,
    reason_code varchar(64),
    occurred_at timestamp not null default current_timestamp
);

create table if not exists manual_release_record (
    id bigserial primary key,
    store_id bigint not null references store(id),
    member_id bigint references member(id),
    order_id bigint references customer_order(id),
    staff_id bigint not null references staff(id),
    reason varchar(255) not null,
    evidence_urls jsonb,
    risk_flag boolean not null default false,
    created_at timestamp not null default current_timestamp
);

create table if not exists material_category (
    id bigserial primary key,
    code varchar(32) not null unique,
    name varchar(64) not null,
    material_scope varchar(32) not null default 'INTERNAL'
);

create table if not exists material_item (
    id bigserial primary key,
    category_id bigint not null references material_category(id),
    code varchar(64) not null unique,
    name varchar(128) not null,
    unit varchar(16) not null,
    spec varchar(128),
    is_saleable boolean not null default false,
    is_internal_use boolean not null default true,
    safety_stock numeric(12,2) not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists warehouse (
    id bigserial primary key,
    store_id bigint not null references store(id),
    code varchar(64) not null unique,
    name varchar(128) not null,
    warehouse_type varchar(32) not null default 'STORE'
);

create table if not exists material_stock (
    id bigserial primary key,
    warehouse_id bigint not null references warehouse(id),
    material_item_id bigint not null references material_item(id),
    quantity numeric(12,2) not null default 0,
    locked_quantity numeric(12,2) not null default 0,
    updated_at timestamp not null default current_timestamp,
    unique (warehouse_id, material_item_id)
);

create table if not exists material_consumption_record (
    id bigserial primary key,
    store_id bigint not null references store(id),
    material_item_id bigint not null references material_item(id),
    biz_type varchar(32) not null,
    biz_id bigint,
    pet_id bigint references pet_profile(id),
    staff_id bigint references staff(id),
    quantity numeric(12,2) not null,
    occurred_at timestamp not null default current_timestamp
);

create table if not exists risk_event (
    id bigserial primary key,
    store_id bigint references store(id),
    event_type varchar(64) not null,
    event_level varchar(16) not null,
    subject_type varchar(32) not null,
    subject_id bigint,
    content jsonb,
    status varchar(32) not null default 'OPEN',
    created_at timestamp not null default current_timestamp
);

create index if not exists idx_reservation_store_date on reservation(store_id, reservation_date, status);
create index if not exists idx_reservation_pet on reservation(pet_id);
create index if not exists idx_order_member_status on customer_order(member_id, status);
create index if not exists idx_pass_entitlement_member_store on pass_entitlement(member_id, store_id, status);
create index if not exists idx_pass_entitlement_source on pass_entitlement(source_type, source_id);
create index if not exists idx_entry_exit_store_time on entry_exit_record(store_id, occurred_at desc);
create index if not exists idx_manual_release_store_time on manual_release_record(store_id, created_at desc);
create index if not exists idx_material_stock_item on material_stock(material_item_id);
create index if not exists idx_risk_event_store_time on risk_event(store_id, created_at desc);

create table if not exists entry_token (
    id bigserial primary key,
    pass_entitlement_id bigint not null references pass_entitlement(id),
    member_id bigint not null references member(id),
    store_id bigint not null references store(id),
    token_value varchar(64) not null unique,
    status varchar(16) not null default 'ACTIVE',
    expires_at timestamp not null,
    consumed_at timestamp,
    entry_exit_record_id bigint references entry_exit_record(id),
    created_at timestamp not null default current_timestamp
);

create index if not exists idx_entry_token_value on entry_token(token_value);
create index if not exists idx_entry_token_pass on entry_token(pass_entitlement_id, status);
create index if not exists idx_entry_token_member_status on entry_token(member_id, status);
create index if not exists idx_entry_exit_member_park on entry_exit_record(member_id, store_id, occurred_at desc);
