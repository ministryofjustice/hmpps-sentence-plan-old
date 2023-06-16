CREATE TABLE action (
                        id UUID PRIMARY KEY,
                        objective_id UUID NOT NULL,
                        description VARCHAR NOT NULL,
                        intervention_participation NUMERIC(1) NOT NULL,
                        national_intervention_code VARCHAR,
                        accredited_programme VARCHAR,
                        local_intervention VARCHAR,
                        status VARCHAR NOT NULL,
                        owner VARCHAR NOT NULL,
                        created_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


alter table action
    add constraint fk_action_objective foreign key (objective_id) references objective (id)