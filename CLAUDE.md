# Project notes for Claude

- No Claude-related files or directories (e.g. `.claude/`, `CLAUDE.local.md`) may ever be committed to this repo. They must be listed in `.gitignore`.
- Before making any commit, verify `.gitignore` covers all Claude-associated files and that none are staged.

## EventSphere mock design color scheme

Derived from the mobile screenshots in `MockDesigns/` (Event-detailed-comments.png, event-details.png). Use these when building or updating any mock design for this app.

- Background (app canvas): `#F4502F` (red-orange)
- Header/side bars: `#000000` (black)
- Card / surface background: `#FFFFFF` (white)
- Primary text: `#1A1A1A`
- Secondary/muted text: `#6B7280`
- Primary accent (buttons, links, verified badge, active tab): `#2F80ED`
- Light accent (secondary button/pill backgrounds): `#E3F0FF`
- Highlight yellow (promo banner text, "Cars & Culture" branding): `#FFD400`
- Border/divider on cards: `#E5E7EB`

## EventSphere mock design icons

Use **Bootstrap Icons** for all icons in mock designs — never emojis. Loaded via CDN:

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
```

Usage: `<i class="bi bi-calendar-event"></i>`. Browse the full icon set at https://icons.getbootstrap.com/.
