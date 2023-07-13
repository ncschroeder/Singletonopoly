/**
 * This enum allows us to have objects that represent non-property spaces. We can easily place these on the board
 * and then when a player lands on a space, we can easily check if that space object is an instance of this enum
 * and which instance it is and take appropriate action.
 */
enum class NonPropertySpace {
    START,
    DRAW_ACTION_CARD,
    BREAK_TIME,
    VACATION,
    GO_ON_VACATION;
    
    /**
     * Either "Start", "Draw Action Card", "Break Time", "Vacation", or "Go On Vacation".
     */
    val friendlyName: String = createFriendlyName()

    override fun toString() = friendlyName
}