package singletonopoly

import kotlin.random.Random
import kotlin.random.nextInt
import singletonopoly.Misc.toStringWithCommas
import singletonopoly.actiondeck.*
import singletonopoly.board.*
import singletonopoly.board.properties.*
import singletonopoly.players.*

fun getFormattedInput(): String = readln().trim().lowercase()

fun printInvalidInputText(start: String = "Invalid input") {
    print("$start. Try again: ")
}

/**
 * question is a yes or no question.
 */
fun promptForYOrN(question: String): Boolean {
    print("$question (y/n) ")
    while (true) {
        when (getFormattedInput()) {
            "y" -> return true
            "n" -> return false
        }
        printInvalidInputText()
    }
}

fun main() {
    while (true) {
        Game()
        if (!promptForYOrN("Would you like to play again?")) {
            return
        }
        println()
    }
}

/**
 * Class with data and functions for a game of Singletonopoly. A game is started upon instantiation of this class.
 */
class Game {
    val playerManager: PlayerManager
    var curPlayer: Player
    var numDoubleRollsInARow = 0
    val actionDeck = ActionDeck()
    
    /**
     * This gets set to true if a player drops out in askAboutPreRollAction, a player gets sent to
     * vacation, or if a player is on vacation and tries to rolls doubles.
     */
    var turnOver = false
    
    /**
     * This gets set to true if a game is ended early in askAboutPreRollAction or when a player drops out,
     * either in askAboutPreRollAction or askAboutGettingMoney, and there's only 1 player left in the game.
     */
    var gameOver = false
    
    /**
     * This is used for user input and having it be a member of the Game class allows us to not have
     * to declare a variable every time we need to store user input.
     */
    lateinit var input: String
    
    
    // Valid input values and options used in multiple functions.
    val rollDiceVal = "rl"
    
    val seePropInfoVal = "pr"
    val seePropInfoOption = "$seePropInfoVal: See property info"
    
    val seePlayerInfoVal = "pl"
    val seePlayerInfoOption = "$seePlayerInfoVal: See player info"
    
    val tradeVal = "tr"
    val tradeOption = "$tradeVal: Trade"
    
    val pawnVal = "pn"
    val pawnOption = "$pawnVal: Pawn properties"
    
    val sellRestaurantsVal = "sr"
    val sellRestaurantsOption = "$sellRestaurantsVal: Sell restaurants"
    
    val dropOutVal = "dr"
    val dropOutOption = "$dropOutVal: Drop out of the game"
    
    val goBackVal = "gb"
    val goBackOption = "$goBackVal: Go back"
    
    
    fun printBlankLnAnd(obj: Any) {
        println()
        println(obj)
    }
    
    
    init {
        println("Singletonopoly\n")
        
        getOrderedPlayerNames().let {
            printBlankLnAnd("The order of the players is:")
            for (name in it) println(name)
            
            playerManager = PlayerManager(it)
            curPlayer = playerManager.curPlayer
        }
        
        while (true) {
            printBlankLnAnd(
                "It's ${curPlayer.name}'s turn${if (numDoubleRollsInARow > 0) " again" else ""}."
            )
            askAboutPreRollAction()
            if (gameOver) break
            if (!turnOver) {
                curPlayer.numTurnsSpentOnVacation?.let(::askAboutVacationAction)
            }
            if (!turnOver) rollDiceAndMove()
            if (gameOver) break
            
            // Prepare for next turn.
            turnOver = false
            if (numDoubleRollsInARow == 0) {
                curPlayer = playerManager.switchToNextPlayer()
            }
            
            print("\nEnd of turn. Enter \"c\" to continue: ")
            while (getFormattedInput() != "c") {
                printInvalidInputText()
            }
        }
        
        // Unless the game is ended early, there should be a winner.
        playerManager.winnerName?.let { println("\nThe winner is $it!\n") }
        
        // Make props unowned in case another game is started.
        PropertyManager.makePropsUnowned()
    }
    
    
    class DiceRoll {
        val die1Val: Int = Random.nextInt(1..6)
        val die2Val: Int = Random.nextInt(1..6)
        val isDoubles: Boolean = die1Val == die2Val
        val total: Int = die1Val + die2Val
    }
    
    
    /**
     * Has the user enter the number of players and then the names of the players. A dice roll gets
     * generated for each player and a list of names ordered by dice roll totals in descending order
     * gets returned.
     *
     * If there are dice roll total ties, then the order of the players that tied will be randomized.
     * For example; if 1 player gets a 12, 2 players get a 10, and 1 player gets a 7; then the player
     * that got the 12 will be 1st and the player that got the 7 will be last and the order of the
     * other 2 players will be randomized and one of them will be 2nd and the other will be 3rd.
     */
    fun getOrderedPlayerNames(): List<String> {
        print("How many players? Enter a number ≥ 2 and ≤ 8: ")
        val numPlayers: Int
        
        while (true) {
            val inputInt: Int? = getFormattedInput().toIntOrNull()
            if (inputInt != null && inputInt in 2..8) {
                numPlayers = inputInt
                break
            }
            printInvalidInputText()
        }
        
        class NameAndRollTotal(val name: String, val diceRollTotal: Int)
        
        val playerNamesAndRollTotals = ArrayList<NameAndRollTotal>(numPlayers)
        
        for (playerNum: Int in 1..numPlayers) {
            print("\nEnter a unique name for player $playerNum that has 1 to 20 characters: ")
            var name: String
            
            while (true) {
                name = readln().trim()
                
                when {
                    name.length !in 1..20 -> printInvalidInputText()
                    
                    playerNamesAndRollTotals.any { it.name == name } ->
                        printInvalidInputText(start = "$name is already a name for another player")
                    
                    else -> break
                }
            }
            
            with(DiceRoll()) {
                println("$name rolled a $die1Val and a $die2Val for a total of $total.")
                playerNamesAndRollTotals.add(NameAndRollTotal(name, total))
            }
        }
        
        return (
            playerNamesAndRollTotals
            .groupBy(keySelector = { it.diceRollTotal }, valueTransform = { it.name })
            .entries
            .sortedByDescending { (rollTotal, _) -> rollTotal }
            .flatMap { (_, names: List<String>) -> names.shuffled() }
        )
    }
    
    
    fun handleCurPlayerDroppingOutAndPropsBecomeUnowned() {
        println("${curPlayer.name} dropped out so their properties are now unowned.")
        PropertyManager.makePropsUnowned(owner = curPlayer)
    }
    
    
    /**
     * Asks the current player about any actions they would like to do before they roll the dice and
     * move, or do a vacation action if they're on vacation.
     */
    fun askAboutPreRollAction() {
        val actionPropGroups = ActionPropertyGroups.forPropsOwnedBy(curPlayer)
        
        // Valid input values
        val vacationActionVal = "va"
        val seeBoardSpacesVal = "bd"
        val endGameVal = "eg"
        val unpawnVal = "up"
        val buyRestaurantsVal = "br"
        
        val optionsStartString = """
            ${curPlayer.name}, enter one of the following:
            ${
                if (curPlayer.isOnVacation) "$vacationActionVal: Do a vacation action"
                else "$rollDiceVal: Roll the dice and move"
            }
            $seeBoardSpacesVal: See the spaces on the board
            $seePropInfoOption
            $seePlayerInfoOption
            $tradeOption
            $dropOutOption
            $endGameVal: End the game
            """.trimIndent()
        
        while (true) {
            println(optionsStartString)
            if (actionPropGroups.hasPawnable) {
                println(pawnOption)
            }
            if (actionPropGroups.hasUnpawnable) {
                println("$unpawnVal: Unpawn properties")
            }
            if (actionPropGroups.hasRestaurantAddable) {
                println("$buyRestaurantsVal: Buy restaurants")
            }
            if (actionPropGroups.hasRestaurantRemovable) {
                println(sellRestaurantsOption)
            }
            
            while (true) {
                input = getFormattedInput()
                
                if (
                    (!curPlayer.isOnVacation && input == rollDiceVal) ||
                    (curPlayer.isOnVacation && input == vacationActionVal)
                ) {
                    return
                }
                
                when (input) {
                    seeBoardSpacesVal -> printBlankLnAnd(Board.SPACES_STRING)
                    
                    seePropInfoVal -> askAboutSeeingPropInfo(playerWhoWantsToKnow = curPlayer)
                    
                    seePlayerInfoVal -> printBlankLnAnd(playerManager.getPlayerInfo())
                    
                    tradeVal -> {
                        if (
                            askAboutTrading(
                                initiatingPlayer = curPlayer,
                                initiatingPlayerNeedsMoney = false
                            )
                        ) {
                            actionPropGroups.refresh()
                        }
                    }
                    
                    dropOutVal -> {
                        if (promptForYOrN("${curPlayer.name}, are you sure that you want to drop out?")) {
                            curPlayer.removeFromGame()
                            if (playerManager.onePlayerIsInGame) {
                                gameOver = true
                            } else {
                                turnOver = true
                                handleCurPlayerDroppingOutAndPropsBecomeUnowned()
                            }
                            return
                        }
                    }
                    
                    endGameVal -> {
                        if (promptForYOrN("Are you sure that you want to end the game?")) {
                            gameOver = true
                            return
                        }
                    }
                    
                    pawnVal -> {
                        if (actionPropGroups.hasPawnable) {
                            if (
                                askAboutPawning(
                                    player = curPlayer,
                                    pawnableProps = actionPropGroups.pawnable
                                )
                            ) {
                                actionPropGroups.refresh()
                            }
                        } else {
                            printInvalidInputText()
                            continue
                        }
                    }
                    
                    unpawnVal -> {
                        if (actionPropGroups.hasUnpawnable) {
                            if (askAboutUnpawning(unpawnableProps = actionPropGroups.unpawnable)) {
                                actionPropGroups.refresh()
                            }
                        } else {
                            printInvalidInputText()
                            continue
                        }
                    }
                    
                    buyRestaurantsVal -> {
                        if (actionPropGroups.hasRestaurantAddable) {
                            if (
                                askAboutBuyingRestaurants(
                                    restaurantAddableStreets = actionPropGroups.restaurantAddable
                                )
                            ) {
                                actionPropGroups.refresh()
                            }
                        } else {
                            printInvalidInputText()
                            continue
                        }
                    }
                    
                    sellRestaurantsVal -> {
                        if (actionPropGroups.hasRestaurantRemovable) {
                            if (
                                askAboutSellingRestaurants(
                                    player = curPlayer,
                                    restaurantRemovableStreets = actionPropGroups.restaurantRemovable
                                )
                            ) {
                                actionPropGroups.refresh()
                            }
                        } else {
                            printInvalidInputText()
                            continue
                        }
                    }
                    
                    else ->{
                        printInvalidInputText()
                        continue
                    }
                }
                
                println()
                break
            }
        }
    }
    
    
    fun moveCurPlayerAhead(numSpaces: Int) {
        with(curPlayer) {
            position += numSpaces
            if (position > Board.NUM_SPACES) {
                position -= Board.NUM_SPACES
                handleCurPlayerMakingRevolution()
            }
        }
        evaluatePosition()
    }
    
