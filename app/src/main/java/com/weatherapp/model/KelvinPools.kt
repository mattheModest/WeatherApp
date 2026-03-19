package com.weatherapp.model

internal object KelvinPools : PoolSet {
    private val TR = ClimateZone.TROPICAL
    private val ST = ClimateZone.SUBTROPICAL
    private val TE = ClimateZone.TEMPERATE
    private val OC = ClimateZone.OCEANIC
    private val NO = ClimateZone.NORDIC

    // ── Verdict pools ────────────────────────────────────────────────────────

    override val stormVerdict: ZonedPool = mapOf(
        TR to listOf(
            "ITCZ storm cell. Heavy lightning and rainfall. Stay inside.",
            "Squall line. Tropical convective instability. Stay inside.",
            "Mesoscale convective system. Rates exceed 50mm/hr. Inside.",
            "Deep tropical convection. Lightning and heavy rain. Inside."
        ),
        ST to listOf(
            "Thunderstorm cell. Subtropical convective trigger. Stay in.",
            "Severe convection. Subtropical instability released. Inside.",
            "Subtropical thunderstorm. High CAPE. Lightning risk is real.",
            "Intense convective storm. Subtropical energy releasing. Shelter."
        ),
        TE to listOf(
            "Active thunderstorm overhead. Lightning significant. Stay in.",
            "Cumulonimbus with lightning. Atmosphere has right of way.",
            "Severe convective activity. Inside is the correct answer now.",
            "Mesoscale system. Thunder and lightning. This is a real storm."
        ),
        OC to listOf(
            "Extratropical cyclone with storms. Atlantic fetch serious.",
            "Explosive cyclogenesis. Pressure dropping fast. Stay inside.",
            "Deep low with convective cells. Maritime gales and lightning.",
            "Severe maritime storm. Low intensified past forecast. Inside."
        ),
        NO to listOf(
            "Polar front cyclone. Convective cores. Rare. Stay inside.",
            "Severe Nordic storm. Norwegian Sea low. Full energy budget.",
            "Arctic cyclone. Wind, lightning, heavy precipitation. Inside.",
            "Extratropical bomb cyclone. Rapid intensification. Stay out."
        )
    )

