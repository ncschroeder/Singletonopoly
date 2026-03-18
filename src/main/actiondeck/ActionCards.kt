package singletonopoly.actiondeck

import singletonopoly.board.properties.PropertyManager

/*
This file has the ActionCard abstract class and all its subclasses and objects that inherit from it.

During a game when we handle landing on a "Draw Action Card" space, we access an ActionCard at the
top of the ActionDeck and always display its message and then check its type and use that to take
appropriate action. Smart casting is used to get unique data.
*/

/**
 * message never ends with a period since during the game, a quotation mark followed by the message
 * followed by a period and quotation mark is displayed.
 */
abstract class ActionCard(val message: String)

class MoneyGainCard(val amount: Int) : ActionCard(message = "You gain $$amount")

class MoneyLossCard(val amount: Int) : ActionCard(message = "You lose $$amount")

class GetMoneyFromOtherPlayersCard(val moneyToGetFromEachPlayer: Int) :
    ActionCard(message = "You get $$moneyToGetFromEachPlayer from every other player")

class PayMoneyToOtherPlayersCard(val moneyToPayEachPlayer: Int) :
    ActionCard(message = "You must pay every other player $$moneyToPayEachPlayer")

class RestaurantMaintenanceCard(val feePerRestaurant: Int) :
    ActionCard(message = "You must pay $$feePerRestaurant per restaurant that you own for maintenance")

class MoveAheadCard(val numSpaces: Int): ActionCard(message = "Move ahead $numSpaces spaces")

class MoveBackCard(val numSpaces: Int): ActionCard(message = "Move back $numSpaces spaces")

abstract class MoveToPropertyCard(propName: String, message: String) : ActionCard(message) {
    val propPosition: Int = PropertyManager.getPropPosition(propName)
}

object LagosAvenueCard :
    MoveToPropertyCard(
        propName = "Lagos Avenue",
        message = "Lagos is the most populated city in Africa. Move to Lagos Avenue"
    )

object KnuthStreetCard :
    MoveToPropertyCard(
        propName = "Knuth Street",
        message =
            // Use single quotes for the quote since this message will be displayed within double quotes.
            "Computer scientist Donald Knuth once said 'Programs are meant to be read by humans and " +
            "only incidentally for computers to execute.' Move to Knuth Street"
    )

object GoOnVacationCard : ActionCard(message = "Go On Vacation")

object GetOffVacationFreeCard :
    ActionCard(message = "Get off Vacation free. Keep this card and use it when needed, or trade it.")

