create table if not exists person
(
    id      uuid primary key,
    crn     varchar null,
    noms_id varchar null,
    constraint person_crn unique (crn),
    constraint person_noms_id unique (noms_id)
);


create table if not exists sentence_plan
(
    id           uuid primary key,
    person_id    uuid,
    created_date timestamp
)