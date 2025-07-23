package singletonopoly

import kotlin.random.Random
import kotlin.random.nextInt
import singletonopoly.Misc.toStringWithCommas
import singletonopoly.actiondeck.*
import singletonopoly.board.*
import singletonopoly.board.properties.*
import singletonopoly.players.*

fun getFormattedInput(): String = readln().trim().lowercase()

const val INVALID_INPUT_TEXT = "Invalid input"

fun printInvalidInput() {
    println(INVALID_INPUT_TEXT)
}

fun main() {
    while (true) {
        Game()
        playAgainAskLoop@ while (true) {
            print("\nWould you like to play again? (y/n): ")
            when (getFormattedInput()) {
                "y" -> {
                    println()
                    break@playAgainAskLoop
                }
                
                "n" -> return
                
                else -> {
                    println()
                    printInvalidInput()
                }
            }
        }
    }
}


/**
 * Class with data and functions for a game of Singletonopoly. A game is started upon instantiation of this class.
 */
class Game {
    val actionDeck = ActionDeck()
    val playerManager: PlayerManager
    var numDoubleRollsInARow = 0
    var goAgain = false
    var turnOver = false
    var curPlayer: Player
    var gameOver = false
    lateinit var input: String
    
    
    init {
        println("You started a game of Singletonopoly.")
        getOrderedPlayerNames().let {
            println("\nThe order of the players is:")
            it.forEach(::println)
            
            playerManager = PlayerManager(it)
            curPlayer = playerManager.curPlayer
        }
        
        while (true) {
            println("\nIt's ${curPlayer.name}'s turn.")
            
            // The goAgain, turnOver, and gameOver variables might change during the functions that get called.
            askAboutPreRollAction()
            if (gameOver) {
                break
            }
            if (currentPlayer.isOnVacation && !turnOver) {
                askAboutVacationAction()
            }
            if (!turnOver) {
                rollDiceAndMove()
            }
            if (!turnOver) {
                evaluatePosition()
            }
            if (gameOver) {
                break
            }
            
            // Prepare for next turn.
            if (turnOver) {
                turnOver = false
            }
            
            if (goAgain) {
                goAgain = false
            } else {
                currentPlayer = playerManager.switchToNextPlayer()
            }
            
            println("\nEnd of turn. Type \"c\" and press Enter to continue:")
            while (getFormattedInput() != "c") { }
        }
        
        // Unless the game is ended early, there should be a winner. Print a message if there's a winner.
        playerManager.winnerName?.let { println("The winner is $it!") }
        
        // Make properties unowned in case another game is started.
        PropertyGroups.makePropertiesUnowned()
    }
    
