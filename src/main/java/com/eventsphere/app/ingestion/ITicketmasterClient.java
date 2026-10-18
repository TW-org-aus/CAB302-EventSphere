package com.eventsphere.app.ingestion;

import java.io.IOException;
import java.time.Instant;

public interface ITicketmasterClient {
    TicketmasterClient.EventPage fetchEvents(String city, Instant from, Instant to, int page, int size)
            throws IOException, InterruptedException;
}