    fun handleCurPlayerMakingRevolution() {
        curPlayer.money += 512
        println("They made a revolution so they gained $512 and now have $${curPlayer.moneyString}.")
    }
    
    fun handleGoOnVacation() {
        curPlayer.sendToVacation()
        turnOver = true
        // If the player rolled doubles, they won't get to go again in this situation.
        numDoubleRollsInARow = 0
    }
    
    fun handlePlayerDroppingOutAndStuffGoesToAnotherPlayer(dropOutPlayer: Player, newOwner: Player) {
        println("${dropOutPlayer.name} dropped out so their money and properties go to ${newOwner.name}.")
        newOwner.incrementMoneyAndPrintUpdate(dropOutPlayer.money)
        PropertyManager.transferOwnership(curOwner = dropOutPlayer, newOwner = newOwner)
    }
    
    fun rollDiceAndMove() {
        val diceRoll = DiceRoll()
        with(diceRoll) {
            printBlankLnAnd("${curPlayer.name} rolled a $die1Val and a $die2Val for a total of $total.")
        }
        
        if (diceRoll.isDoubles) {
            if (++numDoubleRollsInARow == 3) {
                println("They rolled doubles thrice, or 3 times, in a row so they must go on vacation.")
                handleGoOnVacation()
                return
            }
            println("These are doubles so they get to go again.")
        } else {
            numDoubleRollsInARow = 0
        }
        
        moveCurPlayerAhead(numSpaces = diceRoll.total)
    }
    
    /**
     * Evaluates the position that the current player landed on and takes appropriate action.
     */
    fun evaluatePosition() {
        print("They landed on position ${curPlayer.position}, which is ")
        
        when (val curBoardSpace: Any? = Board.getSpace(curPlayer.position)) {
            is Property -> {
                print(curBoardSpace.name)
                if (curBoardSpace is Street) {
                    print(" in the ${curBoardSpace.nHood.friendlyName} neighborhood")
                }
                curBoardSpace.owner.let {
                    if (it != null) {
                        handleLandingOnOwnedProp(prop = curBoardSpace, owner = it)
                    } else {
                        handleLandingOnUnownedProp(prop = curBoardSpace)
                    }
                }
            }
            
            is NonPropertySpace -> {
                print("\"$curBoardSpace")
                
                when (curBoardSpace) {
                    NonPropertySpace.START,
                    NonPropertySpace.VACATION,
                    NonPropertySpace.BREAK_TIME -> println(",\" which has no effect.")
                    
                    NonPropertySpace.GO_ON_VACATION -> {
                        println(".\"")
                        handleGoOnVacation()
                    }
                    
                    NonPropertySpace.DRAW_ACTION_CARD -> {
                        println(".\"")
                        handleLandingOnDrawActionCard()
                    }
                }
            }
            
            else -> Misc.printError(
                "curPlayer.position = ${curPlayer.position} and this caused Board.getSpace to return $curBoardSpace."
            )
        }
    }
    
