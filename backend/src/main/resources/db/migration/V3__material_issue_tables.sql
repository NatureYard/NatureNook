create table if not exists material_issue_order (
    id bigserial primary key,
    store_id bigint not null references store(id),
    warehouse_id bigint not null references warehouse(id),
    staff_id bigint not null references staff(id),
    biz_type varchar(32) not null,
    biz_id bigint,
    status varchar(32) not null default 'ISSUED',
    remark varchar(255),
    created_at timestamp not null default current_timestamp
);

create table if not exists material_issue_item (
    id bigserial primary key,
    issue_order_id bigint not null references material_issue_order(id),
    material_item_id bigint not null references material_item(id),
    quantity numeric(12,2) not null,
    unit varchar(16) not null
);

