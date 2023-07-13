/**
 * Used for querying and updating properties, though this object isn't the only thing that does those actions.
 */
object PropertyManager {
    init {
        /*
        The Board is an object declaration (singleton), just like the PropertyManager. Access a property on the
        Board here so that when we create ActionCards and call getPropertyPosition below, both the PropertyManager
        and the Board will get initialized. The Board sets the property positions so initializing the Board will
        allow getPropertyPosition to return accurate positions.
        */
        Board.numberOfSpaces
    }
    
    private val allProperties: List<Property> =
        SuperStore.bothStores + GolfClub.allClubs + Neighborhood.getAllStreets()
    
    /**
     * Throws an exception if there's no property with the specified name.
     */
    fun getPropertyPosition(name: String): Int =
        allProperties.first { it.name == name }.position
    
    fun getPropertiesOwnedBy(player: Player): List<Property> =
        allProperties.filter { it.owner == player }
    
    /**
     * Returns a string that contains headings and info about all properties. If includeAll is true then more
     * info that's less important is included.
     */
    fun getPropertiesInfo(includeAll: Boolean): String =
        buildList<CharSequence>(capacity = 5) {
            add("${if (includeAll) "All" else "Basic"} Info for All Properties")
            if (includeAll) {
                add(
                    SuperStore.store1.run {
                        "For Super Stores and Golf Clubs: Purchase Price: $$purchasePrice, " +
                        "Pawn Price: $$pawnPrice, Unpawn Price: $$unpawnPrice"
                    }
                )
            }
            add(SuperStore.getStoresInfo(includeAll))
            add(GolfClub.getClubsInfo(includeAll))
            add(Neighborhood.getStreetsInfo(includeAll))
        }
        .joinToString(separator = "\n\n")
    
    fun makePropertiesUnowned(player: Player) {
        getPropertiesOwnedBy(player).forEach { it.makeUnowned() }
    }
    
    fun makePropertiesUnowned() {
        allProperties.forEach { it.makeUnowned() }
    }
    
    fun transferOwnership(currentOwner: Player, newOwner: Player) {
        getPropertiesOwnedBy(currentOwner).forEach { it.owner = newOwner }
    }
}