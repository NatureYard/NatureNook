alter table customer_order
    add column if not exists reservation_id bigint references reservation(id);

create index if not exists idx_customer_order_reservation on customer_order(reservation_id);

update customer_order
set reservation_id = 1001
where id = 1
  and reservation_id is null;
