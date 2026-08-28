package com.fureva.veripup.config

/**
 * Static geographic reference data used to populate state and city selectors
 * throughout the breeder onboarding and profile flows.
 *
 * Covers all 50 US states. Each state maps to a curated list of its seven
 * most populous cities, giving users meaningful location options without
 * requiring a full address database.
 */
object StateCityConfig {
    /**
     * A mapping from two-letter US state codes (upper-case) to an ordered list
     * of the state's top cities by population.
     *
     * Keys are standard USPS state abbreviations (e.g. `"AK"`, `"CA"`).
     * Values are lists of city names ordered roughly by population size,
     * largest first.
     */
    val topCitiesByState: Map<String, List<String>> = mapOf(
        "AL" to listOf("Huntsville", "Birmingham", "Montgomery", "Mobile", "Tuscaloosa", "Hoover", "Dothan"),
        "AK" to listOf("Anchorage", "Fairbanks", "Juneau", "Badger", "Knik-Fairview", "College", "Wasilla"),
        "AZ" to listOf("Phoenix", "Tucson", "Mesa", "Chandler", "Gilbert", "Glendale", "Scottsdale"),
        "AR" to listOf("Little Rock", "Fayetteville", "Fort Smith", "Springdale", "Jonesboro", "Rogers", "Conway"),
        "CA" to listOf("Los Angeles", "San Diego", "San Jose", "San Francisco", "Fresno", "Sacramento", "Long Beach"),
        "CO" to listOf("Denver", "Colorado Springs", "Aurora", "Fort Collins", "Lakewood", "Thornton", "Arvada"),
        "CT" to listOf("Bridgeport", "Stamford", "New Haven", "Hartford", "Waterbury", "Norwalk", "Danbury"),
        "DE" to listOf("Wilmington", "Dover", "Newark", "Middletown", "Smyrna", "Milford", "Seaford"),
        "FL" to listOf("Jacksonville", "Miami", "Tampa", "Orlando", "St. Petersburg", "Hialeah", "Tallahassee"),
        "GA" to listOf("Atlanta", "Columbus", "Augusta", "Macon", "Savannah", "Athens", "Sandy Springs"),
        "HI" to listOf("Honolulu", "East Honolulu", "Pearl City", "Hilo", "Kailua", "Waipahu", "Kaneohe"),
        "ID" to listOf("Boise", "Meridian", "Nampa", "Idaho Falls", "Pocatello", "Caldwell", "Coeur d'Alene"),
        "IL" to listOf("Chicago", "Aurora", "Joliet", "Naperville", "Rockford", "Elgin", "Springfield"),
        "IN" to listOf("Indianapolis", "Fort Wayne", "Evansville", "South Bend", "Carmel", "Fishers", "Bloomington"),
        "IA" to listOf("Des Moines", "Cedar Rapids", "Davenport", "Sioux City", "Iowa City", "Waterloo", "Ames"),
        "KS" to listOf("Wichita", "Overland Park", "Kansas City", "Olathe", "Topeka", "Lawrence", "Shawnee"),
        "KY" to listOf("Louisville", "Lexington", "Bowling Green", "Owensboro", "Covington", "Richmond", "Georgetown"),
        "LA" to listOf("New Orleans", "Baton Rouge", "Shreveport", "Lafayette", "Lake Charles", "Kenner", "Bossier City"),
        "ME" to listOf("Portland", "Lewiston", "Bangor", "South Portland", "Auburn", "Biddeford", "Sanford"),
        "MD" to listOf("Baltimore", "Frederick", "Rockville", "Gaithersburg", "Bowie", "Hagerstown", "Annapolis"),
        "MA" to listOf("Boston", "Worcester", "Springfield", "Cambridge", "Lowell", "Brockton", "Quincy"),
        "MI" to listOf("Detroit", "Grand Rapids", "Warren", "Sterling Heights", "Ann Arbor", "Lansing", "Dearborn"),
        "MN" to listOf("Minneapolis", "St. Paul", "Rochester", "Duluth", "Bloomington", "Brooklyn Park", "Plymouth"),
        "MS" to listOf("Jackson", "Gulfport", "Southaven", "Hattiesburg", "Biloxi", "Meridian", "Tupelo"),
        "MO" to listOf("Kansas City", "St. Louis", "Springfield", "Columbia", "Independence", "Lee's Summit", "O'Fallon"),
        "MT" to listOf("Billings", "Missoula", "Great Falls", "Bozeman", "Butte", "Helena", "Kalispell"),
        "NE" to listOf("Omaha", "Lincoln", "Bellevue", "Grand Island", "Kearney", "Fremont", "Hastings"),
        "NV" to listOf("Las Vegas", "Henderson", "Reno", "North Las Vegas", "Sparks", "Carson City", "Fernley"),
        "NH" to listOf("Manchester", "Nashua", "Concord", "Derry", "Rochester", "Dover", "Keene"),
        "NJ" to listOf("Newark", "Jersey City", "Paterson", "Elizabeth", "Edison", "Woodbridge", "Lakewood"),
        "NM" to listOf("Albuquerque", "Las Cruces", "Rio Rancho", "Santa Fe", "Roswell", "Farmington", "Clovis"),
        "NY" to listOf("New York", "Buffalo", "Yonkers", "Rochester", "Syracuse", "Albany", "New Rochelle"),
        "NC" to listOf("Charlotte", "Raleigh", "Greensboro", "Durham", "Winston-Salem", "Fayetteville", "Cary"),
        "ND" to listOf("Fargo", "Bismarck", "Grand Forks", "Minot", "West Fargo", "Williston", "Dickinson"),
        "OH" to listOf("Columbus", "Cleveland", "Cincinnati", "Toledo", "Akron", "Dayton", "Parma"),
        "OK" to listOf("Oklahoma City", "Tulsa", "Norman", "Broken Arrow", "Lawton", "Edmond", "Moore"),
        "OR" to listOf("Portland", "Eugene", "Salem", "Gresham", "Hillsboro", "Bend", "Beaverton"),
        "PA" to listOf("Philadelphia", "Pittsburgh", "Allentown", "Reading", "Erie", "Scranton", "Bethlehem"),
        "RI" to listOf("Providence", "Warwick", "Cranston", "Pawtucket", "East Providence", "Woonsocket", "Coventry"),
        "SC" to listOf("Charleston", "Columbia", "North Charleston", "Mount Pleasant", "Rock Hill", "Greenville", "Summerville"),
        "SD" to listOf("Sioux Falls", "Rapid City", "Aberdeen", "Brookings", "Watertown", "Mitchell", "Yankton"),
        "TN" to listOf("Nashville", "Memphis", "Knoxville", "Chattanooga", "Clarksville", "Murfreesboro", "Franklin"),
        "TX" to listOf("Houston", "San Antonio", "Dallas", "Austin", "Fort Worth", "El Paso", "Arlington"),
        "UT" to listOf("Salt Lake City", "West Valley City", "West Jordan", "Provo", "Orem", "Sandy", "St. George"),
        "VT" to listOf("Burlington", "South Burlington", "Rutland", "Essex", "Colchester", "Bennington", "Milton"),
        "VA" to listOf("Virginia Beach", "Chesapeake", "Norfolk", "Richmond", "Newport News", "Alexandria", "Hampton"),
        "WA" to listOf("Seattle", "Spokane", "Tacoma", "Vancouver", "Bellevue", "Kent", "Everett"),
        "WV" to listOf("Charleston", "Huntington", "Morgantown", "Parkersburg", "Wheeling", "Martinsburg", "Fairmont"),
        "WI" to listOf("Milwaukee", "Madison", "Green Bay", "Kenosha", "Racine", "Appleton", "Waukesha"),
        "WY" to listOf("Cheyenne", "Casper", "Laramie", "Gillette", "Rock Springs", "Sheridan", "Evanston")
    )

    /**
     * Returns the list of supported cities for the given US state code.
     *
     * The lookup is case-insensitive — `"ak"` and `"AK"` both work.
     *
     * @param stateCode Two-letter US state abbreviation (e.g. `"AK"`).
     * @return The ordered list of cities for the state, or an empty list if
     *   the state code is not recognised.
     */
    fun citiesFor(stateCode: String): List<String> = topCitiesByState[stateCode.uppercase()] ?: emptyList()
}
