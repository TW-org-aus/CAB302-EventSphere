package com.eventsphere.app.seeding;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.Database.Database;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.dao.SourceDAO;
import com.eventsphere.app.ingestion.TicketmasterClient;
import com.eventsphere.app.ingestion.TicketmasterIngestor;

import java.sql.Connection;


// to everyone comming accross this class wondering what on earth it does, this is just a single method
// that manually seeds the db, yes I could have grouped it in ticketmasterIngestion class, yes I know this breaks OOP
// slightly with a class only having one method, but I thought it was neccicary to separate it from the other classes
// so that nobody is confused about what it does. You can't win em all.


/** Standalone entry point that runs one Ticketmaster pull immediately, bypassing the staleness check; useful for seeding a fresh database or testing ingestion. */
public class TicketmasterIngestMain {



    public static void main(String[] args) throws Exception {
        try (Connection connection = Database.openConnection()) {
            new DBController(connection);
            int count = new TicketmasterIngestor(new TicketmasterClient(), new EventDAO(connection), new SourceDAO(connection), connection).run();
            System.out.println("Ticketmaster: " + count + " events upserted in total");
        }
    }
}
