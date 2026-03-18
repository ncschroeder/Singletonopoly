package singletonopoly.board.properties

import singletonopoly.Misc
import singletonopoly.Misc.joinToStringBuilder
import singletonopoly.board.Board
import singletonopoly.board.properties.PropertyManager.getInfo

class StreetGroups private constructor(private val streetGroupsList: List<List<Street>>) {
    /**
     * Returns a StringBuilder (CharSequence) and not a string since the chars in that StringBuilder
     * will be added to the string built in PropertyGroups.getInfo.
     */
    fun getInfo(heading: String, end: String? = null, sayIfNHoodIsOwned: Boolean = false): CharSequence =
        buildList(capacity = streetGroupsList.size + 2) {
            add(heading)
            
            for (streetGroup: List<Street> in streetGroupsList) {
                val nHood = streetGroup.first().nHood
                val infoHeading: String =
                    nHood.friendlyName.let {
                        if (sayIfNHoodIsOwned && nHood.onePlayerOwnsAllStreets) "$it (Owned)"
                        else it
                    }
                add(streetGroup.getInfo(heading = infoHeading, commonInfo = nHood.streetsCommonInfo))
            }
            
            if (end != null) add(end)
        }
        .joinToStringBuilder(separator = "\n\n")
    
    fun isEmpty(): Boolean = streetGroupsList.isEmpty()
    
    fun isNotEmpty(): Boolean = streetGroupsList.isNotEmpty()
    
    fun filter(predicate: (Street) -> Boolean): StreetGroups =
        StreetGroups(
            streetGroupsList.mapNotNull { it.filter(predicate).takeIf { it.isNotEmpty() } }
        )
    
    fun filterByPositions(positionsInput: String): StreetGroups {
        val positionsSet: Set<Int> = Misc.getIntsIn(positionsInput)
        return filter { it.position in positionsSet }
    }
    
    fun toStreetList(): List<Street> = streetGroupsList.flatten()
    
    companion object {
        fun forAllStreets() =
            StreetGroups(Neighborhood.entries.map { it.streets })
    }
}