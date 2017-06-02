
    create table roles (
        roles varchar(255) not null,
        user_id varchar(255) not null,
        primary key (roles, user_id)
    ) type=InnoDB;

    create table users (
        user_id varchar(255) not null,
        passwd varchar(255),
        primary key (user_id)
    ) type=InnoDB;

    alter table roles 
        add index FK67A8EBDD249F692 (user_id), 
        add constraint FK67A8EBDD249F692 
        foreign key (user_id) 
        references users (user_id);
