# Farm Carbon Service

A Spring Boot microservice for tracking farm-level carbon emissions — fertilizer use, fuel combustion, irrigation energy, livestock, and tillage — designed to sit alongside [FarmBeats Agentic AI](https://github.com/AmaniGudali95/farmbeats_Agentic_AI), a Python/FastAPI precision-agriculture advisor. Built as a hands-on Spring/Java practice project, with a secondary goal of exploring what a *carbon-neutral-aware* application — one that helps a farm actually reach net-zero, and that's mindful of its own hosting footprint — looks like in practice.

---

## Why this project

Two motivations converge here:

1. **Spring practice with real depth** — not a tutorial CRUD app. This project deliberately works through JPA/Hibernate mapping, Kafka producer/consumer decoupling, external API integration, Redis caching, and Actuator-based metrics — the kind of backend architecture used in production systems.
2. **A genuine sustainability angle** — the domain (carbon emissions tracking) and the engineering practice (green software principles) reinforce each other. The application both helps *farms* reduce their footprint and is built with an eye toward minimizing its *own* hosting footprint.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      REST API (Spring MVC)                    │
│   /farms   /farms/{id}/activities   /farms/{id}/report        │
│   /sustainability/footprint (own hosting footprint, WIP)      │
└───────────────────────────┬────────────────────────────────┘
                            │
                 ┌──────────▼──────────┐
                 │   PostgreSQL / H2    │  ← JPA/Hibernate
                 └──────────────────────┘
                            │
                 ┌──────────▼──────────┐
                 │   Kafka (activity-    │  ← decouples request/response
                 │   logged topic)       │    from emissions calculation
                 └──────────┬──────────┘
                            │
              ┌─────────────▼─────────────┐
              │  EmissionCalculationService │
              └──────┬───────────────┬─────┘
                     │               │
        ┌────────────▼───┐   ┌──────▼────────────┐
        │ Carbon Interface │   │ EmissionFactor     │
        │ API (fuel,        │   │ Calculator          │
        │ electricity)       │   │ (fertilizer,         │
        │ — cached via Redis │   │ livestock, tillage)   │
        └────────────────────┘   └──────────────────────┘
```

**Stack:** Spring Boot 4.1.0, Java 17, Hibernate/JPA, H2 (dev) / PostgreSQL (prod profile), Apache Kafka (via Spring Kafka), Redis (caching), Spring Boot Actuator (metrics), Maven.

---

## Domain model

| Entity | Purpose |
|---|---|
| `Farm` | Name, location, size (hectares), crop type |
| `Activity` | A logged farm event — type, quantity, unit, date |
| `EmissionRecord` | The calculated CO2e result for an activity, with source attribution |

**Activity types and how each is calculated:**

| Type | Calculation source |
|---|---|
| `FUEL_COMBUSTION` | Carbon Interface API (real, live) |
| `IRRIGATION_ENERGY` | Carbon Interface API — electricity endpoint (real, live) |
| `FERTILIZER_APPLICATION` | Static IPCC-style factor (N2O-adjusted, kg CO2e/kg) |
| `LIVESTOCK` | Static factor (kg CO2e/head/year, simplified enteric + manure) |
| `TILLAGE` | Static factor — **negative** value, modeling reduced-till as a sequestration credit |

---

## Event-driven flow

`POST /farms/{farmId}/activities` does **not** call the emissions calculation synchronously. Instead:

1. Controller saves the `Activity`, publishes an `ActivityLoggedEvent` to Kafka, and returns immediately.
2. A separate Kafka consumer (`ActivityLoggedConsumer`) picks up the event on its own thread, re-fetches the activity, and calls `EmissionCalculationService`.

**Why:** decouples the user-facing request from a potentially slow external API call (Carbon Interface). A burst of activity logs doesn't block on external latency, and Spring Kafka's default retry/backoff handles transient external-API failures automatically (verified during development — see "Lessons learned" below).

---

## External integrations

### Real, live integrations
- **Carbon Interface** (carboninterface.com) — real emissions estimation API for fuel combustion and electricity. Responses are cached in Redis (24h TTL) to avoid redundant calls for repeated quantities.
- **Open-Meteo** — used within the FarmBeats companion project for real GPS-based weather forecasts.

### Simulated integrations (documented, not hidden)
Real-world equivalents of these require vendor/dealer relationships not available to a solo project. Each is built using a shared adapter pattern so a real implementation can be dropped in later without touching any downstream code:

- **John Deere Operations Center** (`JohnDeereFieldOperationAdapter`) — stands in for tractor/tillage telemetry. A production version would pull real field-operation records from Deere's API.
- **Valley/Netafim irrigation controllers** — designed, not yet implemented. Would provide real per-cycle electricity usage instead of manual entry.
- **Allflex/CowManager herd management** — designed, not yet implemented. Would provide real RFID-based headcount instead of manual entry.

**Integration architecture:** `FieldOperationSource` interface → each adapter normalizes its vendor's data into a shared `ExternalFieldOperation` record → `ActivityIngestionService` feeds it through the exact same `Activity` → Kafka → `EmissionCalculationService` pipeline as manual entry. Adding a new source requires zero changes to existing code.

### FarmBeats sensor data — explicitly not used for emissions inference
FarmBeats' `/sensor/{field_id}` soil-moisture data is documented (in its own README) as simulated with random variation, and is advisory (what *should* happen), not a record of what *did* happen. Emissions data should come from systems of record (equipment telemetry, meters), not inferred from advisory sensor readings — this was a deliberate design decision after evaluating and rejecting a sensor-delta-based inference approach.

---

## Sustainability engineering ("green software" practices)

Two separate concerns, both addressed:

### 1. The application's own hosting footprint
- `spring.jpa.open-in-view: false` — avoids holding DB connections open for the full request lifecycle.
- Response compression enabled.
- HikariCP pool explicitly sized (avoids over-provisioning idle DB connections).
- Redis caching for external API calls — fewer redundant calls means less compute on both sides.
- **In progress:** `sustainability` package — estimates the app's own operational carbon footprint by combining live grid carbon intensity (Electricity Maps API) with an estimated energy draw derived from Actuator's CPU/uptime metrics.
  - **Honest limitation:** the energy estimate uses a generic watts-per-core constant and a single point-in-time CPU reading extrapolated across total uptime — not real hardware power curves or continuous sampling. A production version would ingest real cloud billing/usage data through a tool like [Cloud Carbon Footprint](https://www.cloudcarbonfootprint.org/), using the cloud provider's own per-instance-type power coefficients, and would typically run as a periodic batch job against billing data rather than a live in-app calculation.
- **Documented, not yet deployed:** target hosting region chosen for low grid carbon intensity (e.g., AWS `eu-north-1`); Kubernetes HPA config drafted to scale down during low traffic instead of running fixed capacity continuously.

### 2. Helping farms reach carbon neutrality (planned — see roadmap)
Current functionality measures emissions. Reaching "neutral" requires also tracking reductions/removals and offsets against a target — see roadmap below.

---

## Testing

- `EmissionFactorCalculatorTest` — pure unit tests on the static-factor math (no mocks, no Spring context).
- `EmissionCalculationServiceTest` — mocked `CarbonInterfaceClient`, verifies correct routing per activity type and that static-factor types never call the external API.
- `ActivityEventPublisherTest` — verifies Kafka send call shape without a real broker.
- `EmissionRecordRepositoryTest` (`@DataJpaTest`) — specifically re-proves the aggregation query's `COALESCE` handles farms with zero emission records (a real bug caught during manual testing — see below).
- `ActivityControllerTest` / `ReportControllerTest` (`@WebMvcTest`) — HTTP layer, mocked repositories/services.

Run with `mvn test`.

---

## Lessons learned / notable bugs caught during development

Worth keeping as a record — several real, instructive bugs surfaced while building this:

- **Transposed-letter typos in config are the hardest bugs to spot visually**: `kakfa` instead of `kafka` (YAML key), `famrs` instead of `farms` (`@RequestMapping` path), `COALSCE` instead of `COALESCE` (JPQL), `CONTROLER` instead of `CONTROLLER` (Docker Compose env var). None caused compile errors — all surfaced only at runtime, several with confusing downstream symptoms (a `@RequestMapping` typo produced a generic 404 with no indication of *why*).
- **IDE auto-import picked the wrong class twice** for generically-named classes (`Logger` from `java.util.logging` instead of `org.slf4j`; `StringDeserializer` from Jackson instead of Kafka's own) — worth always checking the full package path when multiple auto-import options appear.
- **Spring Boot 4 / Spring Kafka 4.1 compatibility gap**: Spring Kafka's `ErrorHandlingDeserializer`/`JsonDeserializer` still depend on classic Jackson 2 (`com.fasterxml.jackson.databind`), which Spring Boot 4 no longer includes by default (it ships Jackson 3 under `tools.jackson.*`). Fixed by explicitly adding `com.fasterxml.jackson.core:jackson-databind` alongside the new Jackson 3 dependency — both coexist without conflict.
- **`kind`-based local Kubernetes clusters don't expose `NodePort` directly to `localhost`** (the cluster node is itself a Docker container) — `kubectl port-forward` is the reliable way to reach a service locally regardless of cluster type.

---

## Roadmap

### Carbon-neutral domain features (not yet built)
- `CarbonTarget` entity — a farm's declared neutrality goal (baseline, target, target date).
- Broader reduction/removal activity types (cover cropping, agroforestry, biochar, renewable-powered irrigation) — extends the existing `EmissionFactorCalculator` pattern that `TILLAGE` already established (negative-emission activities).
- `GET /farms/{id}/neutrality-status` — net position (emissions − reductions − offsets) against target.
- `OffsetPurchase` entity + an offset-registry verification adapter (Verra/Gold Standard) — same adapter pattern as the equipment-telemetry integrations above; real registry access has the same vendor-relationship limitation as John Deere/Valley/Allflex, so a simulated adapter is the realistic scope here too.
- Carbon-aware recommendations, reasoning over a farm's emissions data the way FarmBeats' existing Claude-based ReAct agent reasons over sensor data — the most interesting, least "just another entity" piece of this roadmap.

### FarmBeats UI integration
Two services, one farmer-facing experience is the goal. For a portfolio/demo scope: extend FarmBeats' existing `static/index.html` with a carbon-report card that calls farm-carbon-service's REST API directly (requires CORS config on this service). For genuine production scale: a Backend-for-Frontend (BFF) service aggregating both backends server-side, so the browser only ever talks to one public API, auth is centralized, and neither backend is directly internet-exposed.

### Sustainability tracking, production-grade version
Not planned for this project specifically (requires real cloud billing history to be meaningful) but the correct next step would be integrating [Cloud Carbon Footprint](https://www.cloudcarbonfootprint.org/) against real deployment usage data, replacing the in-app estimate described above.

---

## Running locally

Default profile uses an in-memory H2 database — no setup needed beyond Kafka and Redis:

```bash
docker compose up -d          # starts Kafka and Redis
mvn spring-boot:run
```

To run against PostgreSQL instead:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export CARBON_INTERFACE_API_KEY=your_key
export ELECTRICITY_MAPS_API_KEY=your_key   # once sustainability tracking is added
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Required environment variables
| Variable | Purpose |
|---|---|
| `CARBON_INTERFACE_API_KEY` | Fuel/electricity emissions estimation |
| `ELECTRICITY_MAPS_API_KEY` | Live grid carbon intensity (sustainability package) |
| `DB_USERNAME` / `DB_PASSWORD` | Only needed for the `postgres` profile |

---

## Related project

[FarmBeats Agentic AI](https://github.com/AmaniGudali95/farmbeats_Agentic_AI) — the companion Python/FastAPI precision-agriculture advisor this service is designed to sit alongside. Built independently but sharing the same underlying goal: practical, data-driven tools for farms, with an honest accounting of what's real, what's simulated, and why.