    fun handleLandingOnUnownedProp(prop: Property) {
        // This print statement is a continuation of print statements from evaluatePosition.
        println(", which is unowned.")
        val purchasePrice: Int = prop.purchasePrice
        val buyPropMessage = """
            ${curPlayer.name}, you have $${curPlayer.moneyString}. Would you like to buy ${prop.name} for $${purchasePrice.toStringWithCommas()}?
            Enter "y", "n", or "$seePropInfoVal" to see property info: """.trimIndent()
        
        outer@ while (true) {
            print(buyPropMessage)
            
            while (true) {
                when (getFormattedInput()) {
                    "y" -> {
                        if (curPlayer.money < purchasePrice) {
                            if (
                                !askAboutGettingMoney(
                                    player = curPlayer,
                                    moneyNeeded = purchasePrice,
                                    playerHasChoice = true,
                                    gotMoneyMessageEnd = "buy ${prop.name}"
                                )
                            ) {
                                println("${curPlayer.name} isn't buying ${prop.name}.")
                                return
                            }
                        }
                        
                        prop.owner = curPlayer
                        curPlayer.decrementMoneyAndPrintUpdate(purchasePrice)
                        return
                    }
                    
                    "n" -> return
                    
                    seePropInfoVal -> {
                        askAboutSeeingPropInfo(playerWhoWantsToKnow = curPlayer)
                        println()
                        continue@outer
                    }
                    
                    else -> printInvalidInputText()
                }
            }
        }
    }
    
    fun handleLandingOnOwnedProp(prop: Property, owner: Player) {
        // These print statements are a continuation of print statements from evaluatePosition.
        print(", which is owned by ")
        if (owner === curPlayer) {
            println("them.")
            return
        }
        println("${owner.name}.")
        if (prop.isPawned) {
            println("This property is pawned so ${curPlayer.name} doesn't have to pay a fee.")
            return
        }
        
        val fee: Int
        
        when (prop) {
            is Street -> {
                fee = prop.curFee
                println(
                    prop.numRestaurants.let {
                        "There${if (it == 1) "'s 1 restaurant" else " are ${if (it == 0) "no" else it} restaurants"} " +
                        "on this street and the fee is $${fee.toStringWithCommas()}."
                    }
                )
            }
            
            is GolfClub -> prop.FeeData().let {
                fee = it.fee
                val numClubsString = Misc.getNumThingsString(it.numClubsOwnedByOwner, "golf club")
                println("${owner.name} owns $numClubsString and the fee is $$fee.")
            }
            
            is SuperStore -> {
                val diceRoll = DiceRoll()
                val feeData = SuperStore.FeeData()
                fee = diceRoll.total * feeData.multiplier
                
                print(
                    if (feeData.onePlayerOwnsBothStores) "${owner.name} owns both super stores"
                    else "This is the only super store that ${owner.name} owns"
                )
                println(" so ${curPlayer.name} must roll the dice and pay ${feeData.multiplier} times its total.")
                with(diceRoll) {
                    println(
                        "${curPlayer.name} rolled a $die1Val and a $die2Val for a total of $total " +
                        "so they must pay $$fee."
                    )
                }
            }
        }
        
        if (curPlayer.money < fee) {
            if (
                !askAboutGettingMoney(
                    player = curPlayer,
                    moneyNeeded = fee,
                    playerHasChoice = false,
                    gotMoneyMessageEnd = "pay the fee to ${owner.name}"
                )
            ) {
                if (!gameOver) {
                    handlePlayerDroppingOutAndStuffGoesToAnotherPlayer(
                        dropOutPlayer = curPlayer,
                        newOwner = owner
                    )
                }
                return
            }
        }
        
        curPlayer.decrementMoneyAndPrintUpdate(fee)
        owner.incrementMoneyAndPrintUpdate(fee)
    }
    
