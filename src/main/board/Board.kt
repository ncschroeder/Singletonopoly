package singletonopoly.board

import singletonopoly.board.properties.*

object Board {
    /**
     * Has values that are NonPropertySpaces and Properties that represent spaces. The keys are the positions
     * of those spaces.
     */
    private val positionsAndSpaces: Map<Int, Any> =
        arrayOf(
            //        Space                     Position
            NonPropertySpace.START,             // 1
            Neighborhood.VAUXHALL.street1,      // 2
            Neighborhood.VAUXHALL.street2,      // 3
            Neighborhood.VAUXHALL.street3,      // 4
            GolfClub.neoshoClub,                // 5
            NonPropertySpace.DRAW_ACTION_CARD,  // 6
            Neighborhood.MONROVIA.street1,      // 7
            SuperStore.newtonStore,             // 8
            Neighborhood.MONROVIA.street2,      // 9
            Neighborhood.MONROVIA.street3,      // 10
            Neighborhood.OZARK.street1,         // 11
            NonPropertySpace.BREAK_TIME,        // 12
            Neighborhood.OZARK.street2,         // 13
            Neighborhood.OZARK.street3,         // 14
            Neighborhood.AUGUSTA.street1,       // 15
            NonPropertySpace.DRAW_ACTION_CARD,  // 16
            Neighborhood.AUGUSTA.street2,       // 17
            GolfClub.granbyClub,                // 18
            Neighborhood.AUGUSTA.street3,       // 19
            Neighborhood.LITTLE_ITALY.street1,  // 20
            NonPropertySpace.VACATION,          // 21
            Neighborhood.LITTLE_ITALY.street2,  // 22
            Neighborhood.LITTLE_ITALY.street3,  // 23
            NonPropertySpace.DRAW_ACTION_CARD,  // 24
            GolfClub.monettClub,                // 25
            NonPropertySpace.GO_ON_VACATION,    // 26
            Neighborhood.GAUSS.street1,         // 27
            Neighborhood.GAUSS.street2,         // 28
            Neighborhood.GAUSS.street3,         // 29
            NonPropertySpace.BREAK_TIME,        // 30
            Neighborhood.TURING.street1,        // 31
            NonPropertySpace.DRAW_ACTION_CARD,  // 32
            Neighborhood.TURING.street2,        // 33
            SuperStore.leibnizStore,            // 34
            Neighborhood.TURING.street3,        // 35
            Neighborhood.HAMPTON.street1,       // 36
            GolfClub.auroraClub,                // 37
            NonPropertySpace.DRAW_ACTION_CARD,  // 38
            Neighborhood.HAMPTON.street2,       // 39
            Neighborhood.HAMPTON.street3        // 40
        )
        .withIndex()
        .associate { (index: Int, space: Any) -> index + 1 to space }
        .onEach { (position, space) ->
            if (space is Property) {
                space.position = position
            }
        }
    
    fun getSpace(position: Int): Any? = positionsAndSpaces[position]
    
    val NUM_SPACES: Int = positionsAndSpaces.size
    
    val VACATION_POSITION: Int =
        positionsAndSpaces
        .asIterable()
        .first { (_, space) -> space === NonPropertySpace.VACATION }
        .let { (position, _) -> position }
    
    /**
     * Has a heading and lines that say the positions of the board and what space is at each position.
     */
    val SPACES_STRING: String
    
    init {
        val spaceToString: (Map.Entry<Int, Any>) -> String =
            { (position, space) ->
                val ending: String =
                    when (space) {
                        is Street -> "${space.name}, ${space.nHood.friendlyName}"
                        
                        is Property -> space.name
                        
                        is NonPropertySpace -> space.friendlyName
                        
                        else -> throw Exception("Board had a space that wasn't a Property nor a NonPropertySpace: $space")
                    }
                
                // Put 1-space indent for lines that start with a single digit.
                "${if (position < 10) " " else ""}$position -> $ending"
            }
        
        SPACES_STRING =
            positionsAndSpaces
            .asIterable()
            .joinToString(prefix = "Board Spaces\n", transform = spaceToString, separator = "\n")
    }
}
