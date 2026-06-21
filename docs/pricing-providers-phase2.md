# Phase 2: External Pricing Providers

This document describes the adapter architecture for verifying trip cost estimates against external APIs. Phase 1 uses AI-generated indicative ranges; Phase 2 merges verified quotes where adapters are available and falls back to AI estimates otherwise.

## Goals

- Replace or augment AI cost breakdown lines with **verified**, **estimated**, or **unknown** labels
- Gate car-specific adapters (fuel, parking, car rental sub-lines) on transport modes in the user's `PreferenceProfile`
- Ship adapters incrementally behind feature flags (`app.pricing.*` in `application.yml`)
- Keep local dev working with stub adapters when API keys are absent

## Core abstractions

```java
public interface PricingProvider {
    /** Human-readable provider id, e.g. "booking-accommodation". */
    String id();

    /** Whether this provider should run for the given profile and cost category. */
    boolean supports(PreferenceProfile profile, PricingCategory category);

    /** Fetch a price range for the trip context. Returns empty if unavailable. */
    Optional<CostRangeResult> quote(PricingContext context);
}

public record PricingContext(
    String destination,
    double lat,
    double lng,
    LocalDate startDate,
    LocalDate endDate,
    PreferenceProfile profile,
    WeatherSnapshot weather
) {}

public record CostRangeResult(
    int min,
    int max,
    String currency,
    PricingConfidence confidence, // VERIFIED | ESTIMATED | UNKNOWN
    String source,
    Map<String, String> metadata
) {}

public enum PricingCategory {
    ACCOMMODATION,
    FOOD,
    ATTRACTIONS,
    TRANSPORT,
    TRANSPORT_PUBLIC_TRANSIT,
    TRANSPORT_TRAINS,
    TRANSPORT_FLIGHTS,
    TRANSPORT_CAR_RENTAL,
    TRANSPORT_FUEL,
    TRANSPORT_PARKING
}
```

```java
@Service
public class PricingProviderRegistry {
    List<PricingProvider> providers;

    public Map<PricingCategory, CostRangeResult> quoteAll(PricingContext context) {
        return providers.stream()
            .filter(p -> p.supports(context.profile(), categoryFor(p)))
            .flatMap(p -> p.quote(context).stream())
            .collect(...);
    }
}
```

`ItineraryGenerationService` orchestrates:

1. Build `PricingContext` from trip + weather
2. Run registry (respecting car-mode gate)
3. Pass verified hints into `TripPlanPromptBuilder` or merge post-AI
4. Label each line in `ProposalCostBreakdownResponse` with confidence

## Car-mode gate

Use `PreferenceProfileUtils.hasCarMode(profile)` before invoking:

- `FuelPricingAdapter`
- `ParkingPricingAdapter`
- `CarRentalPricingAdapter` (only when `BUDGET_CAR_RENTAL` or `PREMIUM_CAR_RENTAL` selected)

When no car mode is selected, transport breakdown excludes fuel, parking, tolls, and rental sub-lines.

## Adapter catalog

| Adapter | `supports()` gate | Candidate API | Notes |
|---------|-------------------|---------------|-------|
| `BookingAccommodationAdapter` | always (accommodation) | Booking.com Demand API | Partner approval required; cache by destination + dates + party |
| `ExpediaAccommodationAdapter` | fallback OTA | Expedia Rapid | Alternative if Booking unavailable |
| `AmadeusFlightAdapter` | `FLIGHTS` in profile | Amadeus / Duffel | Needs origin airport (future: user home) |
| `Rome2RioTransitAdapter` | `PUBLIC_TRANSIT` or `TRAINS` | Rome2Rio | Route-level quotes; cache by OD pair + date |
| `GtfsTariffAdapter` | regional transit | GTFS + local tariff DB | Country-specific rollout |
| `CarTrawlerRentalAdapter` | rental modes | CarTrawler / Rentalcars | Pickup at destination |
| `FuelEstimateAdapter` | car modes | Route km (OSRM) × EU fuel index | Estimate tier, rarely verified |
| `ParkopediaParkingAdapter` | car modes | Parkopedia | City parking averages |
| `NumbeoFoodAdapter` | always (food) | Numbeo COL index | Always `ESTIMATED` |
| `PlacesAttractionsAdapter` | attractions | Google Places price level | Per-activity hints |

## Configuration

```yaml
app:
  pricing:
    enabled: true
    providers:
      accommodation:
        type: stub # booking | expedia | stub
        api-key: ${BOOKING_API_KEY:}
      flights:
        type: stub
        api-key: ${AMADEUS_API_KEY:}
      transit:
        type: stub
      car-rental:
        type: stub
      fuel:
        type: stub
      parking:
        type: stub
      food:
        type: numbeo
```

Each adapter reads its block and no-ops (returns empty) when disabled or missing credentials.

## Merge strategy

1. **Pre-AI**: inject verified accommodation/flight ranges into prompt as grounding hints
2. **Post-AI**: reconcile AI breakdown with adapter quotes:
   - Prefer verified adapter min/max when confidence is `VERIFIED`
   - Keep AI range when adapter empty
   - Widen total range if AI and adapter disagree significantly (>30%)
3. **UI**: show badge per line (`Verified` / `Estimated` / `Unknown`)

## Rollout order

1. Accommodation (single OTA partner)
2. Flights (when `FLIGHTS` selected)
3. Public transit / trains (Rome2Rio or regional GTFS)
4. Car rental (rental modes only)
5. Fuel + parking (car modes only)
6. Food index (always estimated)

## Testing

- Unit tests per adapter with recorded fixtures (WireMock)
- Registry integration test: car mode off → fuel/parking adapters skipped
- Contract test: merged breakdown always satisfies `includesCarCosts` consistency with profile

## Security & compliance

- Store API keys in env/secrets manager, never in repo
- Rate-limit and cache external calls per trip generation
- Affiliate/partner compliance for Booking, Airbnb, flight OTAs
- Sanitize `additionalRequirements` before any external query logging
