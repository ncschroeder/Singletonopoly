package singletonopoly.board.properties

import singletonopoly.board.Board
import singletonopoly.players.Player

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
        Board.numSpaces
    }
    
    private val allProperties: List<Property> =
        SuperStore.bothStores + GolfClub.allClubs + Neighborhood.getAllStreets()
    
    /**
     * Throws an exception if there's no property with the specified name.
     */
    fun getPropertyPosition(name: String): Int =
        allProperties.first { it.name == name }.position
    
    fun getPropertiesOwnedBy(player: Player): List<Property> =
        allProperties.filter { it.owner === player }
    
    private val extraInfo: String =
        SuperStore.newtonStore.run {
            "For Super Stores and Golf Clubs: Purchase Price: $$purchasePrice, " +
            "Pawn Gain: $$pawnGain, Unpawn Price: $$unpawnPrice"
        }
    
    /**
     * Returns a string that contains headings and info about all properties. If includeAll is true then more
     * info that's less important is included.
     */
    fun getInfoForAllProperties(includeAllInfo: Boolean): String =
        buildList(capacity = 5) {
            add("${if (includeAllInfo) "All" else "Basic"} Info for All Properties")
            if (includeAllInfo) add(extraInfo)
            add(SuperStore.getStoresInfo(includeAllInfo))
            add(GolfClub.getClubsInfo(includeAllInfo))
            add(Neighborhood.getStreetsInfo(includeAllInfo))
        }
        .joinToString(separator = "\n\n")
    
    fun getInfoForPropertiesOwnedBy(
        player: Player,
        properties: List<Property> = getPropertiesOwnedBy(player)
    ): String =
        if (properties.isEmpty()) {
            "${player.name} doesn't own any properties."
        } else {
            properties.joinToString(
                prefix = "Basic info for properties owned by ${player.name}\n\n",
                transform = { it.getBasicInfo() },
                separator = "\n"
            )
        }
    
    fun getNumRestaurantsOwnedBy(player: Player): Int =
        getPropertiesOwnedBy(player)
        .filterIsInstance<Street>()
        .sumOf { it.numRestaurants }
    
    fun makePropertiesUnowned(owner: Player) {
        getPropertiesOwnedBy(owner).forEach { it.makeUnowned() }
    }
    
    fun makePropertiesUnowned() {
        allProperties.forEach { it.makeUnowned() }
    }
    
    fun transferOwnership(curOwner: Player, newOwner: Player) {
        getPropertiesOwnedBy(curOwner).forEach { it.owner = newOwner }
    }
    
    
    /**
     * This class is used when asking a player about pre-roll actions and getting money. Options for doing certain
     * actions are conditionally shown depending on the values of the boolean properties. If a player can do 1 of
     * the actions and decides to, the corresponding list is passed as an arg for the corresponding function for
     * that action.
     */
    class PropertyLists(private val player: Player) {
        lateinit var pawnable: List<Property>
            private set
        
        lateinit var unpawnable: List<Property>
            private set
        
        lateinit var restaurantAddable: List<Street>
            private set
        
        lateinit var restaurantRemovable: List<Street>
            private set
        
        /**
         * Used at instantiation and in situations where a player does any action to any of their properties.
         * All lists need to be refreshed since if the contents of 1 list are different from what they used to be,
         * then it's possible that other lists will have contents that are different from what they used to be.
         * For example, if a player pawns a property that was in the pawnable list, not only do the contents of
         * the pawnable list need to change but also the contents of the unpawnable list, since the property
         * that just got pawned is now unpawnable.
         *
         */
        fun refresh() {
            val properties: List<Property> = getPropertiesOwnedBy(player)
            pawnable = properties.filter { it.canBePawned }
            unpawnable = properties.filter { it.canBeUnpawned }
            val streets: List<Street> = properties.filterIsInstance<Street>()
            restaurantAddable = streets.filter { it.restaurantCanBeAdded }
            restaurantRemovable = streets.filter { it.restaurantCanBeRemoved }
        }
        
        init {
            refresh()
        }
        
        val hasPawnable: Boolean
            get() = pawnable.isNotEmpty()
        
        val hasUnpawnable: Boolean
            get() = unpawnable.isNotEmpty()
        
        val hasRestaurantAddable: Boolean
            get() = restaurantAddable.isNotEmpty()
        
        val hasRestaurantRemovable: Boolean
            get() = restaurantRemovable.isNotEmpty()
    }
}
