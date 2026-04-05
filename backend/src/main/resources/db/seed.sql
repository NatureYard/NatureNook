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
    (1, 3, 10, 0)
on conflict (warehouse_id, material_item_id) do nothing;

