package singletonopoly

import kotlin.random.Random
import kotlin.random.nextInt
import singletonopoly.Misc.toStringWithCommas
import singletonopoly.actiondeck.*
import singletonopoly.board.*
import singletonopoly.board.properties.*
import singletonopoly.players.*

fun printBlankLnAnd(obj: Any) {
    println()
    println(obj)
}

fun getFormattedInput(): String = readln().trim().lowercase()

const val INVALID_INPUT_TEXT = "Invalid input"

fun printInvalidInput() {
    printBlankLnAnd(INVALID_INPUT_TEXT)
}

fun promptForYOrN(message: String): Boolean {
    while (true) {
        printBlankLnAnd("$message (y/n)")
        
        when (getFormattedInput()) {
            "y" -> return true
            
            "n" -> return false
        }
        
        printInvalidInput()
    }
}

fun main() {
    do {
        Game()
    } while (promptForYOrN("Would you like to play again?"))
}

/**
 * Class with data and functions for a game of Singletonopoly. A game is started upon instantiation of this class.
 */
class Game {
    val playerManager: PlayerManager
    var curPlayer: Player
    var numDoubleRollsInARow = 0
    var goAgain = false
    var turnOver = false
    var gameOver = false
    val actionDeck = ActionDeck()
    
    /**
     * This is used for user input and having it be a member of the Game class allows us to not have
     * to declare a variable every time we need to store user input.
     */
    lateinit var input: String
    
    // Valid input values and options for input options used in multiple functions.
    val seePropInfoVal = "pr"
    val seePropInfoOption = "$seePropInfoVal: See property info"
    
    val seePlayersInfoVal = "pl"
    val seePlayersInfoOption = "$seePlayersInfoVal: See players info"
    
    val makeTradeVal = "tr"
    val makeTradeOption = "$makeTradeVal: Make a trade with another player"
    
    val pawnVal = "pn"
    val pawnOption = "$pawnVal: Pawn your properties"
    
