create table if not exists shedlock (
    name varchar(64) not null,
    lock_until timestamp(3) not null,
    locked_at timestamp(3) not null default current_timestamp(3),
    locked_by varchar(255) not null,
    primary key (name)
);

create table if not exists invoice_outbox_tasks (
    id varchar(36) not null,
    order_id varchar(36) not null unique,
    status varchar(20) not null default 'PENDING',
    retry_count int not null default 0,
    created_at datetime(6) not null,
    processed_at datetime(6),
    primary key (id),
    index idx_invoice_outbox_status(status)
);

