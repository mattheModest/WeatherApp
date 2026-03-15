package com.weatherapp.model

internal object GravesPools : PoolSet {
    private val TR = ClimateZone.TROPICAL
    private val ST = ClimateZone.SUBTROPICAL
    private val TE = ClimateZone.TEMPERATE
    private val OC = ClimateZone.OCEANIC
    private val NO = ClimateZone.NORDIC

    // ── Verdict pools ────────────────────────────────────────────────────────

    override val stormVerdict: ZonedPool = mapOf(
        TR to listOf(
            "A tropical storm. The sky has made its feelings about your day abundantly clear. Stay inside.",
            "The monsoon has cancelled your plans. You were going to have to cancel them eventually anyway.",
            "Thunderstorm. The tropics occasionally remind you who's in charge. Today is one of those days.",
            "Active tropical storm. You had somewhere to be. The atmosphere was unaware of this and did not care."
        ),
        ST to listOf(
            "A summer thunderstorm. The heat built all day and now it's paying out, all at once, on you.",
            "The sky has committed completely. It saved this for you specifically, it feels like.",
            "Severe storm. You were warned by the heat and the pressure and the sky's general mood. Here it is.",
            "Thunderstorm. Subtropical summers have a tradition of ending exactly like this."
        ),
        TE to listOf(
            "A proper storm. You had somewhere to be. The sky had a prior engagement.",
            "Thunderstorm. Today was always going to go like this.",
            "The sky has formally cancelled your day. Stay inside.",
            "Active storm. You had plans. How ambitious of you."
        ),
        OC to listOf(
            "Atlantic storm. The ocean spent several days building this and it has delivered it directly to your door.",
            "A proper Atlantic gale. The weather here has always meant business. Today especially.",
            "Maritime storm. You live near the ocean. Occasionally it expresses itself. Today is one of those occasions.",
            "The Atlantic low has arrived. You could have predicted this. In fact, you probably did. Stay inside."
        ),
        NO to listOf(
            "Arctic storm. The polar atmosphere has been withholding and now it isn't. Stay inside.",
            "A proper Nordic gale. The weather was always going to be like this. You knew when you moved here.",
            "Polar storm. Darkness, wind, cold. The holy trinity of Nordic winter. Indoor plans only.",
            "Severe Arctic weather. The polar vortex is having a moment and you are in its way."
        )
    )

