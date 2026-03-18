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
            "Sky's feelings: unmistakable. Stay inside.",
            "Monsoon cancelled your plans. Eventually anyway.",
            "The tropics remind you who's in charge. Today.",
            "The atmosphere didn't know your schedule. Nor did it care."
        ),
        ST to listOf(
            "The heat built all day and paid out on you. Stay in.",
            "The sky committed. It saved this for you specifically.",
            "Warned by heat and pressure. Here it is. Full gear.",
            "Subtropical summers always end like this. Stay inside."
        ),
        TE to listOf(
            "You had somewhere to be. The sky had a prior engagement.",
            "Today was always going to go like this.",
            "The sky has formally cancelled your day. Stay inside.",
            "You had plans. How ambitious of you. Stay inside."
        ),
        OC to listOf(
            "The ocean built this for days. Delivered to your door.",
            "Atlantic gale. The weather here means business. Today.",
            "Maritime storm. The ocean expresses itself. Stay in.",
            "The Atlantic low arrived. You knew it would. Stay inside."
        ),
        NO to listOf(
            "Polar atmosphere: no longer withholding. Stay inside.",
            "Nordic gale. You knew when you moved here. Stay in.",
            "Darkness, wind, cold. The Nordic trinity. Indoor plans.",
            "Polar vortex having a moment. You're in its way. Inside."
        )
    )

    override val heavyRainVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Respects nothing and no one. Full waterproofing.",
            "Monsoon fully committed. Full gear or stay in.",
            "The sky has decided. Are you prepared or not?",
            "Tropically pouring. Ruins shoes. Waterproof everything."
        ),
        ST to listOf(
            "Rare but arrived with conviction. Full waterproofing.",
            "The sky committed harder than most people ever do.",
            "Heat built this all season. It's here. Full gear.",
            "Add 'significantly wetter' to your itinerary. Full gear."
        ),
        TE to listOf(
            "Absolutely pouring. Umbrella, jacket, low expectations.",
            "The sky committed like most people never do. Full gear.",
            "Finds you no matter what. Waterproof jacket.",
            "Planning to go out? Add 'getting soaked' unless you gear up."
        ),
        OC to listOf(
            "Thorough, persistent, indifferent to your plans. Full kit.",
            "The ocean clearing surplus moisture. Over you. Today.",
            "You live here. You know how this goes. Full waterproofing.",
            "Atlantic front delivered. Heavy rain, no apology. Full kit."
        ),
        NO to listOf(
            "Works on you from multiple directions. Full waterproofing.",
            "Cold. Wet. Persistent. Impressive in its unpleasantness.",
            "Pouring in near-freezing temperatures. Full gear.",
            "Heavy rain, Arctic air behind it. Waterproof and warm."
        )
    )

    override val rainVerdict: ZonedPool = mapOf(
        TR to listOf(
            "You knew this was coming. The sky is very consistent.",
            "Warm, heavy, unbothered by your schedule. Umbrella.",
            "The tropics are not subtle about precipitation. Umbrella.",
            "Afternoon convection started. It'll stop when it decides."
        ),
        ST to listOf(
            "One of the few rainy days, and it chose today. Umbrella.",
            "No intention of stopping for you specifically. Umbrella.",
            "Moderate, persistent, unbothered by your plans.",
            "The subtropical trough didn't ask. Neither did I. Umbrella."
        ),
        TE to listOf(
            "Take an umbrella or arrive wet. Both have consequences.",
            "No intention of stopping for you specifically. Umbrella.",
            "Moderate, persistent, unbothered by your plans.",
            "Still raining at 2pm. You'll thank yourself for the umbrella."
        ),
        OC to listOf(
            "It's raining. Of course it's raining. Umbrella.",
            "The forecast said rain. The forecast was correct. Umbrella.",
            "Steady, oceanic, unhurried. The maritime climate at work.",
            "Again. The Atlantic has wet opinions today. Umbrella."
        ),
        NO to listOf(
            "Nordic rain commits to making you feel it. Umbrella.",
            "Cold and raining and both continue for a while. Umbrella.",
            "Frontal rain in cold air. It is like this. Umbrella.",
            "Cold rain. Not the warm tropical variety. Gear up."
        )
    )

    override val drizzleVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Humidity made worse by moisture. Hood optional.",
            "It drizzles between storms here. You'll survive. Hood.",
            "As subtle as the tropics get. Light coverage.",
            "The air barely has to try. Hood if you care about your hair."
        ),
        ST to listOf(
            "The coast arrived at your door uninvited. Hood.",
            "Sky being passive aggressive, subtropical edition. Hood.",
            "Not enough for an umbrella. Too wet to ignore entirely.",
            "Not enough to ruin your day. Enough to make it worse. Hood."
        ),
        TE to listOf(
            "Not enough to cancel plans. Enough to make things worse.",
            "Ruins your hair; not wet enough to justify staying in.",
            "Too wet to ignore, not wet enough to complain about properly.",
            "The sky is being passive aggressive. Hood or light layer."
        ),
        OC to listOf(
            "The oceanic default mode. Hood up and get on with it.",
            "Too light to mention. Too persistent to ignore. Hood.",
            "Drizzle. You live here. You know about the drizzle. Hood.",
            "Just background moisture here. Hood."
        ),
        NO to listOf(
            "Not dramatic. Just persistent and below your tolerance.",
            "Near-freezing is an option. Light waterproofing.",
            "Near-freezing drizzle. Hat and light coverage.",
            "Cold and slightly damp. You'll arrive cold and damp. Hood."
        )
    )

    override val snowVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Snow here. The atmosphere departed from the script.",
            "Snow here. That sentence shouldn't exist today. Layers.",
            "Tropical snowfall. The sky knows. It doesn't care. Bundle up.",
            "Snow. Yes, here. No, it's not usual. All warm layers."
        ),
        ST to listOf(
            "Snow. A genuine cold outbreak at this latitude. Bundle up.",
            "Cold snap arrived with consequences. Layers.",
            "Infrastructure not designed for this. Neither were you.",
            "White out there. Everything takes longer. Everything icy."
        ),
        TE to listOf(
            "Beautiful, treacherous, inconvenient. Bundle up.",
            "Snow today. Everything colder. Everything takes longer.",
            "White out there. The black ice is not announcing itself.",
            "Greeting card outside. Commute does not match."
        ),
        OC to listOf(
            "Heavy, cold, icing on contact. Watch your footing.",
            "Looks soft. Isn't. Dense and slippery. Full winter kit.",
            "Atlantic cold produced frozen precipitation. Your problem now.",
            "Wet, heavy, slippery. Watch every surface. Bundle up."
        ),
        NO to listOf(
            "Snow. Yes. Still. You signed up for this. Bundle up.",
            "Full winter kit. How cold is the only question.",
            "Frozen and beautiful and your commute is ruined. Standard.",
            "Snowing again. The Norse gods were not joking. All layers."
        )
    )

    override val veryWindyVerdict: ZonedPool = mapOf(
        TR to listOf(
            "System edge or worse approaching. Stay inside.",
            "Tropical atmosphere organised against you. Stay inside.",
            "Something serious is happening. Stay inside and monitor.",
            "Sky has strong opinions today. Don't argue from outside."
        ),
        ST to listOf(
            "The subtropical low committed. You don't have to.",
            "Outside is hostile. It doesn't need to be your problem.",
            "The rare subtropical dramatic day. Stay inside.",
            "The atmosphere made a point. You needn't go out to receive it."
        ),
        TE to listOf(
            "Wind with a point to make. Stay inside if you can.",
            "Outside having a terrible time. You don't have to join it.",
            "Head down, don't dawdle. If you must go out.",
            "Strong wind. Sensible people stay in. Are you sensible?"
        ),
        OC to listOf(
            "The ocean built this over days. Now it's here. Stay in.",
            "Full maritime gale. The Atlantic does not do halves.",
            "The ocean being entirely itself. Do not go outside.",
            "Atlantic recommendation: indoor plans. Follow it."
        ),
        NO to listOf(
            "Exposed skin: immediate problem at these speeds. Inside.",
            "Polar vortex extended your way. Not gently. Stay in.",
            "Dangerous combination. This is a stay-inside day.",
            "Cold plus speed equals hostile. Indoor plans only."
        )
    )

    override val windyVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Umbrella is a liability today. Leave it.",
            "Your plans just got harder. No apology from the forecast.",
            "Windy. The tropics stopped cooperating. Skip the umbrella.",
            "Tropics not cooperating. Windproof layer. Lose the umbrella."
        ),
        ST to listOf(
            "Umbrella will embarrass you today. Leave it at home.",
            "Strong winds and no apology. Windproof layer.",
            "Umbrella already lost today. Don't take it out.",
            "Pressure gradient has opinions. Windproof layer."
        ),
        TE to listOf(
            "Umbrella is a trap today. Leave it.",
            "Your plans just got harder. No apology from the forecast.",
            "Outside working against you today. Skip the umbrella.",
            "The wind doesn't care that you have somewhere to be."
        ),
        OC to listOf(
            "Strong, sustained, indifferent to your umbrella. Leave it.",
            "Coast doing what coasts do. Windproof layer.",
            "Atlantic sharing its wind generously today. Skip the umbrella.",
            "The oceanic climate expressing itself through kinetic energy."
        ),
        NO to listOf(
            "Cold, strong, wind chill-amplified. Windproof shell.",
            "Wind chill worse than the thermometer. Windproof shell.",
            "Umbrella never worked here. Windproof outer layer.",
            "Arctic doesn't ask how you feel about it. Windproof shell."
        )
    )

    override val hotVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Humidity makes existing an effort today. Water, shade, SPF.",
            "Very hot and humid. Your body is working hard. Hydrate.",
            "Aggressively unpleasant. Water. Shade. Sunscreen. Move fast.",
            "Heat index extreme. Sunscreen, water, tempered expectations."
        ),
        ST to listOf(
            "Sun has strong opinions and you're in them. Sunscreen.",
            "Maximum insolation delivered to your location. Sunscreen.",
            "Pavement contributing to the heat. Water, sunscreen.",
            "Sun means it. Sunscreen. Water. Recalibrate expectations."
        ),
        TE to listOf(
            "Sun has strong opinions and you're in them. Sunscreen.",
            "Actually hot. Hydrate and don't be dramatic about it.",
            "Warm. You'll be reminded of it constantly. Sunscreen, water.",
            "The heat means it. Sunscreen. Water. Tempered expectations."
        ),
        OC to listOf(
            "Atlantic not cooling things today. Enjoy it or suffer it.",
            "Blocking high removed the sea breeze. Sunscreen still needed.",
            "Uncomfortable, which this climate rarely has to apologise for.",
            "Maritime heatwave. Rare. Disorienting. Hydrate. Sunscreen."
        ),
        NO to listOf(
            "Not that hot. But UV here is deceptive. Sunscreen.",
            "It will pass. But now: sunscreen and water.",
            "Warm enough to notice. UV accumulates here. Sunscreen.",
            "Midnight sun means more UV than you think. Sunscreen."
        )
    )

    override val warmVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Go outside before the afternoon decides things.",
            "Tropics temporarily not being difficult. Enjoy this.",
            "Sky cooperating. Take advantage before it stops.",
            "Tropical atmosphere in its most agreeable mode. Go outside."
        ),
        ST to listOf(
            "Warm and clear. Zero complaints available. Suspicious.",
            "Subtropical high delivered. Go before the forecast changes.",
            "Warm, sunny, uneventful. Weather is not your problem.",
            "The bar was low. It cleared it."
        ),
        TE to listOf(
            "Warm and clear. Zero complaints available. Suspicious.",
            "Go before the forecast remembers what it's supposed to do.",
            "Warm, sunny, uneventful. Weather is not your problem.",
            "The bar was low. It cleared it."
        ),
        OC to listOf(
            "Warm maritime day. These don't come often. Go outside.",
            "Rare warm and clear coast day. Note it. Use it.",
            "Atlantic cooperating. Warm and dry. The exception. Go out.",
            "Clear by oceanic standards. This is not nothing. Go outside."
        ),
        NO to listOf(
            "Warm here. That's not a sentence this forecast says often.",
            "The thermometer cooperating. Possibly temporarily. Go.",
            "Nordic warmth making its case. Sun staying late. Use it.",
            "Actually warm today. Rare enough to comment on. Use it."
        )
    )

    override val lightJacketVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Cool here. Something unusual is happening. Light layer.",
            "A light layer today — not a usual tropical sentence. Jacket.",
            "Cooler than expected. Brief atmospheric cooperation. Jacket.",
            "Cool or anomalously cold depending on your outlook. Jacket."
        ),
        ST to listOf(
            "A bit cool. Grab a light jacket and get on with it.",
            "Cool subtropical day. Not dramatic. Light layer, done.",
            "Mild but you'll feel it. A light layer. Nothing else needed.",
            "Light jacket weather. Nothing else to report. Rare."
        ),
        TE to listOf(
            "A bit cool. Grab a light jacket and get on with it.",
            "Cool enough to notice. Light layer and you'll be fine.",
            "Mild but you'll feel it by mid-morning. Light layer.",
            "Light jacket weather. Not much else to report. Something."
        ),
        OC to listOf(
            "Light jacket and you're set. Ocean keeping things civil.",
            "Mild and slightly cool. Atlantic moderating. Light layer.",
            "Dampness makes it feel like more than it is. Light jacket.",
            "Light jacket weather. Forecast has nothing else to add."
        ),
        NO to listOf(
            "Mild here. You'll want a light jacket. Wind is the factor.",
            "Still a jacket. Nordic wind chill rewrites the thermometer.",
            "Temperature says manageable. Wind will offer a second opinion.",
            "A reasonable day. Light jacket. Appreciate it."
        )
    )

    override val jacketVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Cold for the tropics. Body not used to this. Jacket.",
            "Conditions imported from further away. Jacket.",
            "Tropical cold. Jacket and mild bewilderment.",
            "Sub-12°C in the tropics. Something unusual. Jacket."
        ),
        ST to listOf(
            "Cold. Jacket. Don't try to tough it out. Nobody's watching.",
            "Subtropical winter mode activated. Jacket.",
            "Cold enough to matter. Warmth not back yet. Jacket.",
            "Single digits. Forecast has made its position clear. Jacket."
        ),
        TE to listOf(
            "Cold. Jacket. Don't try to tough it out. Nobody's watching.",
            "Sunshine is decorative today. Not helping. Jacket.",
            "Cold enough. Jacket. Gloves if you're honest with yourself.",
            "Single digits. Weather made its position clear. Jacket."
        ),
        OC to listOf(
            "Dampness makes it worse than the number. Jacket.",
            "Cold and damp. Ocean not warming today. Jacket. Gloves.",
            "Maritime cold. Atlantic not providing warmth. Jacket.",
            "Wind off the water handles the rest. Jacket."
        ),
        NO to listOf(
            "Cold. Moderate for Nordic. Still cold. Jacket.",
            "Nordic cold. Which is cold. Jacket and possibly gloves.",
            "Wind chill makes the thermometer optimistic. Jacket.",
            "Nordic climate reminding you of its main characteristic."
        )
    )

    override val bundleUpVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Properly cold. In the tropics. Every layer you can find.",
            "No adaptation for this here. Everything warm.",
            "Tropical cold anomaly and a personal problem. Bundle up.",
            "Nothing here is designed for this. All the layers."
        ),
        ST to listOf(
            "Properly cold. All the layers. Every single one.",
            "Cold outbreak has nothing against you. It doesn't care.",
            "Every gap in your cold weather kit. Cold found it already.",
            "Cold. Genuinely cold. Bundle up and come to terms with it."
        ),
        TE to listOf(
            "Properly cold. All the layers. Every single one.",
            "The cold has nothing personal against you. It just doesn't care.",
            "Whatever gap exists between scarf and collar. Cold found it.",
            "Cold. Genuinely cold. Bundle up and come to terms with it."
        ),
        OC to listOf(
            "Wet and freezing. Every gap matters more. Bundle up.",
            "Ocean contributing today. Not helping. All the layers.",
            "Cold and damp is harder than cold and dry. Bundle up.",
            "Cold finds every gap. Base, mid, waterproof outer. All of it."
        ),
        NO to listOf(
            "Full thermal system. Every layer. This is just winter.",
            "Polar air arrived unambiguously. Bundle up completely.",
            "Wind chill is the real number. Not a kind one. Every layer.",
            "Cold is not dramatic here. It's simply absolute. Full layers."
        )
    )

    override val allClearVerdict: ZonedPool = mapOf(
        TR to listOf(
            "Tropical sky took the day off from difficulty. Go outside.",
            "ITCZ moved away. Briefly uncomplicated. Enjoy it.",
            "Nothing bad happening. Tropics granted a window. Use it.",
            "No storms forming. A genuinely good day. I'm sorry."
        ),
        ST to listOf(
            "Clear. No excuse to stay home. The day is yours. Sorry.",
            "Clear skies. Don't get used to it.",
            "Nothing bad today. Go while the high is cooperating.",
            "The forecast has nothing to complain about. Unusual."
        ),
        TE to listOf(
            "Clear. No excuse to stay home. The day is yours. Sorry.",
            "Clear skies. Don't get used to it.",
            "Nothing bad today. Go outside while it lasts.",
            "Statistically your best day this week. Spend it wisely."
        ),
        OC to listOf(
            "The Atlantic paused. Doesn't happen often. Go outside.",
            "Clear skies on the coast. Note the date.",
            "No rain. No wind. No clouds. Inexplicably cooperating.",
            "Maritime weather on break. Go before it remembers itself."
        ),
        NO to listOf(
            "Clear. Dark or light by season, but clear. Go outside.",
            "Cold but clear. Best available option here. Use it.",
            "Polar high delivered its one favour. Go before the front.",
            "Clear. No excuses. Sorry about the temperature."
        )
    )

    // ── Mood pools ───────────────────────────────────────────────────────────

    override val stormMood: ZonedPool = mapOf(
        TR to listOf(
            "The monsoon doesn't care about your plans. It never did.",
            "Tropical storms are a feature. The sky lives here too.",
            "You had somewhere to be. The storm claimed the afternoon.",
            "Nature reminding you it doesn't need your schedule."
        ),
        ST to listOf(
            "The heat built all day for this. It was always going here.",
            "Sky saved everything and delivered it at once. Subtropics.",
            "You had plans. Atmosphere had other ideas. Atmosphere wins.",
            "Inevitable, dramatic, indifferent to your afternoon."
        ),
        TE to listOf(
            "Not angry at you specifically. You're just in the way.",
            "You had plans. The sky had other ideas. Sky won.",
            "Nature doesn't need your schedule.",
            "Excellent excuse to stay in. The silver lining."
        ),
        OC to listOf(
            "The Atlantic built this for days. You were in the way.",
            "Maritime storms have patience. This one waited. It's here.",
            "You live on the Atlantic coast. This is part of the deal.",
            "The ocean sent its regards. Wet, loud, stay inside."
        ),
        NO to listOf(
            "Polar atmosphere not being subtle about its feelings.",
            "Arctic storm. You knew what this place was.",
            "The darkness and wind found each other. It happens here.",
            "Nordic winter expressing itself comprehensively. Let it."
        )
    )

    override val heavyRainMood: ZonedPool = mapOf(
        TR to listOf(
            "The tropical sky commits. Today it committed to this.",
            "You will get wet. The only question is how much of you.",
            "Heavy even by tropical standards. Which are not low.",
            "Heavy and warm and relentless. Welcome to the rainy season."
        ),
        ST to listOf(
            "You will get wet. Which part surrenders first is the question.",
            "Heavy subtropical rain arrives with something personal.",
            "Everything outdoors rescheduled by the sky.",
            "Gutters working overtime. You won't be dry. Accept it early."
        ),
        TE to listOf(
            "You will get wet. Which part surrenders first is the question.",
            "Coming down hard. Bring everything. Expect nothing.",
            "Heavy. Relentless. Very much your problem now.",
            "Gutters working overtime. You won't be dry. Accept it early."
        ),
        OC to listOf(
            "The Atlantic front made its point. Comprehensively.",
            "Atlantic heavy rain has a thoroughness. It finds everywhere.",
            "Pouring. Oceanic moisture content: significant. All down now.",
            "Heavy maritime rain. Part of the arrangement here."
        ),
        NO to listOf(
            "Cold heavy rain. Both unpleasant. Together, formidable.",
            "The cold makes the rain feel personal.",
            "Heavy and cold and it will continue. Arctic efficiency.",
            "Heavy rain in near-freezing air. Comfort not on offer."
        )
    )

    override val rainMood: ZonedPool = mapOf(
        TR to listOf(
            "Afternoon rain again. You knew. We all knew.",
            "Tropics have a consistent relationship with rain. As usual.",
            "Wet out there. Warm and wet. The tropical baseline.",
            "Daily rain event. Arriving on schedule."
        ),
        ST to listOf(
            "The puddles are forming. You will find the deep one.",
            "Not as common as it should be. Not welcome today either.",
            "Wet out there. Been wetter. Will be wetter again. Fine.",
            "Steady subtropical rain. Sky making a rare thorough effort."
        ),
        TE to listOf(
            "The puddles are forming. You will find the deep one.",
            "The rain doesn't know your name. It doesn't need to.",
            "Wet out there. Been wetter. Will be wetter again.",
            "Persistent and unpleasant. You'll survive. Probably."
        ),
        OC to listOf(
            "Rain. You live here. Umbrella.",
            "Atlantic fronts do what Atlantic fronts do. Umbrella.",
            "It's raining. This is the coast. These things coexist.",
            "Persistently wet. The oceanic baseline."
        ),
        NO to listOf(
            "Cold air is behind it. The rain is just the opening act.",
            "Nordic rain is not warm. That's the main thing.",
            "Cold frontal rain. You'll survive. Dress for it.",
            "Rain. Cold rain. At least it's not snow. Yet."
        )
    )

    override val drizzleMood: ZonedPool = mapOf(
        TR to listOf(
            "Drama is over. What remains is just damp air.",
            "Tropics couldn't commit to full rain. This is what you get.",
            "Tropical drizzle is the sky being polite. Note the restraint.",
            "It drizzles between storms here. This is that."
        ),
        ST to listOf(
            "Arrive somewhere damp and nobody will understand why.",
            "Coast reaching inland today. Just damp enough to annoy.",
            "It drizzles in the way life sometimes drizzles. You know.",
            "Passive aggressive precipitation. Subtropical edition."
        ),
        TE to listOf(
            "Arrive somewhere slightly damp. Nobody will understand why.",
            "Not a real rain. Just atmospheric disappointment. With moisture.",
            "It drizzles in the way life sometimes drizzles. You know.",
            "Clings to your jacket. Makes everything slightly worse."
        ),
        OC to listOf(
            "Defining precipitation of this coast. You know this one.",
            "Technically not raining. Making everything wet regardless.",
            "Maritime drizzle. Not dramatic. Just permanent.",
            "Fine oceanic precipitation. Just exists. You know what to do."
        ),
        NO to listOf(
            "Drizzle here doesn't have the luxury of being merely annoying.",
            "Near-freezing drizzle. Not enough to name. Too cold to ignore.",
            "The cold makes the subtlety feel sarcastic.",
            "Damp and cold and persistent. Nordic drizzle."
        )
    )

    override val snowMood: ZonedPool = mapOf(
        TR to listOf(
            "Atmosphere departed from the expected narrative.",
            "Inconvenient and not supposed to be happening here.",
            "Something unusual. Infrastructure and people: underprepared.",
            "Snow. Here. Yes. The world is full of cold surprises."
        ),
        ST to listOf(
            "Picturesque. Treacherous. Somehow both at once.",
            "Greeting card outside. Commute does not match.",
            "Subtropical snow is an event. Black ice unannounced.",
            "Rarer and therefore worse. Subtropical edition."
        ),
        TE to listOf(
            "Picturesque. Treacherous. Somehow both at once.",
            "Beautiful outside. Commute ruined. Happy winter.",
            "Snow is frozen disappointment. Wear boots.",
            "Peaceful, if you don't have to go anywhere. Do you?"
        ),
        OC to listOf(
            "Wet, heavy, converts to ice on contact. Watch every surface.",
            "Not the light powder variety. Compacts. Refreezes. Watch out.",
            "Looks soft. It is not soft. Water content ensured that.",
            "White and slippery and the gritters are doing their best."
        ),
        NO to listOf(
            "Snow. Yes. Still. This is Nordic winter. Bundle up.",
            "White and frozen and normal here. Boots. Traction. Patience.",
            "Beautiful. Cold. Your commute is still your problem.",
            "Again. The sky here has a very consistent aesthetic."
        )
    )

    override val windMood: ZonedPool = mapOf(
        TR to listOf(
            "Trade winds past their usual position. Informative.",
            "Something untied is now someone else's problem.",
            "Strong tropical winds. Impressive, in a grim way.",
            "Atmosphere moving with purpose. You are not the purpose."
        ),
        ST to listOf(
            "Something untied is now someone else's problem.",
            "Feels personal. It isn't. The pressure gradient doesn't care.",
            "Umbrella would have embarrassed you anyway. Right call.",
            "Impressive in the way that keeps you inside."
        ),
        TE to listOf(
            "Something untied is now someone else's problem.",
            "Feels personal. It isn't. The wind just doesn't care.",
            "Umbrella would have embarrassed you anyway. Leave it.",
            "Impressive, in a grim sort of way."
        ),
        OC to listOf(
            "Atlantic expressing opinions at volume today.",
            "Ocean built this over hundreds of kilometres. Full delivery.",
            "Makes you understand why walls here are thick.",
            "Ocean expressing itself through wind. You are receiving it."
        ),
        NO to listOf(
            "Cold and speed: doing something the thermometer understates.",
            "Already cold. Wind saw room for improvement.",
            "You're in the redistribution path. Polar air masses moving.",
            "Cold, fast, efficient. Arctic not wasting effort on warmth."
        )
    )

    override val breezeMood: ZonedPool = mapOf(
        TR to listOf(
            "Trade winds at their most agreeable. The best it gets.",
            "A pleasant breeze. The reason people live here.",
            "Warm breeze. Atmosphere cooperating. Appreciate it.",
            "Trade winds in pleasant mode. Best the tropics routinely offer."
        ),
        ST to listOf(
            "The atmosphere's version of a shrug. Comfortable today.",
            "Not enough wind to cause problems. Not enough cloud either.",
            "The breeze is manageable. Nothing to add. Fine.",
            "Breezy and mild. High in its most agreeable configuration."
        ),
        TE to listOf(
            "The atmosphere's version of a shrug. Still a shrug.",
            "Not enough wind for problems. Not enough sun to celebrate.",
            "Your umbrella will behave. Mostly.",
            "The most inoffensive weather. Something to be grateful for."
        ),
        OC to listOf(
            "Atlantic breeze. Moderate, sustained, utterly characteristic.",
            "Enough wind to notice. Not enough to matter.",
            "Maritime breeze. Your umbrella will hold. Probably.",
            "Breezy coastal conditions. Atlantic at its least dramatic."
        ),
        NO to listOf(
            "Cool and sharp. Wind chill adds a note the thermometer misses.",
            "Moderate wind in cold air. Windproof layer. Inoffensive day.",
            "Arctic breeze decided to be moderate today. A small favour.",
            "Breezy and cold. Nordic shrug. Still needs a windproof layer."
        )
    )

    override val allClearWarmMood: ZonedPool = mapOf(
        TR to listOf(
            "Afternoon storms aren't here yet. Window exists. Use it.",
            "Tropics briefly not being difficult. Appreciate this.",
            "A good tropical day. File it away for the rainy season.",
            "Nice out. Genuinely. Eat lunch outside."
        ),
        ST to listOf(
            "Go outside. Appreciate it. File this away for January.",
            "A good day. Note the date. They're not all like this.",
            "Make the most of it. Forecast has weekend opinions.",
            "Warm and pleasant. Bar was low. Cleared it impressively."
        ),
        TE to listOf(
            "Go outside. Appreciate it. File this away for January.",
            "A good day. Note the date. They're not all like this.",
            "Make the most of it. Forecast has weekend opinions.",
            "Warm and pleasant. Bar was low. Cleared it impressively."
        ),
        OC to listOf(
            "Go outside. Atlantic having a good day. So are you.",
            "Warm and clear on the coast. This is why people stay.",
            "Note the date. Actually good here. Not endless.",
            "Oceanic climate justifying itself. Today is that occasion."
        ),
        NO to listOf(
            "Warm and clear. Brief window. Go outside. The window is real.",
            "Warm Nordic day. File this away for February.",
            "Nordic warmth, making its case. Go outside and receive it.",
            "Warm and clear. Sun staying out longer. Worth noting."
        )
    )

    override val allClearNeutralMood: ZonedPool = mapOf(
        TR to listOf(
            "Storms possible this afternoon. They always are.",
            "No rain right now. Tropics noted this. Planning accordingly.",
            "Clear. Calm. ITCZ hasn't decided anything yet.",
            "Not actively bad. Tropics withholding judgment."
        ),
        ST to listOf(
            "Clear skies. Don't get used to it.",
            "Not actively bad. That's today's headline.",
            "Weather's fine. Everything else is still your problem.",
            "Go outside. Sun will be back to ignoring you soon enough."
        ),
        TE to listOf(
            "Clear skies. Don't get used to it.",
            "Not actively bad. That's today's headline.",
            "Weather's fine. Everything else is still your problem.",
            "Go outside. Sun will be back to ignoring you soon enough."
        ),
        OC to listOf(
            "Not raining. Oceanic climate briefly suspended its habits.",
            "Clear maritime conditions. Weather not your problem today.",
            "No rain, no wind, no drama. Atlantic resting. So should you.",
            "Fine today. Note that. Fine is not the baseline here."
        ),
        NO to listOf(
            "Clear Nordic skies. Cold but clear. Objectively reasonable.",
            "Not storming. Not snowing. Clear. Nordic winter allows this.",
            "Clear and cold. Polar high doing you a favour. Note it.",
            "Arctic took the day off from being difficult. Note the date."
        )
    )

    override val greyMood: ZonedPool = mapOf(
        TR to listOf(
            "Tropical sky deciding something. Usually decides rain.",
            "Grey tropical sky. Afternoon will have opinions. For now: clear.",
            "Clouds without rain. Tropics pacing themselves.",
            "Could be worse. Could be about to be worse."
        ),
        ST to listOf(
            "Could be worse. Could always be worse.",
            "Not raining, technically. Sky weighing its options.",
            "Grey is just the default some days. Remember that.",
            "Not threatening. Just grey. Forecast uncommitted."
        ),
        TE to listOf(
            "Could be worse. Could always be worse.",
            "Not raining, technically. Sky keeping options open.",
            "Grey is just the default. Try to remember that.",
            "The sun exists. You just can't see it. Comforting, maybe."
        ),
        OC to listOf(
            "Overcast. Just overcast. Oceanic climate understated.",
            "Atlantic stratus settled in. Not raining. Just grey.",
            "Low cloud base. Sky indifferent. No rain. Just grey.",
            "Atlantic overcast. Default state. No precipitation. Just this."
        ),
        NO to listOf(
            "Low sun, persistent cloud, limited light. Not raining. Grey.",
            "Diffuse light is even, if dim. Carry on.",
            "Grey. The Nordic winter default. Just the colour of winter.",
            "Sun is technically there. Not visible. Carry on."
        )
    )
}
