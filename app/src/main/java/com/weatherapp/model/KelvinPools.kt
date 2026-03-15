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
            "ITCZ convective cell overhead. Towering cumulonimbus, lightning, intense rainfall. This is a tropical thunderstorm — stay inside.",
            "Squall line development. Tropical convective instability producing rapid-onset severe weather. Do not attempt to wait this out.",
            "Mesoscale convective system — tropical variety. Rainfall rates can exceed 50mm/hr. The atmosphere is entirely in charge right now.",
            "Deep convection triggered by tropical surface heating. Lightning, thunder, and heavy rain simultaneously. Inside is the only answer."
        ),
        ST to listOf(
            "Thunderstorm cell — subtropical convective trigger. Low dewpoint meets surface heating and this is what happens. Stay in.",
            "Severe convective activity. The subtropical atmosphere has accumulated enough instability to express itself violently. Inside.",
            "Summer thunderstorm: classic subtropical heat-driven convection. The CAPE values made this inevitable. Lightning risk is real.",
            "Intense convective storm. Subtropical high has broken down today and the atmosphere is releasing the energy. Shelter."
        ),
        TE to listOf(
            "Active thunderstorm cell overhead. Lightning frequency is significant. Stay in.",
            "Cumulonimbus development with electrical activity. The atmosphere has right of way today.",
            "Severe convective activity in progress. Inside is the correct answer right now.",
            "Mesoscale convective system producing thunder and lightning. This is a real storm."
        ),
        OC to listOf(
            "Extratropical cyclone with embedded thunderstorms. The Atlantic fetch has delivered significant energy to this system.",
            "Explosive cyclogenesis event. Pressure dropping fast, convective cells developing. This is beyond umbrella territory.",
            "Deep low pressure with convective activity. Maritime storm conditions — sustained gales and electrical activity.",
            "Severe maritime storm. The low tracking in from the west has intensified beyond forecast. Stay indoors."
        ),
        NO to listOf(
            "Polar front cyclone with convective cores. These are rare this far north — and serious. Stay inside.",
            "Severe Nordic storm. Low pressure from the Norwegian Sea is delivering the full Atlantic energy budget today.",
            "Arctic cyclone making landfall. Wind, lightning, heavy precipitation. The polar vortex is not being subtle.",
            "Extratropical bomb cyclone. Rapid intensification overnight produced severe conditions. Do not go out."
        )
    )

    override val heavyRainVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Monsoon-intensity rainfall. 30-50mm/hr. Tropical precipitation rates overwhelm standard waterproofing — full gear required.",
            "Convective downpour, tropical intensity. This is not midlatitude rain. Precipitation rates are extreme. Waterproof everything.",
            "Heavy tropical rainfall event. ITCZ position and high moisture content combine for intense accumulation. Serious waterproofing needed.",
            "Tropical rainfall: high intensity. Drainage will be overwhelmed in minutes at these rates. Gear up fully or stay in."
        ),
        ST to listOf(
            "Subtropical heavy rain event. Moisture-laden air and convective instability producing 20-40mm/hr. Full waterproofing required.",
            "Intense convective rainfall, subtropical system. Rates are well beyond what umbrellas manage. Waterproof jacket essential.",
            "Heavy rain — subtropical convective trigger. Falling faster than drainage can clear. Full waterproofing, not just an umbrella.",
            "High precipitation rate. Subtropical storm moisture is significant. This isn't commuter drizzle — proper gear today."
        ),
        TE to listOf(
            "Convective rainfall event, 20-30mm/hr. An umbrella won't cut it alone — full waterproofing.",
            "High precipitation intensity. Mesoscale convective system overhead. Waterproof everything.",
            "Rainfall intensity: heavy. 15-25mm/hr accumulation. Jacket required, umbrella insufficient alone.",
            "Cumulonimbus discharge event. High rainfall rate. Proper gear or stay inside."
        ),
        OC to listOf(
            "Frontal heavy rain, maritime origin. Atlantic air mass carrying significant moisture content. Waterproof jacket mandatory.",
            "Warm conveyor belt associated with Atlantic low. Rainfall rates 15-30mm/hr. Umbrella alone is insufficient today.",
            "Persistent heavy rain from maritime front. The system is slow-moving — this continues for hours. Full waterproofing.",
            "Orographic enhancement on the windward side today. Rainfall rates are elevated. Waterproof gear essential."
        ),
        NO to listOf(
            "Heavy precipitation from polar front system. Near-freezing temperatures make this worse than it sounds. Full waterproofing.",
            "Cold heavy rain, possible sleet mix. Nordic frontal system delivering significant precipitation. Full waterproof kit.",
            "Rain and sleet mix at these temperatures. Waterproof and insulating layers both needed simultaneously.",
            "Heavy precipitation event — liquid phase, but barely. Wind chill makes the felt temperature significantly worse. Gear up."
        )
    )

    override val rainVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Afternoon convective shower — textbook tropical pattern. Umbrella required; this is warm, heavy, and brief but serious.",
            "Tropical rainfall, moderate intensity. High humidity means the rain feels heavier than it measures. Umbrella.",
            "Convective rain shower, ITCZ proximity. 5-15mm/hr — a real umbrella situation, not a hood situation.",
            "Tropical frontal rain band. Warm and wet, consistent precipitation. Umbrella or waterproof layer."
        ),
        ST to listOf(
            "Subtropical rain event — possibly the only real rain this month. 5-10mm/hr, consistent. Umbrella.",
            "Subtropical cyclone moisture reaching the area. More organised than a tropical shower. Umbrella and light jacket.",
            "Rain from subtropical trough. Not heavy, not brief. Consistent rainfall for several hours. Umbrella.",
            "Organised rain band in subtropical regime. Steady 4-8mm/hr. Umbrella or waterproof layer today."
        ),
        TE to listOf(
            "Frontal precipitation, moderate intensity. 5-10mm/hr. Umbrella strongly advised.",
            "Warm front rain band. Steady rainfall — this isn't a passing shower. Proper waterproofing.",
            "Organised rainfall, 4-8mm/hr. Classic midlatitude cyclone activity. Umbrella.",
            "Low pressure front delivering consistent rain. Umbrella or waterproof jacket."
        ),
        OC to listOf(
            "Atlantic frontal system delivering moderate rainfall. This is the oceanic baseline — expect it to be persistent.",
            "Maritime frontal rain. Characteristic of Atlantic airflow — not heavy, but steady and thorough. Umbrella.",
            "Frontal rain from approaching Atlantic low. 5-10mm/hr, will continue for some time. Umbrella and waterproof layer.",
            "Oceanic air mass producing organised rainfall. The weather system is slow-moving. Umbrella is the right call."
        ),
        NO to listOf(
            "Cold frontal rain, possibly mixed with sleet at elevation. Nordic precipitation is efficient — dress for it. Umbrella.",
            "Polar front delivering cold rain. Wind chill makes this feel significantly worse than the thermometer suggests. Umbrella.",
            "Cold rain, sustained. The polar air mass is not warming this. Waterproof and warm layers both needed.",
            "Frontal rain in cold air. Not snowing, but near enough that layers matter. Umbrella and jacket."
        )
    )

    override val drizzleVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Fine droplet precipitation — residual from convective clearing. Officially drizzle by classification. Light coverage.",
            "Post-convective drizzle. The main event has passed; what's left is sub-millimeter droplets. Hood is sufficient.",
            "Tropical mist. ITCZ humidity is high enough that moisture precipitates without significant convection. Light layer.",
            "Drizzle from maritime tropical air. The water content is there, but the dynamics aren't. Hood optional."
        ),
        ST to listOf(
            "Orographic drizzle — moist air forced over terrain, fine droplets on the windward side. Light jacket.",
            "Subtropical stratus drizzle. The inversion layer trapping moisture overhead produces this fine precipitation. Hood.",
            "Drizzle — advection fog event producing precipitation. Sub-millimeter droplets, low intensity. Light coverage.",
            "Fine drizzle from subtropical marine layer. Low cloud base, persistent moisture. Hood is sufficient."
        ),
        TE to listOf(
            "Droplet diameter under 0.5mm — technically mist by classification. Hood optional.",
            "Precipitation rate under 1mm/hr. This barely qualifies as rain. Light coverage.",
            "Orographic drizzle. You'll feel it but won't get soaked. Hood recommended.",
            "Fine precipitation particles. Accumulation negligible. Light jacket with coverage is sufficient."
        ),
        OC to listOf(
            "Classic maritime drizzle. The north Atlantic specialises in this — persistent, fine, and pervasive. Hood strongly recommended.",
            "Oceanic drizzle — advection of saturated marine air producing fine precipitation. You won't get soaked, but you'll get wet.",
            "Stratus drizzle, maritime origin. This is the defining precipitation type of Atlantic coastal climates. Hood required.",
            "Fine drizzle from marine stratus. Technically minimal precipitation. Practically: everything will be damp. Hood up."
        ),
        NO to listOf(
            "Cold drizzle, near-freezing. More unpleasant than the precipitation rate suggests due to temperature. Hat and light waterproofing.",
            "Nordic drizzle — cold and persistent. At these temperatures, even light moisture is uncomfortable without coverage.",
            "Fine precipitation in cold air. The droplets are sub-millimeter but the cold amplifies the unpleasantness. Light waterproofing.",
            "Near-freezing drizzle. Accumulation is low but chill factor makes this worse than its precipitation rate implies."
        )
    )

    override val snowVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Frozen precipitation at tropical latitude — this requires significant upper-level cold intrusion. Rare event. Layers and caution.",
            "Snow: highly anomalous at this latitude. Upper troposphere cold air advection has created surface freezing. Full cold gear.",
            "Unusual snow event. Upper-level trough creating below-freezing temperatures at the surface — geologically rare here.",
            "Surface snowfall in the tropics. Upper-level cold intrusion event. Significant — layers, warm footwear, exercise caution."
        ),
        ST to listOf(
            "Winter snow event — subtropical cold snap. Arctic outbreak has pushed the freezing level to the surface. Warm layers.",
            "Anomalous snowfall for this region. Cold air advection from polar origins has created freezing surface conditions. Bundle up.",
            "Snow: a genuine meteorological event at this latitude. Cold outbreak, measurable accumulation possible. Full winter preparation.",
            "Subtropical snowfall. This doesn't happen often, which is why it's disrupting everything. Full winter kit."
        ),
        TE to listOf(
            "Frozen precipitation event. Dendritic crystal formation — 2-5cm accumulation expected. Warm layers, watch your footing.",
            "Snow — water in its most structurally interesting phase. Surface temperatures at or below 0°C.",
            "Solid precipitation. Ice crystal aggregation producing measurable accumulation. Full winter kit.",
            "Snowfall event underway. Traction advisable. Proper winter layers."
        ),
        OC to listOf(
            "Snow from maritime polar air mass. Cold enough aloft, mild enough at surface to mean variable precipitation type. Watch for ice.",
            "Oceanic snow event — maritime temperatures often cause snow/rain/sleet mix. Waterproof and warm layers simultaneously.",
            "Cold front with snow. The maritime influence means it may transition to sleet or rain. Full waterproof winter kit.",
            "Snow, maritime polar airmass. Surface temperatures hovering around 0°C — any accumulation will also be wet and heavy."
        ),
        NO to listOf(
            "Arctic snowfall. This is not the maritime variety — cold, dry, light powder with measurable accumulation. Full winter system.",
            "Polar snowfall event. Wind chill is pushing felt temperature well below air temperature. Every layer counts.",
            "Snow: the Nordic standard. Proper winter gear — base layer, insulation, shell. Ice under fresh snow is the real risk.",
            "Significant snowfall from polar air mass. Accumulation expected. Traction aids advisable. Full winter preparation."
        )
    )

    override val veryWindyVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Tropical cyclone-proximity winds or squall line passage. Sustained winds at gale force. Do not be outside.",
            "Extreme wind event for tropical latitude. Deep low pressure or squall line producing dangerous conditions. Inside.",
            "Gale-force tropical winds. Whether squall or system edge, these are hazardous. Minimize all outdoor exposure.",
            "Severe tropical wind event. 65+ km/h. The only sensible response is to remain inside."
        ),
        ST to listOf(
            "Subtropical cyclone or intense low producing gale conditions. These systems intensify rapidly. Stay in.",
            "Gale-force winds from subtropical low pressure. The system is compact and intense. Outdoor exposure: inadvisable.",
            "Near-gale conditions from subtropical storm system. 65+ km/h sustained. Buildings are the right place to be.",
            "Severe wind event. Subtropical dynamics have produced gale-force conditions. Minimize outdoor time."
        ),
        TE to listOf(
            "Near-gale conditions, Beaufort 8. Wind speeds above 65km/h. Minimize outdoor exposure.",
            "Severe wind event. The kind that closes bridges and grounds small aircraft.",
            "Gale-force sustained winds. 65+ km/h. Outdoor exposure should be purposeful and brief.",
            "Wind gusts to gale force. Not a day to be cavalier about outdoor plans."
        ),
        OC to listOf(
            "Atlantic gale. The fetch across open ocean has accelerated this system beyond most land-based wind events. Stay in.",
            "Deep Atlantic cyclone producing near-gale conditions. Gusts will exceed the sustained figure significantly. Inside.",
            "Severe oceanic storm reaching the coast. Wind speed aloft is translating to surface gusts well above 65km/h.",
            "Maritime gale. The unobstructed Atlantic fetch means this is more intense than equivalent inland events."
        ),
        NO to listOf(
            "Polar vortex displacement event producing severe Arctic winds. This is a genuine meteorological hazard. Stay inside.",
            "Arctic gale from Norwegian Low pressure system. Wind chill at these speeds creates immediate frostbite risk outdoors.",
            "Severe Nordic wind event. 65+ km/h in sub-zero air. Wind chill makes felt temperature extremely dangerous.",
            "Arctic gale conditions. The combination of speed and temperature is hazardous. Do not be outside."
        )
    )

    override val windyVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Trade wind strengthening or tropical trough producing significant surface winds. Umbrella use inadvisable. Windproof layer.",
            "Organised wind event for the tropics — likely trade wind surge or squall approach. Skip the umbrella. Secure loose items.",
            "Elevated tropical surface winds. Not squall-level, but aerodynamic drag will be noticeable. Windproof outer layer.",
            "Strong trade winds or approaching system. 40-65km/h. Umbrella is impractical today."
        ),
        ST to listOf(
            "Subtropical pressure gradient producing notable surface winds. Umbrella impractical at these speeds. Windproof layer.",
            "Strong gradient winds from subtropical high edge. 40-65km/h gusts expected. Windproof layer instead of umbrella.",
            "Significant wind event for the region. Synoptic pressure gradient strong today. Windproof outer layer.",
            "Strong winds: subtropical trough/high interface. Umbrella viability threshold exceeded. Windproof jacket."
        ),
        TE to listOf(
            "40-65km/h sustained winds. Above umbrella viability threshold — skip it. Windproof layer.",
            "Strong gradient winds. Gusts likely. Loose objects will move. Secure anything you value.",
            "Synoptic-scale wind event. Umbrella is a liability at these speeds. Windproof outer layer.",
            "Significant surface winds. Aerodynamic drag becomes a real factor in your commute today."
        ),
        OC to listOf(
            "Atlantic-driven surface winds, 40-65km/h. The oceanic fetch amplifies these compared to inland equivalents. Windproof layer.",
            "Strong maritime gradient winds. The pressure differential across the low is steep. Umbrella is not viable today.",
            "Oceanic wind event. Sustained flow from the Atlantic is producing gusty conditions onshore. Skip the umbrella.",
            "Maritime wind strengthening ahead of Atlantic system. 40-65km/h range. Secure loose items; windproof layer."
        ),
        NO to listOf(
            "Nordic wind event, possibly with embedded Arctic air. 40-65km/h, felt temperature significantly below actual. Windproof shell.",
            "Strong polar-origin winds. The wind chill factor at these speeds and temperatures is the real story. Windproof layer.",
            "Arctic surface winds, 40-65km/h. Wind chill makes this feel 8-12°C colder than air temperature. Windproof gear.",
            "Significant Nordic wind. Cold air advection amplifying the chill beyond what the thermometer indicates. Windproof shell."
        )
    )

    override val hotVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Tropical heat: ambient temperature well above 30°C with relative humidity 70-90%. Heat index exceeds 40°C. Hydration is critical.",
            "Extreme heat index conditions. The combination of tropical temperature and humidity overwhelms evaporative cooling. Stay hydrated.",
            "High heat and humidity day. Wet-bulb temperature approaching dangerous thresholds. Limit outdoor exertion, drink water constantly.",
            "Tropical thermal load: temperature 32°C+, humidity extreme. Your body will struggle to cool via evaporation. Water is essential."
        ),
        ST to listOf(
            "Subtropical heat — high temperature, elevated UV index. The Hadley cell descending air produces clear skies and intense solar radiation.",
            "Dry heat conditions from subtropical high pressure. UV index is extreme. Sunscreen is not a suggestion.",
            "High solar irradiance under subtropical high. Temperature above 35°C possible. SPF, hat, hydration. The data demands it.",
            "Subtropical heat: elevated UV, low humidity, intense insolation. Felt heat from direct radiation is significant. Protect yourself."
        ),
        TE to listOf(
            "High solar irradiance, ambient temperature above 28°C. UV index elevated. Sunscreen is not optional.",
            "Significant thermal load today. Hydrate more than you think you need to — that's the science.",
            "Dry heat conditions, elevated UV. Light breathable layers and SPF. The data demands it.",
            "Temperature in the upper 20s. Thermodynamically, your cooling mechanisms will be active all day."
        ),
        OC to listOf(
            "Maritime heatwave event — unusual for this climate. Blocking high has suppressed Atlantic advection. UV still elevated. Sunscreen.",
            "Anomalously warm for oceanic climate. The blocking anticyclone has removed the usual maritime cooling. Hydrate and use SPF.",
            "Warm maritime day above 28°C — relatively rare here. High UV under clear skies. Sunscreen still matters.",
            "Oceanic heat event. The usual maritime moderation is absent today. Treat this as a proper hot day — water and SPF."
        ),
        NO to listOf(
            "Nordic heat event. Above 25°C at this latitude is climatologically significant. UV intensity is elevated despite the latitude.",
            "Warm Nordic day — the midnight sun effect means UV exposure accumulates over extended daylight hours. Sunscreen.",
            "Anomalously warm for high latitude. High pressure with extended daylight producing above-25°C surface temperatures. SPF essential.",
            "Hot by Nordic standards. The body adapts to cold and this genuinely impacts heat tolerance. Hydrate more than usual."
        )
    )

    override val warmVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Moderate tropical conditions — the brief window between morning cool and afternoon heat. Thermally comfortable for now.",
            "Trade wind cooling keeping temperatures in the comfortable range. Enjoy the thermal moderation.",
            "20-27°C in the tropics: this is the pleasant shoulder period before the heat builds. No special gear required.",
            "Comfortable tropical morning/evening conditions. The heat index is manageable at these humidity levels."
        ),
        ST to listOf(
            "Optimal subtropical conditions. 20-27°C under the subtropical high — the best this climate reliably offers.",
            "Warm and clear. The subtropical atmosphere is in its most pleasant configuration today.",
            "Ideal subtropical conditions. Temperature comfortable, UV manageable, no precipitation mechanism. Clean forecast.",
            "20-27°C, low humidity under subtropical high pressure. The conditions are, by most metrics, excellent."
        ),
        TE to listOf(
            "20-27°C sits comfortably within the human thermoneutral zone. Conditions are, by most metrics, ideal.",
            "Mild thermal conditions, clear sky. The meteorological word for this is 'fine.'",
            "Optimal temperature range for outdoor activity. The body handles this effortlessly.",
            "Clean forecast. Comfortable thermal conditions. No atmospheric drama."
        ),
        OC to listOf(
            "Warm maritime day. The Atlantic airflow is providing moderated warmth — the oceanic climate's optimal expression.",
            "Good conditions by oceanic standards. 20-27°C, maritime air, no active fronts. Uncommon and worth appreciating.",
            "Warm and clear — the maritime atmosphere is cooperating. No layering needed.",
            "Optimal oceanic conditions. Atlantic moderation producing comfortable temperatures. No precipitation signature."
        ),
        NO to listOf(
            "Warm Nordic day. Above 20°C at this latitude represents genuine climatological warmth. The conditions are good.",
            "Good summer conditions. The Nordic summer thermal peak is brief — this is it. Enjoy the thermoneutral zone.",
            "Warm day for the latitude. Sun angle is lower but daylight duration compensates. Comfortable and clear.",
            "20-27°C in the Nordic zone: this is peak summer. The data looks genuinely pleasant today."
        )
    )

    override val lightJacketVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Cool by tropical standards — below 20°C means either elevation or an unusual cold air intrusion. Light layer advisable.",
            "Sub-20°C in the tropics represents a genuine cold anomaly. Light jacket for comfort, especially at elevation.",
            "Tropical cool spell. The air mass is anomalously cold for this latitude. A light layer is warranted.",
            "12-19°C at tropical latitude: upper-level cold intrusion or highland cooling. Light jacket."
        ),
        ST to listOf(
            "Cool subtropical day. 12-19°C is the cool end of the subtropical range — a light jacket handles it.",
            "Subtropical cool spell. The usual warmth has been displaced by cold air advection from higher latitudes. Light layer.",
            "12-19°C in the subtropical zone — cooler than average. Light jacket and you're covered.",
            "Cool subtropical air mass. Below the thermoneutral zone for most. Light jacket is the correct response."
        ),
        TE to listOf(
            "12-19°C — the crossover point where jacket becomes advisable. Below the thermoneutral zone for most.",
            "Cool ambient conditions. The physiology recommends a light layer.",
            "Mild but cool. Human body starts active heat conservation below 18°C. Light jacket.",
            "12-19°C with light wind — a light layer handles this comfortably. Wind chill will make it feel cooler."
        ),
        OC to listOf(
            "Cool maritime day. The oceanic temperature moderation keeps it from being cold, but 12-19°C is jacket territory.",
            "Maritime cool — the Atlantic airflow is providing its characteristic mild-but-cool conditions. Light jacket.",
            "12-19°C under oceanic influence. The persistent dampness amplifies the chill slightly. Light waterproof layer ideal.",
            "Cool oceanic conditions. Maritime air keeps extreme cold away, but a light layer is warranted at 12-19°C."
        ),
        NO to listOf(
            "Mild Nordic day — 12-19°C here often comes with wind that pushes the felt temperature 3-5°C lower. Light jacket essential.",
            "Cool to the Nordic baseline. A light jacket is routine equipment in this climate — it's warranted today.",
            "12-19°C in the Nordic zone. Wind chill factor makes this feel colder than the thermometer suggests. Layer up.",
            "Light jacket weather — the Nordic version, where wind chill makes the temperature figure somewhat optimistic."
        )
    )

    override val jacketVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Cold by tropical standards. Sub-12°C at this latitude indicates significant cold air intrusion or high elevation. Jacket.",
            "5-11°C in the tropics: highly anomalous cold event or significant elevation. Full jacket, people are not adapted to this here.",
            "Unusually cold tropical day. The cold air mass has origin far outside this climate zone. Jacket required.",
            "Sub-12°C at tropical latitude — cold air intrusion event. The local population will feel this acutely. Jacket."
        ),
        ST to listOf(
            "Cold subtropical day. 5-11°C indicates a genuine Arctic or polar outbreak has reached these latitudes. Jacket needed.",
            "Subtropical cold snap. Cold air advection from polar origins has pushed temperatures into jacket territory.",
            "Cold: 5-11°C in a subtropical climate. This happens, but it's notable. Jacket, possibly more.",
            "Polar outbreak reaching subtropical latitude. 5-11°C — much colder than baseline. Jacket."
        ),
        TE to listOf(
            "5-11°C. Cold enough for active thermogenesis. Wind chill pushes felt temperature 3-5°C lower. Jacket.",
            "Single digits — the body works harder to stay warm. Let the jacket help.",
            "Below 12°C, cold becomes genuinely noticeable. Jacket and layers if you're out for a while.",
            "Temperature 5-11°C. Core temperature management is relevant at this level. Jacket."
        ),
        OC to listOf(
            "Cold maritime day. 5-11°C with Atlantic humidity makes this feel colder than equivalent inland temperatures. Jacket.",
            "Cool oceanic air mass producing single-digit temperatures. The maritime dampness amplifies cold perception. Jacket.",
            "Jacket weather. The oceanic climate moderates the worst cold but 5-11°C with wind is still cold. Layer up.",
            "Maritime cold — 5-11°C, likely with wind from the Atlantic. The wind chill makes the jacket essential, not optional."
        ),
        NO to listOf(
            "5-11°C is a relatively mild day by Nordic standards. Jacket standard — wind chill may require more.",
            "Nordic moderate cold. 5-11°C here is often accompanied by wind that drops felt temperature significantly. Jacket.",
            "Cold but manageable. The Nordic baseline makes 5-11°C unremarkable, but a jacket remains necessary.",
            "Jacket weather. Wind chill in the Nordic zone often brings felt temperature 5-8°C below air temperature."
        )
    )

    override val bundleUpVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Sub-5°C at tropical latitude: this is a severe cold event. The local population is not cold-adapted. Everything warm you have.",
            "Extreme cold anomaly for tropical climate. Sub-5°C requires full thermal system — the body here is not used to this.",
            "Near-freezing tropical temperatures. Unusual event — full layering required. Local infrastructure and populations are not adapted.",
            "Sub-5°C in the tropics: cold air outbreak of remarkable scale. Full winter layers. The body responds poorly to this level of cold."
        ),
        ST to listOf(
            "Severe subtropical cold event. Sub-5°C temperatures typically hit subtropical populations harder due to lower cold adaptation.",
            "Near-freezing conditions in subtropical climate. Full thermal system — base layer, insulation, outer shell. This is unusual here.",
            "Cold snap at sub-5°C. Subtropical infrastructure and populations are not designed for this. Layer comprehensively.",
            "Extreme cold for this latitude. Below 5°C with wind produces dangerous wind chill values. Bundle up completely."
        ),
        TE to listOf(
            "Sub-5°C. Peripheral vasoconstriction kicks in fast. Full thermal system — base layer, mid layer, outer shell.",
            "Near-freezing ambient temperature. Wind chill can bring felt temperature 5-8°C lower. Everything you own.",
            "Cold. Genuinely cold. Base layer, insulating mid-layer, outer shell. The whole system today.",
            "Below 5°C is where the physiology really earns it. Layer up comprehensively."
        ),
        OC to listOf(
            "Cold maritime day, sub-5°C. The dampness of the Atlantic air amplifies cold penetration. Full thermal system.",
            "Near-freezing oceanic conditions. Cold and wet is more physiologically demanding than cold and dry. Complete layering.",
            "Sub-5°C with maritime air. The humidity drives wind chill higher and cold feels more pervasive. Bundle up completely.",
            "Freezing maritime conditions. Wind, cold, and moisture together require full thermal protection."
        ),
        NO to listOf(
            "Sub-5°C: standard Nordic winter temperature. Full polar layering — base, mid, insulation, shell. Wind chill is the real number.",
            "Cold Nordic day. The wind chill factor at these temperatures is significant. Full thermal system — every layer.",
            "Polar baseline conditions. Below 5°C with any wind produces dangerous wind chill. The full winter kit is not optional.",
            "Deep cold by any standard. Nordic winters routinely require complete layering systems — today is one of those days."
        )
    )

    override val allClearVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Clear. The ITCZ has paused. No convective trigger. The tropical sky is delivering a rare calm window.",
            "Tropical clear day — no convective development forecast. Anticyclonic conditions overhead. Go outside.",
            "No precipitation mechanism. The tropical atmosphere is uncharacteristically settled. Enjoy this.",
            "Clear tropical conditions. Stable air mass, no convective instability. The weather is cooperating fully."
        ),
        ST to listOf(
            "Subtropical high pressure fully dominant. No cloud, no precipitation. The Hadley cell is delivering its signature clear day.",
            "Clear. The descending dry air of the subtropical anticyclone is doing what it does best — nothing, which means perfect conditions.",
            "Classic subtropical high: clear, dry, stable. Zero precipitation probability. Go outside.",
            "Anticyclonic subsidence producing clear conditions. No atmospheric interference today. You're good."
        ),
        TE to listOf(
            "High pressure doing its job. Zero atmospheric interference today. You're clear.",
            "Anticyclonic conditions, stable air mass, no precipitation mechanism present. Textbook clear.",
            "Zero precipitation probability. Dewpoint well below ambient. Conditions are optimal.",
            "Barometric pressure holding steady. The atmosphere has nothing for you today."
        ),
        OC to listOf(
            "Clear maritime conditions — the Atlantic pressure system is in its favorable configuration. Rare enough to be notable.",
            "Clear. The oceanic weather machine has paused. No fronts incoming for the next 24 hours. Good day.",
            "Anticyclonic blocking producing clear skies over the maritime zone. Enjoy this while the Atlantic remains cooperative.",
            "Clear oceanic day. No frontal activity, no precipitation. The weather system has given you the day off."
        ),
        NO to listOf(
            "Scandinavian blocking high producing clear Arctic conditions. Cold but clear — the Nordic definition of a perfect winter day.",
            "Clear. The polar vortex is stable, no fronts incoming. Clear, cold, still. A good Nordic day.",
            "Arctic clear conditions. High pressure, no precipitation. Extended daylight (summer) or long night (winter), but clear.",
            "Nordic clear day. Polar high pressure overhead, stable conditions. The atmosphere has nothing to throw at you today."
        )
    )

    // ── Mood pools ───────────────────────────────────────────────────────────

    override val stormMood: ZonedPool = mapOf(
        TR to listOf(
            "Tropical lightning events involve return stroke currents of 10,000–200,000 amperes. Observe from inside.",
            "The ITCZ produces more lightning than any other atmospheric feature on earth. Today's storm is a data point.",
            "This convective system released more energy in an hour than a nuclear weapon. The scale is genuinely impressive.",
            "Cloud-to-ground lightning in tropical systems averages 3-8 flashes per minute at peak intensity. Worth watching through a window."
        ),
        ST to listOf(
            "Subtropical thunderstorms have a particular character — they build fast, discharge intensely, and clear fast.",
            "The convective available potential energy that produced this storm has been building all day. The atmosphere is efficient.",
            "Summer thunderstorm: spectacular physics. Superheated updrafts, ice formation aloft, electrical discharge. From inside.",
            "The storm cell will clear in 1-3 hours. The atmosphere is releasing accumulated instability. Watch from safety."
        ),
        TE to listOf(
            "Thunderstorms are superheated air columns discharging static. Beautiful, actually. From inside.",
            "The lightning is genuinely impressive. Worth watching from a window.",
            "Somewhere a meteorologist is very excited about this. You don't have to be.",
            "Storm cell will pass in 2-4 hours. The atmosphere has right of way until then."
        ),
        OC to listOf(
            "Atlantic storms are extratropical systems — wider, longer-lasting, and less lightning-intensive than tropical equivalents.",
            "The energy in this system comes from the temperature gradient between polar and tropical air. Ocean storms are powerful.",
            "Maritime storm systems can sustain for days on Atlantic energy. This one has been building across the ocean since yesterday.",
            "Extratropical cyclones are the Atlantic's primary weather mechanism. This is a significant one. Stay in and appreciate it safely."
        ),
        NO to listOf(
            "Polar cyclones are relatively rare and significant. The atmospheric dynamics here are different from midlatitude systems.",
            "Arctic storm systems are driven by intense temperature gradients. The wind is the primary hazard here, not lightning.",
            "Norwegian Sea lows have crossed hundreds of kilometres of open water to get here. They arrive with considerable energy.",
            "Nordic storm conditions: the barometric pressure drop was rapid. The atmosphere corrected aggressively. Wait it out."
        )
    )

    override val heavyRainMood: ZonedPool = mapOf(
        TR to listOf(
            "Tropical rainfall at these rates will overwhelm urban drainage within minutes. The infrastructure was not designed for this.",
            "High-intensity tropical precipitation. The water has nowhere to go quickly enough. Flooding risk is real.",
            "This is monsoon-scale rainfall. Enjoy the sound of it from inside.",
            "Tropical heavy rain is qualitatively different from midlatitude rain. The droplets are larger, the rate is higher, and the sound is remarkable."
        ),
        ST to listOf(
            "Subtropical heavy rain — rare but intense when it arrives. Drainage systems are being fully tested right now.",
            "Convective heavy rain in the subtropics dumps quickly and dramatically. This won't last long. But while it lasts, it's serious.",
            "Intense subtropical rainfall. The low humidity baseline makes this event feel more dramatic than a temperate equivalent.",
            "The atmosphere is releasing a significant moisture load. Give it time. The clearing after subtropical heavy rain is usually fast."
        ),
        TE to listOf(
            "The drainage infrastructure will be tested today.",
            "This is what meteorologists classify as 'notable' rainfall. They're not wrong.",
            "High precipitation rate. The atmosphere is very committed to this today.",
            "Convective activity is significant. Respect it."
        ),
        OC to listOf(
            "Atlantic moisture load is significant. The front has been collecting water vapour across hundreds of kilometres of ocean.",
            "Maritime heavy rain often persists for hours. The Atlantic system is slow-moving and moisture-rich.",
            "The oceanic air mass contains an extraordinary amount of water vapour. All of it appears to be precipitating today.",
            "Heavy oceanic rainfall — the characteristic output of deep maritime low pressure. Drainage systems are working hard."
        ),
        NO to listOf(
            "Cold heavy rain in the Nordic zone has a particular unpleasantness — the temperature removes any ambiguity about how this feels.",
            "Nordic heavy precipitation. The cold air mass means this may be rain, sleet, or mixed. All options are equally unpleasant.",
            "High precipitation rate in near-freezing air. The body loses heat rapidly in these conditions. Gear up.",
            "Heavy rain at near-zero temperatures. The physiological cold stress is compounded by precipitation. Full coverage needed."
        )
    )

    override val rainMood: ZonedPool = mapOf(
        TR to listOf(
            "Classic midlatitude cyclone activity. The drainage systems are getting a workout.",
            "Tropical afternoon convection is normal and self-limiting. This will pass.",
            "Good steady rain. The kind that actually replenishes the water table.",
            "The front will clear by evening. In the meantime, it's just wet."
        ),
        ST to listOf(
            "Subtropical rain is not the everyday baseline here. The water table will appreciate it even if you don't.",
            "This rain is doing useful work — the region's water systems need it. Cold comfort, but it's there.",
            "Subtropical trough rainfall. It won't last forever. The high will reassert itself.",
            "Rain event in a typically dry climate. The infrastructure wasn't really built for it. Allow extra time for everything."
        ),
        TE to listOf(
            "Classic midlatitude cyclone activity. The drainage systems are getting a workout.",
            "Frontal rain — steady, persistent, not dramatic. Just wet.",
            "Good steady rain. The kind that actually replenishes the water table.",
            "The front will clear by evening. In the meantime, it's just wet."
        ),
        OC to listOf(
            "Atlantic frontal rain. This is the oceanic climate's default expression. The front will clear eventually.",
            "Maritime rain — steady, mild, relentless. The oceanic baseline.",
            "The front has been crossing the ocean for several days. It's here now. It will pass.",
            "Frontal system rain. The Atlantic keeps these events coming at reliable intervals. The drainage is used to it."
        ),
        NO to listOf(
            "Cold frontal rain in the Nordic zone. The temperature differential is what drives the system. The physics are efficient.",
            "Nordic rain often precedes temperature drops as cold air moves in behind the front. Watch the pressure falling.",
            "Frontal rain with cold air advection behind it. May transition to snow as temperatures drop. Plan accordingly.",
            "Persistent cold rain from the polar front. This system is moving slowly. Account for the duration."
        )
    )

    override val drizzleMood: ZonedPool = mapOf(
        TR to listOf(
            "This is the kind of precipitation that makes you question whether it's actually raining.",
            "Post-convective moisture. The main storm event has passed; what's left is atmospheric residue.",
            "Drizzle at tropical humidity levels feels different — the air is already saturated. You notice it more.",
            "Light precipitation from residual cloud. Not significant. Carry on."
        ),
        ST to listOf(
            "Subtropical drizzle is unusual. Marine layer advection is the most likely cause — the coast effect at work.",
            "Drizzle in a dry climate — the stratus layer is thin, the precipitation minimal. It will clear.",
            "Orographic drizzle: moist air forced over terrain producing fine precipitation on the windward side. Specific and brief.",
            "Fine precipitation. Low intensity. The dry-climate baseline makes this feel more notable than it would elsewhere."
        ),
        TE to listOf(
            "This is the kind of precipitation that makes you question whether it's actually raining.",
            "Drizzle is just clouds that couldn't commit. Not the atmosphere's finest effort.",
            "Technically precipitation. Practically, just damp air.",
            "Droplets are sub-millimeter. Your hair notices before your jacket does."
        ),
        OC to listOf(
            "Oceanic drizzle is not accidental — the maritime stratus layer persistently produces it. This is the climate expressing itself.",
            "Atlantic drizzle: the north oceanic climate's most characteristic output. Fine, pervasive, thoroughly damp.",
            "The drizzle will continue until conditions improve. Conditions may not improve. This is oceanic climate.",
            "Fine marine precipitation. Not dramatic. Just persistent. Your jacket knows."
        ),
        NO to listOf(
            "Cold drizzle in the Nordic zone has a particular character — the temperature makes it feel more serious than the rate suggests.",
            "Near-freezing fine precipitation. The droplets are small but the cold amplifies the discomfort.",
            "Drizzle at low temperatures. The saturation point is lower in cold air — this is the atmosphere at its least dramatic.",
            "Cold fine precipitation. Technically minimal. The temperature does the rest of the work."
        )
    )

    override val snowMood: ZonedPool = mapOf(
        TR to listOf(
            "Snowfall at tropical latitude is a genuine atmospheric event. Upper-level cold intrusion reaching the surface — rare physics.",
            "This is climatologically unusual. Enjoy observing it. The infrastructure is not prepared for it.",
            "Snow at this latitude suggests upper-tropospheric cold air descended to the surface. Anomalous and interesting.",
            "Tropical snowfall: a meteorological curiosity. The thermodynamics required to produce this here are significant."
        ),
        ST to listOf(
            "Subtropical snow is rare enough that the infrastructure and population are not adapted. Exercise particular caution.",
            "Snow event at this latitude: Arctic air outbreak of significant magnitude. Unusual and disruptive.",
            "Snowfall here is an event. The systems that produce it are significant cold air outbreaks. Take it seriously.",
            "No two snowflakes share the same crystal structure. Worth appreciating, especially as they're rare here."
        ),
        TE to listOf(
            "No two snowflakes share the same crystal structure. Worth appreciating, even underfoot.",
            "The albedo effect will make it brighter than you expect. Your eyes will adjust.",
            "Snow is just water in its most structurally interesting phase.",
            "Accumulation rate is predictable. Your commute is not. Give yourself time."
        ),
        OC to listOf(
            "Oceanic snow is typically wet and heavy — high water content from the maritime air mass. Accumulation compacts quickly.",
            "Maritime snow: expect the heavy, wet variety. It compacts underfoot and refreezes as ice. Traction is the primary concern.",
            "Snow from maritime polar air is structurally different from continental powder. Wet, dense, and slippery.",
            "The ocean's influence on temperature means this snow may be transitional. Watch for sleet or ice glaze as temperature drops."
        ),
        NO to listOf(
            "Nordic powder snow: low water content, high crystal quality. The structure is beautiful. The footing is treacherous.",
            "Proper Arctic snow. Cold and dry means light accumulation that drifts significantly in any wind.",
            "Snow is the Nordic climate's native expression. The infrastructure is designed for it. You should be too.",
            "High-latitude snowfall. Low solar angle means accumulation persists. Ice under fresh snow is the real hazard."
        )
    )

    override val windMood: ZonedPool = mapOf(
        TR to listOf(
            "Tropical wind events are often precursors to larger systems. The pressure gradient is informative.",
            "Trade wind surge or tropical low approach. The wind direction and backing/veering pattern tells you where it's going.",
            "Strong tropical winds: the atmosphere is actively redistributing air masses. The Coriolis effect is doing its job.",
            "Elevated tropical surface winds. Kinetic energy in the lower atmosphere is significant today."
        ),
        ST to listOf(
            "Strong subtropical gradient winds. The pressure differential between the high and low is steep today.",
            "Wind at these speeds in dry subtropical air creates additional evaporative effects. Stay hydrated.",
            "Significant wind event for the region. The Beaufort scale puts this in the strong breeze to near-gale range.",
            "The aerodynamic drag becomes a practical factor in movement at these wind speeds. Kinetic energy is real."
        ),
        TE to listOf(
            "At these speeds, aerodynamic drag becomes a real factor in your commute.",
            "Wind force 6+ on the Beaufort scale. Respect the kinetic energy.",
            "The pressure gradient is steep today. That's why it's sustained, not just gusting.",
            "Significant wind event. The atmosphere is redistributing air masses with some enthusiasm."
        ),
        OC to listOf(
            "Atlantic gradient winds are typically sustained rather than gusty. The fetch over open ocean creates organised airflow.",
            "Maritime wind: the ocean surface boundary layer creates a different character to these winds compared to continental events.",
            "North Atlantic wind event. The low pressure tracking across the ocean is producing a steep pressure gradient.",
            "Oceanic winds have covered significant fetch before reaching here. The energy and organisation reflect that."
        ),
        NO to listOf(
            "Arctic wind: the temperature combined with wind speed creates a wind chill far below the ambient temperature.",
            "Polar wind event. The wind chill factor is the operationally relevant number today, not air temperature.",
            "Nordic winds carry cold air efficiently. The wind chill formula: the real temperature is what you feel.",
            "Arctic surface flow. Significant wind chill. The physical exposure time outdoors matters today."
        )
    )

    override val breezeMood: ZonedPool = mapOf(
        TR to listOf(
            "Trade wind conditions: characteristic of tropical anticyclones, cooling and drying the lower atmosphere efficiently.",
            "Classic tropical breeze. The trade winds are the most reliable wind system on Earth. Today they're pleasant.",
            "Gentle trade wind flow. Beaufort 3-4. The tropical baseline at its most comfortable.",
            "Trade wind breeze — the engine of tropical climate. Today it's in its most agreeable mode."
        ),
        ST to listOf(
            "Good sailing conditions, if that's relevant to your day.",
            "The breeze from the subtropical high edge is clean and dry. The kind that makes a warm day comfortable.",
            "Beaufort 4 conditions under subtropical high influence. Characterful without being hostile.",
            "A proper subtropical breeze. The trade wind edge is producing organised, comfortable airflow."
        ),
        TE to listOf(
            "Beaufort 4 — leaves in constant motion, light branches moving. Characterful without being hostile.",
            "Good sailing conditions, if that's relevant to your day.",
            "The trees are indicating a moderate breeze. They're rarely wrong.",
            "A proper Beaufort 4. Interesting without being hostile."
        ),
        OC to listOf(
            "Maritime breeze — the Atlantic producing its characteristic moderate, sustained airflow. The ocean moderates everything, including this.",
            "Oceanic Beaufort 4. The maritime influence makes this feel different to equivalent inland wind speeds. Less gusty, more organised.",
            "Atlantic breeze. The fetch creates organised laminar flow at surface level. Steady rather than gusty.",
            "A proper north Atlantic breeze. Characterful. Not hostile. The ocean's version of a pleasant day."
        ),
        NO to listOf(
            "Nordic breeze: even a Beaufort 4 at these temperatures requires a windproof layer. The wind chill is the story.",
            "Moderate wind at northern latitudes. The chill factor makes this feel more significant than the speed suggests.",
            "Arctic breeze — the cold air amplifies the wind chill factor even at moderate speeds. Outer layer needed.",
            "Northern latitude breeze. Beaufort 4 with cold air feels sharper than equivalent conditions at lower latitudes."
        )
    )

    override val allClearWarmMood: ZonedPool = mapOf(
        TR to listOf(
            "Optimal tropical conditions. The dry season clear period delivers exactly these conditions. Make use of it.",
            "Warm, clear tropical conditions. Heat index manageable, UV significant — stay hydrated and use sunscreen.",
            "The inter-seasonal clear period in tropical climates is genuinely optimal. The data is good.",
            "Tropical clear and warm: the atmosphere is cooperating at the thermal level. UV remains the one concern."
        ),
        ST to listOf(
            "Subtropical clear warm day: this is the climate type's optimal expression. High solar irradiance, clear air, comfortable temperature.",
            "Perfect conditions by subtropical standards. The Hadley cell is operating as advertised.",
            "Clear, warm, low humidity under the subtropical high. The data is genuinely very good.",
            "Optimal subtropical conditions. The atmosphere is in its most cooperative configuration today."
        ),
        TE to listOf(
            "Conditions like this are why people move to places like this.",
            "Optimal temperature range for outdoor activity. The data is genuinely good.",
            "Nothing to monitor. Nothing to prepare for. Unusual, honestly.",
            "Peak solar irradiance, comfortable temperature. The atmosphere is cooperating."
        ),
        OC to listOf(
            "Warm maritime clear day. These are genuinely uncommon in oceanic climates. Note the date.",
            "Clear and warm by oceanic standards. The Atlantic is not interfering today. Appreciate it.",
            "Optimal oceanic conditions. Clear, warm, no fronts. The kind of day that justifies living near the ocean.",
            "Maritime clear and warm — the rare combination that makes oceanic climates occasionally worth it."
        ),
        NO to listOf(
            "Nordic warm clear day. Extended daylight with clear skies at high latitude is a genuine event. Go outside.",
            "Summer conditions in the Nordic zone: warm and clear. The midnight sun adds hours of usable daylight. Use them.",
            "Clear and warm at this latitude. The UV intensity is lower but the daylight duration compensates. Enjoy this.",
            "Good Nordic summer day. The season is brief. The data is good. The atmosphere is cooperating. Go outside."
        )
    )

    override val allClearNeutralMood: ZonedPool = mapOf(
        TR to listOf(
            "No atmospheric drama today. The tropical weather machine has paused. Appreciate the stillness.",
            "Clear. Calm. The ITCZ has moved away. This is the dry season at its most settled.",
            "Nothing to monitor. The tropical atmosphere is in its quiet phase.",
            "Clear tropical conditions. No precipitation mechanism. No atmospheric interference. Unusually calm."
        ),
        ST to listOf(
            "Nothing to monitor. Nothing to prepare for. The subtropical high is behaving perfectly.",
            "Clear and stable. The descending air of the Hadley cell is doing its job. Unremarkably good conditions.",
            "No atmospheric drama today. The subtropical anticyclone has suppressed all activity.",
            "Clear. Calm. Dry. The subtropical climate in its baseline expression."
        ),
        TE to listOf(
            "Nothing to monitor. Nothing to prepare for. Unusual, honestly.",
            "No atmospheric drama today. The meteorological equivalent of a day off.",
            "The system is stable. That's the whole story.",
            "Clear. Calm. Unremarkable by the best possible definition."
        ),
        OC to listOf(
            "Clear oceanic conditions. The Atlantic has paused between systems. Enjoy the window.",
            "No fronts. No active weather. The oceanic atmosphere has granted a meteorological intermission.",
            "Clear maritime conditions. This is a gap between Atlantic systems. Make the most of it.",
            "Nothing to monitor. The oceanic weather machine is temporarily at rest."
        ),
        NO to listOf(
            "Clear Nordic conditions. High pressure, stable polar air, no precipitation. Cold but clear — the best option here.",
            "Nordic clear day. The Scandinavian blocking high is doing its job. Stable, settled, cold.",
            "No atmospheric drama. The polar high has everything suppressed. Clear and cold.",
            "Stable polar conditions. No fronts incoming. The Arctic atmosphere is, briefly, at rest."
        )
    )

    override val greyMood: ZonedPool = mapOf(
        TR to listOf(
            "Overcast but not actively raining. The cloud base is low but convective instability is limited. Carry on.",
            "The cloud layer is suppressing the usual solar intensity. A welcome change from full tropical sun, actually.",
            "Grey in the tropics usually precedes convective development in the afternoon. Keep an eye on it.",
            "Stratus layer without precipitation. The tropical atmosphere is gathering energy. Not yet though."
        ),
        ST to listOf(
            "Subtropical overcast — marine stratus advection keeping the sky grey without producing rain. It will clear.",
            "Low cloud layer from marine influence. The cloud base is cosmetic today — no precipitation mechanism.",
            "Grey subtropical day. The inversion layer is trapping cloud at low level. It'll burn off.",
            "Overcast. The descending air of the subtropical high will reassert itself. This is temporary."
        ),
        TE to listOf(
            "The diffuse lighting is actually quite even. Great day for photography, objectively speaking.",
            "No rain mechanism present. This is atmospheric ambiance, not weather.",
            "Cloud base is high. The grey is cosmetic, not structural.",
            "It's just water vapor suspended at altitude. Carry on."
        ),
        OC to listOf(
            "Oceanic overcast: the maritime stratus layer is the default state here. It's not threatening — it's just the baseline.",
            "The cloud cover is from the marine boundary layer. Persistent, low-level, and not producing rain. Just grey.",
            "Atlantic stratus. This is the oceanic climate expressing its characteristic overcast. No rain. Just grey.",
            "Maritime overcast. The diffuse light is even and consistent. The cloud base is high enough not to worry about."
        ),
        NO to listOf(
            "Nordic grey — the persistent overcast of high latitude winters. No precipitation. Just limited light.",
            "Overcast at northern latitude. Diffuse light, low sun angle. The cloud base is not threatening — just present.",
            "Grey, still, overcast. The Nordic climate's default winter expression. No precipitation today.",
            "Low cloud cover without precipitation. The Nordic winter grey. Carry on — this is baseline here."
        )
    )
}