    fun handleLandingOnDrawActionCard() {
        val topCard: ActionCard = actionDeck.topCard
        println("They drew an Action Card and it says \"${topCard.message}.\"")
        
        /*
        Get off vacation free cards are the only type of card where the Action Deck top card
        won't get moved to the bottom.
         */
        if (topCard !is GetOffVacationFreeCard) {
            actionDeck.moveTopCardToBottom()
        }
        
        when (topCard) {
            is MoneyGainCard -> curPlayer.incrementMoneyAndPrintUpdate(topCard.amount)
            
            is MoneyLossCard -> {
                val lossAmount: Int = topCard.amount
                
                if (curPlayer.money < lossAmount) {
                    if (
                        !askAboutGettingMoney(
                            player = curPlayer,
                            moneyNeeded = lossAmount,
                            playerHasChoice = false,
                            gotMoneyMessageEnd = "handle the money loss action card"
                        )
                    ) {
                        if (!gameOver) {
                            handleCurPlayerDroppingOutAndPropsBecomeUnowned()
                        }
                        return
                    }
                }
                
                curPlayer.decrementMoneyAndPrintUpdate(lossAmount)
            }
            
            is PayMoneyToOtherPlayersCard -> {
                val otherPlayers: List<Player> =
                    playerManager.getOtherPlayersInGame(excludingPlayer = curPlayer)
                val moneyToPayEachPlayer: Int = topCard.moneyToPayEachPlayer
                val totalMoneyToPay: Int = moneyToPayEachPlayer * otherPlayers.size
                
                val sentenceStart =
                    (if (otherPlayers.size == 1) "There's 1 other player"
                    else "There are ${otherPlayers.size} other players") +
                    " in the game so ${curPlayer.name}"
                
                if (curPlayer.money >= totalMoneyToPay) {
                    curPlayer.money -= totalMoneyToPay
                    println("$sentenceStart lost $$totalMoneyToPay and now has $${curPlayer.moneyString}.")
                } else {
                    println("$sentenceStart must pay $$totalMoneyToPay.")
                    
                    if (
                        !askAboutGettingMoney(
                            player = curPlayer,
                            moneyNeeded = totalMoneyToPay,
                            playerHasChoice = false,
                            gotMoneyMessageEnd = "pay the other players"
                        )
                    ) {
                        if (!gameOver) {
                            handleCurPlayerDroppingOutAndPropsBecomeUnowned()
                        }
                        return
                    }
                    
                    curPlayer.decrementMoneyAndPrintUpdate(totalMoneyToPay)
                }
                
                otherPlayers.forEach {
                    it.incrementMoneyAndPrintUpdate(moneyToPayEachPlayer)
                }
            }
            
            is GetMoneyFromOtherPlayersCard -> {
                val moneyToGetFromEachPlayer: Int = topCard.moneyToGetFromEachPlayer
                val otherPlayers: List<Player> =
                    playerManager.getOtherPlayersInGame(excludingPlayer = curPlayer)
                var numPlayersWhoPaid = 0
                
                for (otherPlayer in otherPlayers) {
                    if (otherPlayer.money < moneyToGetFromEachPlayer) {
                        if (
                            !askAboutGettingMoney(
                                player = otherPlayer,
                                moneyNeeded = moneyToGetFromEachPlayer,
                                playerHasChoice = false,
                                gotMoneyMessageEnd = "pay ${curPlayer.name}"
                            )
                        ) {
                            if (gameOver) return
                            handlePlayerDroppingOutAndStuffGoesToAnotherPlayer(
                                dropOutPlayer = otherPlayer,
                                newOwner = curPlayer
                            )
                            continue
                        }
                    }
                    
                    otherPlayer.decrementMoneyAndPrintUpdate(moneyToGetFromEachPlayer)
                    numPlayersWhoPaid++
                }
                
                val moneyGained: Int = moneyToGetFromEachPlayer * numPlayersWhoPaid
                val numPlayersWhoPaidString = Misc.getNumThingsString(numPlayersWhoPaid, "player")
                with (curPlayer) {
                    money += moneyGained
                    println("$numPlayersWhoPaidString paid so $name gained $$moneyGained and now has $$moneyString.")
                }
            }
            
            is RestaurantMaintenanceCard -> {
                val numRestaurantsOwned: Int = PropertyManager.getNumRestaurantsOwnedBy(curPlayer)
                if (numRestaurantsOwned == 0) {
                    println("They don't own any restaurants so they don't have to pay anything.")
                    return
                }
                
                val maintenanceFee: Int = topCard.feePerRestaurant * numRestaurantsOwned
                val maintenanceFeeString = '$' + maintenanceFee.toStringWithCommas()
                val numRestaurantsString = Misc.getNumThingsString(numRestaurantsOwned, "restaurant")
                val sentenceStart = "They own $numRestaurantsString so they"
                
                if (curPlayer.money >= maintenanceFee) {
                    curPlayer.money -= maintenanceFee
                    println("$sentenceStart lost $maintenanceFeeString and now have $${curPlayer.moneyString}.")
                } else {
                    println("$sentenceStart must pay $maintenanceFeeString.")
                    
                    if (
                        !askAboutGettingMoney(
                            player = curPlayer,
                            moneyNeeded = maintenanceFee,
                            playerHasChoice = false,
                            gotMoneyMessageEnd = "pay the maintenance fee"
                        )
                    ) {
                        if (!gameOver) {
                            handleCurPlayerDroppingOutAndPropsBecomeUnowned()
                        }
                        return
                    }
                }
                
                curPlayer.decrementMoneyAndPrintUpdate(maintenanceFee)
            }
            
            is MoveAheadCard -> moveCurPlayerAhead(topCard.numSpaces)
            
            is MoveBackCard -> {
                with(curPlayer) {
                    position -= topCard.numSpaces
                    if (position < 1) {
                        position = Board.NUM_SPACES + position
                    }
                }
                evaluatePosition()
            }
            
            is MoveToPropertyCard -> {
                topCard.propPosition.let {
                    if (it < curPlayer.position) {
                        handleCurPlayerMakingRevolution()
                    }
                    curPlayer.position = it
                }
                evaluatePosition()
            }
            
            is GoOnVacationCard -> handleGoOnVacation()
            
            is GetOffVacationFreeCard -> {
                curPlayer.incrementNumGetOffVacationCardsAndPrintUpdate()
                actionDeck.removeGetOffVacationCardAtTop()
            }
        }
    }
    
    
    /**
     * numTurnsSpentOnVacation is from the curPlayer.numTurnsSpentOnVacation Int? property. If that
     * property isn't null, then it'll be used as the arg.
     */
    fun askAboutVacationAction(numTurnsSpentOnVacation: Int) {
        val turnString =
            numTurnsSpentOnVacation.let {
                when (it) {
                    0 -> "first"
                    1 -> "second"
                    2 -> "third (last)"
                    else -> {
                        Misc.printError(
                            "${curPlayer.name} was on vacation with a numTurnsSpentOnVacation value of $it."
                        )
                        (it + 1).toString()
                    }
                }
            }
        
        val feeToGetOffVacation = 128
        
        // Valid input values
        val payVal = "py"
        val getOffVacationCardVal = "gt"
        
        val optionsString = """
            ${curPlayer.name}, this is your $turnString turn on vacation.
            Enter one of the following:
            $payVal: Pay $$feeToGetOffVacation to get off vacation
            $rollDiceVal: Try to roll doubles
            """
            .trimIndent()
            .let {
                if (!curPlayer.hasAGetOffVacationCard) it
                else it + "\n$getOffVacationCardVal: Use a get off vacation free card"
            }
        
        fun printGetToRollDiceText() {
            println("${curPlayer.name} gets to roll the dice and move.")
        }
        
        outer@ while (true) {
            printBlankLnAnd(optionsString)
            
            while (true) {
                when (getFormattedInput()) {
                    payVal -> {
                        if (curPlayer.money < feeToGetOffVacation) {
                            if (
                                !askAboutGettingMoney(
                                    player = curPlayer,
                                    moneyNeeded = feeToGetOffVacation,
                                    playerHasChoice = true,
                                    gotMoneyMessageEnd = "get off vacation"
                                )
                            ) {
                                continue@outer
                            }
                        }

                        curPlayer.decrementMoneyAndPrintUpdate(feeToGetOffVacation)
                        curPlayer.removeFromVacation()
                        printGetToRollDiceText()
                        return
                    }
                    
                    rollDiceVal -> with(DiceRoll()) {
                        printBlankLnAnd("${curPlayer.name} rolled a $die1Val and a $die2Val.")
                        
                        when {
                            isDoubles -> {
                                println(
                                    "These are doubles so they're off vacation and get to advance " +
                                    "$total spaces but don't get to roll again."
                                )
                                curPlayer.removeFromVacation()
                                moveCurPlayerAhead(numSpaces = total)
                            }
                            
                            numTurnsSpentOnVacation == 2 -> {
                                println("${curPlayer.name} spent 3 turns on vacation so it's now over.")
                                curPlayer.removeFromVacation()
                            }
                            
                            else -> curPlayer.numTurnsSpentOnVacation = numTurnsSpentOnVacation + 1
                        }
                        
                        turnOver = true
                        return
                    }
                    
                    getOffVacationCardVal -> {
                        if (curPlayer.hasAGetOffVacationCard) {
                            curPlayer.decrementNumGetOffVacationCardsAndPrintUpdate()
                            curPlayer.removeFromVacation()
                            actionDeck.insertGetOffVacationCardAtBottom()
                            printGetToRollDiceText()
                            return
                        }
                    }
                }
                
                printInvalidInputText()
            }
        }
    }
    
    
    /**
     * playerWhoWantsToKnow can be the current player if they want to see property info at
     * the beginning of their turn. It can also be one of the 2 players involved in a trade.
     */
    fun askAboutSeeingPropInfo(playerWhoWantsToKnow: Player) {
        // Valid input values
        val allPropsInfoVal = "ap"
        val playerPropsInfoVal = "yp"
        
        val optionsString = """
            ${playerWhoWantsToKnow.name}, enter one of the following:
            $allPropsInfoVal: See info for all properties
            $playerPropsInfoVal: See info for your properties
            $goBackOption
            """.trimIndent()
        
        val allPropsInfo: String by lazy {
            PropertyGroups.forAllProps.getInfo(heading = "Info for All Properties")
        }
        
        val playerPropsInfo: String by lazy {
            PropertyGroups.forPropsOwnedBy(playerWhoWantsToKnow)
            .run {
                if (isEmpty()) "${playerWhoWantsToKnow.name} doesn't own any properties."
                else getInfo(ownerOfProps = playerWhoWantsToKnow)
            }
        }
        
        while (true) {
            printBlankLnAnd(optionsString)
            
            while (true) {
                val infoToPrint: String =
                    when (getFormattedInput()) {
                        allPropsInfoVal -> allPropsInfo
                        
                        playerPropsInfoVal -> playerPropsInfo
                        
                        goBackVal -> return
                        
                        else -> {
                            printInvalidInputText()
                            continue
                        }
                    }
                
                printBlankLnAnd(infoToPrint)
                break
            }
        }
    }
    
    
    fun printNoValidPositionsText() {
        printInvalidInputText(start = "No valid positions were entered")
    }
    
    
    /**
     * The processes for pawning and unpawning props have some similarities so this function is used
     * for both. askAboutPawning is used to determine which. This returns true if a prop was pawned
     * or unpawned and false otherwise.
     */
    fun askAboutPawningOrUnpawning(
        askAboutPawning: Boolean,
        player: Player,
        pawnableOrUnpawnableProps: PropertyGroups
    ): Boolean {
        val propsInfoHeading =
            "Here are the properties owned by ${player.name} that can be " +
            (if (askAboutPawning) "" else "un") + "pawned:"
        val propsInfo: String = pawnableOrUnpawnableProps.getInfo(propsInfoHeading)
        
        while (true) {
            printBlankLnAnd(propsInfo)
            print("""
                
                ${player.name}, enter the positions of the properties you would like to ${if (askAboutPawning) "" else "un"}pawn
                with each position separated by whitespace or enter "$goBackVal" to go back: """.trimIndent()
            )
            var selectedProps: PropertyGroups
            
            while (true) {
                input = getFormattedInput()
                if (input == goBackVal) return false
                
                selectedProps = pawnableOrUnpawnableProps.filterByPositions(positionsInput = input)
                if (selectedProps.isNotEmpty()) break
                
                printNoValidPositionsText()
            }
            
            val selectedPropsList: List<Property> = selectedProps.toPropList()
            val totalMoneyGainOrPrice: Int =
                selectedPropsList.sumOf { if (askAboutPawning) it.pawnPrice else it.unpawnPrice }
            
            printBlankLnAnd(selectedProps.getInfo(heading = "Here are the selected properties:"))
            print(
                "\n$${totalMoneyGainOrPrice.toStringWithCommas()} will " +
                (if (askAboutPawning) "be gained" else "need to be paid") + ". "
            )
            if (!promptForYOrN("${player.name}, do you confirm this?")) {
                continue
            }
            
            if (askAboutPawning) {
                selectedPropsList.forEach { it.pawn() }
                player.incrementMoneyAndPrintUpdate(totalMoneyGainOrPrice)
            } else {
                if (player.money < totalMoneyGainOrPrice) {
                    if (
                        !askAboutGettingMoney(
                            player = player,
                            moneyNeeded = totalMoneyGainOrPrice,
                            playerHasChoice = true,
                            gotMoneyMessageEnd = "unpawn their properties"
                        )
                    ) {
                        println("${player.name} will be taken back to the properties selection section.")
                        continue
                    }
                }
                
                selectedPropsList.forEach { it.unpawn() }
                player.decrementMoneyAndPrintUpdate(totalMoneyGainOrPrice)
            }
            
            return true
        }
    }
    
