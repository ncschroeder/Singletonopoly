package singletonopoly.board.properties

import singletonopoly.Misc.joinToStringBuilder
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
    
    private fun List<Property>.toCharSequence(start: String, extraInfo: String): CharSequence =
        buildList {
            add(start)
            add(extraInfo)
            for (prop in this@toCharSequence) {
                add(prop.getBasicInfo())
            }
        }
        .joinToStringBuilder(separator = "\n")
    
    private fun getPositions(positionsInput: String): Set<Int> =
        positionsInput
        .split(Regex("\\s+"))
        .mapNotNullTo(HashSet()) { it.toIntOrNull() }
    
    class StreetGroups(private val streets: List<List<Street>>): Iterable<Street> {
        fun filter(predicate: (Street) -> Boolean): StreetGroups =
            StreetGroups(
                streets
                .map { it.filter(predicate) }
                .filter { it.isNotEmpty() }
            )
        
        fun filterByPositions(positionsInput: String): StreetGroups {
            val positionsSet: Set<Int> = getPositions(positionsInput)
            return filter { it.position in positionsSet }
        }
        
        fun toCharSequence(start: String, end: String? = null, sayIfNHoodIsOwned: Boolean = false): CharSequence =
            buildList {
                add(start)
                
                for (streetList in streets) {
                    val nHood = streetList.first().neighborhood
                    val charSeqStart: String =
                        nHood.friendlyName.let {
                            if (sayIfNHoodIsOwned && nHood.onePlayerOwnsAllStreets) "$it (Owned)"
                            else it
                        }
                    add(streetList.toCharSequence(start = charSeqStart, extraInfo = nHood.extraInfo))
                }
                
                if (end != null) add(end)
            }
            .joinToStringBuilder(separator = "\n\n")
        
        private val flattenedStreets: List<Street> by lazy { streets.flatten() }
        
        override fun iterator(): Iterator<Street> = flattenedStreets.iterator()
    }
    
    class PropertyGroups private constructor(
        val stores: List<SuperStore>,
        val clubs: List<GolfClub>,
        val streetGroups: StreetGroups
    ): Iterable<Property> {
        fun toString(start: String?, end: String? = null, sayIfNHoodIsOwned: Boolean = false) =
            buildList(capacity = 3) {
                if (start != null) add(start)
                add(extraInfo)
                
                if (stores.isNotEmpty()) {
                    add(
                        stores.toCharSequence(
                            start = "Super Stores",
                            extraInfo = SuperStore.EXTRA_INFO
                        )
                    )
                }
                
                if (clubs.isNotEmpty()) {
                    add(
                        clubs.toCharSequence(
                            start = "Golf Clubs",
                            extraInfo = GolfClub.extraInfo
                        )
                        
                    )
                }
                
                if (streetGroups.any()) {
                    add(streetGroups.toCharSequence(start = "Streets", sayIfNHoodIsOwned = sayIfNHoodIsOwned))
                }
                
                if (end != null) add(end)
            }
            .joinToString(separator = "\n\n")
        
        override fun toString() = toString(start = null, end = null)
        
        fun filter(predicate: (Property) -> Boolean): PropertyGroups =
            PropertyGroups(
                stores = stores.filter(predicate),
                clubs = clubs.filter(predicate),
                streetGroups = streetGroups.filter(predicate)
            )
            
        fun filterByPositions(positionsInput: String): PropertyGroups {
            val positionsSet: Set<Int> = getPositions(positionsInput)
            return filter { it.position in positionsSet }
        }
        
        private val combinedProperties: List<Property> by lazy {
            stores + clubs + streetGroups
        }
        
        override fun iterator(): Iterator<Property> = combinedProperties.iterator()
        
        companion object {
            val forAllProps =
                PropertyGroups(
                    stores = SuperStore.bothStores,
                    clubs = GolfClub.allClubs,
                    streetGroups = StreetGroups(Neighborhood.entries.map { it.streets })
                )
            
            fun forPropsOwnedBy(player: Player): PropertyGroups =
                forAllProps.filter { it.owner === player }
        }
    }
    
    
    class ActionPropertyGroups private constructor(val owner: Player) {
        lateinit var pawnable: PropertyGroups
            private set
        
        lateinit var unpawnable: PropertyGroups
            private set
        
        lateinit var restaurantAddable: StreetGroups
            private set
        
        lateinit var restaurantRemovable: StreetGroups
            private set
        
        fun refresh() {
            val ownerPropertyGroups = PropertyGroups.forPropsOwnedBy(owner)
            pawnable = ownerPropertyGroups.filter { it.canBePawned }
            unpawnable = ownerPropertyGroups.filter { it.canBeUnpawned }
            restaurantAddable = ownerPropertyGroups.streetGroups.filter { it.restaurantCanBeAdded }
            restaurantRemovable = ownerPropertyGroups.streetGroups.filter { it.restaurantCanBeRemoved }
        }
        
        init {
            refresh()
        }
        
        val hasPawnable: Boolean
            get() = pawnable.any()
        
        val hasUnpawnable: Boolean
            get() = unpawnable.any()
        
        val hasRestaurantAddable: Boolean
            get() = restaurantAddable.any()
        
        val hasRestaurantRemovable: Boolean
            get() = restaurantRemovable.any()
        
        companion object {
            fun forPropsOwnedBy(player: Player) = ActionPropertyGroups(player)
        }
    }
    
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
