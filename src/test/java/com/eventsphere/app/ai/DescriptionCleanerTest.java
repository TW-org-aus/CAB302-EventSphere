package com.eventsphere.app.ai;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DescriptionCleanerTest {

    private MockAiClient ai;
    private DescriptionCleaner cleaner;

    @BeforeEach
    void setUp() {
        ai = new MockAiClient();
        cleaner = new DescriptionCleaner(ai);
    }

    @Test
    void nullDescriptionPassesThrough() {
        Event e = event(1, null);
        assertNull(cleaner.clean(e));
        assertEquals(0, ai.callCount);
    }

    @Test
    void blankDescriptionPassesThrough() {
        Event e = event(1, "   ");
        assertEquals("   ", cleaner.clean(e));
        assertEquals(0, ai.callCount);
    }

    @Test
    void nullAiReturnsOriginal() {
        DescriptionCleaner noAi = new DescriptionCleaner(null);
        Event e = event(1, "raw <b>html</b>");
        assertEquals("raw <b>html</b>", noAi.clean(e));
    }

    @Test
    void cleanReturnsAiReply() {
        ai.setReply("Cleaned description.");
        Event e = event(1, "raw html mess");
        assertEquals("Cleaned description.", cleaner.clean(e));
    }

    @Test
    void resultIsCachedOnSecondCall() {
        ai.setReply("Cleaned once.");
        Event e = event(1, "raw");
        cleaner.clean(e);
        cleaner.clean(e);
        assertEquals(1, ai.callCount);
    }

    @Test
    void aiFailureReturnsOriginal() {
        ai.setShouldThrow(true);
        Event e = event(1, "original description");
        assertEquals("original description", cleaner.clean(e));
    }

    @Test
    void blankAiReplyReturnsOriginal() {
        ai.setReply("   ");
        Event e = event(1, "original");
        assertEquals("original", cleaner.clean(e));
    }

    private static Event event(int id, String description) {
        return new Event(id, "Concert", description, Category.MUSIC,
                Instant.now().plusSeconds(3600), null,
                "Brisbane Arena", "123 St", null, null,
                null, null, Instant.now(), false, 1, null, 0, 0);
    }
}