    fun askAboutPawning(player: Player, pawnableProps: PropertyGroups): Boolean =
        askAboutPawningOrUnpawning(askAboutPawning = true, player, pawnableProps)
    
    // Only the current player is able to unpawn properties.
    fun askAboutUnpawning(unpawnableProps: PropertyGroups): Boolean =
        askAboutPawningOrUnpawning(askAboutPawning = false, curPlayer, unpawnableProps)
    
    
    /**
     * The processes for buying and selling restaurants have some similarities so this function is
     * used for both. askAboutBuying is used to determine which. This returns true if a restaurant
     * was bought or sold and false otherwise.
     */
    fun askAboutBuyingOrSellingRestaurants(
        askAboutBuying: Boolean,
        player: Player,
        restaurantAddableOrRemovableStreets: StreetGroups
    ): Boolean {
        val streetsInfoHeading =
            "Here are the streets owned by ${player.name} where a restaurant can be " +
            (if (askAboutBuying) "bought" else "sold") + ':'
        val streetsInfo =
            restaurantAddableOrRemovableStreets
            .getInfo(streetsInfoHeading)
            .toString()
        
        while (true) {
            printBlankLnAnd(streetsInfo)
            print("""
                
                ${player.name}, enter the positions of the streets you would like to ${(if (askAboutBuying) "buy" else "sell")} a restaurant for
                with each position separated by whitespace or enter "$goBackVal" to go back: """.trimIndent()
            )
            var selectedStreets: StreetGroups
            
            while (true) {
                input = getFormattedInput()
                if (input == goBackVal) return false
                
                selectedStreets =
                    restaurantAddableOrRemovableStreets.filterByPositions(positionsInput = input)
                if (selectedStreets.isNotEmpty()) break
                
                printNoValidPositionsText()
            }
            
            val selectedStreetsList: List<Street> = selectedStreets.toStreetList()
            val totalMoneyPriceOrGain: Int =
                selectedStreetsList.sumOf {
                    if (askAboutBuying) it.nHood.restaurantPurchasePrice
                    else it.nHood.restaurantSellingPrice
                }
            
            printBlankLnAnd(selectedStreets.getInfo(heading = "Here are the selected streets:"))
            print(
                "\n$${totalMoneyPriceOrGain.toStringWithCommas()} will " +
                (if (askAboutBuying) "need to be paid" else "be gained") + ". "
            )
            if (!promptForYOrN("${player.name}, do you confirm this?")) {
                continue
            }
            
            if (!askAboutBuying) {
                selectedStreetsList.forEach { it.removeRestaurant() }
                player.incrementMoneyAndPrintUpdate(totalMoneyPriceOrGain)
            } else {
                if (player.money < totalMoneyPriceOrGain) {
                    if (
                        !askAboutGettingMoney(
                            player = player,
                            moneyNeeded = totalMoneyPriceOrGain,
                            playerHasChoice = true,
                            gotMoneyMessageEnd = "buy restaurants"
                        )
                    ) {
                        println("${player.name} will be taken back to the streets selection section.")
                        continue
                    }
                }
                
                selectedStreetsList.forEach { it.addRestaurant() }
                player.decrementMoneyAndPrintUpdate(totalMoneyPriceOrGain)
            }
            
            return true
        }
    }
    
