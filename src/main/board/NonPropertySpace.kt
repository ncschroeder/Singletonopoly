package singletonopoly.board

/**
 * This enum allows us to have objects that represent non-property spaces. We can easily place these on the board
 * and then when a player lands on a space, we can easily check if that space object is an instance of this enum
 * and which instance it is and take appropriate action.
 */
enum class NonPropertySpace(val friendlyName: String) {
    START("Start"),
    DRAW_ACTION_CARD("Draw Action Card"),
    BREAK_TIME("Break Time"),
    VACATION("Vacation"),
    GO_ON_VACATION("Go on Vacation");
    
    override fun toString() = friendlyName
}