    override val heavyRainVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Tropical downpour. It is coming down in a way that respects nothing and no one. Full waterproofing.",
            "The monsoon is expressing itself fully today. Everything is getting wet. Full gear or stay in.",
            "Heavy tropical rain. The sky has decided. You can only decide whether you're prepared for it or not.",
            "Pouring. Tropically pouring. The kind that ends conversations and ruins shoes. Waterproof everything."
        ),
        ST to listOf(
            "Heavy subtropical rain — rare, but it arrived with conviction. Waterproof jacket and low expectations.",
            "The sky has committed to this in a way that most people never commit to anything. Full waterproofing.",
            "Pouring. The summer heat has been building this and now it's here. Full gear.",
            "Heavy rain. Subtropical intensity. Whatever you're planning, add 'significantly wetter' to the itinerary."
        ),
        TE to listOf(
            "It is absolutely pouring. Umbrella, waterproof jacket, and low expectations.",
            "The sky has committed to this in a way most people never commit to anything. Full waterproofing.",
            "Heavy rain. The kind that finds you no matter what. Waterproof jacket.",
            "Pouring. Whatever you're planning, add getting wet to the itinerary unless you gear up properly."
        ),
        OC to listOf(
            "Heavy Atlantic rain. The ocean sent this. It's thorough, persistent, and deeply uninterested in your plans.",
            "Pouring. The Atlantic is clearing its surplus moisture inventory over this general area today.",
            "Heavy maritime rain. You live here. You know how this goes. Full waterproofing.",
            "The Atlantic front has delivered. Heavy rain, no apology. Full waterproof kit."
        ),
        NO to listOf(
            "Heavy cold rain. The kind that works on you from multiple directions simultaneously. Full waterproofing.",
            "Nordic heavy precipitation. Cold. Wet. Persistent. The combination is impressive in its unpleasantness.",
            "Pouring in near-freezing temperatures. Both the rain and the cold are your problem now. Full gear.",
            "Heavy rain, Arctic air behind it. Everything is cold and wet and getting colder. Waterproof and warm."
        )
    )

    override val rainVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Afternoon tropical rain. You knew this was coming. The sky here is very consistent about it.",
            "It's raining. Properly tropical rain — warm, heavy, entirely unbothered by your schedule. Umbrella.",
            "Rain today. The tropics are not subtle about precipitation. Umbrella or arrive wet.",
            "Bring an umbrella. The afternoon convection has started and it will continue until it decides to stop."
        ),
        ST to listOf(
            "Raining. One of the few rainy days this season, and it chose today. Take an umbrella.",
            "It's raining and it has no intention of stopping for you specifically. Umbrella.",
            "Rain today. Moderate, persistent, and thoroughly unbothered by your plans.",
            "Umbrella. The subtropical trough didn't ask for your input and neither did the forecast."
        ),
        TE to listOf(
            "Raining. Properly. Take an umbrella or arrive wet — both are valid choices with different consequences.",
            "It's raining and it has no intention of stopping for you specifically. Umbrella.",
            "Rain today. Moderate, persistent, and thoroughly unbothered by your plans.",
            "Bring an umbrella. You'll thank yourself at around 2pm when it's still raining."
        ),
        OC to listOf(
            "It's raining. Of course it's raining. Umbrella.",
            "Atlantic frontal rain. The forecast said rain. The forecast was correct. Umbrella.",
            "Rain. Steady, oceanic, unhurried. The maritime climate is doing what it does.",
            "Raining. Again. The Atlantic has opinions about this coast and today they are wet ones. Umbrella."
        ),
        NO to listOf(
            "Cold rain. Nordic rain has a particular commitment to making you feel it. Umbrella and jacket.",
            "It's raining and it is cold and both of these things will continue for a while. Umbrella.",
            "Frontal rain in cold air. It was going to be like this. It is like this. Umbrella.",
            "Rain. Cold rain. The Nordic atmosphere does not offer the warm tropical variety. Gear up."
        )
    )

    override val drizzleVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Tropical drizzle. Not enough to cancel plans, just enough to make the humidity worse. Hood optional.",
            "Residual moisture from the last convective event. It drizzles between storms here. You'll survive. Hood.",
            "Post-rain mist. The tropics don't do subtle, but this is as close as it gets. Light coverage.",
            "Drizzling. The air is so saturated it barely has to try. Hood if you care about your hair."
        ),
        ST to listOf(
            "Marine drizzle. The coast has arrived at your door uninvited. Hood.",
            "Drizzle. The sky is being passive aggressive, subtropical edition. Light coverage.",
            "Fine precipitation from stratus layer. Not enough to justify an umbrella. Too wet to ignore entirely.",
            "Drizzling. Not enough to ruin your day. Exactly enough to make it slightly worse. Hood."
        ),
        TE to listOf(
            "Drizzling. Not enough to cancel plans. Just enough to make everything slightly worse.",
            "Technically precipitation. Just enough to ruin your hair and not enough to justify staying in.",
            "Drizzle. The cruelest weather — too wet to ignore, not wet enough to complain about properly.",
            "The sky is being passive aggressive. Hood or light layer."
        ),
        OC to listOf(
            "Atlantic drizzle. The oceanic climate's default mode. Hood up and get on with it.",
            "Fine maritime rain. Too light to mention. Too persistent to ignore. You know what to do.",
            "Drizzle. You live here. You know about the drizzle. Hood.",
            "Oceanic drizzle — the kind of precipitation that just exists here as background moisture. Hood."
        ),
        NO to listOf(
            "Cold drizzle. Not dramatic. Not warm. Just persistent and slightly below the temperature of your tolerance.",
            "Nordic fine precipitation. Freezing is an option the forecast hasn't ruled out. Light waterproofing.",
            "Drizzle at near-freezing temperatures. The cruelest cold-weather variant. Hat and light coverage.",
            "Cold mist masquerading as drizzle. You'll arrive somewhere cold and slightly damp. Hood."
        )
    )

    override val snowVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Snow. In the tropics. The atmosphere has departed from the script entirely. Bundle up.",
            "It is snowing here, which is a sentence that should not be possible today. Every warm layer you own.",
            "Tropical snowfall. This is not normal. The sky knows that. It doesn't care. Bundle up.",
            "Snow. Yes, here. No, it's not usual. Yes, you need every warm thing you have."
        ),
        ST to listOf(
            "Snow. A genuine cold outbreak has reached this latitude and expressed itself fully. Bundle up.",
            "Snowing. The subtropical cold snap has arrived, and it arrived with consequences. Layers.",
            "Snow event. The infrastructure here was not designed for this, and neither were you. Bundle up.",
            "White out there. Cold outbreak. Everything takes longer. Everything is more treacherous. Layers."
        ),
        TE to listOf(
            "Snowing. Beautiful, treacherous, and deeply inconvenient. Bundle up.",
            "Snow today. Everything takes longer. Everything is colder. Bundle up.",
            "White out there. Watch your footing. The black ice is not announcing itself.",
            "Snowing. The world looks like a greeting card. The commute does not."
        ),
        OC to listOf(
            "Wet oceanic snow. Heavy, cold, and turning to ice on contact with surfaces. Watch your footing.",
            "Maritime snow. It looks soft. It isn't. The water content makes it dense and slippery. Full winter kit.",
            "Snowing. Atlantic cold air has managed to produce frozen precipitation and it's your problem now.",
            "Snow, oceanic variety. Wet, heavy, slippery. Watch every surface. Bundle up."
        ),
        NO to listOf(
            "Snow. Yes. Still. This is what you signed up for. Bundle up.",
            "Nordic snowfall. Full winter kit. The question isn't whether it's cold — it's how cold.",
            "Snow. The world is frozen and beautiful and your commute is ruined. Standard Nordic winter.",
            "Snowing. Again. The Norse gods were not joking about this place. Everything you own."
        )
    )

    override val veryWindyVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Gale-force tropical winds. This is either a system edge or something worse approaching. Stay in.",
            "Very dangerous wind conditions. The tropical atmosphere has organised itself against outdoor activity. Inside.",
            "Gale winds in the tropics — something serious is happening. Stay inside and monitor conditions.",
            "Near-gale tropical conditions. The sky has strong opinions today. Do not argue with it from outside."
        ),
        ST to listOf(
            "Gale conditions. The subtropical low has committed. You don't have to.",
            "Very strong wind. The outside is currently hostile. It does not need to be your problem.",
            "Near-gale winds. This is the rare dramatic day the subtropical climate occasionally produces. Stay in.",
            "Gale-force winds. The atmosphere is making a point. You don't have to go out there to receive it."
        ),
        TE to listOf(
            "It is genuinely very windy. The kind of wind with a point to make. Stay in if you can.",
            "Gale conditions. The outside is having a terrible time. You don't have to join it.",
            "Near-gale winds. If you have to go out, keep your head down and don't dawdle.",
            "Very strong wind. Not dangerous if you're sensible. Are you sensible? Unclear."
        ),
        OC to listOf(
            "Atlantic gale. The ocean spent days building this. Now it's here. Stay inside.",
            "Full maritime gale. The Atlantic does not do things by halves. This is the whole thing.",
            "Gale conditions on the coast. This is the ocean being entirely itself. Do not go outside.",
            "Near-gale maritime winds. The Atlantic has made a strong recommendation about indoor plans. Follow it."
        ),
        NO to listOf(
            "Arctic gale. Wind chill at these speeds creates immediate problems for exposed skin. Do not go outside.",
            "Nordic gale. The polar vortex has extended itself in your direction and it is not being gentle.",
            "Gale conditions, Arctic air. The combination is dangerous. This is a stay-inside day, non-negotiably.",
            "Near-gale Nordic winds. The cold plus the speed creates something genuinely hostile. Indoor plans only."
        )
    )

    override val windyVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Properly windy for the tropics. The umbrella is a structural liability today. Leave it.",
            "Strong trade wind or trough winds. Your plans just got harder. The forecast offers no apology.",
            "It's very windy and the wind does not care that you have somewhere to be. Skip the umbrella.",
            "Windy enough that the tropics have stopped cooperating. Windproof layer. Abandon the umbrella."
        ),
        ST to listOf(
            "Windy. The umbrella will embarrass you today. Leave it at home.",
            "Strong winds and the forecast has nothing to say for itself. Windproof layer.",
            "It's very windy and your umbrella has already lost today. Don't take it out.",
            "Properly windy subtropical day. The pressure gradient has strong opinions. Windproof layer."
        ),
        TE to listOf(
            "Properly windy. The umbrella is a trap today — leave it.",
            "Strong wind. Your plans just got harder. The forecast has no apologies.",
            "Windy enough that the outside is working against you today. Skip the umbrella.",
            "It's very windy and the wind does not care that you have somewhere to be."
        ),
        OC to listOf(
            "Atlantic wind. Strong, sustained, and deeply unbothered by your umbrella. Leave it.",
            "Properly maritime windy. This is the coast doing what coasts do. Windproof layer.",
            "Wind from the Atlantic, which has plenty of it and is sharing generously today. Skip the umbrella.",
            "Windy. Very. The oceanic climate is expressing itself through kinetic energy today."
        ),
        NO to listOf(
            "Nordic wind. Cold, strong, wind chill-amplified, and entirely indifferent to your discomfort. Windproof shell.",
            "It's windy and cold and the wind chill is worse than the thermometer suggests. Windproof shell.",
            "Strong polar winds. The umbrella was never going to work here. Windproof outer layer.",
            "Properly windy and cold. The Arctic doesn't ask how you're feeling about it. Windproof shell."
        )
    )

    override val hotVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Tropical heat with extreme humidity. The kind of hot that makes you question all your decisions. Water, shade, SPF.",
            "Very hot and very humid. Your body is working hard just to exist today. Hydrate constantly.",
            "Heat and humidity combined into something aggressively unpleasant. Water. Shade. Sunscreen. Keep moving.",
            "The heat index is extreme. The sun does not like you specifically today. Sunscreen, water, tempered expectations."
        ),
        ST to listOf(
            "Extreme subtropical heat. The sun has strong opinions and you're in the middle of them. Sunscreen.",
            "Very hot. The Hadley cell has delivered maximum insolation to your location. This is on you now.",
            "Hot. Properly hot. The kind where the pavement contributes. Sunscreen, water, and recalibrated ambitions.",
            "Subtropical peak heat. The sun is here and it means it. Sunscreen. Water. Tempered expectations."
        ),
        TE to listOf(
            "Very hot. The sun has strong opinions today and you're in the middle of them. Sunscreen.",
            "Hot. Actually hot. Stay hydrated and try not to be dramatic about it.",
            "It's going to be warm and you are going to be reminded of it constantly. Sunscreen, water.",
            "The heat is here and it means it. Sunscreen. Water. Tempered expectations."
        ),
        OC to listOf(
            "Anomalous oceanic heat. The Atlantic is not cooling things today. Enjoy it or suffer it — both are options.",
            "Hot by maritime standards. The blocking high has removed the usual sea breeze. Sunscreen is still necessary.",
            "Warm enough to be uncomfortable, which is not something this climate usually has to apologise for. Sunscreen.",
            "Maritime heatwave. Rare and disorienting. Hydrate. Use sunscreen. Try not to be surprised by it."
        ),
        NO to listOf(
            "Hot by Nordic standards. Which is still not that hot. But UV at this latitude is deceptive — use sunscreen.",
            "Anomalously warm. The Nordic summer occasionally delivers this. It will pass, but for now, sunscreen and water.",
            "Warm enough to notice, which here means something. Sunscreen. Daylight hours are long; UV exposure accumulates.",
            "Nordic warm day. The midnight sun means more UV hours than you think. Sunscreen is non-optional."
        )
    )

    override val warmVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Warm and clear. The pleasant tropical window before the heat builds. Go outside before the afternoon decides things.",
            "Decent conditions. The tropics are temporarily not being difficult about it. Enjoy this.",
            "Warm, clear, manageable. The sky is cooperating and you should take advantage before it stops.",
            "Nothing bad is happening right now. The tropical atmosphere is in its most agreeable mode. Go outside."
        ),
        ST to listOf(
            "Warm and clear. Zero complaints available. This is suspicious.",
            "Genuinely nice out. The subtropical high has delivered. Go before the forecast changes its mind.",
            "Warm, sunny, uneventful. The weather is, for once, not your problem.",
            "Nothing bad is happening today. The bar was low and it cleared it."
        ),
        TE to listOf(
            "Warm and clear. Zero complaints available. This is suspicious.",
            "Nice out. Genuinely nice. Go before the forecast remembers what it's supposed to be doing.",
            "Warm, sunny, uneventful. The weather is, for once, not your problem.",
            "Nothing bad is happening today. The bar was low and it cleared it."
        ),
        OC to listOf(
            "Warm maritime day. These don't come often. You know that. Go outside.",
            "Genuinely warm and clear on the coast. A rare configuration. Note it. Use it.",
            "The Atlantic is being cooperative. Warm and dry. This is the exception, not the rule. Go outside.",
            "Warm and clear by oceanic standards. This is not nothing. Go outside while it's like this."
        ),
        NO to listOf(
            "Warm Nordic day. The short summer is briefly delivering. Go outside.",
            "Summer arrived today. Possibly temporarily. Go outside before it reassesses its position.",
            "Warm, clear, and the sun is staying up late. The Nordic summer is making a case for itself. Use it.",
            "Actually warm today. By any standard. The Nordic summer occasionally justifies itself. Today is one of those days."
        )
    )

    override val lightJacketVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Cool by tropical standards. Something unusual is happening atmospherically. Light layer and mild curiosity.",
            "A light layer is warranted today, which is not usually a sentence about the tropics. Take the jacket.",
            "Cooler than expected. The tropical atmosphere is briefly cooperating with lower latitudes. Light jacket.",
            "Pleasantly cool or anomalously cold, depending on your perspective. Light jacket handles it."
        ),
        ST to listOf(
            "A bit cool. Grab a light jacket and get on with it.",
            "Cool subtropical day. Not dramatic. Light layer and you're set.",
            "Mild but you'll feel it. A light layer addresses this and nothing else needs addressing.",
            "Light jacket weather. Not much to report, which is a sentence this forecast rarely gets to say."
        ),
        TE to listOf(
            "A bit cool. Grab a light jacket and get on with it.",
            "Cool enough that you'll notice. Light layer and you'll be fine.",
            "Mild but you'll feel it by mid-morning. A light layer addresses this.",
            "Light jacket weather. Not much else to report, which is something."
        ),
        OC to listOf(
            "Cool maritime day. Light jacket and you're set. The ocean is keeping things from being worse.",
            "Mild and slightly cool. The Atlantic is moderating. Light layer and it's fine.",
            "Cool coastal conditions. Light jacket covers it. The dampness makes it feel slightly more than it is.",
            "Light jacket weather. Oceanic cool. The forecast has nothing else to add, for once."
        ),
        NO to listOf(
            "Mild by Nordic standards. Which is to say: you'll want a light jacket. The wind is the deciding factor.",
            "Not that cold. Still a jacket. The Nordic wind chill has a way of changing the thermometer's story.",
            "Cool Nordic day. Light jacket. The temperature says manageable; the wind will offer a second opinion.",
            "Light jacket weather in the Nordic sense: this is actually a reasonable day. Appreciate it."
        )
    )

    override val jacketVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Cold for the tropics. Jacket. The body here is not used to this and it will let you know.",
            "Unusually cold. The atmosphere has imported some conditions from further away. Jacket.",
            "Cold. Tropical cold, but cold. Jacket and mild bewilderment.",
            "Sub-12°C in the tropics. Something unusual is happening. Jacket. Possibly two."
        ),
        ST to listOf(
            "Cold. Jacket. Don't try to tough it out — nobody's watching.",
            "Cold snap. The subtropical sky has remembered it has a winter mode. Jacket.",
            "Cold enough to matter. Jacket. The warmth will be back. It is not back yet.",
            "Single digits. Jacket. The forecast has made its position on today clear."
        ),
        TE to listOf(
            "Cold. Jacket. Don't try to tough it out — nobody's watching.",
            "Jacket weather. The sunshine is decorative. It is not helping.",
            "Cold enough to matter. Jacket. Maybe gloves if you're honest with yourself.",
            "Single digits. Jacket. The weather has made its position clear."
        ),
        OC to listOf(
            "Cold maritime day. The dampness makes it worse than the number suggests. Jacket.",
            "Cold and damp. The ocean is not warming things today. Jacket. Maybe gloves.",
            "Maritime cold. Jacket. The Atlantic is not providing any warmth right now.",
            "Cold coastal conditions. Jacket. The wind coming off the water will see to the rest."
        ),
        NO to listOf(
            "Cold. By most standards. Still a moderate Nordic day, but cold. Jacket.",
            "Cold today. Nordic cold. Which is cold. Jacket and possibly gloves.",
            "Jacket weather. The wind chill makes the actual temperature somewhat optimistic. Jacket.",
            "Cold. The Nordic climate reminding you of its primary characteristic. Jacket."
        )
    )

    override val bundleUpVerdict: ZonedPool = mapOf(
        TR to listOf(
            "It's properly cold. In the tropics. All the layers. Every single one you can find.",
            "Bitter cold for this climate zone. The body has no adaptation for this. Everything warm.",
            "Sub-5°C in the tropics. This is a genuine meteorological anomaly and a genuine personal problem. Bundle up.",
            "Cold. Actually cold. Tropically cold, which is worse because nothing here is designed for it. All the layers."
        ),
        ST to listOf(
            "It's properly cold. All the layers. Every single one.",
            "Bitter cold snap. The subtropical cold outbreak has nothing against you. It just doesn't care. Bundle up.",
            "Sub-zero feels. Whatever gap exists in your cold weather clothing — the cold has already found it.",
            "Cold. Genuinely cold. Bundle up and come to terms with it."
        ),
        TE to listOf(
            "It's properly cold. All the layers. Every single one.",
            "Bitter out there. The cold has nothing personal against you. It just doesn't care.",
            "Sub-zero feels. Whatever gap exists between your scarf and collar — the cold has already found it.",
            "Cold. Genuinely cold. Bundle up and come to terms with it."
        ),
        OC to listOf(
            "Maritime cold. Wet and freezing. The dampness makes every gap in your clothing matter more. Bundle up.",
            "Cold Atlantic air. The ocean is not helping today. It is contributing. All the layers.",
            "Bitter maritime cold. Cold and damp is harder than cold and dry. Bundle up completely.",
            "Near-freezing with Atlantic wind. The cold finds every gap. Base layer, mid layer, waterproof outer. All of it."
        ),
        NO to listOf(
            "Deep Nordic cold. This is what everything was always going to be like. Full thermal system. Every layer.",
            "Properly cold by any standard. The polar air has arrived with no ambiguity. Bundle up completely.",
            "Arctic cold. The wind chill is the real number and it is not a kind one. Every layer you own.",
            "Nordic winter conditions. The cold is not dramatic. It is simply absolute. Full layering system."
        )
    )

    override val allClearVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Clear. The tropical sky has taken the day off from making things difficult. Go outside.",
            "Clear skies. The ITCZ has moved away. The atmosphere is briefly uncomplicated. Enjoy it.",
            "Nothing bad is happening. The tropics have granted a window. It won't last forever. Use it.",
            "Clear tropical day. No storms forming. No humidity events. A genuinely good day. I'm sorry."
        ),
        ST to listOf(
            "Clear. No atmospheric excuse to stay home. The day is yours. I'm sorry.",
            "Clear skies. Don't get used to it.",
            "Nothing bad is happening today. Go outside while the subtropical high is cooperating.",
            "Clear. Sunny. The forecast has nothing to complain about, which is unusual for it."
        ),
        TE to listOf(
            "Clear. No atmospheric excuse to stay home. The day is yours. I'm sorry.",
            "Clear skies. Don't get used to it.",
            "Nothing bad is happening today, weather-wise. Go outside. While it lasts.",
            "Perfectly clear. Statistically, this is your best day this week. Spend it wisely."
        ),
        OC to listOf(
            "Clear. The Atlantic has paused. This doesn't happen often here. Go outside.",
            "Clear skies on the coast. Note the date. They are not always like this.",
            "No rain. No wind. No clouds. The oceanic climate is briefly, inexplicably, cooperating.",
            "Clear. The maritime weather has taken an uncharacteristic break. Go outside before it remembers itself."
        ),
        NO to listOf(
            "Clear. Dark or light depending on the season, but clear. Go outside.",
            "Nordic clear day. Cold but clear. The best available option here. Use it.",
            "Clear skies. The polar high has delivered its one favour. Go outside before the next front arrives.",
            "Clear. No excuses. The Nordic outdoors is available today. I'm sorry for the temperature."
        )
    )

    // ── Mood pools ───────────────────────────────────────────────────────────

    override val stormMood: ZonedPool = mapOf(
        TR to listOf(
            "The monsoon doesn't care about your plans. It never did.",
            "Tropical storms are a feature, not a bug. You live here. The sky lives here too.",
            "You had somewhere to be. The storm had already claimed the afternoon.",
            "Nature reminding you it doesn't need your schedule. Tropically."
        ),
        ST to listOf(
            "The heat built all day for this. In a way, it was always going to end here.",
            "The sky saved everything up and delivered it at once. That's just how the subtropics work.",
            "You had plans. The atmosphere had other ideas. The atmosphere wins, as usual.",
            "A summer storm. Inevitable, dramatic, and entirely indifferent to your afternoon."
        ),
        TE to listOf(
            "The weather isn't angry at you specifically. You're just in the way.",
            "You had plans. The sky had other ideas. The sky won.",
            "Nature reminding you it doesn't need your schedule.",
            "A proper storm. The good news is you have an excellent excuse to stay in."
        ),
        OC to listOf(
            "The Atlantic has been building this for days. You were eventually going to be in the way.",
            "Maritime storms have a patience to them. This one waited. Now it's here.",
            "You live on the Atlantic coast. This is a condition of that arrangement.",
            "The ocean sent its regards. They are wet and loud and require you to stay inside."
        ),
        NO to listOf(
            "The polar atmosphere is not being subtle about its feelings.",
            "Arctic storm. You knew what this place was when you arrived.",
            "The darkness and the wind have found each other. This happens here.",
            "The Nordic winter is expressing itself comprehensively today. Let it."
        )
    )

    override val heavyRainMood: ZonedPool = mapOf(
        TR to listOf(
            "The tropical sky commits to things. Today it committed to this.",
            "You will get wet. The only question is how much of you.",
            "It's coming down hard. Even by tropical standards, which are not low.",
            "Heavy and warm and entirely relentless. Welcome to the rainy season."
        ),
        ST to listOf(
            "You will get wet. The only question is which part of you surrenders first.",
            "Heavy subtropical rain arrives with a certainty that feels personal.",
            "Coming down hard. Everything you planned outdoors has been rescheduled by the sky.",
            "The gutters are working overtime. You won't be dry. Accept this early."
        ),
        TE to listOf(
            "You will get wet. The only question is which part of you surrenders first.",
            "Coming down hard. Bring everything. Expect nothing.",
            "Heavy. Relentless. Very much your problem now.",
            "The gutters are working overtime. You won't be dry. Accept this early."
        ),
        OC to listOf(
            "The Atlantic front has made its point. Comprehensively.",
            "Heavy Atlantic rain has a thoroughness to it. It finds everywhere.",
            "Pouring. The oceanic moisture content is significant and it's all coming down today.",
            "Heavy maritime rain. You live here. This is part of the arrangement."
        ),
        NO to listOf(
            "Cold heavy rain. Both unpleasant. Together, formidable.",
            "Nordic heavy precipitation. The cold makes the rain feel personal.",
            "It is heavy and cold and it will continue. The Arctic is efficient.",
            "Heavy rain in near-freezing air. Comfort is not on offer today."
        )
    )

    override val rainMood: ZonedPool = mapOf(
        TR to listOf(
            "Afternoon rain again. You knew. We all knew.",
            "The tropics have a very consistent relationship with rain. Today is not an exception.",
            "Wet out there. Warm and wet. The tropical baseline.",
            "It's not a storm. Just the daily rain event. Arriving on schedule."
        ),
        ST to listOf(
            "The puddles are forming. You will find the deep one.",
            "Rain in the subtropics. Not as common as it should be. Not welcome today regardless.",
            "Wet out there. Been wetter. Will be wetter again. This is fine.",
            "Steady subtropical rain. The sky is making a rare but thorough effort."
        ),
        TE to listOf(
            "The puddles are forming. You will find the deep one.",
            "The rain doesn't know your name. It doesn't need to.",
            "Wet out there. Been wetter. Will be wetter again.",
            "It's not a storm. Just persistent and unpleasant. You'll survive. Probably."
        ),
        OC to listOf(
            "Rain. You live here. Umbrella.",
            "The Atlantic front is doing what Atlantic fronts do. Umbrella.",
            "It's raining. This is the coast. These things coexist.",
            "Wet. Persistently. The oceanic baseline expressing itself."
        ),
        NO to listOf(
            "Cold frontal rain. The cold air behind it is on its way. The rain is just the opening act.",
            "Nordic rain is not warm. That's the main thing to know.",
            "Frontal rain with everything cold about it. You'll survive. Dress for it.",
            "Rain. Cold rain. At least it's not snow. Yet."
        )
    )

    override val drizzleMood: ZonedPool = mapOf(
        TR to listOf(
            "Post-convective mist. The drama is over; what remains is just damp air.",
            "Residual moisture. The tropics couldn't quite commit to a full rain today. This is what you get.",
            "Tropical drizzle is the sky being polite. Appreciate the restraint.",
            "It drizzles between storms here sometimes. This is that."
        ),
        ST to listOf(
            "You'll arrive somewhere slightly damp and nobody will understand why.",
            "Marine layer drizzle. The coast reaches inland today. Just damp enough to be annoying.",
            "It drizzles in the way that life sometimes drizzles. You know the feeling.",
            "Passive aggressive precipitation. Subtropical edition."
        ),
        TE to listOf(
            "You'll arrive somewhere slightly damp and nobody will understand why.",
            "Not a real rain. Just atmospheric disappointment with moisture.",
            "It drizzles in the way that life sometimes drizzles. You know the feeling.",
            "It'll cling to your jacket and make everything slightly worse. Welcome to today."
        ),
        OC to listOf(
            "Atlantic drizzle. The defining precipitation of the north oceanic coast. You know this one.",
            "The sky is technically not raining. It is, however, making everything wet. Hood.",
            "Maritime drizzle. Not dramatic. Just permanent. Welcome to the coast.",
            "Fine oceanic precipitation. The kind that just exists. You know what to do."
        ),
        NO to listOf(
            "Cold fine precipitation. The drizzle here doesn't have the luxury of being merely annoying — it's cold too.",
            "Near-freezing drizzle. The worst kind. Not enough to name, too cold to ignore.",
            "Nordic drizzle. The cold makes the subtlety feel sarcastic.",
            "Fine cold mist. Damp and cold and persistent. The Nordic climate's version of drizzle."
        )
    )

    override val snowMood: ZonedPool = mapOf(
        TR to listOf(
            "Snow in the tropics. The atmosphere has departed from the expected narrative.",
            "Picturesque and deeply inconvenient and genuinely not supposed to be happening here.",
            "The sky has done something unusual. Infrastructure and people are both underprepared.",
            "Snow. Here. Yes. The world is full of surprises, most of them cold."
        ),
        ST to listOf(
            "Picturesque. Treacherous. Somehow both at once.",
            "The world looks like a greeting card. The commute does not.",
            "Subtropical snow is an event. The black ice will not announce itself.",
            "Snow is frozen disappointment. Subtropical edition — rarer and therefore worse."
        ),
        TE to listOf(
            "Picturesque. Treacherous. Somehow both at once.",
            "The world is beautiful and your commute is ruined. Happy winter.",
            "Snow is just frozen disappointment. Wear boots.",
            "Peaceful, if you don't have to go anywhere. Do you have to go anywhere?"
        ),
        OC to listOf(
            "Maritime snow: wet, heavy, and converting to ice on contact with anything. Watch every surface.",
            "Oceanic snow is not the light powder variety. It compacts. It refreezes. Watch your footing.",
            "The snow looks soft. It is not soft. The water content made sure of that.",
            "Heavy wet snow. The world is white and slippery and the gritters are trying their best."
        ),
        NO to listOf(
            "Snow. Yes. Still. This is the Nordic winter. Bundle up.",
            "The world is white and frozen and this is normal here. Boots. Traction. Patience.",
            "Nordic snow. Light, dry, drifting. Beautiful. Cold. The commute is your problem.",
            "Snowfall. Again. The sky here has a very consistent aesthetic."
        )
    )

    override val windMood: ZonedPool = mapOf(
        TR to listOf(
            "The trade winds have strengthened past their usual position. Something is informative about this.",
            "Tropical wind event. Something that wasn't tied down is now someone else's problem.",
            "Strong tropical winds. Impressive, in a grim sort of way.",
            "The atmosphere is moving with purpose today. You are not the purpose."
        ),
        ST to listOf(
            "Something that wasn't tied down is now someone else's problem.",
            "Feels personal. It isn't. The pressure gradient just doesn't care.",
            "The umbrella would have embarrassed you anyway. You made the right call leaving it.",
            "Significant wind. Impressive, in the kind of way that makes you stay inside."
        ),
        TE to listOf(
            "Something that wasn't tied down is now someone else's problem.",
            "Feels personal. It isn't. The wind just doesn't care.",
            "The umbrella would have embarrassed you anyway. Leave it.",
            "Impressive, in a grim sort of way."
        ),
        OC to listOf(
            "The Atlantic has opinions and today it is expressing them at volume.",
            "Maritime wind. The ocean built this over hundreds of kilometres. It arrived with the full investment.",
            "Strong coastal wind. The kind that makes you understand why people build houses with thick walls here.",
            "The ocean is expressing itself through wind today. You are receiving the message."
        ),
        NO to listOf(
            "Arctic wind. The cold and the speed combined is doing something the thermometer is understating.",
            "Nordic wind. It was already cold. The wind saw room for improvement.",
            "The polar atmosphere is redistributing air masses. You are in the redistribution path.",
            "Cold and fast and efficient. The Arctic doesn't waste effort on warmth today."
        )
    )

    override val breezeMood: ZonedPool = mapOf(
        TR to listOf(
            "Trade winds at their most agreeable. The tropical baseline at its best.",
            "A pleasant tropical breeze. The kind that makes you understand why people live here.",
            "Warm breeze. The atmosphere is being cooperative. Appreciate it.",
            "The trade winds are in their pleasant mode today. This is the best the tropics routinely offer."
        ),
        ST to listOf(
            "The atmosphere's version of a shrug. A comfortable shrug today.",
            "Not enough wind to cause problems. Not enough cloud to complain about. A day.",
            "The breeze is manageable. The forecast has nothing to add. That's fine.",
            "Breezy and mild. The subtropical high is in its most agreeable configuration."
        ),
        TE to listOf(
            "The atmosphere's version of a shrug. Still a shrug though.",
            "Not enough wind to cause problems. Not enough sun to celebrate. A day.",
            "Your umbrella will behave. Mostly.",
            "Breezy. The most inoffensive weather. Something to be grateful for, I suppose."
        ),
        OC to listOf(
            "Atlantic breeze. Moderate, sustained, and utterly characteristic of this coast.",
            "The ocean is breathing today. Enough wind to notice, not enough to matter.",
            "Maritime breeze. Your umbrella will hold. Probably.",
            "Breezy coastal conditions. The Atlantic in its least dramatic mode."
        ),
        NO to listOf(
            "Nordic breeze. Cool and sharp. The wind chill adds a note that the thermometer doesn't fully explain.",
            "Moderate wind in cold air. A light windproof layer and you're set. Today is inoffensive.",
            "The Arctic breeze has decided to be moderate today. A small favour.",
            "Breezy and cold. The Nordic version of a shrug. Still requires a windproof layer."
        )
    )

    override val allClearWarmMood: ZonedPool = mapOf(
        TR to listOf(
            "Go outside. The afternoon storms aren't here yet. That window exists. Use it.",
            "Warm and clear and the tropics are briefly not being difficult. Appreciate this.",
            "A good tropical day. Note the date. File it away for the rainy season.",
            "Nice out. Genuinely nice. The tropical atmosphere is cooperating. Eat lunch outside."
        ),
        ST to listOf(
            "Go outside. Appreciate it. File this day away for January.",
            "A good day. Note the date. They're not all like this.",
            "Make the most of it. The forecast has opinions about the weekend.",
            "Warm and pleasant. The bar was low and it cleared it impressively."
        ),
        TE to listOf(
            "Go outside. Appreciate it. File this day away for January.",
            "A good day. Note the date. They're not all like this.",
            "Make the most of it. The forecast has opinions about the weekend.",
            "Warm and pleasant. The bar was low and it cleared it impressively."
        ),
        OC to listOf(
            "Go outside. The Atlantic is having a good day. So are you.",
            "Warm and clear on the coast. This is why people stay despite everything else.",
            "Note the date. An actually good day here. They are not endless.",
            "The oceanic climate occasionally justifies itself. Today is that occasion."
        ),
        NO to listOf(
            "Summer is briefly here. Go outside. The window is real.",
            "Warm Nordic day. File this away for February.",
            "The Nordic summer is making its case. Go outside and receive it.",
            "Clear and warm and the sun is staying up. This is the season justifying itself. Use it."
        )
    )

    override val allClearNeutralMood: ZonedPool = mapOf(
        TR to listOf(
            "Clear tropical skies. Storms are possible this afternoon. They're always possible this afternoon.",
            "No rain right now. The tropics have noted this and are planning accordingly.",
            "Clear. Calm. The ITCZ hasn't decided anything yet today.",
            "It's not actively bad. The tropics are withholding judgment."
        ),
        ST to listOf(
            "Clear skies. Don't get used to it.",
            "It's not actively bad. That's today's headline.",
            "Weather's fine. Everything else is still your problem.",
            "Go outside. The sun will be back to ignoring you soon enough."
        ),
        TE to listOf(
            "Clear skies. Don't get used to it.",
            "It's not actively bad. That's today's headline.",
            "Weather's fine. Everything else is still your problem.",
            "Go outside. The sun will be back to ignoring you soon enough."
        ),
        OC to listOf(
            "Not raining. The oceanic climate has briefly suspended its habits.",
            "Clear maritime conditions. Enjoy the weather not being your problem today.",
            "No rain, no wind, no drama. The Atlantic is resting. So should you.",
            "It's fine today. Note that. Fine is not the baseline here."
        ),
        NO to listOf(
            "Clear Nordic skies. Cold but clear. A reasonable day, objectively.",
            "Not storming. Not snowing. Clear. The Nordic winter occasionally allows this.",
            "Clear and cold. The polar high is doing you a favour. Note it.",
            "No atmospheric drama. The Arctic has decided to take the day off from being difficult."
        )
    )

    override val greyMood: ZonedPool = mapOf(
        TR to listOf(
            "Overcast but not active yet. The tropical atmosphere is deciding something. It usually decides rain.",
            "Grey tropical sky. Afternoon will likely have opinions. For now, you're clear.",
            "Clouds without rain. The tropics are pacing themselves.",
            "Cloud cover without conviction. Could be worse. Could be about to be worse."
        ),
        ST to listOf(
            "Could be worse. Could always be worse.",
            "Not raining, technically. The subtropical sky is weighing its options.",
            "Grey is just the default setting some days. Try to remember that.",
            "Overcast. Not threatening. Just grey. The forecast has nothing to commit to."
        ),
        TE to listOf(
            "Could be worse. Could always be worse.",
            "Not raining, technically. The sky is keeping its options open.",
            "Grey is just the default setting. Try to remember that.",
            "The sun exists. You just can't see it. Comforting, maybe."
        ),
        OC to listOf(
            "Overcast. Just overcast. The oceanic climate at its most understated.",
            "Grey maritime day. The Atlantic stratus has settled in. It's not raining. It's just grey.",
            "The cloud base is low and the sky is indifferent. No rain. Just grey. Just this.",
            "Atlantic overcast. The default state. No precipitation. Just the character of the coast."
        ),
        NO to listOf(
            "Nordic grey. Low sun angle, persistent cloud cover, limited light. Not raining. Just grey.",
            "Overcast at high latitude. The diffuse light is even, if dim. Carry on.",
            "Grey. The Nordic winter default. No precipitation. Just the colour of winter.",
            "The sun is technically there. It is not, however, visible. Carry on."
        )
    )
}
