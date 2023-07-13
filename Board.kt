object Board {
    /**
     * Has values that are NonPropertySpaces and Properties that represent spaces. The keys are the positions
     * of those spaces.
     */
    private val positionsAndSpaces: Map<Int, Any> =
        listOf(
            //        Space                     Position
            NonPropertySpace.START,             // 1
            Neighborhood.VAUXHALL.street1,      // 2
            Neighborhood.VAUXHALL.street2,      // 3
            Neighborhood.VAUXHALL.street3,      // 4
            GolfClub.club1,                     // 5
            Neighborhood.MONROVIA.street1,      // 6
            NonPropertySpace.DRAW_ACTION_CARD,  // 7
            Neighborhood.MONROVIA.street2,      // 8
            Neighborhood.MONROVIA.street3,      // 9
            SuperStore.store1,                  // 10
            NonPropertySpace.BREAK_TIME,        // 11
            Neighborhood.OZARK.street1,         // 12
            Neighborhood.OZARK.street2,         // 13
            NonPropertySpace.DRAW_ACTION_CARD,  // 14
            Neighborhood.OZARK.street3,         // 15
            Neighborhood.AUGUSTA.street1,       // 16
            NonPropertySpace.VACATION,          // 17
            Neighborhood.AUGUSTA.street2,       // 18
            GolfClub.club2,                     // 19
            Neighborhood.AUGUSTA.street3,       // 20
            Neighborhood.LITTLE_ITALY.street1,  // 21
            NonPropertySpace.DRAW_ACTION_CARD,  // 22
            Neighborhood.LITTLE_ITALY.street2,  // 23
            Neighborhood.LITTLE_ITALY.street3,  // 24
            GolfClub.club3,                     // 25
            Neighborhood.GAUSS.street1,         // 26
            Neighborhood.GAUSS.street2,         // 27
            NonPropertySpace.DRAW_ACTION_CARD,  // 28
            Neighborhood.GAUSS.street3,         // 29
            NonPropertySpace.BREAK_TIME,        // 30
            NonPropertySpace.GO_ON_VACATION,    // 31
            Neighborhood.TURING.street1,        // 32
            Neighborhood.TURING.street2,        // 33
            SuperStore.store2,                  // 34
            NonPropertySpace.DRAW_ACTION_CARD,  // 35
            Neighborhood.TURING.street3,        // 36
            Neighborhood.HAMPTON.street1,       // 37
            GolfClub.club4,                     // 38
            NonPropertySpace.DRAW_ACTION_CARD,  // 39
            Neighborhood.HAMPTON.street2,       // 40
            Neighborhood.HAMPTON.street3        // 41
        )
        .associateByPosition()
    
    /**
     * Returns the space object at the position specified or null if that position is < 1 or > the number of spaces.
     */
    fun getSpace(position: Int): Any? = positionsAndSpaces[position]
    
    val numberOfSpaces: Int = positionsAndSpaces.size
    
    var vacationPosition: Int
        private set
    
    /**
     * Has a heading and lines that say the positions of the board and what space is at each position.
     */
    val spacesString: String
    
    override fun toString() = spacesString
    
    init {
        val spaceToString: (Map.Entry<Int, Any>) -> String =
            { (position, space) ->
                val ending: String =
                    when (space) {
                        is Street -> "${space.name}, ${space.neighborhood.friendlyName}"
                        is Property -> space.name
                        is NonPropertySpace -> space.friendlyName
                        else -> throw Exception("Board had a space that wasn't a Property nor a NonPropertySpace: $space")
                    }
                
                // Put 1 space indent for lines that start with a single digit
                "${if (position < 10) " " else ""}$position -> $ending"
            }
        
        spacesString =
            positionsAndSpaces
            .asIterable()
            .joinToString(
                prefix = "Board Spaces\n",
                transform = spaceToString,
                separator = "\n"
            )
        
        
        vacationPosition = 0
        for ((position: Int, space: Any) in positionsAndSpaces) {
            when (space) {
                is Property -> space.position = position
                NonPropertySpace.VACATION -> vacationPosition = position
            }
        }
    }
}