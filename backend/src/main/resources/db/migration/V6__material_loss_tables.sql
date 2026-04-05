create table if not exists material_loss_order (
    id bigserial primary key,
    store_id bigint not null references store(id),
    warehouse_id bigint not null references warehouse(id),
    staff_id bigint not null references staff(id),
    status varchar(32) not null default 'PENDING',
    remark varchar(255),
    created_at timestamp not null default current_timestamp
);

create table if not exists material_loss_item (
    id bigserial primary key,
    loss_order_id bigint not null references material_loss_order(id),
    material_item_id bigint not null references material_item(id),
    quantity numeric(12,2) not null,
    unit varchar(16) not null,
    reason varchar(255)
);
