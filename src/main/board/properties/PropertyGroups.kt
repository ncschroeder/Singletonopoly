package singletonopoly.board.properties

import singletonopoly.Misc
import singletonopoly.board.properties.PropertyManager.getInfo
import singletonopoly.players.Player

class PropertyGroups private constructor(
    val superStores: List<SuperStore>,
    val golfClubs: List<GolfClub>,
    val streetGroups: StreetGroups
) {
    fun getInfo(heading: String, end: String? = null, sayIfNHoodIsOwned: Boolean = false): String =
        buildList(capacity = 6) {
            add(heading)
            
            if (superStores.isNotEmpty() || golfClubs.isNotEmpty()) {
                add(PropertyManager.SUPER_STORES_AND_GOLF_CLUBS_COMMON_INFO)
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
    
    /**
     * Used in askAboutSeeingPropInfo and askAboutTrading on PropertyGroups objects returned by
     * PropertyGroups.forPropsOwnedBy.
     */
    fun getInfo(ownerOfProps: Player) =
        getInfo(heading = "Info for Properties Owned by ${ownerOfProps.name}")

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
        val positionsSet: Set<Int> = Misc.getIntsIn(positionsInput)
        return filter { it.position in positionsSet }
    }
    
    fun toPropList(): List<Property> =
        superStores + golfClubs + streetGroups.toStreetList()
    
    companion object {
        val forAllProps =
            PropertyGroups(
                superStores = SuperStore.bothStores,
                golfClubs = GolfClub.allClubs,
                streetGroups = StreetGroups.forAllStreets()
            )
        
        fun forPropsOwnedBy(player: Player): PropertyGroups =
            forAllProps.filter { it.owner === player }
    }
}
