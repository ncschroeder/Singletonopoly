package singletonopoly.board.properties

import singletonopoly.players.Player

/**
 * This class is used when asking a player about pre-roll actions and getting money. Options for doing
 * certain actions are conditionally shown depending on the values of the boolean properties. If a
 * player can do one of  the actions and decides to, the corresponding PropertyGroups or StreetGroups
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
     * Used at instantiation and in situations where a player does any action to any of their properties.
     * All group objects need to be refreshed since if the contents of 1 object are different from what
     * they used to be, then it's possible that other objects should have contents that are different
     * from what they used to be. For example, if a player pawns a property that was in the pawnable
     * object, then not only do the contents of the pawnable object need to change but also the contents
     * of the unpawnable object, since the property that just got pawned is now unpawnable.
     */
    fun refresh() {
        val propGroupsForAllProps = PropertyGroups.propGroupsForAllProps
        
        pawnable =
            propGroupsForAllProps.filter { it.owner === owner && it.canBePawned }
        
        unpawnable =
            propGroupsForAllProps.filter { it.owner === owner && it.canBeUnpawned }
        
        restaurantAddable =
            propGroupsForAllProps.streetGroups.filter {
                it.owner === owner && it.restaurantCanBeAdded
            }
        
        restaurantRemovable =
            propGroupsForAllProps.streetGroups.filter {
                it.owner === owner && it.restaurantCanBeRemoved
            }
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