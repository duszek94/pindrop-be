ALTER TABLE trips ADD COLUMN preference_profile JSONB;

ALTER TABLE trip_proposals ADD COLUMN cost_breakdown_json TEXT;
