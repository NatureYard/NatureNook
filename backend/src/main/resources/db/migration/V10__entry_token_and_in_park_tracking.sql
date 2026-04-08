-- 一次性入园令牌表：用于动态二维码防多人使用
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

-- 在园状态查询加速索引
create index if not exists idx_entry_exit_member_park on entry_exit_record(member_id, store_id, occurred_at desc);
