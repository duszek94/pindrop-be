ALTER TABLE trips ADD COLUMN budget_tier VARCHAR(50);
ALTER TABLE trips ADD COLUMN pace VARCHAR(50);
ALTER TABLE trips ADD COLUMN interests TEXT[];
ALTER TABLE trips ADD COLUMN selected_proposal_type VARCHAR(50);
ALTER TABLE trips ADD COLUMN wizard_step SMALLINT;

CREATE TABLE trip_proposals (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    proposal_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary TEXT,
    estimated_cost_usd INT NOT NULL DEFAULT 0,
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    weather_json TEXT,
    highlights_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_trip_proposal_type UNIQUE (trip_id, proposal_type)
);

CREATE INDEX idx_trip_proposals_trip ON trip_proposals(trip_id);

CREATE TABLE itinerary_days (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    day_number INT NOT NULL,
    day_date DATE NOT NULL,
    CONSTRAINT uq_trip_day_number UNIQUE (trip_id, day_number)
);

CREATE INDEX idx_itinerary_days_trip ON itinerary_days(trip_id);

CREATE TABLE itinerary_activities (
    id BIGSERIAL PRIMARY KEY,
    day_id BIGINT NOT NULL REFERENCES itinerary_days(id) ON DELETE CASCADE,
    start_time TIME NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    place_name VARCHAR(255),
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION,
    weather_json TEXT,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_itinerary_activities_day ON itinerary_activities(day_id, sort_order);