    class DiceRoll {
        val di1Val = Random.nextInt(1..6)
        val di2Val = Random.nextInt(1..6)
        val isDoubles = di1Val == di2Val
        val total = di1Val + di2Val
    }
    
    
    
    
    /**
     * Has the user enter the number of players and then the names of the players. A dice roll is generated
     * for each player and a List of names ordered by dice rolls in descending order is returned.
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
            println()
            printInvalidInput()
        }
        
        class NameAndRollTotal(val name: String, val diceRollTotal: Int)
        
        val playerNamesAndRollTotals = ArrayList<NameAndRollTotal>(numPlayers)
        
        while (playerNamesAndRollTotals.size < numPlayers) {
            val playerNum: Int = playerNamesAndRollTotals.size + 1
            val defaultName = "Player $playerNum"
            println("\nEnter a unique name for player $playerNum or enter nothing for \"$defaultName\":")
            input = readln().trim()
            
            if (playerNamesAndRollTotals.any { it.name == input }) {
                println("$input is already a name for another player. You must enter a unique name.")
                continue
            }
            
            val name: String = input.ifEmpty { defaultName }
            with(DiceRoll()) {
                println("$name got a $di1Val and a $di2Val for their beginning dice roll for a total of $total.")
                playerNamesAndRollTotals.add(NameAndRollTotal(name, total))
            }
        }
        
        // Order players by descending dice rolls. If more than 1 player has the same dice roll, then the
        // order of those players will be randomized.
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
        PropertyGroups.makePropertiesUnowned(owner = curPlayer)
    }
    
    
    /**
     * Asks the current player about any actions they would like to do before they roll and take their turn.
     */
    fun askAboutPreRollAction() {
        val actionPropGroups = ActionPropertyGroups.forPropsOwnedBy(curPlayer)
        
        // Valid input values
        val takeTurnValue = "tt"
        val viewSpacesValue = "sp"
        val viewPropertyInfoValue = "pr"
        val viewPlayerInfoValue = "pl"
        val makeTradeValue = "tr"
        val dropOutValue = "dr"
        val endGameValue = "eg"
        val pawnValue = "pa"
        val unpawnValue = "up"
        val addRestaurantValue = "ar"
        val removeRestaurantValue = "rr"
        
        val optionsStart =
            """
            ${curPlayer.name}, what would you like to do?
            Type one of the following and press Enter:
            
            $takeTurnValue: Take your turn
            $viewSpacesValue: View the board spaces
            $viewPropertyInfoValue: View property info
            $viewPlayerInfoValue: View player info
            $makeTradeValue: Make a trade with another player
            $dropOutValue: Drop out of the game
            $endGameValue: End the game
            """.trimIndent()
        
        preRollActionAskLoop@ while (true) {
            println(optionsStart)
            if (actionPropGroups.hasPawnable) {
            }
            if (actionPropGroups.hasUnpawnable) {
            }
            if (actionPropGroups.hasRestaurantAddable) {
            }
            if (actionPropGroups.hasRestaurantRemovable) {
            }
            
            input = getFormattedInput()
            println()
            
            when (input) {
                takeTurnValue -> return
                
                viewSpacesValue -> println(Board)
                
                viewPropertyInfoValue -> askAboutDisplayingPropertyInfo(playerWhoWantsToKnow = curPlayer)
                
                viewPlayerInfoValue -> println(playerManager.getPlayersInfo())
                
                makeTradeValue -> {
                    if (
                        askAboutTrading(
                            initiatingPlayer = curPlayer,
                            initiatingPlayerNeedsMoney = false
                        )
                    ) {
                        actionPropGroups.refresh()
                    }
                }
                
                dropOutValue -> {
                    dropOutAskLoop@ while (true) {
                        print("${currentPlayer.name}, are you sure that you want to drop out? (y/n): ")
                        when (getFormattedInput()) {
                            "y" -> {
                                curPlayer.removeFromGame()
                                if (playerManager.onePlayerIsInGame) {
                                    gameOver = true
                                } else {
                                    turnOver = true
                                    handleCurPlayerDroppingOutAndPropsBecomeUnowned()
                                }
                                return
                            }
                            
                            "n" -> break@dropOutAskLoop
                            
                            else -> {
                                println()
                                printInvalidInput()
                                println()
                            }
                        }
                    }
                }
                
                endGameValue -> {
                    endGameAskLoop@ while (true) {
                        print("Are you sure that you want to end the game? (y/n): ")
                        when (getFormattedInput()) {
                            "y" -> {
                                gameOver = true
                                return
                            }
                            
                            "n" -> break@endGameAskLoop
                            
                            else -> {
                                println()
                                printInvalidInput()
                                println()
                            }
                        }
                    }
                }
                
                pawnValue -> {
                    if (actionPropGroups.hasPawnable) {
                        if (
                            askAboutPawningProperties(
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
                
                unpawnValue -> {
                        if (askAboutCurPlayerUnpawningProperties(unpawnableProperties = propertyLists.unpawnable)) {
                    if (actionPropGroups.hasUnpawnable) {
                            actionPropGroups.refresh()
                        }
                    } else {
                        printInvalidInput()
                    }
                }
                
                addRestaurantValue -> {
                        if (askAboutCurPlayerAddingRestaurants(restaurantAddableStreets = propertyLists.restaurantAddable)) {
                    if (actionPropGroups.hasRestaurantAddable) {
                            actionPropGroups.refresh()
                        }
                    } else {
                        printInvalidInput()
                    }
                }
                
                removeRestaurantValue -> {
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
            
            println()
        }
    }
    
    
    fun handleCurPlayerMakingRevolution() {
        println("${curPlayer.name} made a revolution!")
        curPlayer.incrementMoneyAndPrintUpdate(512)
    }
    
    
    fun rollDiceAndMove() {
        val diceRoll = DiceRoll()
        with(diceRoll) {
            println("${curPlayer.name} rolled a $di1Val and a $di2Val, for a total of $total.")
        }
        
        if (diceRoll.isDoubles) {
            if (++numDoubleRollsInARow == 3) {
                println("${curPlayer.name} rolled doubles thrice, or 3 times, in a row so they go to Vacation.")
                numDoubleRollsInARow = 0
                curPlayer.sendToVacation()
                turnOver = true
                return
            }
            
            println("These are doubles so they get to go again.")
            goAgain = true
        } else {
            numDoubleRollsInARow = 0
        }
        
        currentPlayer.position += diceRoll.sum
        if (currentPlayer.position > Board.numberOfSpaces) {
            currentPlayer.position -= Board.numberOfSpaces
            currentPlayerMadeRevolution()
        }
    }
    
    
    fun handleGoOnVacation() {
        curPlayer.sendToVacation()
        
        // If the player rolled doubles, they won't get to go again in this situation.
        goAgain = false
        numDoubleRollsInARow = 0
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
                    print(" in the ${curBoardSpace.neighborhood.friendlyName} neighborhood")
                }
                curBoardSpace.owner.let {
                    if (it != null) handleLandingOnOwnedProperty(property = curBoardSpace, owner = it)
                    else handleLandingOnUnownedProperty(property = curBoardSpace)
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
        println(
            "${dropOutPlayer.name} dropped out so their money and properties " +
            "will go to ${newPropsOwner.name}."
        )
        newPropsOwner.incrementMoneyAndPrintUpdate(dropOutPlayer.money)
        PropertyGroups.transferOwnership(curOwner = dropOutPlayer, newOwner = newPropsOwner)
    }
    
    fun printGotMoneyMessage(player: Player = curPlayer, messageEnd: String) {
        println("${player.name} got the money to $messageEnd.")
    }
    
    
    fun handleLandingOnOwnedProperty(property: Property, owner: Player) {
        print(", which is owned by ")
        if (owner === curPlayer) {
            println("themself.")
            return
        }
        println("${owner.name}.")
        if (property.isPawned) {
            println("This property is pawned so ${curPlayer.name} doesn't have to pay a fee.")
            return
        }
        
        val fee: Int
        
        when (property) {
            is Street -> {
                fee = property.curFee
                println(
                    property.numRestaurants.let {
                        "There${if (it == 1) "'s 1 restaurant" else " are $it restaurants"} on " +
                        "this street and the fee is $${fee.toStringWithCommas()}."
                    }
                )
            }
            
            is GolfClub -> {
                val feeData: GolfClub.FeeData = property.FeeData()
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
                        "${curPlayer.name} rolled a $di1Val and a $di2Val for a total of $total " +
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
    
    
    fun handleLandingOnUnownedProperty(property: Property) {
        println(", which is unowned.")
        val purchasePrice: Int = property.purchasePrice
        
        while (true) {
            println(
                "${curPlayer.name}, you have $${curPlayer.moneyString}. Would you like to buy " +
                "${property.name} for $${purchasePrice.toStringWithCommas()}? (y/n)"
            )
            
            when (getFormattedInput()) {
                "y" -> {
                    if (curPlayer.money < purchasePrice) {
                        if (
                            !askAboutGettingMoney(
                                player = curPlayer,
                                moneyNeeded = purchasePrice,
                                playerHasChoice = true
                            )
                        ) {
                            println("${curPlayer.name} isn't buying ${property.name}.")
                            return
                        }
                        
                        printGotMoneyMessage(messageEnd = "buy ${property.name}")
                    }
                    
                    property.owner = curPlayer
                    curPlayer.decrementMoneyAndPrintUpdate(purchasePrice)
                    return
                }
                
                "n" -> return
                
                else -> {
                    println()
                    printInvalidInput()
                    println()
                }
            }
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
            is MoneyGainCard -> currentPlayer.incrementMoneyAndPrintUpdate(topCard.gainAmount)
            
            is MoneyLossCard -> {
                val lossAmount: Int = topCard.lossAmount
                
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
            
            is GiveMoneyToOtherPlayersCard -> {
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
            
            is MoveAheadCard -> {
                    }
                }
                evaluatePosition()
            }
            
            is MoveBackCard -> {
                with(curPlayer) {
                    position -= topCard.numSpaces
                    if (position < 1) {
                        position = Board.numSpaces + position
                    }
                }
                evaluatePosition()
            }
            
            is MoveToPropertyCard -> {
                topCard.propertyPosition.let {
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
    
    
    fun askAboutVacationAction() {
        val turnString =
            curPlayer.numTurnsOnVacation.let {
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
        val payValue = "py"
        val rollValue = "rl"
        val getOffVacationCardValue = "go"
        
        val feeToGetOffVacation = 128
        
        val optionsString =
            """
            ${curPlayer.name}, you're on Vacation and this is your $turnString turn on Vacation.
            What would you like to do? Type one of the following and press Enter:
            $payValue: Pay $$feeToGetOffVacation to get off Vacation
            $rollValue: Try to roll doubles
            """
            .trimIndent()
            .plus(
                if (curPlayer.hasAGetOffVacationCard) {
                    "\n$getOffVacationCardValue: Use a get off Vacation free card"
                } else ""
            )
        
        vacationActionAskLoop@ while (true) {
            println()
            println(optionsString)
            
            when (getFormattedInput()) {
                payValue -> {
                    if (curPlayer.money < feeToGetOffVacation) {
                        if (
                            !askAboutGettingMoney(
                                player = curPlayer,
                                moneyNeeded = feeToGetOffVacation,
                                playerHasChoice = true
                            )
                        ) {
                            continue@vacationActionAskLoop
                        }
                        
                        printGotMoneyMessage(messageEnd = "get off vacation")
                    }
                    
                    println("\n${curPlayer.name} chose to pay so they get to take their turn.")
                    curPlayer.decrementMoneyAndPrintUpdate(feeToGetOffVacation)
                    curPlayer.removeFromVacation()
                    return
                }
                
                rollValue -> {
                    val diceRoll = DiceRoll()
                    println("\n${curPlayer.name} rolled a ${diceRoll.di1Val} and a ${diceRoll.di2Val}.")
                    
                    if (diceRoll.isDoubles) {
                        println("These are doubles so they're off Vacation and get to advance ${diceRoll.total} spaces.")
                        curPlayer.removeFromVacation()
                        curPlayer.position += diceRoll.total
                        /*
                        Don't need to check if the player made a revolution since the Vacation space
                        is at position 21, there are 40 spaces, and the max total dice roll is 12.
                         */
                        evaluatePosition()
                    } else {
                        if (++curPlayer.numTurnsOnVacation == 3) {
                            println("${curPlayer.name} spent 3 turns on Vacation so it's now over.")
                            curPlayer.removeFromVacation()
                        }
                    }
                    
                    turnOver = true
                    return
                }
                
                getOffVacationCardValue -> {
                    println()
                    if (curPlayer.hasAGetOffVacationCard) {
                        println(
                            "${curPlayer.name} chose to use a get off Vacation free card so they " +
                            "get to take their turn."
                        )
                        curPlayer.removeFromVacation()
                        curPlayer.decrementNumGetOffVacationCardsAndPrintUpdate()
                        actionDeck.insertGetOffVacationCardAtBottom()
                        return
                    } else {
                        printInvalidInput()
                    }
                }
                
                else -> {
                    println()
                    printInvalidInput()
                }
            }
        }
    }
    
    
    fun askAboutDisplayingPropertyInfo(playerWhoWantsToKnow: Player) {
        // Valid input values
        val basicInfoYourPropsValue = "by"
        val basicInfoAllPropsValue = "ba"
        val allInfoAllPropsValue = "aa"
        
        val optionsString =
            """
            ${playerWhoWantsToKnow.name}, enter one of the following and press Enter:
            
            $basicInfoYourPropsValue: View basic info for your properties
            $basicInfoAllPropsValue: View basic info for all properties
            $allInfoAllPropsValue: View all info for all properties
            b: Go back
            """.trimIndent()
        
        val allPropsInfo: String by lazy { PropertyGroups.getInfoForAllProps() }
        val playerPropsInfo: String by lazy {
            PropertyGroups.getInfoForPropsOwnedBy(playerWhoWantsToKnow)
        }
        
        while (true) {
            println(optionsString)
            input = getFormattedInput()
            if (input == "b") return
            
        }
    }
    
    
    /**
     * This extension function will be called on either a list of properties or streets. The positionsInput param
     * is for a string of user input that should consist of space-separated ints for property positions but can
     * consist of anything. This returns a new list of properties that are in the properties list this was called
     * with but are filtered to include the properties whose position is in positionsInput.
     */
    fun <P: Property> List<P>.filterByPositions(positionsInput: String): List<P> {
        // Let inputInts consist of all space-separated ints in positionsInput and ignore non-int values.
        val inputInts: List<Int> =
            positionsInput
            .split(Regex(" +"))
            .mapNotNull { it.toIntOrNull() }
        
        return filter { it.position in inputInts }
    }
    
    fun printInvalidPositionsText() {
        println("No valid positions were entered.")
    }
    
    fun askAboutPawningOrUnpawning(
        pawning: Boolean,
        player: Player,
        pawnableOrUnpawnableProps: PropertyGroups
    ): Boolean {
        
        val propGroupsStringStart =
            "Here are the properties owned by ${player.name} that can be ${if (pawning) "" else "un"}pawned:"
        val propGroupsString = pawnableOrUnpawnableProps.toString(propGroupsStringStart)
        
        while (true) {
            println()
            println(propGroupsString)
            println(
                "\n${player.name}, enter the positions of the properties you would like to " +
                "${if (pawning) "" else "un"}pawn with each position separated by whitespace or enter " +
                "\"b\" to go back:"
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
                $${totalMoneyGainOrPrice.toStringWithCommas()} will ${if (pawning) "be gained" else "have to be paid"}.
                ${player.name}, do you confirm this?
                """.trimIndent()
            
            val selectedPropsConfirmationString =
                selectedProps
                .toString(
                    start = "Here are the selected properties:",
                    end = confirmationStringEnd
                )
            
            if (!promptForYOrN(selectedPropsConfirmationString)) continue
            
            if (!pawning && player.money < totalMoneyGainOrPrice) {
                if (
                    !askAboutGettingMoney(
                        player = player,
                        moneyNeeded = totalMoneyGainOrPrice,
                        playerHasChoice = true
                    )
                ) {
                    println("\n${player.name} will be taken back to the properties selection section.")
                    continue
                }
                
                println()
                printGotMoneyMessage(player = player, messageEnd = "unpawn their properties")
            }
            
            break
        }
        
        if (pawning) {
            selectedProps.forEach { it.pawn() }
            player.incrementMoneyAndPrintUpdate(totalMoneyGainOrPrice)
        } else {
            selectedProps.forEach { it.unpawn() }
            player.decrementMoneyAndPrintUpdate(totalMoneyGainOrPrice)
        }
        
        return true
    }
    
        askAboutPawningOrUnpawning(pawning = true, player, pawnableProps)
    fun askAboutPawning(player: Player, pawnableProps: PropertyGroups): Boolean =
    
        askAboutPawningOrUnpawning(pawning = false, curPlayer, unpawnableProps)
    fun askAboutUnpawning(unpawnableProps: PropertyGroups): Boolean =
    
    
    fun askAboutAddingOrRemovingRestaurants(
        addingRestaurants: Boolean,
        player: Player,
        restaurantAddableOrRemovableStreets: StreetGroups
    ): Boolean {
        
        val streetGroupsStringStart =
            "Here are the streets owned by ${player.name} that can have a restaurant " +
            "${if (addingRestaurants) "added" else "removed"}:"
        
        val streetGroupsString =
            restaurantAddableOrRemovableStreets
            .toCharSequence(streetGroupsStringStart)
            .toString()
        
        while (true) {
            println()
            println(streetGroupsString)
            println(
                "\n${player.name}, enter the positions of the properties you would like to " +
                if (addingRestaurants) "add a restaurant to" else "remove a restaurant from" +
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
                    if (askAboutAdding) it.restaurantAddPrice else it.restaurantRemovalGain
                }
            
            val confirmationStringEnd =
                """
                $${totalMoneyGainOrPrice.toStringWithCommas()} will ${if (addingRestaurants) "have to be paid" else "be gained"}.
                ${player.name}, do you confirm this?
                """.trimIndent()
            
            val selectedStreetsConfirmationString =
                selectedStreets
                .toCharSequence(
                    start = "Here are the selected streets:",
                    end = confirmationStringEnd
                )
                .toString()
            
            if (!promptForYOrN(selectedStreetsConfirmationString)) continue
            
            if (addingRestaurants && player.money < totalMoneyGainOrPrice) {
                if (
                    !askAboutGettingMoney(
                        player = player,
                        moneyNeeded = totalMoneyGainOrPrice,
                        playerHasChoice = true
                    )
                ) {
                    println("\n${player.name} will be taken back to the streets selection section.")
                    continue
                }
                
                println()
                printGotMoneyMessage(player = player, messageEnd = "add restaurants to their streets")
            }
            
            break
        }
        
        if (addingRestaurants) {
            selectedStreets.forEach { it.addRestaurant() }
            player.decrementMoneyAndPrintUpdate(totalMoneyGainOrPrice)
        } else {
            selectedStreets.forEach { it.removeRestaurant() }
            player.incrementMoneyAndPrintUpdate(totalMoneyGainOrPrice)
        }
        
        return true
    }
    
        askAboutAddingOrRemovingRestaurants(addingRestaurants = true, curPlayer, restaurantAddableStreets)
    fun askAboutAddingRestaurants(restaurantAddableStreets: StreetGroups): Boolean =
    fun askAboutRemovingRestaurants(player: Player, restaurantRemovableStreets: StreetGroups): Boolean =
    
        askAboutAddingOrRemovingRestaurants(addingRestaurants = false, player, restaurantRemovableStreets)
        
    
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
     */
    fun askAboutGettingMoney(player: Player, moneyNeeded: Int, playerHasChoice: Boolean): Boolean {
        val actionPropGroups = ActionPropertyGroups.forPropsOwnedBy(player)
        
        // Valid input values
        val exitValue = "ex"
        val makeTradeValue = "tr"
        val pawnValue = "pa"
        val removeRestaurantValue = "rr"
        
        while (player.money < moneyNeeded) {
            println()
            println(
                """
                ${player.name}, you need $${moneyNeeded.toStringWithCommas()} and currently have $${player.moneyString}.
                Select one of the following options to get more money:
                $exitValue: ${if (playerHasChoice) "Go back" else "Drop out of the game"}
                $makeTradeValue: Make a trade with another player
                """.trimIndent()
            )
            if (actionPropGroups.hasPawnable) {
                println("$pawnValue: Pawn some of your properties")
            }
            if (actionPropGroups.hasRestaurantRemovable) {
                println("$removeRestaurantValue: Remove restaurants from some of your streets")
            }
            
            when (getFormattedInput()) {
                exitValue -> {
                    if (!playerHasChoice) {
                        player.removeFromGame()
                        if (playerManager.onePlayerIsInGame) {
                            gameOver = true
                        }
                    }
                    return false
                }
                
                makeTradeValue -> {
                    println()
                    if (askAboutTrading(initiatingPlayer = player, initiatingPlayerNeedsMoney = true)) {
                        actionPropGroups.refresh()
                    }
                }
                
                pawnValue -> {
                    println()
                    if (actionPropGroups.hasPawnable) {
                        if (askAboutPawning(player = player, pawnableProps = actionPropGroups.pawnable)) {
                            actionPropGroups.refresh()
                        }
                    } else {
                        printInvalidInput()
                    }
                }
                
                removeRestaurantValue -> {
                    println()
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
                
                else -> {
                    println()
                    printInvalidInput()
                }
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
            buildList {
                if (money > 0) add('$' + money.toStringWithCommas())
                
                props?.let {
                    val end =
                        "If a street in an owned neighborhood gets traded, then that neighborhood will " +
                        "no longer be owned so all restaurants in it will be sold and the street fees " +
                        "will go back to the starting fees."
                    
                    add(it.toString(start = "The following properties:", end, sayIfNHoodIsOwned = true))
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
            
            props?.forEach { p ->
                p.owner = newOwner
                if (p !is Street) return@forEach
                
                val nHood = p.neighborhood
                if (nHood.numRestaurants == 0) return@forEach
                
                println()
                println(
                    if (nHood.numRestaurants == 1) {
                        "There's 1 restaurant in the ${nHood.friendlyName} " +
                        "neighborhood so this will be sold."
                    } else {
                        "There are ${nHood.numRestaurants} restaurants in the " +
                        "${nHood.friendlyName} neighborhood so these will be sold."
                    }
                )
                
                // Sell all restaurants in the neighborhood.
                nHood.removeRestaurants()
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
        
        // Valid input values
        val propertyInfoValue = "pr"
        val playerInfoValue = "pl"
        val cancelValue = "c"
        
        val startOptionsString = buildString {
            appendLine(
                """
                ${initiatingPlayer.name}, type one of the following and press Enter:
                $propertyInfoValue: See property info
                $playerInfoValue: See player info
                $cancelValue: Cancel the trade
                """.trimIndent()
            )
            
            for ((intString, player) in otherPlayersMap) {
                appendLine("${intString}: Initiate a trade with ${player.name}")
            }
        }
        
        mainTradingLoop@ while (true) {
            println("${initiatingPlayer.name}, you're at the beginning of the trade process.")
            val otherPlayer: Player
            
            tradeBeginningLoop@ while (true) {
                println(startOptionsString)
                input = getFormattedInput()
                
                when (input) {
                    propertyInfoValue -> {
                        println()
                        askAboutDisplayingPropertyInfo(playerWhoWantsToKnow = initiatingPlayer)
                    }
                    
                    playerInfoValue -> {
                        println()
                        println(playerManager.getPlayersInfo())
                    }
                    
                    cancelValue -> return false
                    
                    else -> {
                        // Check if a valid number was entered. If so, set otherPlayer to the correct player.
                        val nullableOtherPlayer: Player? = otherPlayersMap[input]
                        if (nullableOtherPlayer != null) {
                            otherPlayer = nullableOtherPlayer
                            break@tradeBeginningLoop
                        }
                        println()
                        printInvalidInput()
                    }
                }
                
                println()
            }
            
            
            // Prevent the player from continuing if there is nothing that can be traded besides money.
            val otherPlayerProps = PropertyGroups.forPropsOwnedBy(otherPlayer)
            val otherPlayerHasAProp: Boolean = otherPlayerProps.isNotEmpty()
            if (!initiatingPlayerHasAProp && !initiatingPlayer.hasAGetOffVacationCard &&
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
            
            
            // Part for having the initiating player select what they want.
            
            val wantedItems = TradeItems(owner = otherPlayer)
            val nextValue = "g"
            val nextPartSentencePart = "\"$nextValue\" to go to the next part of the trade process"
            
            selectWhatIsWanted@ while (true) {
                moneyAskLoop1@ while (true) {
                    // Only ask the initiating player the following question if they don't need money, because
                    // if they do need money then it's an automatic yes.
                    if (!initiatingPlayerNeedsMoney) {
                        print("${initiatingPlayer.name}, would you like any money from ${otherPlayer.name}? (y/n): ")
                        input = getFormattedInput()
                        
                        if (input == "n") {
                            break@moneyAskLoop1
                        } else if (input != "y") {
                            println()
                            printInvalidInput()
                            println()
                            continue@moneyAskLoop1
                        }
                    }
                    
                    moneyAskLoop2@ while (true) {
                        print("\n${otherPlayer.name} has $${otherPlayer.moneyString}. Enter an amount")
                        println(
                            if (initiatingPlayerNeedsMoney) {
                                ". You need money so entering an amount is mandatory."
                            } else {
                                " or \"b\" to go back:"
                            }
                        )
                        
                        input = getFormattedInput()
                        if (input == "b" && !initiatingPlayerNeedsMoney) {
                            continue@moneyAskLoop1
                        }
                        
                        val moneyAmount: Int? = input.toIntOrNull()
                        val minMoneyAmount = if (initiatingPlayerNeedsMoney) 1 else 0
                        if (moneyAmount != null && moneyAmount in minMoneyAmount..otherPlayer.money) {
                            wantedItems.money = moneyAmount
                            break@moneyAskLoop1
                        }
                        println()
                        printInvalidInput()
                    }
                }
                
                if (otherPlayerHasAProp) {
                    propertyAskLoop1@ while (true) {
                        print("${initiatingPlayer.name}, would you like any properties from ${otherPlayer.name}? (y/n): ")
                        input = getFormattedInput()
                        
                        when (input) {
                            "n" -> break@propertyAskLoop1
                            
                            "y" -> {
                                val propertiesString = ""
                                
                                
                                while (true) {
                                    println()
                                    println(propertiesString)
                                    println()
                                    println(
                                        "Enter the positions of all the properties you would like with each " +
                                        "position separated by whitespace or enter $nextPartSentencePart:"
                                    )
                                    input = getFormattedInput()
                                    if (input == nextValue) break@propertyAskLoop1
                                    
                                    val selectedProps: PropertyManager.PropertyGroups =
                                        otherPlayerProps.filterByPositions(positionsInput = input)
                                    if (selectedProps.any()) {
                                        wantedItems.props = selectedProps
                                        break@propertyAskLoop1
                                    }
                                    
                                    println()
                                    printInvalidPositionsText()
                                }
                            }
                            
                            else -> {
                                println()
                                printInvalidInput()
                                println()
                            }
                        }
                    }
                }
                
                if (otherPlayer.hasAGetOffVacationCard) {
                    getOffVacationCardsAskLoop1@ while (true) {
                        print(
                            "${initiatingPlayer.name}, would you like any get off Vacation free cards " +
                            "from ${otherPlayer.name}? (y/n): "
                        )
                        input = getFormattedInput()
                        
                        when (input) {
                            "y" -> {
                                getOffVacationCardsAskLoop2@ while (true) {
                                    val numGetOffVacationCardsString =
                                        Misc.getNumGetOffVacationCardsString(otherPlayer.numGetOffVacationCards)
                                    println(
                                        "\n${otherPlayer.name} has $numGetOffVacationCardsString. " +
                                        "How many would you like? Enter an amount or \"b\" to go back:"
                                    )
                                    input = getFormattedInput()
                                    if (input == "b") continue@getOffVacationCardsAskLoop1
                                    
                                    val amount: Int? = input.toIntOrNull()
                                    if (amount != null && amount in 0..otherPlayer.numGetOffVacationCards) {
                                        wantedItems.numGetOffVacationCards = amount
                                        break@getOffVacationCardsAskLoop1
                                    }
                                    println()
                                    printInvalidInput()
                                }
                            }
                            
                            "n" -> break@getOffVacationCardsAskLoop1
                            
                            else -> {
                                println()
                                printInvalidInput()
                                println()
                            }
                        }
                    }
                }
                
                if (wantedItems.somethingHasBeenChanged) {
                    break@selectWhatIsWanted
                }
                
                while (true) {
                    println()
                    println(
                        """
                        ${initiatingPlayer.name}, you didn't select anything for what you want.
                        Type one of the following and press Enter:
                        ta: Try to select something again
                        st: Go to the start of the trade process
                        c: Cancel the trade
                        """.trimIndent()
                    )
                    
                    when (getFormattedInput()) {
                        "ta" -> continue@selectWhatIsWanted
                        
                        "st" -> continue@mainTradingLoop
                        
                        "c" -> return false
                        
                        else -> {
                            println()
                            printInvalidInput()
                        }
                    }
                }
            }
            
            
            // Part for having the initiating player select what to offer.
            
            val offeredItems = TradeItems(owner = initiatingPlayer)
            
            selectWhatToOffer@ while (true) {
                // Only give the initiating player the choice of offering money if they are not in need of money
                // and they chose that they didn't want any money.
                if (!initiatingPlayerNeedsMoney && wantedItems.money == 0) {
                    moneyAskLoop1@ while (true) {
                        print("${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} any money? (y/n): ")
                        input = getFormattedInput()
                        
                        when (input) {
                            "y" -> {
                                moneyAskLoop2@ while (true) {
                                    println(
                                        "\n${initiatingPlayer.name}, you have $${initiatingPlayer.moneyString}, " +
                                        "how much of it would you like to offer? Enter an amount or \"b\" to go back:"
                                    )
                                    input = getFormattedInput()
                                    if (input == "b") continue@moneyAskLoop1
                                    
                                    val moneyAmount: Int? = input.toIntOrNull()
                                    if (moneyAmount != null && moneyAmount in 0..initiatingPlayer.money) {
                                        offeredItems.money = moneyAmount
                                        break@moneyAskLoop1
                                    }
                                    
                                    println()
                                    printInvalidInput()
                                }
                            }
                            
                            "n" -> break@moneyAskLoop1
                            
                            else -> {
                                println()
                                printInvalidInput()
                                println()
                            }
                        }
                    }
                }
                
                if (initiatingPlayerHasAProp) {
                    propertyAskLoop1@ while (true) {
                        print("${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} any properties? (y/n): ")
                        input = getFormattedInput()
                        
                        when (input) {
                            "y" -> {
                                val propertiesString = ""
                                
                                while (true) {
                                    println()
                                    println(propertiesString)
                                    println()
                                    println(
                                        "Enter the positions of the properties you would like to offer with each " +
                                        "position separated by whitespace or enter $nextPartSentencePart:"
                                    )
                                    
                                    input = getFormattedInput()
                                    if (input == nextValue) break@propertyAskLoop1
                                    
                                    
                                    val selectedProps: PropertyManager.PropertyGroups =
                                        initiatingPlayerProps.filterByPositions(positionsInput = input)
                                    if (selectedProps.any()) {
                                        offeredItems.props = selectedProps
                                        break@propertyAskLoop1
                                    }
                                    
                                    println()
                                    printInvalidPositionsText()
                                }
                            }
                            
                            "n" -> break@propertyAskLoop1
                            
                            else -> {
                                println()
                                printInvalidInput()
                                println()
                            }
                        }
                    }
                }
                
                // Only ask the initiating player if they would like to offer any get off Vacation free cards
                // if they have one and they chose that they didn't want any.
                if (initiatingPlayer.hasAGetOffVacationCard && wantedItems.numGetOffVacationCards == 0) {
                    getOffVacationCardsAskLoop1@ while (true) {
                        print(
                            "${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} " +
                            "any get off Vacation free cards? (y/n): "
                        )
                        input = getFormattedInput()
                        
                        when (input) {
                            "y" -> {
                                getOffVacationCardsAskLoop2@ while (true) {
                                    val numGetOffVacationCardsString =
                                        Misc.getNumGetOffVacationCardsString(initiatingPlayer.numGetOffVacationCards)
                                    println(
                                        "\n${initiatingPlayer.name}, you have $numGetOffVacationCardsString. " +
                                        "How many would you like to offer? Enter an amount or \"b\" to go back:"
                                    )
                                    input = getFormattedInput()
                                    if (input == "b") continue@getOffVacationCardsAskLoop1
                                    
                                    val amount: Int? = input.toIntOrNull()
                                    if (amount != null && amount in 0..initiatingPlayer.numGetOffVacationCards) {
                                        offeredItems.numGetOffVacationCards = amount
                                        break@getOffVacationCardsAskLoop1
                                    }
                                    println()
                                    printInvalidInput()
                                }
                            }
                            
                            "n" -> break@getOffVacationCardsAskLoop1
                            
                            else -> {
                                println()
                                printInvalidInput()
                            }
                        }
                    }
                }
                
                if (offeredItems.somethingHasBeenChanged) {
                    break@selectWhatToOffer
                }
                
                while (true) {
                    println()
                    println(
                        """
                        ${initiatingPlayer.name}, you didn't offer anything.
                        Type one of the following and press Enter:
                        ta: Try to offer something again
                        st: Go to the start of the trade process
                        c: Cancel the trade
                        """.trimIndent()
                    )
                    
                    when (getFormattedInput()) {
                        "ta" -> continue@selectWhatToOffer
                        
                        "st" -> continue@mainTradingLoop
                        
                        "c" -> return false
                        
                        else -> {
                            println()
                            printInvalidInput()
                        }
                    }
                }
            }
            
            
            
            // Part for the initiating player to confirm what they want and what they offer.
            
            val offeredItemsString = offeredItems.toString()
            val wantedItemsString = wantedItems.toString()
            
            while (true) {
                println("\nFor this trade, ${initiatingPlayer.name} will give ${otherPlayer.name} the following:")
                println(offeredItemsString)
                println("\n${otherPlayer.name} will give ${initiatingPlayer.name} the following:")
                println(wantedItemsString)
                
                
                // Reuse propertyInfoValue and playerInfoValue from beginning of function.
                println()
                println(
                    """
                    ${initiatingPlayer.name}, do you confirm the trade?
                    Type "y", "n", or one of the following and press Enter:
                    $propertyInfoValue: See property info
                    $playerInfoValue: See player info
                    """.trimIndent()
                )
                
                when (getFormattedInput()) {
                    "y" -> break
                    
                    "n" -> {
                        while (true) {
                            println()
                            println(
                                """
                                ${initiatingPlayer.name}, type one of the following and press Enter:
                                st: Go to the start of the trade process
                                c: Cancel the trade
                                """.trimIndent()
                            )
                            
                            when (getFormattedInput()) {
                                "st" -> continue@mainTradingLoop
                                
                                "c" -> return false
                                
                                else -> {
                                    println()
                                    printInvalidInput()
                                }
                            }
                        }
                    }
                    
                    propertyInfoValue -> {
                        println()
                        askAboutDisplayingPropertyInfo(playerWhoWantsToKnow = initiatingPlayer)
                    }
                    
                    playerInfoValue -> {
                        println()
                        println(playerManager.getPlayersInfo())
                    }
                    
                    else -> {
                        println()
                        printInvalidInput()
                    }
                }
            }
            
            
            // Part for the other player to accept or deny the trade and make the changes if accepted.
            
            while (true) {
                // Reuse propertyInfoValue and playerInfoValue from beginning of function.
                println()
                println(
                    """
                    ${otherPlayer.name}, do you accept the trade?
                    Type "y", "n", or one of the following and press Enter:
                    $propertyInfoValue: See property info
                    $playerInfoValue: See player info
                    """.trimIndent()
                )
                
                when (getFormattedInput()) {
                    "n" -> {
                        println("\n${otherPlayer.name} denies the trade.")
                        return false
                    }
                    
                    propertyInfoValue -> {
                        println()
                        askAboutDisplayingPropertyInfo(playerWhoWantsToKnow = otherPlayer)
                    }
                    
                    playerInfoValue -> {
                        println()
                        println(playerManager.getPlayersInfo())
                    }
                    
                    "y" -> {
                        // Give the other player what was agreed upon.
                        
                        
                        }
                        
                        offeredItems.transferItemsTo(otherPlayer)
                        wantedItems.transferItemsTo(initiatingPlayer)
                        
                        return true
                    }
                    
                    else -> {
                        println()
                        printInvalidInput()
                    }
                }
            }
        }
    }
}
