package singletonopoly.board.properties

import singletonopoly.Misc.joinToStringBuilder
import singletonopoly.board.Board
import singletonopoly.players.Player

object PropertyManager {
    val SUPER_STORES_AND_GOLF_CLUBS_COMMON_INFO: String =
        SuperStore.newtonStore.run {
            "For Super Stores and Golf Clubs: Purchase Price: $$purchasePrice, " +
            "Pawn Gain: $$pawnGain, Unpawn Price: $$unpawnPrice"
        }
    
    /**
     * commonInfo is for info common to all the props in the list.
     *
     * Returns a StringBuilder (CharSequence) and not a string since the chars in that StringBuilder
     * will be added to the StringBuilder created in StreetGroups.getInfo or the string built in
     * PropertyGroups.getInfo.
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
    
    private val allProps: List<Property> = PropertyGroups.forAllProps.toPropList()
    
    fun getPropPosition(name: String): Int {
        /*
        At the beginning of the Game class, an ActionDeck is created and ActionCards are created as
        part of that. The MoveToPropertyCard subclass of ActionCard has a propPosition property and
        that's found by calling this function. This function accesses the position on the found Property.
        The Board object sets the positions of Property objects during the Board's instantiation, which
        isn't going to be done by the time this function is called for the 1st time. We can trigger the
        instantiation by accessing a property.
         */
        Board.NUM_SPACES
        
        return allProps.first { it.name == name }.position
    }
    
    private fun getPropsOwnedBy(player: Player): List<Property> =
        allProps.filter { it.owner === player }
    
    fun getNumRestaurantsOwnedBy(player: Player): Int =
        getPropsOwnedBy(player)
        .sumOf { if (it is Street) it.numRestaurants else 0 }
    
    fun makePropsUnowned() {
        allProps.forEach { it.makeUnowned() }
    }
    
    fun makePropsUnowned(owner: Player) {
        getPropsOwnedBy(owner).forEach { it.makeUnowned() }
    }
    
    fun transferOwnership(curOwner: Player, newOwner: Player) {
        getPropsOwnedBy(curOwner).forEach { it.owner = newOwner }
    }
}
