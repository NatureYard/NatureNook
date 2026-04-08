insert into store (id, code, name, city, address)
values
    (1, 'SH001', '上海萌宠乐园旗舰店', '上海', '浦东新区示例路 1 号'),
    (2, 'HZ001', '杭州萌宠乐园体验店', '杭州', '西湖区示例路 8 号');

insert into staff (id, store_id, name, phone, role_code, status)
values
    (1, 1, '店长A', '13800000001', 'STORE_MANAGER', 'ACTIVE'),
    (2, 1, '前台A', '13800000002', 'FRONT_DESK', 'ACTIVE');

insert into member (id, store_id, name, phone, level, face_bound, risk_tag)
values
    (1, 1, '张三', '13900000001', 'SILVER', true, 'NORMAL'),
    (2, 1, '李四', '13900000002', 'GOLD', true, 'NORMAL');

insert into pet_profile (id, member_id, name, species, breed, gender, weight)
values
    (1, 1, '奶球', 'DOG', '柯基', 'FEMALE', 8.50),
    (2, 2, '布丁', 'CAT', '英短', 'MALE', 4.20);

insert into membership_card (id, member_id, card_type, store_id, status, valid_from, valid_to, face_bound)
values
    (1, 1, 'SEASON_CARD', 1, 'ACTIVE', current_timestamp - interval '10' day, current_timestamp + interval '80' day, true),
    (2, 2, 'YEAR_CARD', 1, 'ACTIVE', current_timestamp - interval '10' day, current_timestamp + interval '355' day, true);

insert into reservation (id, member_id, store_id, pet_id, reservation_type, reservation_date, time_slot, status, amount, source)
values
    (1001, 1, 1, 1, 'TICKET', current_date, '09:00-12:00', 'BOOKED', 68.00, 'CUSTOMER_MINI'),
    (1002, 2, 1, 2, 'GROOMING', current_date, '13:00-15:00', 'BOOKED', 128.00, 'CUSTOMER_MINI');

insert into customer_order (id, member_id, store_id, reservation_id, order_no, order_type, status, payable_amount, paid_amount)
values
    (1, 1, 1, 1001, 'ORD202604050001', 'TICKET', 'PAID', 68.00, 68.00),
    (2, 2, 1, 1002, 'ORD202604050002', 'GROOMING', 'PAID', 128.00, 128.00);

insert into gate_device (id, store_id, code, name, device_type, status, last_seen_at)
values
    (1, 1, 'GATE-SH-001', '上海旗舰店一号闸机', 'FACE_GATE', 'ONLINE', current_timestamp);

insert into pass_entitlement (id, member_id, store_id, source_type, source_id, status, valid_from, valid_to, reentry_policy)
values
    (1, 1, 1, 'DAY_TICKET', 1001, 'ACTIVE', current_timestamp - interval '2' hour, current_timestamp + interval '8' hour, 'SAME_DAY_UNLIMITED');

alter table reservation alter column id restart with 1003;
alter table customer_order alter column id restart with 3;
alter table pass_entitlement alter column id restart with 2;
alter table manual_release_record alter column id restart with 1;
alter table risk_event alter column id restart with 1;
alter table entry_token alter column id restart with 1;
