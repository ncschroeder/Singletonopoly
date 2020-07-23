package commandline

import java.lang.Exception

data class EntropyCard (val type: String, val message: String?, val value: Int?)

class EntropyDeck {
    private val cards = mutableListOf(
        EntropyCard("player to bank", "message 1", 100),
        EntropyCard("bank to player", "You won a hackathon and receive $100", 100),
        EntropyCard("get off vacation free", null, null),
        EntropyCard("relative position change", "Move ahead 4 spaces", 4),
        EntropyCard("relative position change", "Move back 4 spaces", -4),
        EntropyCard("property maintenance", "You must pay $50 per restaurant for maintenance", null)
    )

    init {
        cards.shuffle()
    }

    private var topIndex = 0

    val topCard get() = cards[topIndex]

    fun moveTopCardToBottom() {
        topIndex = (topIndex + 1) % cards.size
    }

    fun removeGetOffVacationCardAtTop() {
        // This should only be called when a "get off vacation" card is at the top
        if (!cards[topIndex].type.equals("get off vacation free")) {
            throw Exception("The top card is not a get off vacation free card")
        }
        cards.removeAt(topIndex)
    }

    fun insertGetOffVacationCardAtBottom() {
        cards.add(topIndex - 1, EntropyCard("get off vacation free", null, null))
        // Increment topIndex since cards will be shifted after inserting a new one
        topIndex++
    }
}