    val removeRestaurantsVal = "rm"
    val removeRestaurantsOption = "$removeRestaurantsVal: Remove restaurants from your streets"
    
    
    init {
        println("You started a game of Singletonopoly.")
        
        getOrderedPlayerNames().let {
            printBlankLnAnd("The order of the players is:")
            for (name in it) println(name)
            
            playerManager = PlayerManager(it)
            curPlayer = playerManager.curPlayer
        }
        
        while (true) {
            printBlankLnAnd("It's ${curPlayer.name}'s turn.")
            
            // The goAgain, turnOver, and gameOver variables might change during the functions that get called.
            askAboutPreRollAction()
            if (gameOver) break
            if (!turnOver) {
                curPlayer.numTurnsOnVacation?.let(::askAboutVacationAction)
            }
            if (!turnOver) rollDiceAndMove()
            if (gameOver) break
            
            // Prepare for next turn.
            turnOver = false
            if (goAgain) {
                goAgain = false
            } else {
                curPlayer = playerManager.switchToNextPlayer()
            }
            printBlankLnAnd("End of turn. Enter \"c\" to continue.")
            while (getFormattedInput() != "c") { }
        }
        
        // Unless the game is ended early, there should be a winner. Print this message if there's a winner.
        playerManager.winnerName?.let { printBlankLnAnd("The winner is $it!") }
        
        // Make properties unowned in case another game is started.
        PropertyGroups.makePropertiesUnowned()
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
        val numPlayers: Int
        
        while (true) {
            print("\nHow many players? Have this number be >= 2 && <= 8: ")
            
            val inputInt: Int? = getFormattedInput().toIntOrNull()
            if (inputInt != null && inputInt in 2..8) {
                numPlayers = inputInt
                break
            }
            
            printInvalidInput()
        }
        
        class NameAndRollTotal(val name: String, val diceRollTotal: Int)
        
        val playerNamesAndRollTotals = ArrayList<NameAndRollTotal>(numPlayers)
        
        while (playerNamesAndRollTotals.size < numPlayers) {
            val playerNum: Int = playerNamesAndRollTotals.size + 1
            val defaultName = "Player $playerNum"
            printBlankLnAnd(
                "Enter a unique name for player $playerNum or enter nothing for \"$defaultName\"."
            )
            input = readln().trim()
            
            if (playerNamesAndRollTotals.any { it.name == input }) {
                println("$input is already a name for another player. You must enter a unique name.")
                continue
            }
            
            val name: String = input.ifEmpty { defaultName }
            with(DiceRoll()) {
                println("$name got a $die1Val and a $die2Val for their beginning dice roll for a total of $total.")
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
        printBlankLnAnd("${curPlayer.name} dropped out so their properties are now unowned.")
        PropertyGroups.makePropertiesUnowned(owner = curPlayer)
    }
    
    
    /**
     * Asks the current player about any actions they would like to do before they roll.
     */
    fun askAboutPreRollAction() {
        val actionPropGroups = ActionPropertyGroups.forPropsOwnedBy(curPlayer)
        
        // Valid input values
        val takeTurnVal = "tt"
        val seeBoardSpacesVal = "bd"
        val dropOutVal = "dr"
        val endGameVal = "eg"
        val unpawnVal = "up"
        val addRestaurantVal = "ar"
        
        val optionsStart =
            """
            ${curPlayer.name}, what would you like to do?
            Enter one of the following:
            
            $takeTurnVal: Take your turn
            $seeBoardSpacesVal: See the spaces on the board
            $seePropInfoOption
            $seePlayersInfoOption
            $makeTradeOption
            $dropOutVal: Drop out of the game
            $endGameVal: End the game
            """.trimIndent()
        
        while (true) {
            printBlankLnAnd(optionsStart)
            if (actionPropGroups.hasPawnable) {
                println(pawnOption)
            }
            if (actionPropGroups.hasUnpawnable) {
                println("$unpawnVal: Unpawn your properties")
            }
            if (actionPropGroups.hasRestaurantAddable) {
                println("$addRestaurantVal: Add restaurants to your streets")
            }
            if (actionPropGroups.hasRestaurantRemovable) {
                println(removeRestaurantsOption)
            }
            
            input = getFormattedInput()
            
            when (input) {
                takeTurnVal -> return
                
                seeBoardSpacesVal -> printBlankLnAnd(Board.SPACES_STRING)
                
                seePropInfoVal -> askAboutSeeingPropInfo(playerWhoWantsToKnow = curPlayer)
                
                seePlayersInfoVal -> printBlankLnAnd(playerManager.getPlayersInfo())
                
                makeTradeVal -> {
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
                    val message = "${curPlayer.name}, are you sure that you want to drop out?"
                    if (promptForYOrN(message)) {
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
                    val message = "Are you sure that you want to end the game?"
                    if (promptForYOrN(message)) {
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
                        printInvalidInput()
                    }
                }
                
                unpawnVal -> {
                    if (actionPropGroups.hasUnpawnable) {
                        if (
                            askAboutUnpawning(
                                unpawnableProps = actionPropGroups.unpawnable
                            )
                        ) {
                            actionPropGroups.refresh()
                        }
                    } else {
                        printInvalidInput()
                    }
                }
                
                addRestaurantVal -> {
                    if (actionPropGroups.hasRestaurantAddable) {
                        if (
                            askAboutAddingRestaurants(
                                restaurantAddableStreets = actionPropGroups.restaurantAddable
                            )
                        ) {
                            actionPropGroups.refresh()
                        }
                    } else {
                        printInvalidInput()
                    }
                }
                
                removeRestaurantsVal -> {
                    if (actionPropGroups.hasRestaurantRemovable) {
                        if (
                            askAboutRemovingRestaurants(
                                player = curPlayer,
                                restaurantRemovableStreets = actionPropGroups.restaurantRemovable
                            )
                        ) {
                            actionPropGroups.refresh()
                        }
                    } else {
                        printInvalidInput()
                    }
                }
                
                else -> printInvalidInput()
            }
        }
    }
    
    
    fun handleCurPlayerMakingRevolution() {
        printBlankLnAnd("${curPlayer.name} made a revolution!")
        curPlayer.incrementMoneyAndPrintUpdate(512)
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
    
    fun handleGoOnVacation() {
        curPlayer.sendToVacation()
        turnOver = true
        
        // If the player rolled doubles, they won't get to go again in this situation.
        goAgain = false
        numDoubleRollsInARow = 0
    }
    
    
    fun rollDiceAndMove() {
        val diceRoll = DiceRoll()
        with(diceRoll) {
            println("${curPlayer.name} rolled a $die1Val and a $die2Val, for a total of $total.")
        }
        
        if (diceRoll.isDoubles) {
            if (++numDoubleRollsInARow == 3) {
                println("${curPlayer.name} rolled doubles thrice, or 3 times, in a row so they go to Vacation.")
                handleGoOnVacation()
                return
            }
            
            println("These are doubles so they get to go again.")
            goAgain = true
        } else {
            numDoubleRollsInARow = 0
        }
        
        moveCurPlayerAhead(numSpaces = diceRoll.total)
    }
    
    
    /**
     * Evaluates the position that the current player landed on and takes appropriate action.
     */
    fun evaluatePosition() {
        print("${curPlayer.name} landed on position ${curPlayer.position}, which is ")
        
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
                println("\"$curBoardSpace\".")
                
                when (curBoardSpace) {
                    NonPropertySpace.START,
                    NonPropertySpace.VACATION,
                    NonPropertySpace.BREAK_TIME -> println("This space has no effect.")
                    
                    NonPropertySpace.GO_ON_VACATION -> handleGoOnVacation()
                    
                    NonPropertySpace.DRAW_ACTION_CARD -> handleLandingOnDrawActionCard()
                }
            }
            
            else -> Misc.printError(
                "curPlayer.position = ${curPlayer.position} and this caused Board.getSpace to return $curBoardSpace"
            )
        }
    }
    
    
    fun handlePlayerDroppingOutAndPropsBecomeOwnedByAnotherPlayer(
        dropOutPlayer: Player,
        newPropsOwner: Player
    ) {
        printBlankLnAnd(
            "${dropOutPlayer.name} dropped out so their money and properties " +
            "will go to ${newPropsOwner.name}."
        )
        newPropsOwner.incrementMoneyAndPrintUpdate(dropOutPlayer.money)
        PropertyGroups.transferOwnership(curOwner = dropOutPlayer, newOwner = newPropsOwner)
    }
    
    fun printGotMoneyMessage(player: Player = curPlayer, messageEnd: String) {
        printBlankLnAnd("${player.name} got the money to $messageEnd.")
    }
    
    
    fun handleLandingOnOwnedProp(prop: Property, owner: Player) {
        // These print statements are a continuation of print statements from evaluatePosition.
        print(", which is owned by ")
        if (owner === curPlayer) {
            println("themself.")
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
                        "There${if (it == 1) "'s 1 restaurant" else " are $it restaurants"} on " +
                        "this street and the fee is $${fee.toStringWithCommas()}."
                    }
                )
            }
            
            is GolfClub -> {
                val feeData: GolfClub.FeeData = prop.FeeData()
                fee = feeData.fee
                val numClubsString = Misc.getNumItemsString(feeData.numClubsOwnedByOwner, "golf club")
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
                println(
                    " so ${curPlayer.name} has to roll the dice and pay ${feeData.multiplier} " +
                    "times that amount."
                )
                with(diceRoll) {
                    println(
                        "${curPlayer.name} rolled a $die1Val and a $die2Val for a total of $total " +
                        "so they have to pay $$fee."
                    )
                }
            }
        }
        
