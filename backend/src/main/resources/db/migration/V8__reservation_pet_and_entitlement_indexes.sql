alter table reservation
    add column if not exists pet_id bigint references pet_profile(id);

create index if not exists idx_reservation_pet on reservation(pet_id);
create index if not exists idx_pass_entitlement_source on pass_entitlement(source_type, source_id);

update reservation
set pet_id = 1
where id = 1001
  and pet_id is null;

update reservation
set pet_id = 2
where id = 1002
  and pet_id is null;
