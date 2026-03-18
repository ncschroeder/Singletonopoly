package singletonopoly.board.properties

import singletonopoly.players.Player

/**
 * This class is used when asking a player about pre-roll actions and getting money. Options for doing
 * certain actions are conditionally shown depending on the values of the boolean properties. If a
 * player can do one of the actions and decides to, the corresponding PropertyGroups or StreetGroups
 * is passed as an arg for the corresponding function for that action.
 */
class ActionPropertyGroups private constructor(val owner: Player) {
    lateinit var pawnable: PropertyGroups
        private set
    
    lateinit var unpawnable: PropertyGroups
        private set
    
    lateinit var restaurantAddable: StreetGroups
        private set
    
    lateinit var restaurantRemovable: StreetGroups
        private set
    
    /**
     * Used at instantiation and after the owner does an action to one of their properties. All groups
     * objects need to be updated since if the state of 1 object is different from what it used to be,
     * then it's possible that other objects should have a state that's different from what it used to
     * be. For example, if a player pawns a property that was in the pawnable PropertyGroups, then not
     * only does the pawnable PropertyGroups needs to be updated but also the unpawnable PropertyGroups,
     * since the property that just got pawned is now unpawnable.
     */
    fun refresh() {
        val ownerPropGroups = PropertyGroups.forPropsOwnedBy(owner)
        pawnable = ownerPropGroups.filter { it.canBePawned }
        unpawnable = ownerPropGroups.filter { it.canBeUnpawned }
        restaurantAddable = ownerPropGroups.streetGroups.filter { it.restaurantCanBeAdded }
        restaurantRemovable = ownerPropGroups.streetGroups.filter { it.restaurantCanBeRemoved }
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
    
    companion object {
        fun forPropsOwnedBy(player: Player) = ActionPropertyGroups(player)
    }
}