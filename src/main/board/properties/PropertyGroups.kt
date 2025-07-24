package singletonopoly.board.properties

import singletonopoly.Misc.joinToStringBuilder
import singletonopoly.board.Board
import singletonopoly.players.Player

class PropertyGroups private constructor(
    val superStores: List<SuperStore>,
    val golfClubs: List<GolfClub>,
    val streetGroups: StreetGroups
) {
    fun getInfo(heading: String, end: String? = null, sayIfNHoodIsOwned: Boolean = false): String =
        buildList(capacity = 5) {
            add(heading)
            
            if (superStores.isNotEmpty() || golfClubs.isNotEmpty()) {
                add(SUPER_STORES_AND_GOLF_CLUBS_COMMON_INFO)
            }
            
            if (superStores.isNotEmpty()) {
                add(superStores.getInfo(heading = "Super Stores", commonInfo = SuperStore.FEES_INFO))
            }
            
            if (golfClubs.isNotEmpty()) {
                add(golfClubs.getInfo(heading = "Golf Clubs", commonInfo = GolfClub.FEES_INFO))
            }
            
            if (streetGroups.isNotEmpty()) {
                add(streetGroups.getInfo(heading = "Streets", sayIfNHoodIsOwned = sayIfNHoodIsOwned))
            }
            
            if (end != null) add(end)
        }
        .joinToString(separator = "\n\n")
    
    fun isEmpty(): Boolean =
        superStores.isEmpty() && golfClubs.isEmpty() && streetGroups.isEmpty()
    
    fun isNotEmpty(): Boolean = !isEmpty()
    
    fun filter(predicate: (Property) -> Boolean): PropertyGroups =
        PropertyGroups(
            superStores = superStores.filter(predicate),
            golfClubs = golfClubs.filter(predicate),
            streetGroups = streetGroups.filter(predicate)
        )
    
    fun filterByPositions(positionsInput: String): PropertyGroups {
        val positionsSet: Set<Int> = getPositions(positionsInput)
        return filter { it.position in positionsSet }
    }
    
    fun toPropList(): List<Property> =
        superStores + golfClubs + streetGroups.toStreetList()
    
    companion object {
        val propGroupsForAllProps =
            PropertyGroups(
                superStores = SuperStore.bothStores,
                golfClubs = GolfClub.allClubs,
                streetGroups = StreetGroups.forAllStreets()
            )
        
        val allPropsList: List<Property> = propGroupsForAllProps.toPropList()
        
        fun forPropsOwnedBy(player: Player): PropertyGroups =
            propGroupsForAllProps.filter { it.owner === player }
        
        /**
         * commonInfo is for info common to all the props in the list.
         */
        fun List<Property>.getInfo(heading: String, commonInfo: String): CharSequence =
            buildList(capacity = size + 2) {
                add(heading)
                add(commonInfo)
                for (prop in this@getInfo) {
                    add(prop.getInfo())
                }
            }
            .joinToStringBuilder(separator = "\n")
        
        private val SUPER_STORES_AND_GOLF_CLUBS_COMMON_INFO: String =
            SuperStore.newtonStore.run {
                "For Super Stores and Golf Clubs: Purchase Price: $$purchasePrice, " +
                "Pawn Gain: $$pawnGain, Unpawn Price: $$unpawnPrice"
            }
        
        /**
         * Returns a set of whitespace-separated ints in positionsInput. Non-int values are ignored.
         */
        fun getPositions(positionsInput: String): Set<Int> =
            positionsInput
            .split(Regex("\\s+"))
            .mapNotNullTo(HashSet()) { it.toIntOrNull() }
        
        fun getInfoForAllProps(): String =
            propGroupsForAllProps.getInfo(heading = "Info for all properties")
        
        /*
        In the Game.askAboutSeeingPropInfo function, this function is called without passing a props arg.
        
        In the Game.askAboutTrading function, the PropertyGroups.forPropsOwnedBy function gets called
        close to the beginning and then the info for the props in the returned PropertyGroups object is
        possibly used later in the askAboutTrading function. That PropertyGroups object is passed as an
        arg to a call to this function.
        
        This function allows the text from the strings below to be used in these situations.
        */
        fun getInfoForPropsOwnedBy(
            player: Player,
            props: PropertyGroups = PropertyGroups.forPropsOwnedBy(player)
        ): String =
            if (props.isEmpty()) {
                "${player.name} doesn't own any properties."
            } else {
                props.getInfo(heading = "Info for properties owned by ${player.name}")
            }
        
        fun getPropPosition(name: String): Int {
            /*
            At the beginning of the Game class, an ActionDeck is created and ActionCards are going to be
            created as part of that. Some ActionCards require the player to move to a property and one
            of its properties is propPosition and that's found by calling this function. This function
            accesses the position on the found Property. The Board object sets the positions of Property
            objects during the Board's initialization, which isn't going to be done when this function
            is called unless we trigger the initialization. 1 way of doing that is by accessing a property.
            */
            Board.NUM_SPACES
            
            return allPropsList.first { it.name == name }.position
        }
        
        private fun getListOfPropsOwnedBy(player: Player): List<Property> =
            allPropsList.filter { it.owner === player }
        
        fun getNumRestaurantsOwnedBy(player: Player): Int =
            getListOfPropsOwnedBy(player)
            .filterIsInstance<Street>()
            .sumOf { it.numRestaurants }
        
        fun makePropertiesUnowned(owner: Player) {
            getListOfPropsOwnedBy(owner).forEach { it.makeUnowned() }
        }
        
        fun makePropertiesUnowned() {
            allPropsList.forEach { it.makeUnowned() }
        }
        
        fun transferOwnership(curOwner: Player, newOwner: Player) {
            getListOfPropsOwnedBy(curOwner).forEach { it.owner = newOwner }
        }
    }
}
