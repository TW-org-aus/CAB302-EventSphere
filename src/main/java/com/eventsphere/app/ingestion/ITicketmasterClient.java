package com.eventsphere.app.ingestion;

import java.io.IOException;
import java.time.Instant;

/** Contract for the Ticketmaster Discovery API client; abstracted so tests can inject a stub. */
public interface ITicketmasterClient {
    TicketmasterClient.EventPage fetchEvents(String city, Instant from, Instant to, int page, int size)
            throws IOException, InterruptedException;
}
