create table if not exists action_masters (
    action_type varchar(30) primary key
);

insert into action_masters (action_type)
values ('STUDY')
on conflict (action_type) do nothing;

create table if not exists game_turn_slots (
    game_turn_slot_id bigint primary key,
    game_session_id bigint not null default 1,
    turn_number integer not null default 1,
    slot_index integer not null default 1,
    action_type varchar(30) not null default 'STUDY',
    action_category varchar(20),
    forced_action_yn boolean not null default false,
    constraint uq_game_turn_slots__game_session_id__turn_number__slot_index
        unique (game_session_id, turn_number, slot_index)
);

alter table game_turn_slots
    drop constraint if exists fk_game_turn_slots__action_master;

alter table game_turn_slots
    alter column action_type type varchar(30);

alter table game_turn_slots
    alter column action_type set default 'STUDY';

alter table game_turn_slots
    add constraint fk_game_turn_slots__action_master
        foreign key (action_type) references action_masters (action_type)
        on update no action
        on delete no action;
