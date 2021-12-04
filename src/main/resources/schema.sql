create table app_user (
    user_id bigint not null,
    password varchar(255),
    username varchar(255),
    primary key (user_id)
);

create table role (
    role_id bigint not null,
    name varchar(255),
    primary key (role_id)
);

create table user_role (
    user_id bigint not null,
    role_id bigint not null,
    primary key (user_id, role_id)
);

-- insert into role(role_id, name) values
--     (1, 'RESEARCHER'),
--     (2, 'PARTICIPANT');

