export default {
  async fetch(request) {
    const url = new URL(request.url);

    if (url.pathname !== "/forecast") {
      return new Response("Not found", { status: 404 });
    }

    const lat = url.searchParams.get("lat_grid");
    const lon = url.searchParams.get("lon_grid");
    const date = url.searchParams.get("date"); // YYYY-MM-DD

    if (!lat || !lon || !date) {
      return new Response("Missing lat_grid, lon_grid, or date", { status: 400 });
    }

    // Fetch 3 days from the requested date so we always have enough hourly data
    const endDate = new Date(date);
    endDate.setDate(endDate.getDate() + 2);
    const endDateStr = endDate.toISOString().slice(0, 10);

    const openMeteoUrl =
      `https://api.open-meteo.com/v1/forecast` +
      `?latitude=${lat}&longitude=${lon}` +
      `&hourly=temperature_2m,precipitation_probability,windspeed_10m,weathercode` +
      `&temperature_unit=celsius` +
      `&windspeed_unit=kmh` +
      `&timezone=auto` +
      `&start_date=${date}&end_date=${endDateStr}`;

    const omRes = await fetch(openMeteoUrl);
    if (!omRes.ok) {
      return new Response("Open-Meteo error: " + omRes.status, { status: 502 });
    }

    const om = await omRes.json();
    const hourly = om.hourly;

    const hourlyForecasts = hourly.time.map((isoTime, i) => {
      const epoch = Math.floor(new Date(isoTime).getTime() / 1000);
      return {
        hour_epoch: epoch,
        temperature_c: hourly.temperature_2m[i] ?? 0,
        precipitation_probability: (hourly.precipitation_probability[i] ?? 0) / 100,
        wind_speed_kmh: hourly.windspeed_10m[i] ?? 0,
        weather_code: hourly.weathercode[i] ?? 0,
      };
    });

    const body = JSON.stringify({
      lat_grid: parseFloat(lat),
      lon_grid: parseFloat(lon),
      fetched_at: new Date().toISOString(),
      hourly_forecasts: hourlyForecasts,
    });

    return new Response(body, {
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });
  },
};
