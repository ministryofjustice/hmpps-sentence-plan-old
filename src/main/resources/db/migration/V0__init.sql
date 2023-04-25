create table if not exists person
(
    id      uuid primary key,
    crn     varchar null,
    constraint person_crn unique (crn)
);


create table if not exists sentence_plan
(
    id           uuid primary key,
    person_id    uuid,
    created_date timestamp
)