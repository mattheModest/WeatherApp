export interface Env {
  WEATHER_CACHE: KVNamespace;
  OPEN_METEO_BASE_URL: string;
}

interface HourlyForecast {
  hour_epoch: number;
  temperature_c: number;
  precipitation_probability: number; // decimal 0.0–1.0
  wind_speed_kmh: number;
  weather_code: number;
}

interface WorkerResponse {
  lat_grid: number;
  lon_grid: number;
  fetched_at: string; // ISO 8601 UTC
  hourly_forecasts: HourlyForecast[];
}

interface OpenMeteoResponse {
  hourly: {
    time: string[];
    temperature_2m: number[];
    precipitation_probability: number[];
    windspeed_10m: number[];
    weathercode: number[];
  };
}

function isValidDate(dateStr: string): boolean {
  return /^\d{4}-\d{2}-\d{2}$/.test(dateStr);
}

function transformResponse(
  latGrid: number,
  lonGrid: number,
  openMeteoData: OpenMeteoResponse
): WorkerResponse {
  const {
    time,
    temperature_2m,
    precipitation_probability,
    windspeed_10m,
    weathercode,
  } = openMeteoData.hourly;

  const hourlyForecasts: HourlyForecast[] = time.map((t, i) => ({
    hour_epoch: Math.floor(new Date(t + "Z").getTime() / 1000),
    temperature_c: temperature_2m[i],
    precipitation_probability: precipitation_probability[i] / 100,
    wind_speed_kmh: windspeed_10m[i],
    weather_code: weathercode[i],
  }));

  return {
    lat_grid: latGrid,
    lon_grid: lonGrid,
    fetched_at: new Date().toISOString(),
    hourly_forecasts: hourlyForecasts,
  };
}

function jsonResponse(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    try {
      const url = new URL(request.url);

      if (url.pathname !== "/forecast") {
        return jsonResponse({ message: "not found" }, 404);
      }

      // Task 2.1 — Parse query params
      const latGridStr = url.searchParams.get("lat_grid");
      const lonGridStr = url.searchParams.get("lon_grid");
      const date = url.searchParams.get("date");

      // Task 2.2 — Validate: missing params
      if (!latGridStr || !lonGridStr || !date) {
        return jsonResponse(
          { message: "missing required params: lat_grid, lon_grid, date" },
          400
        );
      }

      // Task 2.2 — Validate: non-numeric lat/lon
      const latGrid = parseFloat(latGridStr);
      const lonGrid = parseFloat(lonGridStr);
      if (isNaN(latGrid) || isNaN(lonGrid)) {
        return jsonResponse(
          { message: "lat_grid and lon_grid must be numeric" },
          400
        );
      }

      // Validate geographic range
      if (latGrid < -90 || latGrid > 90) {
        return jsonResponse({ message: "lat_grid must be between -90 and 90" }, 400);
      }
      if (lonGrid < -180 || lonGrid > 180) {
        return jsonResponse({ message: "lon_grid must be between -180 and 180" }, 400);
      }

      // Task 2.2 — Validate: date format
      if (!isValidDate(date)) {
        return jsonResponse(
          { message: "date must be in YYYY-MM-DD format" },
          400
        );
      }

      // Task 2.3 — Construct KV key
      const kvKey = `forecast:${latGrid}:${lonGrid}:${date}`;

      // Task 2.4 — Check KV cache
      const cached = await env.WEATHER_CACHE.get(kvKey);
      if (cached !== null) {
        return new Response(cached, {
          status: 200,
          headers: { "Content-Type": "application/json" },
        });
      }

      // Task 3.1 — Construct Open-Meteo URL
      const openMeteoUrl =
        `${env.OPEN_METEO_BASE_URL}/forecast` +
        `?latitude=${latGrid}&longitude=${lonGrid}` +
        `&hourly=temperature_2m,precipitation_probability,windspeed_10m,weathercode` +
        `&timezone=UTC&forecast_days=2`;

      // Task 3.2 — Fetch from Open-Meteo; handle non-ok (AC-4, Task 4.1)
      let meteoResponse: Response;
      try {
        meteoResponse = await fetch(openMeteoUrl);
      } catch {
        // Task 4.2 — Network failure fetching Open-Meteo
        return jsonResponse({ message: "upstream error" }, 502);
      }

      if (!meteoResponse.ok) {
        // Task 4.1 — Non-ok Open-Meteo response
        return jsonResponse({ message: "upstream error" }, 502);
      }

      // Task 3.3 — Transform Open-Meteo response to internal schema
      const openMeteoData: OpenMeteoResponse = await meteoResponse.json();
      const transformed = transformResponse(latGrid, lonGrid, openMeteoData);
      const transformedJson = JSON.stringify(transformed);

      // Task 3.4 — Store in KV with 30-minute TTL (expirationTtl: 1800)
      await env.WEATHER_CACHE.put(kvKey, transformedJson, {
        expirationTtl: 1800,
      });

      // Task 3.5 — Return transformed response (Task 4.4: no envelope wrapper)
      return new Response(transformedJson, {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    } catch {
      // Task 4.3 — Unhandled top-level exception
      return jsonResponse({ message: "internal error" }, 500);
    }
  },
};