    override val heavyRainVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Monsoon rainfall. 30-50mm/hr. Full waterproofing required.",
            "Convective downpour, tropical intensity. Waterproof everything.",
            "Heavy tropical rainfall. ITCZ moisture. High rates. Gear up.",
            "Tropical rainfall: high intensity. Drainage overwhelmed. Gear."
        ),
        ST to listOf(
            "Subtropical heavy rain. 20-40mm/hr. Full waterproofing needed.",
            "Intense convective rainfall. Beyond umbrella range. Jacket.",
            "Heavy subtropical rain. Waterproofing, not just an umbrella.",
            "High precipitation. Subtropical moisture. Proper gear today."
        ),
        TE to listOf(
            "Convective rainfall, 20-30mm/hr. Full waterproofing needed.",
            "High intensity. Mesoscale convective system overhead. Gear.",
            "Heavy rainfall: 15-25mm/hr. Jacket required. No umbrella.",
            "Cumulonimbus discharge. High rate. Gear up or stay inside."
        ),
        OC to listOf(
            "Frontal heavy rain, maritime origin. Waterproof jacket mandatory.",
            "Atlantic warm conveyor belt. 15-30mm/hr. Umbrella insufficient.",
            "Persistent heavy rain from maritime front. Full waterproofing.",
            "Orographic enhancement today. Rainfall rates elevated. Gear up."
        ),
        NO to listOf(
            "Heavy rain from polar front. Near-freezing. Full waterproofing.",
            "Cold heavy rain, possible sleet. Nordic front. Full kit.",
            "Rain and sleet mix. Waterproof and insulating layers both needed.",
            "Heavy precipitation. Liquid phase, barely. Wind chill adds to it."
        )
    )

    override val rainVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Convective shower. Tropical pattern. Umbrella; warm and heavy.",
            "Tropical rainfall. High humidity amplifies the feel. Umbrella.",
            "Convective rain, ITCZ proximity. Umbrella, not a hood situation.",
            "Tropical frontal rain band. Warm and wet. Umbrella or jacket."
        ),
        ST to listOf(
            "Subtropical rain. 5-10mm/hr, consistent. Umbrella.",
            "Subtropical cyclone moisture reaching area. Umbrella and jacket.",
            "Rain from subtropical trough. Consistent rainfall. Umbrella.",
            "Organised rain band, subtropical. 4-8mm/hr. Umbrella today."
        ),
        TE to listOf(
            "Frontal precipitation. 5-10mm/hr. Umbrella strongly advised.",
            "Warm front rain band. Steady rainfall. Proper waterproofing.",
            "Organised rainfall, 4-8mm/hr. Classic midlatitude. Umbrella.",
            "Low front delivering consistent rain. Umbrella or jacket."
        ),
        OC to listOf(
            "Atlantic frontal system. Persistent rain. Umbrella.",
            "Maritime frontal rain. Atlantic airflow. Steady. Umbrella.",
            "Atlantic low approaching. 5-10mm/hr. Umbrella + waterproof.",
            "Oceanic air mass, organised rainfall. Slow-moving. Umbrella."
        ),
        NO to listOf(
            "Cold frontal rain, possible sleet at elevation. Umbrella.",
            "Polar front cold rain. Wind chill makes it worse. Umbrella.",
            "Cold rain, sustained. Wind chill present. Waterproof and warm.",
            "Frontal rain in cold air. Umbrella and jacket both needed."
        )
    )

    override val drizzleVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Post-convective drizzle. Sub-millimeter droplets. Light layer.",
            "Residual convective drizzle. Main event passed. Hood sufficient.",
            "Tropical mist. ITCZ humidity. Fine precipitation. Light layer.",
            "Drizzle from maritime tropical air. Minimal dynamics. Hood."
        ),
        ST to listOf(
            "Orographic drizzle. Fine drops on windward side. Light jacket.",
            "Subtropical stratus drizzle. Inversion layer overhead. Hood.",
            "Advection fog producing fine precipitation. Sub-millimeter.",
            "Fine drizzle from subtropical marine layer. Hood sufficient."
        ),
        TE to listOf(
            "Droplet diameter under 0.5mm. Technically mist. Hood optional.",
            "Precipitation rate under 1mm/hr. Barely qualifies. Light layer.",
            "Orographic drizzle. Feel it but won't get soaked. Hood.",
            "Fine precipitation. Negligible accumulation. Light jacket."
        ),
        OC to listOf(
            "Classic maritime drizzle. Persistent, fine, pervasive. Hood up.",
            "Oceanic drizzle. Saturated marine air. Won't soak, but will wet.",
            "Stratus drizzle, maritime. Atlantic coastal baseline. Hood.",
            "Fine drizzle from marine stratus. Everything gets damp. Hood up."
        ),
        NO to listOf(
            "Cold drizzle, near-freezing. Hat and light waterproofing needed.",
            "Nordic drizzle. Cold and persistent. Light moisture still bites.",
            "Fine precipitation in cold air. Cold amplifies discomfort.",
            "Near-freezing drizzle. Low accumulation; chill makes it worse."
        )
    )

    override val snowVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Snow at tropical latitude. Cold intrusion event. Layer up.",
            "Snow here: upper-troposphere cold advection. Full cold gear.",
            "Unusual snow event. Cold air trough at surface. Layer up.",
            "Surface snowfall in tropics. Cold intrusion. Warm gear required."
        ),
        ST to listOf(
            "Winter snow event. Arctic outbreak reached surface. Warm layers.",
            "Anomalous snowfall. Polar cold air advection. Bundle up fully.",
            "Snow at this latitude: accumulation possible. Full winter layer.",
            "Subtropical snowfall. Unusual and disruptive. Full winter kit."
        ),
        TE to listOf(
            "Frozen precipitation. 2-5cm accumulation expected. Watch footing.",
            "Snow. Water in its most structurally interesting phase. Layer.",
            "Solid precipitation. Ice crystal aggregation. Full winter kit.",
            "Snowfall underway. Traction advisable. Proper winter layers."
        ),
        OC to listOf(
            "Maritime polar snow. Variable precipitation type. Watch for ice.",
            "Oceanic snow. Maritime temps mean snow/rain/sleet mix. Layer.",
            "Cold front with snow. May shift to sleet. Waterproof winter kit.",
            "Snow, maritime polar air. Near 0°C surface. Heavy and wet."
        ),
        NO to listOf(
            "Arctic snowfall. Cold, dry powder with accumulation. Layer.",
            "Polar snowfall. Wind chill well below air temp. Every layer.",
            "Nordic snow standard. Base, insulation, shell. Ice is the risk.",
            "Significant snowfall from polar air. Traction aids advisable."
        )
    )

    override val veryWindyVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Cyclone-proximity or squall line winds. Gale force. Stay in.",
            "Extreme wind for tropical latitude. Dangerous conditions. Inside.",
            "Gale-force tropical winds. Hazardous. Minimize outdoor exposure.",
            "Severe tropical wind. 65+ km/h. The only answer: stay inside."
        ),
        ST to listOf(
            "Subtropical cyclone producing gale conditions. Stay in.",
            "Gale-force winds from subtropical low. Stay inside.",
            "Near-gale from subtropical storm. 65+ km/h. Be inside.",
            "Severe wind. Subtropical gale conditions. Minimize outdoor time."
        ),
        TE to listOf(
            "Near-gale, Beaufort 8. Winds above 65km/h. Minimize exposure.",
            "Severe wind. The kind that closes bridges and grounds aircraft.",
            "Gale-force winds. 65+ km/h. Outdoor time should be brief.",
            "Wind gusts to gale force. Not a day to be outside casually."
        ),
        OC to listOf(
            "Atlantic gale. Ocean fetch accelerated this system. Stay in.",
            "Deep Atlantic cyclone. Gusts exceed sustained figure. Inside.",
            "Severe oceanic storm at coast. Gusts well above 65km/h.",
            "Maritime gale. Unobstructed Atlantic fetch. Intense. Stay in."
        ),
        NO to listOf(
            "Polar vortex displacement. Severe Arctic winds. Stay inside.",
            "Arctic gale from Norwegian Low. Frostbite risk at these speeds.",
            "Severe Nordic wind. 65+ km/h in sub-zero air. Stay inside.",
            "Arctic gale. Speed and temperature combined. Do not go out."
        )
    )

    override val windyVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Trade wind surge or tropical trough. Umbrella impractical. Layer.",
            "Organised tropical wind. Squall approach likely. Windproof layer.",
            "Elevated tropical winds. Drag noticeable. Windproof outer layer.",
            "Strong trade winds or system. 40-65km/h. Skip the umbrella."
        ),
        ST to listOf(
            "Subtropical pressure gradient. Umbrella impractical. Windproof.",
            "Gradient winds from subtropical high edge. 40-65km/h. Windproof.",
            "Significant wind for the region. Strong pressure gradient today.",
            "Subtropical trough/high interface. Umbrella exceeded. Windproof."
        ),
        TE to listOf(
            "40-65km/h sustained. Above umbrella viability. Windproof layer.",
            "Strong gradient winds. Gusts likely. Secure what you value.",
            "Synoptic wind event. Umbrella is a liability. Windproof layer.",
            "Significant surface winds. Aerodynamic drag a factor today."
        ),
        OC to listOf(
            "Atlantic winds, 40-65km/h. Oceanic fetch amplifies them. Layer.",
            "Maritime gradient: steep pressure differential. No umbrella.",
            "Oceanic wind. Atlantic flow producing gusty onshore conditions.",
            "Maritime wind ahead of Atlantic system. 40-65km/h. Windproof."
        ),
        NO to listOf(
            "Nordic wind with Arctic air. 40-65km/h. Windproof shell needed.",
            "Strong polar-origin winds. Wind chill is the real story. Layer.",
            "Arctic winds, 40-65km/h. Wind chill 8-12°C below air temp.",
            "Significant Nordic wind. Cold advection amplifies chill. Layer."
        )
    )

    override val hotVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Tropical heat. Extreme humidity crushes evaporative cooling. Water essential.",
            "Extreme heat index. Tropical temperature and humidity combined. Water.",
            "High heat and humidity. Wet-bulb approaching danger zone. Drink water.",
            "Tropical thermal load: extreme humidity amplifies felt temperature. Water essential."
        ),
        ST to listOf(
            "Subtropical heat. High UV from Hadley cell descending dry air.",
            "Dry heat from subtropical high. UV extreme. Sunscreen mandatory.",
            "High solar irradiance. Intense insolation today. SPF, hat, hydration needed.",
            "Subtropical heat: extreme UV, intense insolation. Protect."
        ),
        TE to listOf(
            "Anomalously warm. UV index elevated. Sunscreen is not optional.",
            "Significant thermal load. Hydrate more than you think you need.",
            "Dry heat, elevated UV. Light breathable layers and SPF.",
            "Peak thermal load today. Your cooling mechanisms will be active all day."
        ),
        OC to listOf(
            "Maritime heatwave. Blocking high suppressed Atlantic cooling.",
            "Anomalously warm. Anticyclone removed maritime cooling. SPF.",
            "Warm maritime day. High UV under clear skies. Sunscreen needed.",
            "Oceanic heat. Maritime moderation absent today. Water and SPF."
        ),
        NO to listOf(
            "Nordic heat event. Anomalously warm. UV elevated. SPF.",
            "Hot for the latitude. Extended daylight means UV accumulates. SPF.",
            "Anomalously warm here. Extended daylight amplifies UV load. SPF.",
            "Hot by Nordic standards. Cold adaptation affects heat tolerance."
        )
    )

    override val warmVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Moderate tropical conditions. Brief window before heat builds.",
            "Trade wind cooling keeping temps comfortable. Enjoy moderation.",
            "Pleasant shoulder before the heat resumes. All clear.",
            "Comfortable tropical conditions. Heat index manageable right now."
        ),
        ST to listOf(
            "Optimal subtropical. Comfortable under the high. Clean day.",
            "Warm and clear. Subtropical atmosphere in its best configuration.",
            "Ideal subtropical: comfortable, manageable UV, no rain today.",
            "Comfortable, low humidity under subtropical high. Excellent."
        ),
        TE to listOf(
            "Squarely in the human thermoneutral zone. Body handles this effortlessly.",
            "Mild thermal conditions, clear sky. The meteorological word is fine.",
            "Optimal temperature range. No jacket needed, no excessive heat.",
            "Clean forecast. Comfortable thermal conditions. No drama."
        ),
        OC to listOf(
            "Warm maritime day. Atlantic airflow providing moderated warmth.",
            "Good oceanic conditions. No active fronts. Uncommon but welcome.",
            "Warm and clear. Maritime atmosphere cooperating. No layers.",
            "Optimal oceanic conditions. Atlantic moderation. No rain."
        ),
        NO to listOf(
            "Warm Nordic day. A degree above the weekly average is an event.",
            "Good conditions for the latitude. Brief warm window. Use it.",
            "Warm for here. Daylight and relative warmth aligning. Go.",
            "Above the Nordic average. Thermodynamically notable. Use it."
        )
    )

    override val lightJacketVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Cool by tropical standards. Unusual cold intrusion. Light layer.",
            "Genuinely cold for this latitude. Light jacket warranted.",
            "Tropical cool spell. Anomalously cold air mass. Light layer.",
            "Below tropical comfort threshold. Cold intrusion event. Light jacket."
        ),
        ST to listOf(
            "Cool subtropical day. Below thermoneutral for this latitude. Light jacket.",
            "Subtropical cool spell. Cold advection from higher latitudes. Light jacket.",
            "Below subtropical comfort threshold. Cooler than average. Light jacket.",
            "Cool subtropical air mass. Below thermoneutral zone. Layer."
        ),
        TE to listOf(
            "Crossover point where heat conservation becomes advisable. Light jacket.",
            "Cool ambient conditions. The physiology recommends a light layer.",
            "Mild but cool. A light jacket is the physiologically correct call.",
            "Cool with any wind. Wind chill will make it feel a few degrees colder."
        ),
        OC to listOf(
            "Cool maritime day. Atlantic cool air. Light jacket warranted.",
            "Maritime cool. Atlantic mild-but-cool conditions. Light jacket.",
            "Cool oceanic conditions. Dampness amplifies chill. Light waterproof layer.",
            "Cool maritime air mass. Light waterproof layer recommended."
        ),
        NO to listOf(
            "Mild by Nordic standards. Wind chill still adds several degrees. Jacket.",
            "Cool to the Nordic baseline. Light jacket is routine here.",
            "Moderate Nordic cold. Wind chill makes it feel several degrees colder. Layer.",
            "Light jacket weather. Nordic wind chill makes the thermometer optimistic."
        )
    )

    override val jacketVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Cold by tropical standards. Cold intrusion or elevation. Jacket.",
            "Anomalously cold for this latitude. Cold air mass intruded. Full jacket.",
            "Unusually cold tropical day. Cold air mass outside zone. Jacket.",
            "Below cold threshold for tropical latitude. Cold intrusion. Jacket required."
        ),
        ST to listOf(
            "Cold subtropical day. Arctic outbreak penetrating this far. Jacket.",
            "Subtropical cold snap. Polar air advection into jacket territory.",
            "Cold for this climate. Arctic outbreak reached subtropical latitude. Jacket.",
            "Polar outbreak to subtropical latitude. Jacket needed."
        ),
        TE to listOf(
            "Wind chill pushes felt temperature several degrees lower. Jacket.",
            "Single digits. Body works harder to stay warm. Let jacket help.",
            "Cold ambient. Jacket and layers if you're out for a while.",
            "Below comfort threshold. Core temperature management is relevant."
        ),
        OC to listOf(
            "Cold maritime day. Humidity amplifies cold significantly. Jacket.",
            "Oceanic single digits. Maritime dampness amplifies cold. Jacket.",
            "Jacket weather. Atlantic wind amplifies the cold. Layer up.",
            "Maritime cold. Atlantic wind and humidity combined. Jacket essential."
        ),
        NO to listOf(
            "Mild by Nordic standards. Still jacket territory. Wind may require more.",
            "Nordic moderate cold. Wind chill drops felt temperature. Jacket.",
            "Cold but unremarkable here. Jacket still required.",
            "Jacket weather. Wind chill brings felt temperature well below ambient."
        )
    )

    override val bundleUpVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Near-freezing at tropical latitude: severe cold event. Layer up.",
            "Extreme cold anomaly for this zone. Full thermal system. Body unready.",
            "Near-freezing tropical temps. Full layering. Infrastructure unready.",
            "Remarkable cold outbreak at tropical latitude. Full winter layers."
        ),
        ST to listOf(
            "Severe subtropical cold. Low cold-adaptation amplifies effect. Layer up.",
            "Near-freezing subtropical. Base, insulation, outer shell.",
            "Cold snap well below norm. Infrastructure not designed for it. Layer.",
            "Extreme cold for this latitude. Wind amplifies severely. Bundle up."
        ),
        TE to listOf(
            "Near-freezing. Vasoconstriction immediate. Base, mid layer, outer shell.",
            "Near-freezing. Wind chill significantly below ambient. Every layer you own.",
            "Cold. Genuinely cold. Base, mid, outer shell. Full system.",
            "Below comfort threshold: the physiology demands it. Layer up comprehensively."
        ),
        OC to listOf(
            "Cold maritime near-freezing. Atlantic dampness amplifies cold severely. Layer.",
            "Near-freezing oceanic. Cold and wet demands complete layering.",
            "Near-freezing with maritime air. Humidity drives chill. Bundle up.",
            "Freezing maritime. Wind, cold, moisture. Full thermal protection."
        ),
        NO to listOf(
            "Standard Nordic winter cold. Base, mid, insulation, shell.",
            "Cold Nordic day. Wind chill significant. Full thermal system.",
            "Polar baseline. Any wind at these temperatures is dangerous. Full kit.",
            "Deep cold. Nordic winters require complete layering. Today qualifies."
        )
    )

    override val allClearVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Clear. ITCZ has paused. No convective trigger. Go outside.",
            "Tropical clear day. No convective development forecast. Go out.",
            "No precipitation mechanism. Tropical atmosphere settled. Enjoy.",
            "Clear tropical conditions. Stable air mass. Cooperating fully."
        ),
        ST to listOf(
            "Subtropical high dominant. No cloud, no rain. Perfect conditions.",
            "Subtropical anticyclone dry air descending. Nothing to worry.",
            "Classic subtropical high: clear, dry, stable. Zero rain. Go out.",
            "Anticyclonic subsidence. Clear conditions. No interference today."
        ),
        TE to listOf(
            "High pressure doing its job. Zero atmospheric interference.",
            "Anticyclonic, stable, no precipitation mechanism. Textbook clear.",
            "Zero precipitation probability. Dewpoint well below ambient.",
            "Barometric pressure steady. Atmosphere has nothing for you today."
        ),
        OC to listOf(
            "Clear maritime conditions. Atlantic in favorable configuration.",
            "Clear. No fronts incoming for the next 24 hours. Good day.",
            "Anticyclonic blocking. Clear skies. Atlantic staying cooperative.",
            "Clear oceanic day. No frontal activity. Weather gave you the day."
        ),
        NO to listOf(
            "Scandinavian blocking high. Clear Arctic conditions. Best option.",
            "Clear. Polar vortex stable, no fronts. Cold, still, and clear.",
            "Arctic clear. High pressure, no precipitation. Cold but clear.",
            "Nordic clear day. Polar high overhead. Atmosphere settled today."
        )
    )

    // ── Mood pools ───────────────────────────────────────────────────────────

    override val stormMood: ZonedPool = mapOf(
        TR to listOf(
            "Tropical lightning: return strokes reach 200,000 amperes. Inside.",
            "The ITCZ produces more lightning than any other feature on Earth.",
            "This convective system released energy of a nuclear weapon.",
            "Tropical lightning peaks at 3-8 flashes/min. Watch from a window."
        ),
        ST to listOf(
            "Subtropical thunderstorms: build fast, discharge, clear fast.",
            "The CAPE producing this storm was building all day. Efficient.",
            "Superheated updrafts, ice aloft, electrical discharge. Inside.",
            "The storm cell will clear in 1-3 hours. Watch from safety."
        ),
        TE to listOf(
            "Thunderstorms: superheated air discharging static. Beautiful.",
            "The lightning is genuinely impressive. Watch from a window.",
            "Somewhere a meteorologist is very excited about this.",
            "Storm cell will pass in 2-4 hours. Atmosphere has right of way."
        ),
        OC to listOf(
            "Atlantic storms are extratropical: wider, longer, less lightning.",
            "This energy comes from the polar-tropical temperature gradient.",
            "Maritime storm systems can sustain for days on Atlantic energy.",
            "Extratropical cyclones are the Atlantic's primary mechanism."
        ),
        NO to listOf(
            "Polar cyclones are rare. Dynamics differ from midlatitude.",
            "Arctic storms driven by temperature gradients. Wind is primary.",
            "Norwegian Sea lows arrive with energy built over hundreds of km.",
            "Nordic storm: rapid pressure drop. The atmosphere corrected hard."
        )
    )

    override val heavyRainMood: ZonedPool = mapOf(
        TR to listOf(
            "Tropical rainfall overwhelms urban drainage within minutes.",
            "High-intensity tropical precipitation. Flooding risk is real.",
            "This is monsoon-scale rainfall. Enjoy the sound from inside.",
            "Tropical heavy rain: large droplets, high rate, remarkable sound."
        ),
        ST to listOf(
            "Subtropical heavy rain: rare but intense. Drainage fully tested.",
            "Subtropical convective rain dumps fast. Won't last, but serious.",
            "Intense subtropical rainfall. Feels more dramatic than temperate.",
            "Atmosphere releasing moisture. Fast clearing usually follows."
        ),
        TE to listOf(
            "The drainage infrastructure will be tested today.",
            "Meteorologists classify this as notable rainfall. They are right.",
            "High precipitation rate. The atmosphere is very committed.",
            "Convective activity is significant. Respect it."
        ),
        OC to listOf(
            "Atlantic front collected vapour over hundreds of km of ocean.",
            "Maritime heavy rain persists for hours. Slow-moving system.",
            "The oceanic air mass is delivering all its vapour today.",
            "Heavy oceanic rain. Output of deep maritime low. Draining."
        ),
        NO to listOf(
            "Cold heavy rain in the Nordic zone is particularly unpleasant.",
            "Nordic heavy precipitation: rain, sleet, or mixed. Equally grim.",
            "High rate in near-freezing air. Body loses heat fast. Gear up.",
            "Heavy rain near zero. Cold stress compounded by precipitation."
        )
    )

    override val rainMood: ZonedPool = mapOf(
        TR to listOf(
            "Classic midlatitude cyclone. Drainage getting a workout.",
            "Tropical afternoon convection is normal. Will pass.",
            "Good steady rain. The kind that replenishes the water table.",
            "The front will clear by evening. In the meantime, just wet."
        ),
        ST to listOf(
            "Subtropical rain isn't the baseline. Water table will benefit.",
            "This rain is doing useful work. Cold comfort, but it's there.",
            "Subtropical trough rainfall. The high will reassert itself.",
            "Rain in a dry climate. Infrastructure wasn't built for it."
        ),
        TE to listOf(
            "Classic midlatitude cyclone. Drainage getting a workout.",
            "Frontal rain. Steady, persistent, not dramatic. Just wet.",
            "Good steady rain. The kind that replenishes the water table.",
            "The front will clear by evening. In the meantime, just wet."
        ),
        OC to listOf(
            "Atlantic frontal rain. Oceanic default. The front will clear.",
            "Maritime rain. Steady, mild, relentless. The oceanic baseline.",
            "The front crossed the ocean for days. It's here. It will pass.",
            "Frontal rain. Atlantic delivers these at reliable intervals."
        ),
        NO to listOf(
            "Cold Nordic frontal rain. Temperature differential drives it.",
            "Nordic rain often precedes temperature drops. Watch pressure.",
            "Frontal rain with cold advection behind. May transition to snow.",
            "Persistent cold rain from polar front. Moving slowly. Allow time."
        )
    )

    override val drizzleMood: ZonedPool = mapOf(
        TR to listOf(
            "The precipitation that makes you question if it's raining.",
            "Post-convective moisture. Main storm passed. Atmospheric residue.",
            "Drizzle at tropical humidity: air already saturated. You notice.",
            "Light precipitation from residual cloud. Carry on."
        ),
        ST to listOf(
            "Subtropical drizzle: marine layer advection. Coast effect.",
            "Drizzle in a dry climate. Stratus layer thin. Will clear.",
            "Orographic drizzle: moist air over terrain, fine drops, windward.",
            "Fine precipitation in a dry climate. More notable than elsewhere."
        ),
        TE to listOf(
            "The precipitation that makes you question if it's raining.",
            "Drizzle is clouds that couldn't commit. Atmosphere at its least.",
            "Technically precipitation. Practically, just damp air.",
            "Sub-millimeter droplets. Hair notices before your jacket does."
        ),
        OC to listOf(
            "Oceanic drizzle. Maritime stratus produces it reliably.",
            "Atlantic drizzle: the north oceanic climate's signature output.",
            "The drizzle continues until conditions improve. Oceanic climate.",
            "Fine marine precipitation. Not dramatic. Persistent. Hood up."
        ),
        NO to listOf(
            "Cold Nordic drizzle. Temperature makes it more serious than rate.",
            "Near-freezing fine precipitation. Cold amplifies the discomfort.",
            "Drizzle at low temperatures. The atmosphere at its least.",
            "Cold fine precipitation. Minimal. Temperature does the rest."
        )
    )

    override val snowMood: ZonedPool = mapOf(
        TR to listOf(
            "Snow at tropical latitude: upper-level cold intrusion. Rare.",
            "Climatologically unusual. Enjoy observing it. Infra unready.",
            "Snow here means upper cold reached the surface. Anomalous.",
            "Tropical snowfall: significant thermodynamics to produce this."
        ),
        ST to listOf(
            "Subtropical snow: infrastructure and population unprepared.",
            "Snow event: significant Arctic outbreak. Unusual and disruptive.",
            "Snowfall here is an event. Major cold air outbreak. Seriously.",
            "No two snowflakes share the same crystal structure. Rare here."
        ),
        TE to listOf(
            "No two snowflakes share the same crystal structure. Worth seeing.",
            "The albedo effect: brighter than expected. Your eyes will adjust.",
            "Snow is water in its most structurally interesting phase.",
            "Accumulation is predictable. Your commute is not. Allow time."
        ),
        OC to listOf(
            "Oceanic snow: wet and heavy. Maritime air compacts it fast.",
            "Maritime snow: heavy and wet. Compacts to ice. Watch traction.",
            "Maritime polar snow is wet, dense, and slippery. Watch your step.",
            "Maritime snow may shift to sleet or ice as temperature drops."
        ),
        NO to listOf(
            "Nordic powder snow: low water content, quality crystals. Icy.",
            "Arctic snow: cold and dry, drifts significantly in any wind.",
            "Nordic snow: the climate's native expression. Prepared for it.",
            "High-latitude snowfall: accumulation persists. Ice under snow."
        )
    )

    override val windMood: ZonedPool = mapOf(
        TR to listOf(
            "Tropical wind events often precede larger systems. Watch.",
            "Trade wind surge or tropical low. Wind direction tells the story.",
            "Strong tropical winds: redistributing air masses. Coriolis busy.",
            "Elevated tropical surface winds. Kinetic energy significant."
        ),
        ST to listOf(
            "Strong subtropical gradient. Pressure differential steep today.",
            "Wind in dry subtropical air increases evaporative loss. Hydrate.",
            "Significant wind event. Beaufort: strong breeze to near-gale.",
            "Aerodynamic drag becomes a practical factor at these wind speeds."
        ),
        TE to listOf(
            "At these speeds, aerodynamic drag is a factor in your commute.",
            "Wind force 6+ on the Beaufort scale. Respect the kinetic energy.",
            "Steep pressure gradient today. That's why it's sustained.",
            "Significant wind. Atmosphere redistributing air with enthusiasm."
        ),
        OC to listOf(
            "Atlantic gradient: sustained not gusty. Ocean fetch organises.",
            "Maritime wind: open ocean creates organised laminar surface flow.",
            "North Atlantic low tracking across ocean. Steep gradient.",
            "Oceanic winds covered significant fetch. Organisation reflects."
        ),
        NO to listOf(
            "Arctic wind: cold plus speed creates wind chill far below.",
            "Polar wind event. Wind chill is the operationally relevant.",
            "Nordic winds carry cold air. Wind chill is the real temperature.",
            "Arctic surface flow. Wind chill significant. Time outdoors."
        )
    )

    override val breezeMood: ZonedPool = mapOf(
        TR to listOf(
            "Trade wind conditions: cooling and drying the lower atmosphere.",
            "Classic tropical breeze. Trade winds most reliable on Earth.",
            "Gentle trade winds. Beaufort 3-4. Tropical baseline at its best.",
            "Trade wind breeze. Engine of tropical climate. Agreeable mode."
        ),
        ST to listOf(
            "Good sailing conditions, if that's relevant to your day.",
            "Subtropical high edge breeze: clean and dry. Makes heat bearable.",
            "Beaufort 4 under subtropical high. Characterful, not hostile.",
            "A proper subtropical breeze. Organised, comfortable airflow."
        ),
        TE to listOf(
            "Beaufort 4. Leaves in constant motion, light branches moving.",
            "Good sailing conditions, if that's relevant to your day.",
            "The trees are indicating a moderate breeze. They're rarely wrong.",
            "A proper Beaufort 4. Interesting without being hostile."
        ),
        OC to listOf(
            "Maritime breeze. Atlantic producing moderate sustained airflow.",
            "Oceanic Beaufort 4. Less gusty than inland. More organised flow.",
            "Atlantic breeze. Fetch creates organised laminar flow at surface.",
            "A proper north Atlantic breeze. Characterful. Not hostile."
        ),
        NO to listOf(
            "Nordic Beaufort 4: wind chill still requires a windproof layer.",
            "Moderate wind at northern latitudes. Chill factor significant.",
            "Arctic breeze. Cold air amplifies chill even at moderate speeds.",
            "Beaufort 4 at high latitudes feels sharper than further south."
        )
    )

    override val allClearWarmMood: ZonedPool = mapOf(
        TR to listOf(
            "Optimal tropical conditions. Dry season clear. Make use of it.",
            "Warm, clear tropical. Heat index manageable. Use sunscreen today.",
            "Inter-seasonal clear period: genuinely optimal. The data is good.",
            "Tropical clear and warm. UV remains the one concern. Enjoy it."
        ),
        ST to listOf(
            "Subtropical clear warm day: the climate's optimal expression.",
            "Perfect subtropical conditions. Hadley cell delivered.",
            "Clear, warm, low humidity under subtropical high. Data very good.",
            "Optimal subtropical conditions. Atmosphere in cooperative mode."
        ),
        TE to listOf(
            "Conditions like this are why people move to places like this.",
            "Optimal temperature range for outdoor activity. Data is good.",
            "Nothing to monitor. Nothing to prepare for. Unusual, honestly.",
            "Peak solar irradiance, comfortable temp. Atmosphere cooperating."
        ),
        OC to listOf(
            "Warm maritime clear day. Genuinely uncommon here. Note the date.",
            "Clear and warm oceanic. Atlantic not interfering. Appreciate.",
            "Optimal oceanic conditions. Clear, warm, no fronts. Worth it.",
            "Maritime clear and warm. The rare combination. Appreciate it."
        ),
        NO to listOf(
            "Nordic warm clear day. Clear skies at high latitude is an event.",
            "Warm and clear at this latitude. Extended daylight adds time.",
            "Clear and warm at this latitude. Daylight compensates for UV.",
            "Good Nordic day. Weather window is brief. Data is good. Go."
        )
    )

    override val allClearNeutralMood: ZonedPool = mapOf(
        TR to listOf(
            "No atmospheric drama. Tropical machine paused. Enjoy the calm.",
            "Clear. Calm. ITCZ moved away. Dry season at its most settled.",
            "Nothing to monitor. Tropical atmosphere in its quiet phase.",
            "Clear tropical conditions. Unusually calm. No atmospheric drama."
        ),
        ST to listOf(
            "Nothing to monitor. Subtropical high is behaving perfectly.",
            "Hadley cell descending dry air. Clear, stable. Unremarkably good.",
            "No atmospheric drama. Subtropical anticyclone suppressed all.",
            "Clear. Calm. Dry. Subtropical climate in its baseline expression."
        ),
        TE to listOf(
            "Nothing to monitor. Nothing to prepare for. Unusual, honestly.",
            "No atmospheric drama today. The meteorological day off.",
            "The system is stable. That's the whole story.",
            "Clear. Calm. Unremarkable by the best possible definition."
        ),
        OC to listOf(
            "Clear oceanic conditions. Atlantic paused between systems. Enjoy.",
            "No fronts. No active weather. Oceanic atmosphere at intermission.",
            "Clear maritime conditions. Gap between Atlantic systems. Use it.",
            "Nothing to monitor. Oceanic weather machine temporarily at rest."
        ),
        NO to listOf(
            "Clear Nordic. Polar high, no precipitation. Cold but clear.",
            "Nordic clear day. Scandinavian blocking high is doing its job.",
            "No atmospheric drama. Polar high has everything suppressed.",
            "Stable polar conditions. No fronts incoming. Arctic at rest."
        )
    )

    override val greyMood: ZonedPool = mapOf(
        TR to listOf(
            "Overcast, not raining. Convective instability limited. Carry on.",
            "Cloud suppressing solar intensity. A break from tropical sun.",
            "Grey in tropics often precedes afternoon convection. Watch it.",
            "Stratus without precipitation. Tropical atmosphere gathering."
        ),
        ST to listOf(
            "Subtropical overcast. Marine stratus without rain. It will clear.",
            "Low cloud from marine influence. Cloud base is cosmetic today.",
            "Grey subtropical day. Inversion trapping cloud low. Burns off.",
            "Overcast. Subtropical high will reassert. This is temporary."
        ),
        TE to listOf(
            "The diffuse lighting is even. Great day for photography.",
            "No rain mechanism present. Atmospheric ambiance, not weather.",
            "Cloud base is high. The grey is cosmetic, not structural.",
            "It's just water vapor suspended at altitude. Carry on."
        ),
        OC to listOf(
            "Oceanic overcast: maritime stratus is the default. Just grey.",
            "Cloud from marine boundary layer. Persistent, low-level, no rain.",
            "Atlantic stratus. Oceanic climate's characteristic grey. No rain.",
            "Maritime overcast. Diffuse even light. Cloud base not a worry."
        ),
        NO to listOf(
            "Nordic grey. Persistent high-latitude overcast. No precipitation.",
            "Overcast at northern latitude. Low sun angle. Not threatening.",
            "Grey, still, overcast. Nordic winter's default. No rain today.",
            "Low cloud, no precipitation. The Nordic winter grey. Baseline."
        )
    )
}
