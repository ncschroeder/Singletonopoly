/**
 * Class with lists of properties owned by a player that can have certain things done to them, as well as booleans
 * for checking if those lists are empty.
 *
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
     * Used at instantiation and in situations where a player may have done any of the actions to any of their
     * properties. All lists need to be refreshed since if the contents of 1 list are different from what they
     * used to be, it's possible that other lists will have contents that are different from what they used to be.
     * For example, if a player pawns a property that was in the pawnable list, not only do the contents of the
     * pawnable list need to change but also the contents of the unpawnable list, since the property that just
     * got pawned is now unpawnable.
     */
    fun refresh() {
        val properties: List<Property> = PropertyManager.getPropertiesOwnedBy(player)
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