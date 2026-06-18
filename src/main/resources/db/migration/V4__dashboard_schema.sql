ALTER TABLE users ADD COLUMN avatar_url VARCHAR(512);
ALTER TABLE users ADD COLUMN interests TEXT[];
ALTER TABLE users ADD COLUMN pace VARCHAR(50);
ALTER TABLE users ADD COLUMN budget_range VARCHAR(50);

CREATE TABLE trips (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255),
    destination VARCHAR(255),
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION,
    start_date DATE,
    end_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNING',
    traveler_count INT NOT NULL DEFAULT 1,
    cover_image_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_trips_user_id ON trips(user_id);
CREATE INDEX idx_trips_user_created ON trips(user_id, created_at DESC, id DESC);

CREATE TABLE itineraries (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT REFERENCES trips(id) ON DELETE SET NULL,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    like_count INT NOT NULL DEFAULT 0,
    cover_image_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_itineraries_author ON itineraries(author_id);
CREATE INDEX idx_itineraries_public_created ON itineraries(is_public, created_at DESC, id DESC);

CREATE TABLE itinerary_likes (
    id BIGSERIAL PRIMARY KEY,
    itinerary_id BIGINT NOT NULL REFERENCES itineraries(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_itinerary_like UNIQUE (itinerary_id, user_id)
);

CREATE INDEX idx_itinerary_likes_user ON itinerary_likes(user_id);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at DESC, id DESC);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id) WHERE read = FALSE;
