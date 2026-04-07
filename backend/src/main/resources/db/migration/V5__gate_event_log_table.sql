create table if not exists gate_event_log (
    id bigserial primary key,
    device_code varchar(64) not null,
    event_type varchar(64),
    detail text,
    created_at timestamp not null default current_timestamp
);
