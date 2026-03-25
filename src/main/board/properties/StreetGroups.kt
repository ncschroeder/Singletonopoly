package singletonopoly.board.properties

import singletonopoly.Misc.joinToStringBuilder
import singletonopoly.board.Board
import singletonopoly.board.properties.PropertyManager.getInfo

/**
 * In streetGroupsList, streets are grouped by neighborhood. Each inner list contains streets that are
 * in the same neighborhood.
 */
class StreetGroups private constructor(private val streetGroupsList: List<List<Street>>) {
    /**
     * Returns a StringBuilder (CharSequence) and not a string since the chars in that StringBuilder
     * will be added to the string built in PropertyGroups.getInfo.
     */
    fun getInfo(heading: String): CharSequence =
        streetGroupsList
        .joinToStringBuilder(
            prefix = heading + "\n\n",
            transform = { streetGroup: List<Street> ->
                val nHood = streetGroup.first().nHood
                streetGroup.getInfo(heading = nHood.friendlyName, commonInfo = nHood.streetsCommonInfo)
            },
            separator = "\n\n"
        )
    
    fun isEmpty(): Boolean = streetGroupsList.isEmpty()
    
    fun isNotEmpty(): Boolean = streetGroupsList.isNotEmpty()
    
    fun filter(predicate: (Street) -> Boolean): StreetGroups =
        StreetGroups(
            streetGroupsList.mapNotNull { it.filter(predicate).takeIf { it.isNotEmpty() } }
        )
    
    /**
     * positionsInput contains user input.
     */
    fun filterByPositions(positionsInput: String): StreetGroups {
        val positionsSet: Set<Int> = PropertyGroups.getIntsIn(positionsInput)
        return filter { it.position in positionsSet }
    }
    
    fun toStreetList(): List<Street> = streetGroupsList.flatten()
    
    companion object {
        fun forAllStreets() =
            StreetGroups(Neighborhood.entries.map { it.streets })
    }
}