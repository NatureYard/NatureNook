insert into store (id, code, name, city, address)
values
    (1, 'SH001', '上海萌宠乐园旗舰店', '上海', '浦东新区示例路 1 号'),
    (2, 'HZ001', '杭州萌宠乐园体验店', '杭州', '西湖区示例路 8 号')
on conflict (id) do nothing;

insert into staff (id, store_id, name, phone, role_code)
values
    (1, 1, '店长A', '13800000001', 'STORE_MANAGER'),
    (2, 1, '前台A', '13800000002', 'FRONT_DESK')
on conflict (id) do nothing;

insert into member (id, store_id, name, phone, level, face_bound)
values
    (1, 1, '张三', '13900000001', 'SILVER', true),
    (2, 1, '李四', '13900000002', 'GOLD', true)
on conflict (id) do nothing;

insert into pet_profile (id, member_id, name, species, breed, gender, weight)
values
    (1, 1, '奶球', 'DOG', '柯基', 'FEMALE', 8.50),
    (2, 2, '布丁', 'CAT', '英短', 'MALE', 4.20)
on conflict (id) do nothing;

insert into membership_card (id, member_id, card_type, store_id, status, valid_from, valid_to, face_bound)
values
    (1, 2, 'YEAR_CARD', 1, 'ACTIVE', current_timestamp - interval '10 day', current_timestamp + interval '355 day', true)
on conflict (id) do nothing;

insert into reservation (id, member_id, store_id, reservation_type, reservation_date, time_slot, status, amount, source)
values
    (1001, 1, 1, 'TICKET', current_date, '09:00-12:00', 'PAID', 68.00, 'CUSTOMER_MINI'),
    (1002, 2, 1, 'GROOMING', current_date, '13:00-15:00', 'BOOKED', 128.00, 'CUSTOMER_MINI')
on conflict (id) do nothing;

insert into customer_order (id, member_id, store_id, order_no, order_type, status, payable_amount, paid_amount)
values
    (1, 1, 1, 'ORD202604050001', 'TICKET', 'PAID', 68.00, 68.00),
    (2, 2, 1, 'ORD202604050002', 'YEAR_CARD', 'PAID', 1288.00, 1288.00)
on conflict (id) do nothing;

insert into gate_device (id, store_id, code, name, device_type, status, last_seen_at)
values
    (1, 1, 'GATE-SH-001', '上海旗舰店一号闸机', 'FACE_GATE', 'ONLINE', current_timestamp)
on conflict (id) do nothing;

insert into pass_entitlement (id, member_id, store_id, source_type, source_id, status, valid_from, valid_to, reentry_policy)
values
    (1, 1, 1, 'DAY_TICKET', 1001, 'ACTIVE', current_timestamp - interval '2 hour', current_timestamp + interval '8 hour', 'SAME_DAY_UNLIMITED'),
    (2, 2, 1, 'YEAR_CARD', 1, 'ACTIVE', current_timestamp - interval '10 day', current_timestamp + interval '355 day', 'CARD_VALIDITY')
on conflict (id) do nothing;

insert into entry_exit_record (id, member_id, store_id, gate_device_id, direction, result, risk_flag, reason_code, occurred_at)
values
    (1, 1, 1, 1, 'ENTRY', 'PASSED', false, null, current_timestamp - interval '3 hour'),
    (2, 1, 1, 1, 'EXIT', 'PASSED', false, null, current_timestamp - interval '1 hour'),
    (3, 1, 1, 1, 'ENTRY', 'PASSED', false, null, current_timestamp - interval '30 minute')
on conflict (id) do nothing;

insert into material_category (id, code, name, material_scope)
values
    (1, 'FEED', '饲料', 'INTERNAL'),
    (2, 'CLEANING', '清洁用品', 'INTERNAL'),
    (3, 'GROOMING', '洗护耗材', 'INTERNAL'),
    (4, 'RETAIL_AND_INTERNAL', '零售及内部共用', 'BOTH')
on conflict (id) do nothing;

insert into warehouse (id, store_id, code, name, warehouse_type)
values
    (1, 1, 'WH-SH-01', '上海旗舰店主仓', 'STORE')
on conflict (id) do nothing;

insert into material_item (id, category_id, code, name, unit, spec, is_saleable, is_internal_use, safety_stock)
values
    (1, 1, 'FEED-DOG-001', '犬用基础粮', 'kg', '10kg/袋', false, true, 20),
    (2, 2, 'CLEAN-001', '地面消毒液', '瓶', '2L/瓶', false, true, 10),
    (3, 3, 'GROOM-001', '低敏沐浴露', '瓶', '500ml/瓶', true, true, 8)
on conflict (id) do nothing;

insert into material_stock (warehouse_id, material_item_id, quantity, locked_quantity)
values
    (1, 1, 32, 0),
    (1, 2, 16, 0),
    (1, 3, 6, 0)
on conflict (warehouse_id, material_item_id) do nothing;

insert into risk_event (id, store_id, event_type, event_level, subject_type, subject_id, content, status)
values
    (1, 1, 'MANUAL_RELEASE_HIGH_FREQUENCY', 'HIGH', 'STAFF', 2, '{"hint":"前台A在低峰时段人工放行频率偏高"}', 'OPEN'),
    (2, 1, 'LOW_STOCK_WARNING', 'MEDIUM', 'MATERIAL', 3, '{"hint":"低敏沐浴露库存低于安全库存"}', 'OPEN')
on conflict (id) do nothing;

