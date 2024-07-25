-- Создание базы данных ticket_db
CREATE DATABASE ticket_db;

-- Подключение к базе данных ticket_db
\c ticket_db;

create type "ticketState" as enum ('Draft','New','Approved','Declined','In_progress','Done','Cancelled');
create type "ticketUrgency" as enum ('Critical','High','Average','Low');

-- Создание таблицы ticket
create table if not exists ticket
(
    id serial primary key,
    "name" varchar(100) not null,
    description varchar (500),
    created_on date not null,
    desired_resolution_date date,
    assignee_id int references "user" (id),
    owner_id int references "user" (id),
    "state" "ticketState" not null,
    category_id int references category (id),
    urgency "ticketUrgency",
    approver_id int references "user" (id)
);