    // Only the current player is able to buy restaurants.
    fun askAboutBuyingRestaurants(restaurantAddableStreets: StreetGroups): Boolean =
        askAboutBuyingOrSellingRestaurants(askAboutBuying = true, curPlayer, restaurantAddableStreets)
    
    fun askAboutSellingRestaurants(player: Player, restaurantRemovableStreets: StreetGroups): Boolean =
        askAboutBuyingOrSellingRestaurants(askAboutBuying = false, player, restaurantRemovableStreets)
    
    
    /**
     * This function implements the money getting section described in the manual. This gets called
     * when a player needs money for the situation they're in and don't have it. This interacts with
     * that player about options they have for getting more money. This returns true if the player
     * gets the money needed and false otherwise.
     *
     * The playerHasChoice param should be true if a player needs money and chose to be in that
     * situation. For example, they wanted to add a restaurant to one of their streets. When this
     * param is true, this function can be exited by having the player either choose to exit or get
     * the money needed. This param should be false if a player needs money and they didn't choose to
     * be in that situation. For example, they landed on an owned property and don't have enough money
     * to pay the fee. When this param is false, this function can be exited by having the player
     * either drop out of the game or get the money needed.
     *
     * The gotMoneyMessageEnd param is conditionally used. The args for it are mostly string literals
     * but also some string templates. These strings are always < 40 chars, so pretty short, and the
     * expressions interpolated into the string templates are pretty cheap to run. Because of this,
     * this param is a string and not a function that returns a string, since that could be ideal if
     * the args for this were strings that are long and/or take a lot of work to create.
     */
    fun askAboutGettingMoney(
        player: Player,
        moneyNeeded: Int,
        playerHasChoice: Boolean,
        gotMoneyMessageEnd: String
    ): Boolean {
        val actionPropGroups = ActionPropertyGroups.forPropsOwnedBy(player)
        
        outer@ while (player.money < moneyNeeded) {
            printBlankLnAnd("""
                ${player.name}, you need $${moneyNeeded.toStringWithCommas()} but only have $${player.moneyString}.
                Enter one of the following:
                ${if (playerHasChoice) goBackOption else dropOutOption}
                $tradeOption
                """.trimIndent()
            )
            if (actionPropGroups.hasPawnable) {
                println(pawnOption)
            }
            if (actionPropGroups.hasRestaurantRemovable) {
                println(sellRestaurantsOption)
            }
            
            while (true) {
                when (getFormattedInput()) {
                    goBackVal -> {
                        if (playerHasChoice) return false
                        else printInvalidInputText()
                    }
                    
                    dropOutVal -> {
                        if (!playerHasChoice) {
                            player.removeFromGame()
                            if (playerManager.onePlayerIsInGame) {
                                gameOver = true
                            }
                            return false
                        } else {
                            printInvalidInputText()
                        }
                    }
                    
                    tradeVal -> {
                        if (askAboutTrading(initiatingPlayer = player, initiatingPlayerNeedsMoney = true)) {
                            actionPropGroups.refresh()
                        }
                        continue@outer
                    }
                    
                    pawnVal -> {
                        if (actionPropGroups.hasPawnable) {
                            if (askAboutPawning(player = player, pawnableProps = actionPropGroups.pawnable)) {
                                actionPropGroups.refresh()
                            }
                            continue@outer
                        } else {
                            printInvalidInputText()
                        }
                    }
                    
                    sellRestaurantsVal -> {
                        if (actionPropGroups.hasRestaurantRemovable) {
                            if (
                                askAboutSellingRestaurants(
                                    player = player,
                                    restaurantRemovableStreets = actionPropGroups.restaurantRemovable
                                )
                            ) {
                                actionPropGroups.refresh()
                            }
                            continue@outer
                        } else {
                            printInvalidInputText()
                        }
                    }
                    
                    else -> printInvalidInputText()
                }
            }
        }
        
        printBlankLnAnd("${player.name} got the money to $gotMoneyMessageEnd.")
        return true
    }
    
    
    /**
     * When a player wants to make a trade, they'll have to select what they want to get and what
     * they want to offer. This class is used for that.
     *
     * The owner param is the owner of the items in this class.
     */
    inner class TradeItems(private val owner: Player) {
        var money = 0
        
        var numGetOffVacationCards = 0
        
        var props: PropertyGroups? = null
        
        val aFieldChanged: Boolean
            get() = money != 0 || numGetOffVacationCards != 0 || props != null
        
        override fun toString() =
            buildString {
                if (money > 0) {
                    append('$' + money.toStringWithCommas())
                }
                
                if (numGetOffVacationCards > 0) {
                    if (isNotEmpty()) appendLine()
                    append(Misc.getNumGetOffVacationCardsString(numGetOffVacationCards))
                }
                
                props?.let {
                    if (isNotEmpty()) append("\n\n")
                    append(it.getInfo(heading = "The following properties:"))
                }
            }
        
        fun transferItemsTo(newOwner: Player) {
            if (money > 0) {
                println()
                owner.decrementMoneyAndPrintUpdate(money)
                newOwner.incrementMoneyAndPrintUpdate(money)
            }
            
            if (numGetOffVacationCards > 0) {
                println()
                owner.decrementNumGetOffVacationCardsAndPrintUpdate(numGetOffVacationCards)
                newOwner.incrementNumGetOffVacationCardsAndPrintUpdate(numGetOffVacationCards)
            }
            
            props?.toPropList()?.forEach { prop ->
                prop.owner = newOwner
                if (prop !is Street) return@forEach
                
                val nHood = prop.nHood
                if (nHood.numRestaurants == 0) return@forEach
                
                printBlankLnAnd(
                    if (nHood.numRestaurants == 1) {
                        "There's 1 restaurant in the ${nHood.friendlyName} neighborhood so this'll be " +
                        "sold for $${nHood.restaurantSellingPrice}."
                    } else {
                        "There are ${nHood.numRestaurants} restaurants in the ${nHood.friendlyName} " +
                        "neighborhood so these'll be sold for $${nHood.restaurantSellingPrice} each."
                    }
                )
                
                val moneyGain: Int = nHood.numRestaurants * nHood.restaurantSellingPrice
                nHood.removeAllRestaurants()
                owner.incrementMoneyAndPrintUpdate(moneyGain)
            }
        }
    }
    