        if (curPlayer.money < fee) {
            if (
                !askAboutGettingMoney(
                    player = curPlayer,
                    moneyNeeded = fee,
                    playerHasChoice = false
                )
            ) {
                // Following condition is true if there are at least 2 players remaining.
                if (!gameOver) {
                    println()
                    handlePlayerDroppingOutAndPropsBecomeOwnedByAnotherPlayer(
                        dropOutPlayer = curPlayer,
                        newPropsOwner = owner
                    )
                }
                
                return
            }
            
            println()
            printGotMoneyMessage(messageEnd = "pay the fee to ${owner.name}")
        }
        
        curPlayer.decrementMoneyAndPrintUpdate(fee)
        owner.incrementMoneyAndPrintUpdate(fee)
    }
    
    
    fun handleLandingOnUnownedProp(prop: Property) {
        // This print statement is a continuation of print statements from evaluatePosition.
        println(", which is unowned.")
        val purchasePrice: Int = prop.purchasePrice
        val message =
            "${curPlayer.name}, you have $${curPlayer.moneyString}. Would you like to buy " +
            "${prop.name} for $${prop.purchasePriceString}?"
        
        if (promptForYOrN(message)) {
            if (curPlayer.money < purchasePrice) {
                if (
                    !askAboutGettingMoney(
                        player = curPlayer,
                        moneyNeeded = purchasePrice,
                        playerHasChoice = true
                    )
                ) {
                    printBlankLnAnd("${curPlayer.name} isn't buying ${prop.name}.")
                    return
                }
                
                println()
                printGotMoneyMessage(messageEnd = "buy ${prop.name}")
            }
            
            prop.owner = curPlayer
            curPlayer.decrementMoneyAndPrintUpdate(purchasePrice)
        }
    }
    
    
    fun handleLandingOnDrawActionCard() {
        val topCard: ActionCard = actionDeck.topCard
        println("${curPlayer.name} drew a card from the Action Deck and it says \"${topCard.message}\".")
        
        // Get off Vacation free cards are the only type of card where the Action Deck top card
        // won't get moved to the bottom.
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
                            playerHasChoice = false
                        )
                    ) {
                        // Following condition is true if there are at least 2 players remaining.
                        if (!gameOver) {
                            handleCurPlayerDroppingOutAndPropsBecomeUnowned()
                        }
                        return
                    }
                    
                    printGotMoneyMessage(messageEnd = "handle the money loss action card")
                }
                
                curPlayer.decrementMoneyAndPrintUpdate(lossAmount)
            }
            
            is PayMoneyToOtherPlayersCard -> {
                val moneyToPayEachPlayer: Int = topCard.moneyToPayEachPlayer
                val otherPlayers: List<Player> =
                    playerManager.getOtherPlayersInGame(excludingPlayer = curPlayer)
                val totalMoneyToPay: Int = moneyToPayEachPlayer * otherPlayers.size
                
                if (curPlayer.money < totalMoneyToPay) {
                    if (
                        !askAboutGettingMoney(
                            player = curPlayer,
                            moneyNeeded = totalMoneyToPay,
                            playerHasChoice = false
                        )
                    ) {
                        // Following condition is true if there are at least 2 players remaining.
                        if (!gameOver) {
                            handleCurPlayerDroppingOutAndPropsBecomeUnowned()
                        }
                        return
                    }
                    
                    printGotMoneyMessage(messageEnd = "pay other players")
                }
                
                for (otherPlayer in otherPlayers) {
                    curPlayer.decrementMoneyAndPrintUpdate(moneyToPayEachPlayer)
                    otherPlayer.incrementMoneyAndPrintUpdate(moneyToPayEachPlayer)
                }
            }
            
            is GetMoneyFromOtherPlayersCard -> {
                val moneyToGetFromEachPlayer: Int = topCard.moneyToGetFromEachPlayer
                val otherPlayers: List<Player> =
                    playerManager.getOtherPlayersInGame(excludingPlayer = curPlayer)
                
                for (otherPlayer in otherPlayers) {
                    if (otherPlayer.money < moneyToGetFromEachPlayer) {
                        if (
                            !askAboutGettingMoney(
                                player = otherPlayer,
                                moneyNeeded = moneyToGetFromEachPlayer,
                                playerHasChoice = false
                            )
                        ) {
                        
                            // Following condition is true if there's 1 player remaining.
                            if (gameOver) return
                            
                            println()
                            handlePlayerDroppingOutAndPropsBecomeOwnedByAnotherPlayer(
                                dropOutPlayer = otherPlayer,
                                newPropsOwner = curPlayer
                            )
                            continue
                        }
                        
                        printGotMoneyMessage(player = otherPlayer, messageEnd = "pay ${curPlayer.name}")
                    }
                    
                    otherPlayer.decrementMoneyAndPrintUpdate(moneyToGetFromEachPlayer)
                    curPlayer.incrementMoneyAndPrintUpdate(moneyToGetFromEachPlayer)
                }
            }
            
            is RestaurantMaintenanceCard -> {
                val numRestaurantsOwned: Int = PropertyGroups.getNumRestaurantsOwnedBy(curPlayer)
                println()
                if (numRestaurantsOwned == 0) {
                    println("${curPlayer.name} doesn't own any restaurants so they don't have to pay anything.")
                    return
                }
                
                val feePerRestaurant: Int = topCard.feePerRestaurant
                val maintenanceFee: Int = feePerRestaurant * numRestaurantsOwned
                val numRestaurantsString = Misc.getNumItemsString(numRestaurantsOwned, "restaurant")
                
                println(
                    "${curPlayer.name} has $numRestaurantsString and must pay $$feePerRestaurant " +
                    "per restaurant so they owe $${maintenanceFee.toStringWithCommas()}."
                )
                
                if (curPlayer.money < maintenanceFee) {
                    if (
                        !askAboutGettingMoney(
                            player = curPlayer,
                            moneyNeeded = maintenanceFee,
                            playerHasChoice = false
                        )
                    ) {
                        // Following condition is true if there are at least 2 players remaining.
                        if (!gameOver) {
                            handleCurPlayerDroppingOutAndPropsBecomeUnowned()
                        }
                        return
                    }
                    
                    printGotMoneyMessage(messageEnd = "pay the maintenance fee")
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
    
    
    fun askAboutVacationAction(numTurnsOnVacation: Int) {
        val turnString =
            numTurnsOnVacation.let {
                when (it) {
                    0 -> "first"
                    1 -> "second"
                    2 -> "third (last)"
                    else -> {
                        Misc.printError("${curPlayer.name} was on Vacation with a numTurnsOnVacation value of $it")
                        (it + 1).toString()
                    }
                }
            }
        
        // Valid input values
        val payVal = "py"
        val rollVal = "rl"
        val getOffVacationCardVal = "gt"
        
        val feeToGetOffVacation = 128
        
        val optionsString =
            """
            ${curPlayer.name}, you're on Vacation and this is your $turnString turn on Vacation.
            What would you like to do? Type one of the following and press Enter:
            $payVal: Pay $$feeToGetOffVacation to get off Vacation
            $rollVal: Try to roll doubles
            """
            .trimIndent()
            .plus(
                if (curPlayer.hasAGetOffVacationCard) {
                    "\n$getOffVacationCardVal: Use a get off Vacation free card"
                } else ""
            )
        
        while (true) {
            printBlankLnAnd(optionsString)
            
            when (getFormattedInput()) {
                payVal -> {
                    if (curPlayer.money < feeToGetOffVacation) {
                        if (
                            !askAboutGettingMoney(
                                player = curPlayer,
                                moneyNeeded = feeToGetOffVacation,
                                playerHasChoice = true
                            )
                        ) {
                            continue
                        }
                        
                        printGotMoneyMessage(messageEnd = "get off Vacation")
                    }
                    
                    printBlankLnAnd("${curPlayer.name} chose to pay so they get to take their turn.")
                    curPlayer.decrementMoneyAndPrintUpdate(feeToGetOffVacation)
                    curPlayer.removeFromVacation()
                    return
                }
                
                rollVal -> with(DiceRoll()) {
                    printBlankLnAnd("${curPlayer.name} rolled a ${die1Val} and a ${die2Val}.")
                    
                    when {
                        isDoubles -> {
                            println("These are doubles so they're off Vacation and get to advance ${total} spaces.")
                            curPlayer.removeFromVacation()
                            moveCurPlayerAhead(numSpaces = total)
                        }
                        
                        numTurnsOnVacation == 2 -> {
                            println("${curPlayer.name} spent 3 turns on Vacation so it's now over.")
                            curPlayer.removeFromVacation()
                        }
                        
                        else -> curPlayer.numTurnsOnVacation = numTurnsOnVacation + 1
                    }
                    
                    turnOver = true
                    return
                }
                
                getOffVacationCardVal -> {
                    if (curPlayer.hasAGetOffVacationCard) {
                        printBlankLnAnd(
                            "${curPlayer.name} chose to use a get off Vacation free card so they " +
                            "get to take their turn."
                        )
                        curPlayer.removeFromVacation()
                        curPlayer.decrementNumGetOffVacationCardsAndPrintUpdate()
                        actionDeck.insertGetOffVacationCardAtBottom()
                        return
                    }
                }
            }
            
            printInvalidInput()
        }
    }
    
    
    fun askAboutSeeingPropInfo(playerWhoWantsToKnow: Player) {
        // Valid input values
        val allPropsInfoValue = "ap"
        val playerPropsInfoValue = "yp"
        
        val optionsString =
            """
            ${playerWhoWantsToKnow.name}, enter one of the following:
            
            $allPropsInfoValue: See info for all properties
            $playerPropsInfoValue: See info for your properties
            b: Go back
            """.trimIndent()
        
        val allPropsInfo: String by lazy { PropertyGroups.getInfoForAllProps() }
        val playerPropsInfo: String by lazy {
            PropertyGroups.getInfoForPropsOwnedBy(playerWhoWantsToKnow)
        }
        
        while (true) {
            printBlankLnAnd(optionsString)
            input = getFormattedInput()
            if (input == "b") return
            
            printBlankLnAnd(
                when (input) {
                    allPropsInfoValue -> allPropsInfo
                    
                    playerPropsInfoValue -> playerPropsInfo
                    
                    else -> INVALID_INPUT_TEXT
                }
            )
        }
    }
    
    
    
    fun printInvalidPositionsText() {
        printBlankLnAnd("No valid positions were entered.")
    }
    
    
    fun askAboutPawningOrUnpawning(
        askAboutPawning: Boolean,
        player: Player,
        pawnableOrUnpawnableProps: PropertyGroups
    ): Boolean {
        val propsInfoHeading =
            "Here are the properties owned by ${player.name} that can be ${if (askAboutPawning) "" else "un"}pawned:"
        val propsInfo: String = pawnableOrUnpawnableProps.getInfo(heading = propsInfoHeading)
        
        while (true) {
            printBlankLnAnd(propsInfo)
            printBlankLnAnd(
                "${player.name}, enter the positions of the properties you would like to " +
                "${if (askAboutPawning) "" else "un"}pawn with each position separated by whitespace " +
                "or enter \"b\" to go back:"
            )
            
            input = getFormattedInput()
            if (input == "b") return false
            
            val selectedProps: PropertyGroups =
                pawnableOrUnpawnableProps.filterByPositions(positionsInput = input)
            if (selectedProps.isEmpty()) {
                printInvalidPositionsText()
                continue
            }
            
            val selectedPropsList: List<Property> = selectedProps.toPropList()
            val totalMoneyGainOrPrice: Int =
                selectedPropsList.sumOf { if (askAboutPawning) it.pawnGain else it.unpawnPrice }
            
            val confirmationStringEnd =
                """
                $${totalMoneyGainOrPrice.toStringWithCommas()} will ${if (askAboutPawning) "be gained" else "have to be paid"}.
                ${player.name}, do you confirm this?
                """.trimIndent()
            
            val selectedPropsConfirmationString =
                selectedProps
                .getInfo(
                    heading = "Here are the selected properties:",
                    end = confirmationStringEnd
                )
            
            if (!promptForYOrN(selectedPropsConfirmationString)) continue
            
            if (askAboutPawning) {
                selectedPropsList.forEach { it.pawn() }
                player.incrementMoneyAndPrintUpdate(totalMoneyGainOrPrice)
            } else {
                if (player.money < totalMoneyGainOrPrice) {
                    if (
                        !askAboutGettingMoney(
                            player = player,
                            moneyNeeded = totalMoneyGainOrPrice,
                            playerHasChoice = true
                        )
                    ) {
                        printBlankLnAnd("${player.name} will be taken back to the properties selection section.")
                        continue
                    }
                    
                    printGotMoneyMessage(player = player, messageEnd = "unpawn their properties")
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
    
    
    fun askAboutAddingOrRemovingRestaurants(
        askAboutAdding: Boolean,
        player: Player,
        restaurantAddableOrRemovableStreets: StreetGroups
    ): Boolean {
        val streetsInfoHeading =
            "Here are the streets owned by ${player.name} that can have a restaurant " +
            "${if (askAboutAdding) "added" else "removed"}:"
        
        val streetsInfo =
            restaurantAddableOrRemovableStreets
            .getInfo(heading = streetsInfoHeading)
            .toString()
        
        while (true) {
            printBlankLnAnd(streetsInfo)
            printBlankLnAnd(
                "${player.name}, enter the positions of the streets you would like to " +
                (if (askAboutAdding) "add a restaurant to" else "remove a restaurant from") +
                " with each position separated by whitespace or enter \"b\" to go back:"
            )
            
            input = getFormattedInput()
            if (input == "b") return false
            
            val selectedStreets: StreetGroups =
                restaurantAddableOrRemovableStreets.filterByPositions(positionsInput = input)
            if (selectedStreets.isEmpty()) {
                printInvalidPositionsText()
                continue
            }
            
            val selectedStreetsList: List<Street> = selectedStreets.toStreetList()
            val totalMoneyGainOrPrice: Int =
                selectedStreetsList.sumOf {
                    if (askAboutAdding) it.restaurantAddingPrice else it.restaurantRemovalGain
                }
            
            val confirmationStringEnd =
                """
                $${totalMoneyGainOrPrice.toStringWithCommas()} will ${if (askAboutAdding) "have to be paid" else "be gained"}.
                ${player.name}, do you confirm this?
                """.trimIndent()
            
            val selectedStreetsConfirmationString =
                selectedStreets
                .getInfo(
                    heading = "Here are the selected streets:",
                    end = confirmationStringEnd
                )
                .toString()
            
            if (!promptForYOrN(selectedStreetsConfirmationString)) continue
            
            if (!askAboutAdding) {
                selectedStreetsList.forEach { it.removeRestaurant() }
                player.incrementMoneyAndPrintUpdate(totalMoneyGainOrPrice)
            } else {
                if (player.money < totalMoneyGainOrPrice) {
                    if (
                        !askAboutGettingMoney(
                            player = player,
                            moneyNeeded = totalMoneyGainOrPrice,
                            playerHasChoice = true
                        )
                    ) {
                        printBlankLnAnd("${player.name} will be taken back to the streets selection section.")
                        continue
                    }
                    
                    printGotMoneyMessage(player = player, messageEnd = "add restaurants to their streets")
                }
                
                selectedStreetsList.forEach { it.addRestaurant() }
                player.decrementMoneyAndPrintUpdate(totalMoneyGainOrPrice)
            }
            
            return true
        }
    }
    
    // Only the current player is able to add restaurants.
    fun askAboutAddingRestaurants(restaurantAddableStreets: StreetGroups): Boolean =
        askAboutAddingOrRemovingRestaurants(askAboutAdding = true, curPlayer, restaurantAddableStreets)
    
    fun askAboutRemovingRestaurants(player: Player, restaurantRemovableStreets: StreetGroups): Boolean =
        askAboutAddingOrRemovingRestaurants(askAboutAdding = false, player, restaurantRemovableStreets)
    
        
    
    /**
     * This is the function for implementing the Money Getting Section described in the manual. This function
     * asks a player what they want to do if they need money for the situation they're in and don't have it.
     * It also makes necessary changes.
     *
     * The playerHasChoice param should be true if a player chooses to do something and they don't have enough
     * money to do that thing. For example, adding a restaurant to one of their streets. For this situation,
     * this function can be exited by having the player either choose to exit or gather all the money that is
     * needed. This param should be false if the player is in a situation where they need money and they
     * didn't choose to be in that situation. For example, if the player lands on an owned property and doesn't
     * have all the money they need to pay the fee. For this situation, this function can be exited
     * by having the player either gather the money needed or drop out of the game.
     *
     * Returns true if the player gets the money needed and false otherwise.
     */
    fun askAboutGettingMoney(player: Player, moneyNeeded: Int, playerHasChoice: Boolean): Boolean {
        val actionPropGroups = ActionPropertyGroups.forPropsOwnedBy(player)
        
        // Valid input value
        val exitVal = "ex"
        
        while (player.money < moneyNeeded) {
            printBlankLnAnd(
                """
                ${player.name}, you need $${moneyNeeded.toStringWithCommas()} and currently have $${player.moneyString}.
                Select one of the following options to get more money:
                $exitVal: ${if (playerHasChoice) "Go back" else "Drop out of the game"}
                $makeTradeOption
                """.trimIndent()
            )
            if (actionPropGroups.hasPawnable) {
                println(pawnOption)
            }
            if (actionPropGroups.hasRestaurantRemovable) {
                println(removeRestaurantsOption)
            }
            
            when (getFormattedInput()) {
                exitVal -> {
                    if (!playerHasChoice) {
                        player.removeFromGame()
                        if (playerManager.onePlayerIsInGame) {
                            gameOver = true
                        }
                    }
                    return false
                }
                
                makeTradeVal -> {
                    if (askAboutTrading(initiatingPlayer = player, initiatingPlayerNeedsMoney = true)) {
                        actionPropGroups.refresh()
                    }
                }
                
                pawnVal -> {
                    if (actionPropGroups.hasPawnable) {
                        if (askAboutPawning(player = player, pawnableProps = actionPropGroups.pawnable)) {
                            actionPropGroups.refresh()
                        }
                    } else {
                        printInvalidInput()
                    }
                }
                
                removeRestaurantsVal -> {
                    if (actionPropGroups.hasRestaurantRemovable) {
                        if (
                            askAboutRemovingRestaurants(
                                player = player,
                                restaurantRemovableStreets = actionPropGroups.restaurantRemovable
                            )
                        ) {
                            actionPropGroups.refresh()
                        }
                    } else {
                        printInvalidInput()
                    }
                }
                
                else -> printInvalidInput()
            }
        }
        
        return true
    }
    
    
    /**
     * When a player wants to make a trade, they will have to select what they want to offer and what they
     * want to receive. These can include money, properties, and get off Vacation free cards. This class
     * is used as a data structure for those.
     */
    class TradeItems(private val owner: Player) {
        var money = 0
        
        var props: PropertyGroups? = null
        
        var numGetOffVacationCards = 0
        /**
         * Is true when one of the fields is different than when this object was instantiated.
         */
        val somethingHasBeenChanged: Boolean
            get() = money != 0 || props != null || numGetOffVacationCards != 0
        
        override fun toString() =
            buildList(capacity = 3) {
                if (money > 0) add('$' + money.toStringWithCommas())
                
                props?.let {
                    val ownedNHoodMessage =
                        "If a street in an owned neighborhood gets traded, then that neighborhood will " +
                        "no longer be owned so all restaurants in it will be sold and the street fees " +
                        "will go back to the starting fees."
                    
                    add(
                        it.getInfo(
                            heading = "The following properties:",
                            end = ownedNHoodMessage,
                            sayIfNHoodIsOwned = true
                        )
                    )
                }
                
                numGetOffVacationCards.let {
                    if (it > 0) {
                        add(Misc.getNumGetOffVacationCardsString(it))
                    }
                }
            }
            .joinToString(separator = "\n\n")
        
        fun transferItemsTo(newOwner: Player) {
            if (money > 0) {
                owner.decrementMoneyAndPrintUpdate(money)
                newOwner.incrementMoneyAndPrintUpdate(money)
            }
            
            props?.toPropList()?.forEach { prop ->
                prop.owner = newOwner
                if (prop !is Street) return@forEach
                
                val nHood = prop.nHood
                if (nHood.numRestaurants == 0) return@forEach
                
                printBlankLnAnd(
                    if (nHood.numRestaurants == 1) {
                        "There's 1 restaurant in the ${nHood.friendlyName} neighborhood so this will be sold."
                    } else {
                        "There are ${nHood.numRestaurants} restaurants in the ${nHood.friendlyName} " +
                        "neighborhood so these will be sold."
                    }
                )
                
                nHood.removeAllRestaurants()
                val moneyGain: Int = nHood.numRestaurants * nHood.restaurantRemovalGain
                owner.incrementMoneyAndPrintUpdate(moneyGain)
            }
            
            numGetOffVacationCards.let {
                if (it > 0) {
                    owner.decrementNumGetOffVacationCardsAndPrintUpdate(it)
                    newOwner.incrementNumGetOffVacationCardsAndPrintUpdate(it)
                }
            }
        }
    }
    
    /**
     * Interacts with the initiating player about trading. This includes asking who they want to trade with and
     * what they want to trade. If a trade is agreed upon, the necessary changes are made.
     *
     * If the initiatingPlayerNeedsMoney param is true, the initiating player will need to select a money amount
     * they want from the player they want to make a trade with and they won't be able to offer money themselves.
     *
     * Returns true if a trade was made and false otherwise.
     */
    fun askAboutTrading(initiatingPlayer: Player, initiatingPlayerNeedsMoney: Boolean): Boolean {
         */
        
        val initiatingPlayerProps = PropertyGroups.forPropsOwnedBy(initiatingPlayer)
        val initiatingPlayerHasAProp: Boolean = initiatingPlayerProps.isNotEmpty()
        
        // Let otherPlayersMap be a Map where the keys are stringified ints that start at 1 and the values are Players.
        val otherPlayersMap: Map<String, Player> =
            playerManager
            .getOtherPlayersInGame(excludingPlayer = initiatingPlayer)
            .withIndex()
            .associate { (index: Int, player) -> (index + 1).toString() to player }
        
        // Valid input values and options for multiple parts of the trade process.
        val cancelVal = "cn"
        val cancelOption = "$cancelVal: Cancel the trade"
        
        val goToStartVal = "st"
        val goToStartOption = "$goToStartVal: Go to the start of the trade process"
        
        val nextPartVal = "nx"
        val nextPartSentencePart = "\"$nextPartVal\" to go to the next part of the trade process"
        
        val first3OptionsString =
            """
            ${initiatingPlayer.name}, enter one of the following:
            $seePropInfoOption
            $seePlayersInfoOption
            $cancelOption
            """.trimIndent()
        
        val makeTradeOptionsString =
            otherPlayersMap
            .asIterable()
            .joinToString(
                transform = { (intString, player) -> "${intString}: Make a trade with ${player.name}" },
                separator = "\n"
            )
        
        mainTradingLoop@ while (true) {
            val otherPlayer: Player
            
            while (true) {
                printBlankLnAnd(first3OptionsString)
                println(makeTradeOptionsString)
                input = getFormattedInput()
                
                when (input) {
                    seePropInfoVal ->
                        askAboutSeeingPropInfo(playerWhoWantsToKnow = initiatingPlayer)
                    
                    seePlayersInfoVal -> printBlankLnAnd(playerManager.getPlayersInfo())
                    
                    cancelVal -> return false
                    
                    else -> {
                        // Check if a valid number was entered. If so, set otherPlayer to the correct player.
                        val nullableOtherPlayer: Player? = otherPlayersMap[input]
                        if (nullableOtherPlayer != null) {
                            otherPlayer = nullableOtherPlayer
                            break
                        }
                        printInvalidInput()
                    }
                }
            }
            
            val otherPlayerProps = PropertyGroups.forPropsOwnedBy(otherPlayer)
            val otherPlayerHasAProp: Boolean = otherPlayerProps.isNotEmpty()
            
            // Prevent the initiating player from continuing if there's nothing that can be traded besides money.
            if (!initiatingPlayerHasAProp && !initiatingPlayer.hasAGetOffVacationCard &&
                !otherPlayerHasAProp && !otherPlayer.hasAGetOffVacationCard
            ) {
                printBlankLnAnd(
                    "There's nothing that ${initiatingPlayer.name} and ${otherPlayer.name} can trade " +
                    "with each other besides money. The trade process will be restarted."
                )
                continue@mainTradingLoop
            }
            
            if (initiatingPlayerNeedsMoney && otherPlayer.money == 0) {
                printBlankLnAnd(
                    "${initiatingPlayer.name} needs money and ${otherPlayer.name} doesn't have any. " +
                    "The trade process will be restarted."
                )
                continue@mainTradingLoop
            }
            
            
            // Part for having the initiating player select what they want.
            
            val wantedItems = TradeItems(owner = otherPlayer)
            
            selectWantedItemsLoop@ while (true) {
                val askAboutMoneyAmount: Boolean =
                    initiatingPlayerNeedsMoney ||
                    run {
                        val message =
                            "${initiatingPlayer.name}, would you like any money from ${otherPlayer.name}?"
                        promptForYOrN(message)
                    }
                
                if (askAboutMoneyAmount) {
                    while (true) {
                        print("\n${otherPlayer.name} has $${otherPlayer.moneyString}. Enter an amount")
                        println(
                            if (initiatingPlayerNeedsMoney) {
                                ". You need money so entering an amount is mandatory."
                            } else {
                                " or $nextPartSentencePart:"
                            }
                        )
                        
                        input = getFormattedInput()
                        if (!initiatingPlayerNeedsMoney && input == nextPartVal) {
                            break
                        }
                        
                        val moneyAmount: Int? = input.toIntOrNull()
                        val minMoneyAmount = if (initiatingPlayerNeedsMoney) 1 else 0
                        if (moneyAmount != null && moneyAmount in minMoneyAmount..otherPlayer.money) {
                            wantedItems.money = moneyAmount
                            break
                        }
                        
                        printInvalidInput()
                    }
                }
                
                if (otherPlayerHasAProp) {
                    val message =
                        "${initiatingPlayer.name}, would you like any properties from ${otherPlayer.name}?"
                    
                    if (promptForYOrN(message)) {
                        val propsInfo: String =
                            PropertyGroups.getInfoForPropsOwnedBy(otherPlayer, otherPlayerProps)

                        while (true) {
                            printBlankLnAnd(propsInfo)
                            printBlankLnAnd(
                                "Enter the positions of all the properties you would like with each " +
                                "position separated by whitespace or enter $nextPartSentencePart:"
                            )
                            input = getFormattedInput()
                            if (input == nextPartVal) break
                            
                            val selectedProps: PropertyGroups =
                                otherPlayerProps.filterByPositions(positionsInput = input)
                            if (selectedProps.isNotEmpty()) {
                                wantedItems.props = selectedProps
                                break
                            }

                            printInvalidPositionsText()
                        }
                    }
                }
                
                if (otherPlayer.hasAGetOffVacationCard) {
                    val message =
                        "${initiatingPlayer.name}, would you like any get off Vacation free cards " +
                        "from ${otherPlayer.name}?"
                    
                    if (promptForYOrN(message)) {
                        val numGetOffVacationCardsString =
                            Misc.getNumGetOffVacationCardsString(otherPlayer.numGetOffVacationCards)

                        while (true) {
                            printBlankLnAnd(
                                "${otherPlayer.name} has $numGetOffVacationCardsString. " +
                                "How many would you like? Enter an amount or $nextPartSentencePart:"
                            )
                            input = getFormattedInput()
                            if (input == nextPartVal) break
                            
                            val cardsAmount: Int? = input.toIntOrNull()
                            if (cardsAmount != null && cardsAmount in 0..otherPlayer.numGetOffVacationCards) {
                                wantedItems.numGetOffVacationCards = cardsAmount
                                break
                            }

                            printInvalidInput()
                        }
                    }
                }
                
                if (wantedItems.somethingHasBeenChanged) {
                    break@selectWantedItemsLoop
                }
                
                while (true) {
                    printBlankLnAnd(
                        """
                        ${initiatingPlayer.name}, you didn't select anything for what you want.
                        Enter one of the following:
                        
                        ta: Try to select something again
                        $goToStartOption
                        $cancelOption
                        """.trimIndent()
                    )
                    
                    when (getFormattedInput()) {
                        "ta" -> continue@selectWantedItemsLoop
                        
                        goToStartVal -> continue@mainTradingLoop
                        
                        cancelVal -> return false
                        
                        else -> printInvalidInput()
                    }
                }
            }
            
            
            // Part for having the initiating player select what to offer.
            
            val offeredItems = TradeItems(owner = initiatingPlayer)
            
            selectItemsToOfferLoop@ while (true) {
                // Only give the initiating player the choice of offering money if they are not in need of money
                // and they chose that they didn't want any money.
                if (!initiatingPlayerNeedsMoney && wantedItems.money == 0) {
                    val message =
                        "${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} any money?"
                    
                    if (promptForYOrN(message)) {
                        while (true) {
                            printBlankLnAnd(
                                "${initiatingPlayer.name}, you have $${initiatingPlayer.moneyString}, " +
                                "how much of it would you like to offer? Enter an amount or $nextPartSentencePart:"
                            )
                            input = getFormattedInput()
                            if (input == nextPartVal) break
                            
                            val moneyAmount: Int? = input.toIntOrNull()
                            if (moneyAmount != null && moneyAmount in 0..initiatingPlayer.money) {
                                offeredItems.money = moneyAmount
                                break
                            }

                            printInvalidInput()
                        }
                    }
                }
                
                if (initiatingPlayerHasAProp) {
                    val message =
                        "${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} any properties?"
                    
                    if (promptForYOrN(message)) {
                        val propsInfo: String =
                            PropertyGroups.getInfoForPropsOwnedBy(initiatingPlayer, initiatingPlayerProps)
                        
                        while (true) {
                            printBlankLnAnd(propsInfo)
                            printBlankLnAnd(
                                "Enter the positions of the properties you would like to offer with each " +
                                "position separated by whitespace or enter $nextPartSentencePart:"
                            )
                            
                            input = getFormattedInput()
                            if (input == nextPartVal) break
                            
                            val selectedProps: PropertyGroups =
                                initiatingPlayerProps.filterByPositions(positionsInput = input)
                            if (selectedProps.isNotEmpty()) {
                                offeredItems.props = selectedProps
                                break
                            }

                            printInvalidPositionsText()
                        }
                    }
                }
                
                // Only ask the initiating player if they would like to offer any get off Vacation free cards
                // if they have one and they chose that they didn't want any.
                if (initiatingPlayer.hasAGetOffVacationCard && wantedItems.numGetOffVacationCards == 0) {
                    val message =
                        "${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} " +
                        "any get off Vacation free cards?"
                    
                    if (promptForYOrN(message)) {
                        val numGetOffVacationCardsString =
                            Misc.getNumGetOffVacationCardsString(initiatingPlayer.numGetOffVacationCards)
                        
                        while (true) {
                            printBlankLnAnd(
                                "${initiatingPlayer.name}, you have $numGetOffVacationCardsString. " +
                                "How many would you like to offer? Enter an amount or $nextPartSentencePart:"
                            )
                            input = getFormattedInput()
                            if (input == nextPartVal) break
                            
                            val cardsAmount: Int? = input.toIntOrNull()
                            if (cardsAmount != null && cardsAmount in 0..initiatingPlayer.numGetOffVacationCards) {
                                offeredItems.numGetOffVacationCards = cardsAmount
                                break
                            }

                            printInvalidInput()
                        }
                    }
                }
                
                if (offeredItems.somethingHasBeenChanged) {
                    break@selectItemsToOfferLoop
                }
                
                while (true) {
                    printBlankLnAnd(
                        """
                        ${initiatingPlayer.name}, you didn't offer anything.
                        Enter one of the following:
                        
                        ta: Try to offer something again
                        $goToStartOption
                        $cancelOption
                        """.trimIndent()
                    )
                    
                    when (getFormattedInput()) {
                        "ta" -> continue@selectItemsToOfferLoop
                        
                        goToStartVal -> continue@mainTradingLoop
                        
                        cancelVal -> return false
                        
                        else -> printInvalidInput()
                    }
                }
            }
            
            
            // Part for the initiating player to confirm what they want and what they offer.
            
            val offeredItemsString = offeredItems.toString()
            val wantedItemsString = wantedItems.toString()
            
            while (true) {
                printBlankLnAnd("For this trade, ${initiatingPlayer.name} will give ${otherPlayer.name} the following:")
                println(offeredItemsString)
                printBlankLnAnd("${otherPlayer.name} will give ${initiatingPlayer.name} the following:")
                println(wantedItemsString)
                
                printBlankLnAnd(
                    """
                    ${initiatingPlayer.name}, do you confirm the trade?
                    Enter "y", "n", or one of the following:
                    $seePropInfoOption
                    $seePlayersInfoOption
                    """.trimIndent()
                )
                
                when (getFormattedInput()) {
                    "y" -> break
                    
                    "n" -> {
                        while (true) {
                            printBlankLnAnd(
                                """
                                ${initiatingPlayer.name}, enter one of the following:
                                $goToStartOption
                                $cancelOption
                                """.trimIndent()
                            )
                            
                            when (getFormattedInput()) {
                                goToStartVal -> continue@mainTradingLoop
                                
                                cancelVal -> return false
                                
                                else -> printInvalidInput()
                            }
                        }
                    }
                    
                    seePropInfoVal ->
                        askAboutSeeingPropInfo(playerWhoWantsToKnow = initiatingPlayer)
                    
                    seePlayersInfoVal -> printBlankLnAnd(playerManager.getPlayersInfo())
                    
                    else -> printInvalidInput()
                }
            }
            
            
            // Part for the other player to accept or deny the trade and make the changes if accepted.
            
            while (true) {
                printBlankLnAnd(
                    """
                    ${otherPlayer.name}, do you accept the trade?
                    Enter "y", "n", or one of the following:
                    $seePropInfoOption
                    $seePlayersInfoOption
                    """.trimIndent()
                )
                
                when (getFormattedInput()) {
                    "y" -> {
                        offeredItems.transferItemsTo(otherPlayer)
                        wantedItems.transferItemsTo(initiatingPlayer)
                        return true
                    }
                    
                    "n" -> {
                        printBlankLnAnd("${otherPlayer.name} denies the trade.")
                        return false
                    }
                    
                    seePropInfoVal ->
                        askAboutSeeingPropInfo(playerWhoWantsToKnow = otherPlayer)
                    
                    seePlayersInfoVal -> printBlankLnAnd(playerManager.getPlayersInfo())
                    else -> printInvalidInput()
                }
            }
        }
    }
}
