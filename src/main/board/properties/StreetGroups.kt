package singletonopoly.board.properties

import singletonopoly.Misc.joinToStringBuilder
import singletonopoly.board.properties.PropertyGroups.Companion.getInfo

class StreetGroups private constructor(private val streetGroups: List<List<Street>>) {
    fun getInfo(heading: String, end: String? = null, sayIfNHoodIsOwned: Boolean = false): CharSequence =
        buildList(capacity = streetGroups.size + 2) {
            add(heading)
            
            for (streetList in streetGroups) {
                val nHood = streetList.first().nHood
                val infoHeading: String =
                    nHood.friendlyName.let {
                        if (sayIfNHoodIsOwned && nHood.onePlayerOwnsAllStreets) "$it (Owned)"
                        else it
                    }
                add(streetList.getInfo(heading = infoHeading, commonInfo = nHood.streetsCommonInfo))
            }
            
            if (end != null) add(end)
        }
        .joinToStringBuilder(separator = "\n\n")
    
    fun isEmpty(): Boolean = streetGroups.isEmpty()
    
    fun isNotEmpty(): Boolean = streetGroups.isNotEmpty()
    
    fun filter(predicate: (Street) -> Boolean): StreetGroups =
        StreetGroups(
            streetGroups
            .map { it.filter(predicate) }
            .filter { it.isNotEmpty() }
        )
    
    fun filterByPositions(positionsInput: String): StreetGroups {
        val positionsSet: Set<Int> = PropertyGroups.getPositions(positionsInput)
        return filter { it.position in positionsSet }
    }
    
    fun toStreetList(): List<Street> = streetGroups.flatten()
    
    companion object {
        fun forAllStreets() =
            StreetGroups(Neighborhood.entries.map { it.streets })
    }
}