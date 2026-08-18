# Retro Macros

A macro-tracking and meal-planning web app with an 80s arcade theme. Calculate your daily calorie and macronutrient targets from your body metrics and goals, generate a 3-day meal plan that hits those targets, then log what you actually ate against them.

**[Live demo](https://retro-macros.onrender.com)** — click "Explore the demo" on the sign-in page. No account needed.

## What it does

- **Macro calculator.** BMR via Mifflin-St Jeor, Harris-Benedict, or Katch-McArdle, then TDEE from activity level, then a calorie adjustment for your goal. Macro splits default to sensible ratios per goal (35/35/30 cutting, 30/40/30 building) and are adjustable with live-validating sliders that must total 100%.
- **Meal plan generation.** Produces a 3-day plan broken into breakfast, lunch, snack, and dinner, with per-meal macro breakdowns that roll up to your daily targets.
- **Grocery list.** Aggregates every ingredient across the plan, combining duplicate items and normalizing units into realistic package quantities.
- **Food logging.** Log meals in plain English ("6 oz grilled chicken, 1.5 cups brown rice"). Macros are estimated automatically, with a rule-based nutrition parser as a fallback so logging never hard-fails.
- **Ingredient swaps.** Swap any single ingredient or a whole meal, with the app tracking which substitutions you keep in order to bias future suggestions.
- **Saved plans and history.** Every generated plan is persisted, with a 7-day rolling log history and daily progress against target.
- **Admin dashboard.** User counts, plan accuracy scoring, and per-user drill-down with impersonation for support.

## Stack

| Layer | Technology |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Web | Spring MVC, Thymeleaf, Bootstrap 5 |
| Security | Spring Security, OAuth 2.0 (Google sign-in) |
| Data | Spring Data JPA, Hibernate, PostgreSQL (prod), H2 (dev) |
| HTTP client | Spring WebFlux `WebClient` |
| Build | Maven |
| Deploy | Docker, Render (app), Neon (Postgres) |

## Architecture notes

A few decisions worth calling out:

**Authentication is OAuth-only, and the demo account rides the same path.** Every controller resolves the current user through `@AuthenticationPrincipal OAuth2User`. Rather than special-casing demo sessions across ten controllers, `DemoLoginController` constructs a real `OAuth2AuthenticationToken` carrying the same attribute names Google returns (`sub`, `email`, `name`, `picture`). Nothing downstream knows the difference.

**The demo account resets on every login.** It is shared by every visitor, so `DemoAccountService` wipes and re-seeds it on each demo sign-in. That guarantees a presentable account regardless of what the previous visitor did, and avoids building a read-only enforcement layer over every write path.

**Meal generation has an offline mode.** `MealPlanService` is built around a pluggable generation step. It can call the Anthropic or OpenAI APIs, or serve plans from a built-in deterministic generator that derives meals from calculated targets without any network call. Which one runs is a config flag, not a code path the caller knows about. The public demo runs offline so hosting costs nothing.

**Nutrition parsing degrades instead of failing.** `NutritionService` attempts a structured parse of free-text food entries first and falls back to a rule-based parser with a built-in macro lookup table. A logging feature that silently stops working is worse than one that is occasionally approximate.

## Running locally

Requires Java 17 and Maven.

```bash
git clone https://github.com/TMGan/Meal_planner_app.git
```

Then build and run it with the demo account enabled and no external API keys needed:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--ai.mock=true --demo.enabled=true"
```

The app starts at `http://localhost:8080` against an in-memory H2 database, with the H2 console at `/h2-console`.

To enable Google sign-in locally, set `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET`, and register `http://localhost:8080/login/oauth2/code/google` as an authorized redirect URI in the Google Cloud Console.

To enable live AI generation instead of the offline generator, set `CLAUDE_API_KEY` and pass `--ai.mock=false`.

## Configuration

| Variable | Purpose |
| --- | --- |
| `DATABASE_URL` | JDBC Postgres URL, for example `jdbc:postgresql://host/db?sslmode=require` |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Database credentials, if not embedded in the URL |
| `APP_URL` | Public base URL, used to build the OAuth redirect URI |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth credentials |
| `AI_MOCK` | `true` serves plans from the offline generator, `false` calls the live API |
| `DEMO_ENABLED` | `true` shows the "Explore the demo" button on the sign-in page |
| `CLAUDE_API_KEY` | Anthropic API key, only needed when `AI_MOCK=false` |

## Deployment

Deployed as a Docker container on Render's free tier, backed by a free Neon Postgres project. `render.yaml` is a Render Blueprint covering the full setup. The `Dockerfile` is a multi-stage build that caches dependencies separately from source and tunes the JVM (SerialGC, capped heap percentage) to fit comfortably inside a 512MB instance.

One deployment gotcha worth documenting: the Google OAuth redirect URI is derived from `APP_URL`, and must also be registered in the Google Cloud Console for the deployed domain. Changing hosting providers without updating both breaks sign-in.
