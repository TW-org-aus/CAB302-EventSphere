package com.eventsphere.app;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;

// new helper class made to de-couple the private image loader that was in the landing page
// Resolves the display URL for a stored event image. Shared by LandingPageController
// and EventPageController so there is a centralised way for resolving img url's
public class EventImages {

    private EventImages() {
    }

    // Remote URLs pass through; anything else is treated as a file under images/.
    //legacy method used by landing page and I currently CBF to change it
    public static String resolveImageUrl(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        if (stored.startsWith("http://") || stored.startsWith("https://")) {
            return stored;
        }
        var resource = EventImages.class.getResource("images/" + stored);
        return resource == null ? null : resource.toExternalForm();
    }

    // Same as resolveImageUrl, but falls back to a category placeholder instead of returning null.
    // newer method used by event details page will eventully become the defeult image resolver method.
    public static String resolveImageUrlWithPlaceholder(Event event) {
        String url = resolveImageUrl(event.getImageUrl());
        return url != null ? url : placeholderUrl(event.getCategory());
    }

    private static String placeholderUrl(Category category) {
        String slug = category == null ? "other" : category.name().toLowerCase().replace('_', '-');
        var resource = EventImages.class.getResource("images/placeholders/placeholder-" + slug + ".png");
        if (resource == null) {
            resource = EventImages.class.getResource("images/placeholders/placeholder-other.png");
        }
        return resource == null ? null : resource.toExternalForm();
    }
}