    /**
     * Interacts with the initiating player about trading. This includes asking who they want to trade
     * with and what they want to trade. If a trade is agreed upon, then the necessary changes are made.
     *
     * If initiatingPlayerNeedsMoney is true, then the initiating player won't be able to offer money
     * and will need to select a money amount they want from the player they want to make a trade with.
     *
     * Returns true if a trade was made and false otherwise.
     */
    fun askAboutTrading(initiatingPlayer: Player, initiatingPlayerNeedsMoney: Boolean): Boolean {
        // Valid input values and options for multiple parts of the trade process.
        val nextPartVal = "nx"
        val nextPartSentencePart = "\"$nextPartVal\" to go to the next part of the trade process"
        
        val cancelVal = "cn"
        val cancelOption = "$cancelVal: Cancel the trade"
        
        val goToStartVal = "st"
        val goToStartOption = "$goToStartVal: Go to the start of the trade process"
        
        val tryAgainVal = "ta"
        
        
        fun String.stripCommas(): String = replace(",", "")
        
        val initiatingPlayerProps = PropertyGroups.forPropsOwnedBy(initiatingPlayer)
        val initiatingPlayerHasAProp: Boolean = initiatingPlayerProps.isNotEmpty()
        
        // Let otherPlayersMap be a map where the keys are stringified ints that start at 1 and the values are Players.
        val otherPlayersMap: Map<String, Player> =
            playerManager
            .getOtherPlayersInGame(excludingPlayer = initiatingPlayer)
            .withIndex()
            .associate { (index: Int, player) -> (index + 1).toString() to player }
        
        val playerOptionsString =
            otherPlayersMap
            .asIterable()
            .joinToString(
                transform = { (intString, player) -> "\n${intString}: Make a trade with ${player.name}" },
                separator = ""
            )
        
        val startOptionsString = """
            ${initiatingPlayer.name}, enter one of the following:
            $seePropInfoOption
            $seePlayerInfoOption
            $cancelOption
            """
            .trimIndent()
            .plus(playerOptionsString)
        
        mainTradingLoop@ while (true) {
            val otherPlayer: Player
            
            startOptionsLoop@ while (true) {
                printBlankLnAnd(startOptionsString)
                
                while (true) {
                    input = getFormattedInput()
                    
                    when (input) {
                        seePropInfoVal -> {
                            askAboutSeeingPropInfo(playerWhoWantsToKnow = initiatingPlayer)
                            continue@startOptionsLoop
                        }
                        
                        seePlayerInfoVal -> {
                            printBlankLnAnd(playerManager.getPlayerInfo())
                            continue@startOptionsLoop
                        }
                        
                        cancelVal -> return false
                        
                        else -> {
                            // Check if a valid number was entered.
                            val nullableOtherPlayer: Player? = otherPlayersMap[input]
                            if (nullableOtherPlayer != null) {
                                otherPlayer = nullableOtherPlayer
                                break@startOptionsLoop
                            }
                            printInvalidInputText()
                        }
                    }
                }
            }
            
            val otherPlayerProps = PropertyGroups.forPropsOwnedBy(otherPlayer)
            val otherPlayerHasAProp: Boolean = otherPlayerProps.isNotEmpty()
            
            if (
                !initiatingPlayerHasAProp && !initiatingPlayer.hasAGetOffVacationCard &&
                !otherPlayerHasAProp && !otherPlayer.hasAGetOffVacationCard
            ) {
                println(
                    "There's nothing that ${initiatingPlayer.name} and ${otherPlayer.name} can trade " +
                    "with each other besides money. The trade process will be restarted."
                )
                continue@mainTradingLoop
            }
            
            if (initiatingPlayerNeedsMoney && otherPlayer.money == 0) {
                println(
                    "${initiatingPlayer.name} needs money and ${otherPlayer.name} doesn't have any. " +
                    "The trade process will be restarted."
                )
                continue@mainTradingLoop
            }
            
            
            // Part for the initiating player to select what they want.
            
            val wantedItems = TradeItems(owner = otherPlayer)
            
            while (true) {
                println()
                val askAboutMoneyAmount: Boolean =
                    initiatingPlayerNeedsMoney ||
                    promptForYOrN("${initiatingPlayer.name}, would you like any money from ${otherPlayer.name}?")
                
                if (askAboutMoneyAmount) {
                    println("${otherPlayer.name} has $${otherPlayer.moneyString}. How much would you like?")
                    print(
                        if (initiatingPlayerNeedsMoney) "You need money so you must enter an amount, optionally with commas: "
                        else "Enter an amount, optionally with commas, or $nextPartSentencePart: "
                    )
                    
                    while (true) {
                        input = getFormattedInput()
                        if (!initiatingPlayerNeedsMoney && input == nextPartVal) {
                            break
                        }
                        
                        val moneyAmount: Int? = input.stripCommas().toIntOrNull()
                        if (moneyAmount != null && moneyAmount in 1..otherPlayer.money) {
                            wantedItems.money = moneyAmount
                            break
                        }
                        
                        printInvalidInputText()
                    }
                    
                    println()
                }
                
                if (otherPlayer.hasAGetOffVacationCard) {
                    val question =
                        "${initiatingPlayer.name}, would you like any get off vacation free cards " +
                        "from ${otherPlayer.name}?"
                    
                    if (promptForYOrN(question)) {
                        val numGetOffVacationCardsString =
                            Misc.getNumGetOffVacationCardsString(otherPlayer.numGetOffVacationCards)
                        print("""
                            ${otherPlayer.name} has $numGetOffVacationCardsString. How many would you like?
                            Enter an amount or $nextPartSentencePart: """.trimIndent()
                        )
                        
                        while (true) {
                            input = getFormattedInput()
                            if (input == nextPartVal) break
                            
                            val cardsAmount: Int? = input.toIntOrNull()
                            if (cardsAmount != null && cardsAmount in 1..otherPlayer.numGetOffVacationCards) {
                                wantedItems.numGetOffVacationCards = cardsAmount
                                break
                            }
                            
                            printInvalidInputText()
                        }
                        
                        println()
                    }
                }
                
                if (otherPlayerHasAProp) {
                    val question =
                        "${initiatingPlayer.name}, would you like any properties from ${otherPlayer.name}?"
                    
                    if (promptForYOrN(question)) {
                        printBlankLnAnd(otherPlayerProps.getInfo(ownerOfProps = otherPlayer))
                        print("""
                            
                            Enter the positions of the properties you would like with each position separated
                            by whitespace or enter $nextPartSentencePart: """.trimIndent()
                        )
                        
                        while (true) {
                            input = getFormattedInput()
                            if (input == nextPartVal) break
                            
                            val selectedProps: PropertyGroups =
                                otherPlayerProps.filterByPositions(positionsInput = input)
                            if (selectedProps.isNotEmpty()) {
                                wantedItems.props = selectedProps
                                break
                            }
                            
                            printNoValidPositionsText()
                        }
                        
                        println()
                    }
                }
                
                if (wantedItems.aFieldChanged) break
                
                println("""
                    
                    ${initiatingPlayer.name}, you didn't select anything for what you would like.
                    Enter one of the following:
                    $tryAgainVal: Try to select something again
                    $goToStartOption
                    $cancelOption
                    """.trimIndent()
                )
                
                while (true) {
                    when (getFormattedInput()) {
                        tryAgainVal -> break
                        
                        goToStartVal -> continue@mainTradingLoop
                        
                        cancelVal -> return false
                        
                        else -> printInvalidInputText()
                    }
                }
            }
            
            
            // Part for the initiating player to select what to offer.
            
            val offeredItems = TradeItems(owner = initiatingPlayer)
            
            while (true) {
                if (!initiatingPlayerNeedsMoney && wantedItems.money == 0) {
                    val question =
                        "${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} any money?"
                    
                    if (promptForYOrN(question)) {
                        print("""
                            ${initiatingPlayer.name}, you have $${initiatingPlayer.moneyString}. How much would you like to offer?
                            Enter an amount, optionally with commas, or $nextPartSentencePart: """.trimIndent()
                        )
                        
                        while (true) {
                            input = getFormattedInput()
                            if (input == nextPartVal) break
                            
                            val moneyAmount: Int? = input.stripCommas().toIntOrNull()
                            if (moneyAmount != null && moneyAmount in 1..initiatingPlayer.money) {
                                offeredItems.money = moneyAmount
                                break
                            }
                            
                            printInvalidInputText()
                        }
                        
                        println()
                    }
                }
                
                if (initiatingPlayer.hasAGetOffVacationCard && wantedItems.numGetOffVacationCards == 0) {
                    val question =
                        "${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} " +
                        "any get off vacation free cards?"
                    
                    if (promptForYOrN(question)) {
                        val numGetOffVacationCardsString =
                            Misc.getNumGetOffVacationCardsString(initiatingPlayer.numGetOffVacationCards)
                        print("""
                            ${initiatingPlayer.name}, you have $numGetOffVacationCardsString. How many would you like to offer?
                            Enter an amount or $nextPartSentencePart: """.trimIndent()
                        )
                        
                        while (true) {
                            input = getFormattedInput()
                            if (input == nextPartVal) break
                            
                            val cardsAmount: Int? = input.toIntOrNull()
                            if (cardsAmount != null && cardsAmount in 1..initiatingPlayer.numGetOffVacationCards) {
                                offeredItems.numGetOffVacationCards = cardsAmount
                                break
                            }
                            
                            printInvalidInputText()
                        }
                        
                        println()
                    }
                }
                
                if (initiatingPlayerHasAProp) {
                    val question =
                        "${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} any properties?"
                    
                    if (promptForYOrN(question)) {
                        printBlankLnAnd(initiatingPlayerProps.getInfo(ownerOfProps = initiatingPlayer))
                        print("""
                            
                            Enter the positions of the properties you would like to offer with each position
                            separated by whitespace or enter $nextPartSentencePart: """.trimIndent()
                        )
                        
                        while (true) {
                            input = getFormattedInput()
                            if (input == nextPartVal) break
                            
                            val selectedProps: PropertyGroups =
                                initiatingPlayerProps.filterByPositions(positionsInput = input)
                            if (selectedProps.isNotEmpty()) {
                                offeredItems.props = selectedProps
                                break
                            }
                            
                            printNoValidPositionsText()
                        }
                        
                        println()
                    }
                }
                
                if (offeredItems.aFieldChanged) break
                
                println("""
                    
                    ${initiatingPlayer.name}, you didn't offer anything.
                    Enter one of the following:
                    $tryAgainVal: Try to offer something again
                    $goToStartOption
                    $cancelOption
                    """.trimIndent()
                )
                
                while (true) {
                    when (getFormattedInput()) {
                        tryAgainVal -> break
                        
                        goToStartVal -> continue@mainTradingLoop
                        
                        cancelVal -> return false
                        
                        else -> printInvalidInputText()
                    }
                }
            }
            
            
            // Part for the initiating player to confirm their selections.
            
            /*
            Put this text all the way to the left since the offeredItems and wantedItems
            strings might have new line chars and text after those has no indentation.
             */
            val tradeString = """
For this trade, ${initiatingPlayer.name} will give ${otherPlayer.name} the following:
$offeredItems


${otherPlayer.name} will give ${initiatingPlayer.name} the following:
$wantedItems


If one player owns all the streets in a neighborhood and at least one of those streets
get traded, then the restaurants on the streets in that neighborhood will be sold.
""".trimIndent()
            
            val confirmAndAcceptOptionsString = """
                Enter "y", "n", or one of the following:
                $seePropInfoOption
                $seePlayerInfoOption
                """.trimIndent()
            
            initiatingPlayerConfirmLoop@ while (true) {
                printBlankLnAnd(tradeString)
                printBlankLnAnd("${initiatingPlayer.name}, do you confirm the trade?")
                println(confirmAndAcceptOptionsString)
                
                while (true) {
                    when (getFormattedInput()) {
                        "y" -> break@initiatingPlayerConfirmLoop
                        
                        "n" -> {
                            printBlankLnAnd("""
                                ${initiatingPlayer.name}, enter one of the following:
                                $goToStartOption
                                $cancelOption
                                """.trimIndent()
                            )
                            
                            while (true) {
                                when (getFormattedInput()) {
                                    goToStartVal -> continue@mainTradingLoop
                                    
                                    cancelVal -> return false
                                    
                                    else -> printInvalidInputText()
                                }
                            }
                        }
                        
                        seePropInfoVal -> {
                            askAboutSeeingPropInfo(playerWhoWantsToKnow = initiatingPlayer)
                            continue@initiatingPlayerConfirmLoop
                        }
                        
                        seePlayerInfoVal -> {
                            printBlankLnAnd(playerManager.getPlayerInfo())
                            continue@initiatingPlayerConfirmLoop
                        }
                        
                        else -> printInvalidInputText()
                    }
                }
            }
            
            
            // Part for the other player to accept or deny the trade and make the changes if accepted.
            
            otherPlayerAcceptLoop@ while (true) {
                printBlankLnAnd("${otherPlayer.name}, do you accept the trade?")
                println(confirmAndAcceptOptionsString)
                
                while (true) {
                    when (getFormattedInput()) {
                        "y" -> {
                            offeredItems.transferItemsTo(otherPlayer)
                            wantedItems.transferItemsTo(initiatingPlayer)
                            return true
                        }
                        
                        "n" -> return false
                        
                        seePropInfoVal -> {
                            askAboutSeeingPropInfo(playerWhoWantsToKnow = otherPlayer)
                            printBlankLnAnd(tradeString)
                            continue@otherPlayerAcceptLoop
                        }
                        
                        seePlayerInfoVal -> {
                            printBlankLnAnd(playerManager.getPlayerInfo())
                            printBlankLnAnd(tradeString)
                            continue@otherPlayerAcceptLoop
                        }
                        
                        else -> printInvalidInputText()
                    }
                }
            }
        }
    }
}
