import kotlin.math.absoluteValue

/*
This file has the ActionCard abstract class and all its subclasses and objects that inherit from it.

During a game when we handle landing on a Draw Action Card space, we access an ActionCard at the top of an
ActionDeck and always display its message and then check its type and use that to take appropriate action.
Smart casting is used to get unique data.
*/

abstract class ActionCard(val message: String)

class MoneyGainCard(val gainAmount: Int) : ActionCard(message = "You gain $$gainAmount")

class MoneyLossCard(val lossAmount: Int) : ActionCard(message = "You lose $$lossAmount")

class GetMoneyFromOtherPlayersCard(val moneyAmount: Int) :
    ActionCard(message = "You get $$moneyAmount from every other player")

class GiveMoneyToOtherPlayersCard(val moneyAmount: Int) :
    ActionCard(message = "You must pay every other player $$moneyAmount")

class PropertyMaintenanceCard(val feePerRestaurant: Int) :
    ActionCard(message = "You must pay $$feePerRestaurant per restaurant for maintenance")

class RelativePositionChangeCard(val moveAmount: Int) :
    ActionCard(message = "Move ${if (moveAmount > 0) "ahead" else "back"} ${moveAmount.absoluteValue} spaces")

abstract class AbsolutePositionChangeCard(nameOfPropertyToMoveTo: String, message: String) : ActionCard(message) {
    val newPosition: Int = PropertyManager.getPropertyPosition(nameOfPropertyToMoveTo)
}

object LagosAvenueCard :
    AbsolutePositionChangeCard(
        nameOfPropertyToMoveTo = "Lagos Avenue",
        message = "Lagos is the most populated city in Africa. Move to Lagos Avenue."
    )

object KnuthStreetCard :
    AbsolutePositionChangeCard(
        nameOfPropertyToMoveTo = "Knuth Street",
        message =
            """
            Computer scientist Donald Knuth once said:
            'Programs are meant to be read by humans and only incidentally for computers to execute'.
            Move to Knuth Street.
            """.trimIndent()
    )

object GetOffVacationFreeCard :
    ActionCard(message = "Get Off Vacation Free. Keep this card and use it when needed, or trade it.")

object GoOnVacationCard : ActionCard(message = "Go On Vacation")