DO $$
DECLARE
    test_user_id BIGINT;
    trip1_id BIGINT;
    trip2_id BIGINT;
    trip3_id BIGINT;
    itin1_id BIGINT;
    itin2_id BIGINT;
    itin3_id BIGINT;
BEGIN
    SELECT id INTO test_user_id FROM users WHERE email = 'test@test.com';
    IF test_user_id IS NULL THEN
        RETURN;
    END IF;

    UPDATE users
    SET avatar_url = 'https://api.dicebear.com/7.x/avataaars/svg?seed=test',
        interests = ARRAY['culture', 'food', 'nature'],
        pace = 'moderate',
        budget_range = 'medium'
    WHERE id = test_user_id;

    INSERT INTO trips (user_id, title, destination, lat, lng, start_date, end_date, status, traveler_count, cover_image_url)
    VALUES (test_user_id, 'Tokyo Adventure', 'Tokyo, Japan', 35.6762, 139.6503,
            CURRENT_DATE + INTERVAL '14 days', CURRENT_DATE + INTERVAL '21 days',
            'PLANNING', 2, 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800')
    RETURNING id INTO trip1_id;

    INSERT INTO trips (user_id, title, destination, lat, lng, start_date, end_date, status, traveler_count, cover_image_url)
    VALUES (test_user_id, 'Barcelona Weekend', 'Barcelona, Spain', 41.3874, 2.1686,
            CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE + INTERVAL '2 days',
            'PLANNING', 1, 'https://images.unsplash.com/photo-1583422409516-2895a77efded?w=800')
    RETURNING id INTO trip2_id;

    INSERT INTO trips (user_id, title, destination, lat, lng, start_date, end_date, status, traveler_count, cover_image_url)
    VALUES (test_user_id, 'Iceland Road Trip', 'Reykjavik, Iceland', 64.1466, -21.9426,
            CURRENT_DATE + INTERVAL '60 days', CURRENT_DATE + INTERVAL '67 days',
            'PLANNING', 4, 'https://images.unsplash.com/photo-1504829857797-ddff29c27927?w=800')
    RETURNING id INTO trip3_id;

    INSERT INTO itineraries (trip_id, author_id, title, is_public, like_count, cover_image_url)
    VALUES (trip1_id, test_user_id, '7 Days in Tokyo', TRUE, 42,
            'https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800')
    RETURNING id INTO itin1_id;

    INSERT INTO itineraries (trip_id, author_id, title, is_public, like_count, cover_image_url)
    VALUES (NULL, test_user_id, 'Hidden Gems of Barcelona', TRUE, 18,
            'https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=800')
    RETURNING id INTO itin2_id;

    INSERT INTO itineraries (trip_id, author_id, title, is_public, like_count, cover_image_url)
    VALUES (trip3_id, test_user_id, 'Iceland Ring Road', TRUE, 95,
            'https://images.unsplash.com/photo-1504829857797-ddff29c27927?w=800')
    RETURNING id INTO itin3_id;

    INSERT INTO itinerary_likes (itinerary_id, user_id)
    VALUES (itin1_id, test_user_id), (itin3_id, test_user_id)
    ON CONFLICT DO NOTHING;

    INSERT INTO notifications (user_id, type, message, read, created_at)
    VALUES
        (test_user_id, 'TRIP_REMINDER', 'Your Tokyo Adventure starts in 2 weeks!', FALSE, NOW() - INTERVAL '1 hour'),
        (test_user_id, 'AI_PROPOSAL_READY', 'AI has generated 3 itinerary options for Barcelona.', FALSE, NOW() - INTERVAL '3 hours'),
        (test_user_id, 'PRICE_ALERT', 'Flight prices to Iceland dropped 15%.', TRUE, NOW() - INTERVAL '1 day'),
        (test_user_id, 'WEATHER_CHANGE', 'Rain expected during your Barcelona trip — consider indoor activities.', FALSE, NOW() - INTERVAL '2 days'),
        (test_user_id, 'SOCIAL_LIKE', 'Someone liked your Iceland Ring Road itinerary.', TRUE, NOW() - INTERVAL '3 days');
END $$;
