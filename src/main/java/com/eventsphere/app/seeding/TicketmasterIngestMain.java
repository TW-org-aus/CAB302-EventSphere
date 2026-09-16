package com.eventsphere.app.seeding;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.Database.Database;
import com.eventsphere.app.ingestion.TicketmasterClient;
import com.eventsphere.app.ingestion.TicketmasterIngestor;

import java.sql.Connection;


// to everyone comming accross this class wondering what on earth it does, this is just a single method
// that manually seeds the db, yes I could have grouped it in ticketmasterIngestion class, yes I know this breaks OOP
// slightly with a class only having one method, but I thought it was neccicary to separate it from the other classes
// so that nobody is confused about what it does. You can't win em all.


// Runs one Ticketmaster pull immediately, ignoring the staleness check. Use it to seed a fresh database.db or to test ingestion.
public class TicketmasterIngestMain {

    private TicketmasterIngestMain() { }

    public static void main(String[] args) throws Exception {
        try (Connection connection = Database.openConnection()) {
            new DBController(connection);
            int count = new TicketmasterIngestor(new TicketmasterClient(), connection).run();
            System.out.println("Ticketmaster: " + count + " events upserted in total");
        }
    }
}
