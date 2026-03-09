import { SELF, env, fetchMock } from "cloudflare:test";
import { beforeAll, afterEach, afterAll, describe, it, expect } from "vitest";

const OPEN_METEO_ORIGIN = "https://api.open-meteo.com";

const MOCK_OPEN_METEO_RESPONSE = {
  hourly: {
    time: ["2026-03-08T00:00", "2026-03-08T01:00"],
    temperature_2m: [14.2, 13.8],
    precipitation_probability: [15, 10],
    windspeed_10m: [18.0, 16.5],
    weathercode: [1, 1],
  },
};

// Expected epoch for "2026-03-08T00:00Z": 1772928000
// Expected epoch for "2026-03-08T01:00Z": 1772931600

interface WorkerResponse {
  lat_grid: number;
  lon_grid: number;
  fetched_at: string;
  hourly_forecasts: Array<{
    hour_epoch: number;
    temperature_c: number;
    precipitation_probability: number;
    wind_speed_kmh: number;
    weather_code: number;
  }>;
}

describe("WeatherApp Cloudflare Worker", () => {
  beforeAll(() => {
    fetchMock.activate();
    fetchMock.disableNetConnect();
  });

  afterEach(() => fetchMock.assertNoPendingInterceptors());

  afterAll(() => fetchMock.deactivate());

  // ─── Test 5.1: Cache hit ────────────────────────────────────────────────
  describe("5.1 Cache hit (AC-1)", () => {
    it("returns cached JSON without calling Open-Meteo", async () => {
      const cachedData: WorkerResponse = {
        lat_grid: 37.8,
        lon_grid: -122.4,
        fetched_at: "2026-03-08T12:00:00Z",
        hourly_forecasts: [
          {
            hour_epoch: 1772928000,
            temperature_c: 14.2,
            precipitation_probability: 0.15,
            wind_speed_kmh: 18.0,
            weather_code: 1,
          },
        ],
      };

      // Pre-populate KV cache
      await env.WEATHER_CACHE.put(
        "forecast:37.8:-122.4:2026-03-08",
        JSON.stringify(cachedData)
      );

      // disableNetConnect ensures Open-Meteo is NOT called — any attempt throws
      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lat_grid=37.8&lon_grid=-122.4&date=2026-03-08"
      );

      expect(response.status).toBe(200);
      const body = (await response.json()) as WorkerResponse;
      expect(body).toEqual(cachedData);
    });
  });

  // ─── Test 5.2: Cache miss ───────────────────────────────────────────────
  describe("5.2 Cache miss (AC-2)", () => {
    it("calls Open-Meteo, stores in KV, returns transformed response", async () => {
      fetchMock
        .get(OPEN_METEO_ORIGIN)
        .intercept({ path: /\/v1\/forecast/ })
        .reply(200, JSON.stringify(MOCK_OPEN_METEO_RESPONSE), {
          headers: { "content-type": "application/json" },
        });

      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lat_grid=37.8&lon_grid=-122.4&date=2026-03-09"
      );

      expect(response.status).toBe(200);
      const body = (await response.json()) as WorkerResponse;
      expect(body.lat_grid).toBe(37.8);
      expect(body.lon_grid).toBe(-122.4);
      expect(typeof body.fetched_at).toBe("string");
      expect(Array.isArray(body.hourly_forecasts)).toBe(true);
      expect(body.hourly_forecasts).toHaveLength(2);

      // Verify data was stored in KV
      const stored = await env.WEATHER_CACHE.get(
        "forecast:37.8:-122.4:2026-03-09"
      );
      expect(stored).not.toBeNull();
    });
  });

  // ─── Test 5.3: Open-Meteo 500 ───────────────────────────────────────────
  describe("5.3 Open-Meteo upstream error (AC-4)", () => {
    it("Open-Meteo 500 → Worker returns 502 with upstream error body", async () => {
      fetchMock
        .get(OPEN_METEO_ORIGIN)
        .intercept({ path: /\/v1\/forecast/ })
        .reply(500, "Internal Server Error");

      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lat_grid=10.0&lon_grid=10.0&date=2026-03-08"
      );

      expect(response.status).toBe(502);
      const body = (await response.json()) as { message: string };
      expect(body).toEqual({ message: "upstream error" });
    });
  });

  // ─── Test 5.3b: Network exception (Task 4.2) ────────────────────────────
  describe("5.3b Network failure (Task 4.2)", () => {
    it("network-level error fetching Open-Meteo → Worker returns 502 upstream error", async () => {
      fetchMock
        .get(OPEN_METEO_ORIGIN)
        .intercept({ path: /\/v1\/forecast/ })
        .replyWithError("ECONNREFUSED");

      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lat_grid=20.0&lon_grid=20.0&date=2026-03-15"
      );

      expect(response.status).toBe(502);
      const body = (await response.json()) as { message: string };
      expect(body).toEqual({ message: "upstream error" });
    });
  });

  // ─── Test 5.4: Missing lat_grid ─────────────────────────────────────────
  describe("5.4 Missing required params (AC-2 validation)", () => {
    it("missing lat_grid → returns 400", async () => {
      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lon_grid=-122.4&date=2026-03-08"
      );
      expect(response.status).toBe(400);
    });

    it("missing lon_grid → returns 400", async () => {
      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lat_grid=37.8&date=2026-03-08"
      );
      expect(response.status).toBe(400);
    });

    it("missing date → returns 400", async () => {
      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lat_grid=37.8&lon_grid=-122.4"
      );
      expect(response.status).toBe(400);
    });

    it("non-numeric lat_grid → returns 400", async () => {
      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lat_grid=abc&lon_grid=-122.4&date=2026-03-08"
      );
      expect(response.status).toBe(400);
    });

    it("invalid date format → returns 400", async () => {
      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lat_grid=37.8&lon_grid=-122.4&date=20260308"
      );
      expect(response.status).toBe(400);
    });

    it("lat_grid out of range (>90) → returns 400", async () => {
      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lat_grid=91&lon_grid=-122.4&date=2026-03-08"
      );
      expect(response.status).toBe(400);
    });

    it("lon_grid out of range (<-180) → returns 400", async () => {
      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lat_grid=37.8&lon_grid=-181&date=2026-03-08"
      );
      expect(response.status).toBe(400);
    });
  });

  // ─── Test 5.5: Transformed schema shape ─────────────────────────────────
  describe("5.5 Transformed schema shape (AC-3)", () => {
    it("transformed schema matches AC-3: field names, types, precipitation as decimal 0.0–1.0", async () => {
      fetchMock
        .get(OPEN_METEO_ORIGIN)
        .intercept({ path: /\/v1\/forecast/ })
        .reply(200, JSON.stringify(MOCK_OPEN_METEO_RESPONSE), {
          headers: { "content-type": "application/json" },
        });

      const response = await SELF.fetch(
        "https://weatherapp-worker.example.com/forecast?lat_grid=51.5&lon_grid=-0.1&date=2026-03-10"
      );

      expect(response.status).toBe(200);
      const body = (await response.json()) as WorkerResponse;

      // Top-level schema fields (AC-3)
      expect(typeof body.lat_grid).toBe("number");
      expect(typeof body.lon_grid).toBe("number");
      expect(typeof body.fetched_at).toBe("string");
      // ISO 8601 UTC format
      expect(body.fetched_at).toMatch(
        /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+Z$/
      );
      expect(Array.isArray(body.hourly_forecasts)).toBe(true);

      // hourly_forecast entry schema
      const f = body.hourly_forecasts[0];
      expect(typeof f.hour_epoch).toBe("number");
      expect(typeof f.temperature_c).toBe("number");
      expect(typeof f.precipitation_probability).toBe("number");
      expect(typeof f.wind_speed_kmh).toBe("number");
      expect(typeof f.weather_code).toBe("number");

      // Precipitation must be decimal (0.0–1.0), NOT percentage (0–100)
      expect(f.precipitation_probability).toBe(0.15); // 15 / 100
      expect(f.precipitation_probability).toBeGreaterThanOrEqual(0.0);
      expect(f.precipitation_probability).toBeLessThanOrEqual(1.0);

      // Verify correct field values and names from transformation
      expect(f.temperature_c).toBe(14.2); // temperature_2m → temperature_c
      expect(f.wind_speed_kmh).toBe(18.0); // windspeed_10m → wind_speed_kmh
      expect(f.weather_code).toBe(1); // weathercode → weather_code
      expect(f.hour_epoch).toBe(1772928000); // "2026-03-08T00:00Z" → epoch
    });
  });
});
