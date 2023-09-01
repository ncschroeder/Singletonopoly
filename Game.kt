import kotlin.random.Random
import kotlin.random.nextInt

fun getFormattedInput(): String = readln().trim().lowercase()

const val invalidInputText = "Invalid input"

fun printInvalidInput() {
    println(invalidInputText)
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
    var currentPlayer: Player
    var numDoubleRollsInARow = 0
    var goAgain = false
    var turnOver = false
    var gameOver = false
    lateinit var input: String
    

    init {
        println("You started a game of Singletonopoly.")
        getOrderedPlayerNames().let {
            println("\nThe order of the players is:")
            it.forEach(::println)

            playerManager = PlayerManager(it)
            currentPlayer = playerManager.currentPlayer
        }
        
        while (true) {
            println("\nIt's ${currentPlayer.name}'s turn.")

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
        PropertyManager.makePropertiesUnowned()
    }
    

    fun getDiceRoll(): Pair<Int, Int> =
        Pair(Random.nextInt(1..6), Random.nextInt(1..6))
    
    val Pair<Int, Int>.sum: Int
        get() = first + second
    
    val Pair<Int, Int>.bothAreSame: Boolean
        get() = first == second
    

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
        
        data class NameAndRoll(val name: String, val diceRoll: Int)
        
        val playerNamesAndRolls = ArrayList<NameAndRoll>(numPlayers)
        
        while (playerNamesAndRolls.size < numPlayers) {
            val playerNumber: Int = playerNamesAndRolls.size + 1
            val defaultName = "Player $playerNumber"
            println("\nEnter a unique name for player $playerNumber or enter nothing for \"$defaultName\":")
            input = readln().trim()
            
            if (playerNamesAndRolls.any { it.name == input }) {
                println("$input is already a name for another player. You must enter a unique name.")
                continue
            }
            
            val name: String = input.ifEmpty { defaultName }
            getDiceRoll().run {
                println("$name got a $first and a $second for their beginning dice roll for a total of $sum.")
                playerNamesAndRolls.add(NameAndRoll(name, diceRoll = sum))
            }
        }
        
        // Order players by descending dice rolls. If more than 1 player has the same dice roll, then the
        // order of those players will be randomized.
        return playerNamesAndRolls
            .groupBy(keySelector = { it.diceRoll }, valueTransform = { it.name })
            .entries
            .sortedByDescending { (diceRoll: Int, _) -> diceRoll }
            .flatMap { (_, names: List<String>) -> names.shuffled() }
    }


    /**
     * Asks the current player about any actions they would like to do before they roll and take their turn.
     */
    fun askAboutPreRollAction() {
        val propertyLists = PropertyLists(currentPlayer)
        
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
            ${currentPlayer.name}, what would you like to do?
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
            if (propertyLists.hasPawnable) {
                println("$pawnValue: Pawn some of your properties")
            }
            if (propertyLists.hasUnpawnable) {
                println("$unpawnValue: Unpawn some of your properties")
            }
            if (propertyLists.hasRestaurantAddable) {
                println("$addRestaurantValue: Add restaurants to some of your streets")
            }
            if (propertyLists.hasRestaurantRemovable) {
                println("$removeRestaurantValue: Remove restaurants from some of your streets")
            }
            
            input = getFormattedInput()
            println()
            
            when (input) {
                takeTurnValue -> return
                
                viewSpacesValue -> println(Board)
                
                viewPropertyInfoValue -> askAboutDisplayingPropertyInfo(playerWhoWantsToKnow = currentPlayer)
                
                viewPlayerInfoValue -> println(playerManager.getPlayersInfo())
                
                makeTradeValue -> {
                    askAboutTrading(
                        initiatingPlayer = currentPlayer,
                        initiatingPlayerNeedsMoney = false
                    )
                    propertyLists.refresh()
                }
                
                dropOutValue -> {
                    dropOutAskLoop@ while (true) {
                        print("${currentPlayer.name}, are you sure that you want to drop out? (y/n): ")
                        when (getFormattedInput()) {
                            "y" -> {
                                currentPlayer.removeFromGame()
                                if (playerManager.onePlayerIsInGame) {
                                    gameOver = true
                                } else {
                                    turnOver = true
                                    PropertyManager.makePropertiesUnowned(currentPlayer)
                                    println("\nAll of the properties of ${currentPlayer.name} are now unowned.")
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
                    if (propertyLists.hasPawnable) {
                        askAboutPawningProperties(
                            player = currentPlayer,
                            pawnableProperties = propertyLists.pawnable
                        )
                        propertyLists.refresh()
                    } else {
                        printInvalidInput()
                    }
                }
                
                unpawnValue -> {
                    if (propertyLists.hasUnpawnable) {
                        askAboutUnpawningProperties(unpawnableProperties = propertyLists.unpawnable)
                        propertyLists.refresh()
                    } else {
                        printInvalidInput()
                    }
                }
                
                addRestaurantValue -> {
                    if (propertyLists.hasRestaurantAddable) {
                        askAboutAddingRestaurants(restaurantAddableStreets = propertyLists.restaurantAddable)
                        propertyLists.refresh()
                    } else {
                        printInvalidInput()
                    }
                }
                
                removeRestaurantValue -> {
                    if (propertyLists.hasRestaurantRemovable) {
                        askAboutRemovingRestaurants(
                            player = currentPlayer,
                            restaurantRemovableStreets = propertyLists.restaurantRemovable
                        )
                        propertyLists.refresh()
                    } else {
                        printInvalidInput()
                    }
                }
                
                else -> printInvalidInput()
            }
            
            println()
        }
    }
    

    fun currentPlayerMadeRevolution() {
        println("${currentPlayer.name} made a revolution!")
        currentPlayer.incrementMoneyAndPrintUpdate(512)
    }
    

    fun rollDiceAndMove() {
        val diceRoll: Pair<Int, Int> = getDiceRoll()
        println(diceRoll.run { "${currentPlayer.name} rolled a $first and a $second, for a total of $sum." })
        
        if (diceRoll.bothAreSame) {
            numDoubleRollsInARow++
            if (numDoubleRollsInARow == 3) {
                println("${currentPlayer.name} rolled doubles thrice, or 3 times, in a row so they go to Vacation.")
                numDoubleRollsInARow = 0
                currentPlayer.sendToVacation()
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
        currentPlayer.sendToVacation()

        // If the player rolled doubles, they won't get to go again in this situation.
        goAgain = false
        numDoubleRollsInARow = 0
    }
    

    /**
     * Evaluates the position that the current player landed on and takes appropriate action.
     */
    fun evaluatePosition() {
        print("${currentPlayer.name} landed on position ${currentPlayer.position}, which is ")
        when (val currentBoardSpace: Any? = Board.getSpace(currentPlayer.position)) {
            is Property -> handleLandingOnProperty(currentBoardSpace)
            
            is NonPropertySpace -> {
                println("\"$currentBoardSpace\".")
                when (currentBoardSpace) {
                    NonPropertySpace.START,
                    NonPropertySpace.VACATION,
                    NonPropertySpace.BREAK_TIME -> println("This space has no effect.")
                    
                    NonPropertySpace.GO_ON_VACATION -> handleGoOnVacation()
                    
                    NonPropertySpace.DRAW_ACTION_CARD -> handleLandingOnDrawActionCard()
                }
            }
            
            else -> printError(
                "currentPlayer.position = ${currentPlayer.position} and this caused Board.getSpace to return $currentBoardSpace"
            )
        }
    }


    fun handleLandingOnProperty(property: Property) {
        print(property.name)
        if (property is Street) {
            print(" in the ${property.neighborhood.friendlyName} neighborhood")
        }
        
        val owner: Player? = property.owner
        if (owner == null) {
            println(", which is unowned.")
            val purchasePrice: Int = property.purchasePrice
            
            while (true) {
                println(
                    "${currentPlayer.name}, you have $${currentPlayer.money}. Would you like to buy " +
                    "${property.name} for $$purchasePrice? (y/n):"
                )
                
                when (getFormattedInput()) {
                    "y" -> {
                        if (currentPlayer.money < purchasePrice) {
                            askAboutGettingMoney(
                                player = currentPlayer,
                                moneyNeeded = purchasePrice,
                                playerHasChoice = true
                            )
                            
                            println()
                            
                            // The following condition will be true if the player originally decided that they would
                            // buy the property but either couldn't gather the money for it or changed their mind.
                            if (currentPlayer.money < purchasePrice) {
                                println("${currentPlayer.name} is not buying this property.")
                                return
                            }
                            
                            println(
                                "${currentPlayer.name} gathered the $$purchasePrice that they need to buy ${property.name}."
                            )
                        }
                        
                        property.owner = currentPlayer
                        currentPlayer.decrementMoneyAndPrintUpdate(purchasePrice)
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
        } else {
            print(", which is owned by ")
            if (owner == currentPlayer) {
                println("themself.")
                return
            }
            println("${owner.name}.")
            if (property.isPawned) {
                println("This property is pawned so ${currentPlayer.name} doesn't have to pay a fee.")
                return
            }
            
            val fee: Int
            when (property) {
                is Street -> {
                    fee = property.currentFee
                    println(
                        property.numRestaurants.let {
                            "There ${if (it == 1) "is 1 restaurant" else "are $it restaurants"} on " +
                            "this street and the fee is $$fee."
                        }
                    )
                }
                
                is GolfClub -> {
                    val feeData: GolfClub.FeeData = property.FeeData()
                    fee = feeData.fee
                    val numClubsString =
                        feeData.numClubsOwnedByOwner.let { "$it golf club${if (it == 1) "" else "s"}" }
                    println("${owner.name} owns $numClubsString and the fee is $$fee.")
                }
                
                is SuperStore -> {
                    val diceRoll: Pair<Int, Int> = getDiceRoll()
                    val feeData = SuperStore.FeeData()
                    fee = diceRoll.sum * feeData.multiplier
                    
                    print(
                        if (feeData.onePlayerOwnsBothStores) "${owner.name} owns both super stores"
                        else "This is the only super store that ${owner.name} owns"
                    )
                    println(
                        " so ${currentPlayer.name} has to roll the dice and pay ${feeData.multiplier} " +
                        "times that amount."
                    )
                    println(
                        diceRoll.run {
                            "${currentPlayer.name} rolled a $first and a $second for a total of $sum " +
                            "so they have to pay $$fee."
                        }
                    )
                }
            }
            
            if (currentPlayer.money < fee) {
                askAboutGettingMoney(
                    player = currentPlayer,
                    moneyNeeded = fee,
                    playerHasChoice = false
                )
                
                // Following condition is true if the current player decided to drop out in the
                // askAboutGettingMoney function.
                if (!currentPlayer.isInGame) {
                    // Following condition is true if there are at least 2 players remaining.
                    if (!gameOver) {
                        println(
                            "\n${currentPlayer.name} dropped out of the game so their money and " +
                            "properties will go to ${owner.name}."
                        )
                        owner.incrementMoneyAndPrintUpdate(currentPlayer.money)
                        PropertyManager.transferOwnership(currentOwner = currentPlayer, newOwner = owner)
                    }
                    
                    return
                }
                
                println("\n${currentPlayer.name} gathered the $$fee that they need to pay the fee to ${owner.name}.")
            }
            
            currentPlayer.decrementMoneyAndPrintUpdate(fee)
            owner.incrementMoneyAndPrintUpdate(fee)
        }
    }
    

    fun handleLandingOnDrawActionCard() {
        val topCard: ActionCard = actionDeck.topCard
        println("${currentPlayer.name} draws a card from the Action Deck and it says \"${topCard.message}\".")
        
        // Get off Vacation free cards are the only type of card where the Action Deck top card
        // won't get moved to the bottom.
        if (topCard !is GetOffVacationFreeCard) {
            actionDeck.moveTopCardToBottom()
        }
                
        when (topCard) {
            is MoneyGainCard -> currentPlayer.incrementMoneyAndPrintUpdate(topCard.gainAmount)
            
            is MoneyLossCard -> {
                val lossAmount: Int = topCard.lossAmount
                
                if (currentPlayer.money < lossAmount) {
                    askAboutGettingMoney(
                        player = currentPlayer,
                        moneyNeeded = lossAmount,
                        playerHasChoice = false
                    )
                    
                    // Following condition is true if the player decided to drop out
                    // in the askAboutGettingMoney function.
                    if (!currentPlayer.isInGame) {
                        // Following condition is true if there are at least 2 players remaining.
                        if (!gameOver) {
                            println("\n${currentPlayer.name} dropped out so their properties are now unowned.")
                            PropertyManager.makePropertiesUnowned(currentPlayer)
                        }
                        return
                    }
                }
                
                currentPlayer.decrementMoneyAndPrintUpdate(lossAmount)
            }
            
            is GiveMoneyToOtherPlayersCard -> {
                val moneyOwedToEachPlayer: Int = topCard.moneyAmount
                val otherPlayers: List<Player> =
                    playerManager.getListOfOtherPlayersInGame(excludingPlayer = currentPlayer)
                val totalMoneyOwed: Int = moneyOwedToEachPlayer * otherPlayers.size
                
                if (currentPlayer.money < totalMoneyOwed) {
                    askAboutGettingMoney(
                        player = currentPlayer,
                        moneyNeeded = totalMoneyOwed,
                        playerHasChoice = false
                    )
                    
                    // Following condition is true if the current player decided to drop out in the
                    // askAboutGettingMoney function.
                    if (!currentPlayer.isInGame) {
                        // Following condition is true if there are at least 2 players remaining.
                        if (!gameOver) {
                            println("\n${currentPlayer.name} dropped out so their properties are now unowned.")
                            PropertyManager.makePropertiesUnowned(currentPlayer)
                        }
                        return
                    }
                }
                
                for (otherPlayer: Player in otherPlayers) {
                    currentPlayer.decrementMoneyAndPrintUpdate(moneyOwedToEachPlayer)
                    otherPlayer.incrementMoneyAndPrintUpdate(moneyOwedToEachPlayer)
                }
            }
            
            is GetMoneyFromOtherPlayersCard -> {
                val moneyReceivedFromEachPlayer: Int = topCard.moneyAmount
                for (otherPlayer: Player in
                    playerManager.getListOfOtherPlayersInGame(excludingPlayer = currentPlayer)
                ) {
                    if (otherPlayer.money < moneyReceivedFromEachPlayer) {
                        askAboutGettingMoney(
                            player = otherPlayer,
                            moneyNeeded = moneyReceivedFromEachPlayer,
                            playerHasChoice = false
                        )
                        
                        // Following condition is true if the other player decided to drop out in the
                        // askAboutGettingMoney function.
                        if (!otherPlayer.isInGame) {
                            // Following condition is true if there's 1 player remaining.
                            if (gameOver) {
                                return
                            }
                            println(
                                "\n${otherPlayer.name} dropped out so their money and properties " +
                                "will go to ${currentPlayer.name}."
                            )
                            currentPlayer.incrementMoneyAndPrintUpdate(otherPlayer.money)
                            PropertyManager.transferOwnership(
                                currentOwner = otherPlayer,
                                newOwner = currentPlayer
                            )
                            continue
                        }
                    }
                    
                    otherPlayer.decrementMoneyAndPrintUpdate(moneyReceivedFromEachPlayer)
                    currentPlayer.incrementMoneyAndPrintUpdate(moneyReceivedFromEachPlayer)
                }
            }
            
            is PropertyMaintenanceCard -> {
                val numRestaurantsOwned: Int =
                    PropertyManager.getPropertiesOwnedBy(currentPlayer)
                    .filterIsInstance<Street>()
                    .sumOf { it.numRestaurants }
                
                println()
                
                if (numRestaurantsOwned == 0) {
                    println("${currentPlayer.name} doesn't have any restaurants so they don't have to pay anything.")
                    return
                }
                
                val feePerRestaurant: Int = topCard.feePerRestaurant
                val maintenanceFee: Int = feePerRestaurant * numRestaurantsOwned
                val numRestaurantsString =
                    numRestaurantsOwned.let { "$it restaurant${if (it == 1) "" else "s"}" }
                
                println(
                    "${currentPlayer.name} has $numRestaurantsString and must pay $$feePerRestaurant " +
                    "per restaurant so they owe $$maintenanceFee."
                )

                if (currentPlayer.money < maintenanceFee) {
                    askAboutGettingMoney(
                        player = currentPlayer,
                        moneyNeeded = maintenanceFee,
                        playerHasChoice = false
                    )
                    
                    // Following condition is true if the player decided to drop out
                    // in the askAboutGettingMoney function.
                    if (!currentPlayer.isInGame) {
                        // Following condition is true if there are at least 2 players remaining.
                        if (!gameOver) {
                            println("\n${currentPlayer.name} dropped out so their properties are now unowned.")
                            PropertyManager.makePropertiesUnowned(currentPlayer)
                        }
                        return
                    }
                }
                
                currentPlayer.decrementMoneyAndPrintUpdate(maintenanceFee)
            }
            
            is RelativePositionChangeCard -> {
                currentPlayer.position += topCard.moveAmount
                when {
                    currentPlayer.position > Board.numberOfSpaces -> {
                        currentPlayer.position -= Board.numberOfSpaces
                        currentPlayerMadeRevolution()
                    }
                    
                    currentPlayer.position < 1 ->
                        currentPlayer.position = Board.numberOfSpaces + currentPlayer.position
                }
                evaluatePosition()
            }
            
            is AbsolutePositionChangeCard -> {
                topCard.newPosition.let {
                    if (it < currentPlayer.position) {
                        currentPlayerMadeRevolution()
                    }
                    currentPlayer.position = it
                }
                evaluatePosition()
            }
            
            is GetOffVacationFreeCard -> {
                currentPlayer.addGetOffVacationCard()
                actionDeck.removeGetOffVacationCardAtTop()
            }
            
            is GoOnVacationCard -> handleGoOnVacation()
        }
    }


    fun askAboutVacationAction() {
        val turnString =
            currentPlayer.numTurnsOnVacation.let {
                when (it) {
                    0 -> "first"
                    1 -> "second"
                    2 -> "third (last)"
                    else -> {
                        printError("${currentPlayer.name} was on Vacation with a numTurnsOnVacation value of $it")
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
            ${currentPlayer.name}, you're on Vacation and this is your $turnString turn on Vacation.
            What would you like to do?
            Type one of the following and press Enter:
            $payValue: Pay $$feeToGetOffVacation to get off Vacation
            $rollValue: Try to roll doubles
            """
            .trimIndent()
            .plus(
                if (currentPlayer.hasAGetOffVacationCard) {
                    "\n$getOffVacationCardValue: Use a get off Vacation free card"
                } else ""
            )
        
        vacationActionAskLoop@ while (true) {
            println()
            println(optionsString)
            
            when (getFormattedInput()) {
                payValue -> {
                    if (currentPlayer.money < feeToGetOffVacation) {
                        askAboutGettingMoney(
                            player = currentPlayer,
                            moneyNeeded = feeToGetOffVacation,
                            playerHasChoice = true
                        )
                        
                        // The following condition will be true if the player couldn't gather the money or
                        // changed their mind.
                        if (currentPlayer.money < feeToGetOffVacation) {
                            continue@vacationActionAskLoop
                        }
                    }
                    
                    println("\n${currentPlayer.name} chose to pay so they get to take their turn.")
                    currentPlayer.decrementMoneyAndPrintUpdate(feeToGetOffVacation)
                    currentPlayer.removeFromVacation()
                    return
                }
                
                rollValue -> {
                    val diceRoll: Pair<Int, Int> = getDiceRoll()
                    println("\n${currentPlayer.name} rolled a ${diceRoll.first} and a ${diceRoll.second}.")
                    
                    if (diceRoll.bothAreSame) {
                        println("These are doubles so they are off Vacation and they get to advance ${diceRoll.sum} spaces.")
                        currentPlayer.removeFromVacation()
                        currentPlayer.position += diceRoll.sum
                        evaluatePosition()
                    } else {
                        currentPlayer.numTurnsOnVacation++
                        if (currentPlayer.numTurnsOnVacation == 3) {
                            println("${currentPlayer.name} spent 3 turns on Vacation so it is now over.")
                            currentPlayer.removeFromVacation()
                        }
                    }
                    
                    turnOver = true
                    return
                }
                
                getOffVacationCardValue -> {
                    println()
                    if (currentPlayer.hasAGetOffVacationCard) {
                        println(
                            "${currentPlayer.name} chose to use a get off Vacation free card so they " +
                            "get to take their turn."
                        )
                        currentPlayer.removeFromVacation()
                        currentPlayer.removeGetOffVacationCard()
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
        
        while (true) {
            println(optionsString)
            input = getFormattedInput()
            if (input == "b") {
                return
            }
            
            println()
            println(
                when (input) {
                    basicInfoYourPropsValue -> {
                        val properties: List<Property> =
                            PropertyManager.getPropertiesOwnedBy(playerWhoWantsToKnow)
                        
                        if (properties.isEmpty()) {
                            "${playerWhoWantsToKnow.name} doesn't own any properties."
                        } else {
                            properties.joinToString(
                                prefix = "Basic info for properties owned by ${playerWhoWantsToKnow.name}\n\n",
                                transform = { it.basicInfo },
                                separator = "\n"
                            )
                        }
                    }
                    
                    basicInfoAllPropsValue -> PropertyManager.getPropertiesInfo(includeAll = false)
                    
                    allInfoAllPropsValue -> PropertyManager.getPropertiesInfo(includeAll = true)
                    
                    else -> invalidInputText
                }
            )
            println()
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
    

    fun askAboutPawningProperties(player: Player, pawnableProperties: List<Property>) {
        println("Here are the properties owned by ${player.name} that can be pawned:")
        pawnableProperties.forEach { println(it.pawnInfo) }
        
        while (true) {
            println(
                "\nEnter the positions of the properties you would like to pawn with each position " +
                "separated by space or enter \"b\" to go back:"
            )
            
            input = getFormattedInput()
            if (input == "b") {
                return
            }
            
            val selectedProperties: List<Property> =
                pawnableProperties.filterByPositions(positionsInput = input)
            
            if (selectedProperties.isEmpty()) {
                printInvalidPositionsText()
                continue
            }
            
            val moneyGain: Int = selectedProperties.sumOf { it.pawnPrice }
            selectedProperties.forEach { it.pawn() }
            println(
                "\nThe following properties were pawned: " +
                selectedProperties.joinToString(transform = { it.name }, separator = ", ")
            )
            player.incrementMoneyAndPrintUpdate(moneyGain)
            return
        }
    }
    

    fun askAboutUnpawningProperties(unpawnableProperties: List<Property>) {
        // Only the current player is able to unpawn properties.
        println("Here are the properties owned by ${currentPlayer.name} that can be unpawned:")
        unpawnableProperties.forEach { println(it.unpawnInfo) }
        
        while (true) {
            println()
            println(
                """
                ${currentPlayer.name}, you have $${currentPlayer.money}
                Enter the positions of the properties you would like to unpawn with each position separated by space or enter "b" to go back:
                """.trimIndent()
            )
            
            input = getFormattedInput()
            if (input == "b") {
                return
            }
            
            val selectedProperties: List<Property> =
                unpawnableProperties.filterByPositions(positionsInput = input)
            
            if (selectedProperties.isEmpty()) {
                println()
                printInvalidPositionsText()
                continue
            }
            
            val moneyOwed: Int = selectedProperties.sumOf { it.unpawnPrice }
            if (currentPlayer.money < moneyOwed) {
                askAboutGettingMoney(
                    player = currentPlayer,
                    moneyNeeded = moneyOwed,
                    playerHasChoice = true
                )
                
                println()
                
                // Following condition will be true if the player couldn't get enough money or changed their mind.
                if (currentPlayer.money < moneyOwed) {
                    println("${currentPlayer.name} isn't unpawning anything.")
                    return
                }
                
                println("${currentPlayer.name} gathered the $$moneyOwed that they need to unpawn their properties.")
            }
            
            selectedProperties.forEach { it.unpawn() }
            println(
                "\nThe following properties were unpawned: " +
                selectedProperties.joinToString(transform = { it.name }, separator = ", ")
            )
            currentPlayer.decrementMoneyAndPrintUpdate(moneyOwed)
            return
        }
    }
    

    fun askAboutAddingRestaurants(restaurantAddableStreets: List<Street>) {
        // Only the current player is able to add restaurants to streets.
        println("Here are the streets owned by ${currentPlayer.name} that can have a restaurant added to it:")
        restaurantAddableStreets.forEach { println(it.restaurantAddInfo) }
        
        while (true) {
            println()
            println(
                """
                ${currentPlayer.name}, you have $${currentPlayer.money}
                Enter the positions of the streets you would like to add a restaurant to with each position separated by space or enter "b" go back:
                """.trimIndent()
            )
            
            input = getFormattedInput()
            if (input == "b") {
                return
            }
            
            val selectedStreets: List<Street> =
                restaurantAddableStreets.filterByPositions(positionsInput = input)
            
            if (selectedStreets.isEmpty()) {
                println()
                printInvalidPositionsText()
                continue
            }
            
            val moneyOwed: Int = selectedStreets.sumOf { it.restaurantAddPrice }
            if (currentPlayer.money < moneyOwed) {
                askAboutGettingMoney(
                    player = currentPlayer,
                    moneyNeeded = moneyOwed,
                    playerHasChoice = true
                )
                
                println()
                
                // Following condition will be true if the player couldn't get enough money or changed their mind.
                if (currentPlayer.money < moneyOwed) {
                    println("${currentPlayer.name} isn't adding any restaurants.")
                    return
                }
                
                println("${currentPlayer.name} gathered the $$moneyOwed that they need to add the restaurants.")
            }
            
            selectedStreets.forEach { it.addRestaurant() }
            println(
                "\nA restaurant was added to each of the following streets: " +
                selectedStreets.joinToString(transform = { it.name }, separator = ", ")
            )
            currentPlayer.decrementMoneyAndPrintUpdate(moneyOwed)
            return
        }
    }
    

    fun askAboutRemovingRestaurants(player: Player, restaurantRemovableStreets: List<Street>) {
        println("Here are the streets owned by ${player.name} that can have a restaurant removed from it:")
        restaurantRemovableStreets.forEach { println(it.restaurantRemoveInfo) }

        while (true) {
            println(
                "\nEnter the positions of the streets you would like to remove a restaurant from with each " +
                "position separated by space or enter \"b\" to go back:"
            )
            
            input = getFormattedInput()
            if (input == "b") {
                return
            }
            
            val selectedStreets: List<Street> =
                restaurantRemovableStreets.filterByPositions(positionsInput = input)
            
            if (selectedStreets.isEmpty()) {
                println()
                printInvalidPositionsText()
                continue
            }
            
            val moneyGain: Int = selectedStreets.sumOf { it.restaurantRemoveGain }
            selectedStreets.forEach { it.removeRestaurant() }
            println(
                "\nA restaurant was removed from each of the following streets: " +
                selectedStreets.joinToString(transform = { it.name }, separator = ", ")
            )
            player.incrementMoneyAndPrintUpdate(moneyGain)
            return
        }
    }

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
    fun askAboutGettingMoney(player: Player, moneyNeeded: Int, playerHasChoice: Boolean) {
        val propertyLists = PropertyLists(player)
        
        // Valid input values
        val exitValue = "ex"
        val makeTradeValue = "tr"
        val pawnValue = "pa"
        val removeRestaurantValue = "rr"
        
        while (player.money < moneyNeeded) {
            println(
                "\n${player.name}, you need $$moneyNeeded and currently have $${player.money}. " +
                "Select one of the following options to get more money:"
            )
            println(
                """
                $exitValue: ${if (playerHasChoice) "Go back" else "Drop out of the game"}
                $makeTradeValue: Make a trade with another player
                """.trimIndent()
            )
            if (propertyLists.hasPawnable) {
                println("$pawnValue: Pawn some of your properties")
            }
            if (propertyLists.hasRestaurantRemovable) {
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
                    return
                }
                
                makeTradeValue -> {
                    println()
                    askAboutTrading(initiatingPlayer = player, initiatingPlayerNeedsMoney = true)
                    propertyLists.refresh()
                }
                
                pawnValue -> {
                    println()
                    if (propertyLists.hasPawnable) {
                        askAboutPawningProperties(player = player, pawnableProperties = propertyLists.pawnable)
                        propertyLists.refresh()
                    } else {
                        printInvalidInput()
                    }
                }
                
                removeRestaurantValue -> {
                    println()
                    if (propertyLists.hasRestaurantRemovable) {
                        askAboutRemovingRestaurants(
                            player = player,
                            restaurantRemovableStreets = propertyLists.restaurantRemovable
                        )
                        propertyLists.refresh()
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
    
    /**
     * Interacts with the initiating player about trading. This includes asking who they want to trade with and
     * what they want to trade. If a trade is agreed upon, the necessary changes are made.
     *
     * If the initiatingPlayerNeedsMoney param is true, the initiating player will need to select a money amount
     * they want from the player they want to make a trade with and they won't be able to offer money themselves.
     */
    fun askAboutTrading(initiatingPlayer: Player, initiatingPlayerNeedsMoney: Boolean) {        
        /**
         * When a player wants to make a trade, they will have to select what they want to offer and what they
         * want to receive. These can include money, properties, and get off Vacation free cards. This class
         * is used as a data structure for those.
         */
        class TradeItems {
            var money = 0
            
            var properties = emptyList<Property>()
            
            var numGetOffVacationCards = 0
            
            /**
             * Is true when either the amount of money, properties, or amount of get off Vacation free cards are
             * different than when this object was instantiated. Is false otherwise.
             */
            val somethingHasBeenChanged: Boolean
                get() = money != 0 || properties.isNotEmpty() || numGetOffVacationCards != 0
        }

        
        // Let otherPlayersMap be a Map where the keys are stringified ints that start at 1 and the values are Players.
        val otherPlayersMap: Map<String, Player> =
            playerManager.getListOfOtherPlayersInGame(excludingPlayer = initiatingPlayer)
            .withIndex()
            .associate { (index: Int, player: Player) -> (index + 1).toString() to player }
        

        mainTradingLoop@ while (true) {
            println("${initiatingPlayer.name}, you are at the beginning of the trade process.")
            
            val otherPlayer: Player
            val propertyInfoValue = "pr"
            val playerInfoValue = "pl"
            val cancelValue = "c"
            
            val optionsString =
                listOf(
                    "${initiatingPlayer.name}, type one of the following and press Enter:",
                    "$propertyInfoValue: See property info",
                    "$playerInfoValue: See player info",
                    "$cancelValue: Cancel the trade"
                )
                .plus(
                    otherPlayersMap.map {
                        (intString, player) -> "${intString}: Initiate a trade with ${player.name}"
                    }
                )
                .joinToString(separator = "\n")
            
            val initiatingPlayerProperties: List<Property> =
                PropertyManager.getPropertiesOwnedBy(initiatingPlayer)
            
            tradeBeginningLoop@ while (true) {
                println(optionsString)
                input = getFormattedInput()

                when (input) {
                    propertyInfoValue -> {
                        println()
                        askAboutDisplayingPropertyInfo(playerWhoWantsToKnow = initiatingPlayer)
                    }
                    
                    playerInfoValue -> println('\n' + playerManager.getPlayersInfo())
                    
                    cancelValue -> return
                    
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
            
            val initiatingPlayerHasAProperty: Boolean = initiatingPlayerProperties.isNotEmpty()
            val otherPlayerProperties: List<Property> = PropertyManager.getPropertiesOwnedBy(otherPlayer)
            val otherPlayerHasAProperty: Boolean = otherPlayerProperties.isNotEmpty()
            
            // Prevent the player from continuing if there is nothing that can be traded besides money.
            if (!initiatingPlayerHasAProperty && !initiatingPlayer.hasAGetOffVacationCard &&
                !otherPlayerHasAProperty && !otherPlayer.hasAGetOffVacationCard
            ) {
                println(
                    "There is nothing that ${initiatingPlayer.name} and ${otherPlayer.name} can trade with each " +
                    "other besides money. The trade process will be restarted."
                )
                continue@mainTradingLoop
            }
            
            if (initiatingPlayerNeedsMoney && otherPlayer.money == 0) {
                println(
                    "${initiatingPlayer.name} needs money and ${otherPlayer.name} doesn't have any. The trade " +
                    "process will be restarted."
                )
                continue@mainTradingLoop
            }
            
            
            // Part for having the initiating player select what they want.

            val wantedItems = TradeItems()

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
                        print("\n${otherPlayer.name} has $${otherPlayer.money}. Enter an amount")
                        println(
                            if (initiatingPlayerNeedsMoney) {
                                ". You need money so entering an amount is mandatory."
                            } else {
                                " or enter \"b\" to go back:"
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
                
                if (otherPlayerHasAProperty) {
                    propertyAskLoop1@ while (true) {
                        print("${initiatingPlayer.name}, would you like any properties from ${otherPlayer.name}? (y/n): ")
                        input = getFormattedInput()

                        when (input) {
                            "n" -> break@propertyAskLoop1
                            
                            "y" -> {
                                println("\nProperties owned by ${otherPlayer.name}:")
                                otherPlayerProperties.forEach { println(it.basicInfo) }
                                
                                propertyAskLoop2@ while (true) {
                                    println(
                                        "\nEnter the positions of all the properties you would like with each " +
                                        "position separated by space or enter \"b\" to go back:"
                                    )
                                    input = getFormattedInput()
                                    if (input == "b") {
                                        continue@propertyAskLoop1
                                    }

                                    val propertiesSelected: List<Property> =
                                        otherPlayerProperties.filterByPositions(positionsInput = input)
                                    
                                    if (propertiesSelected.isEmpty()) {
                                        println()
                                        printInvalidPositionsText()
                                        continue@propertyAskLoop2
                                    }
                                    
                                    wantedItems.properties = propertiesSelected
                                    break@propertyAskLoop1
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
                            "${initiatingPlayer.name}, would you like any get off Vacation free cards from " +
                            "${otherPlayer.name}? (y/n): "
                        )
                        input = getFormattedInput()

                        when (input) {
                            "y" -> {
                                getOffVacationCardsAskLoop2@ while (true) {
                                    println(
                                        "\n${otherPlayer.name} has ${otherPlayer.numGetOffVacationCards} get " +
                                        "off Vacation free cards. How many would you like? Enter an amount or " +
                                        "enter \"b\" to go back:"
                                    )
                                    input = getFormattedInput()
                                    if (input == "b") {
                                        continue@getOffVacationCardsAskLoop1
                                    }
                                    
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
                        ${initiatingPlayer.name}, you didn't select anything for what you want
                        Type one of the following and press Enter:
                        ta: Try again
                        c: Cancel the trade
                        """.trimIndent()
                    )

                    when (getFormattedInput()) {
                        "ta" -> continue@selectWhatIsWanted
                        
                        "c" -> return
                        
                        else -> {
                            println()
                            printInvalidInput()
                        }
                    }
                }
            }
            
            
            // Part for having the initiating player select what to offer.

            val offeredItems = TradeItems()

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
                                        "\n${initiatingPlayer.name}, you have $${initiatingPlayer.money}, how much " +
                                        "of it would you like to offer? Enter an amount or enter \"b\" to go back:"
                                    )
                                    input = getFormattedInput()
                                    if (input == "b") {
                                        continue@moneyAskLoop1
                                    }
                                    
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
                
                if (initiatingPlayerHasAProperty) {
                    propertyAskLoop1@ while (true) {
                        print("${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} any properties? (y/n): ")
                        input = getFormattedInput()

                        when (input) {
                            "y" -> {
                                println("\nProperties owned by ${initiatingPlayer.name}:")
                                initiatingPlayerProperties.forEach { println(it.basicInfo) }
                                
                                propertyAskLoop2@ while (true) {
                                    println(
                                        "\nEnter the positions of the properties you would like to offer with each " +
                                        "position separated by space or enter \"b\" to go back:"
                                    )
                                    input = getFormattedInput()
                                    if (input == "b") {
                                        continue@propertyAskLoop1
                                    }
                                    
                                    val selectedProperties: List<Property> =
                                        initiatingPlayerProperties.filterByPositions(positionsInput = input)
                                    
                                    if (selectedProperties.isEmpty()) {
                                        println()
                                        printInvalidPositionsText()
                                        continue@propertyAskLoop2
                                    }
                                    
                                    offeredItems.properties = selectedProperties
                                    break@propertyAskLoop1
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
                            "${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} any get " +
                            "off Vacation free cards? (y/n): "
                        )
                        input = getFormattedInput()

                        when (input) {
                            "y" -> {
                                getOffVacationCardsAskLoop2@ while (true) {
                                    println(
                                        "\n${initiatingPlayer.name}, you have " +
                                        initiatingPlayer.numGetOffVacationCards +
                                        " get off Vacation free cards. How many would you like to offer? " +
                                        "Enter an amount or \"b\" to go back:"
                                    )
                                    input = getFormattedInput()
                                    if (input == "b") {
                                        continue@getOffVacationCardsAskLoop1
                                    }
                                    
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
                                println()
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
                        ta: Try again
                        c: Cancel the trade
                        """.trimIndent()
                    )

                    when (getFormattedInput()) {
                        "ta" -> continue@selectWhatToOffer
                        
                        "c" -> return
                        
                        else -> {
                            println()
                            printInvalidInput()
                        }
                    }
                }
            }
            
            
            fun getOwnedNeighborhoodMessage(player: Player) =
                "Note: the above property is a street that is in a neighborhood that is currently owned by " +
                "${player.name}. If ${player.name} agrees to the trade, that neighborhood will no longer be owned " +
                "by only them so all restaurants will be sold and the fees will go back to the starting fees."

            
            // Part for the initiating player to confirm what they want and what they offer.

            confirmTrade@ while (true) {
                println("\nFor this trade, ${initiatingPlayer.name} will give ${otherPlayer.name} the following:")
                offeredItems.money.let { if (it > 0) println("\n$$it") }
                
                if (offeredItems.properties.isNotEmpty()) {
                    println("\nThe following properties:")
                    for (p: Property in offeredItems.properties) {
                        println(p.basicInfo)
                        if (p is Street && p.neighborhood.onePlayerOwnsAllStreets) {
                            println(getOwnedNeighborhoodMessage(initiatingPlayer))
                        }
                    }
                }
                
                offeredItems.numGetOffVacationCards.let {
                    if (it > 0) {
                        println()
                        println(
                            if (it == 1) "A get off Vacation free card"
                            else "$it get off Vacation free cards"
                        )
                    }
                }


                println("\n${otherPlayer.name} will give ${initiatingPlayer.name} the following:")
                wantedItems.money.let { if (it > 0) println("\n$$it") }

                if (wantedItems.properties.isNotEmpty()) {
                    println("\nThe following properties:")
                    for (p: Property in wantedItems.properties) {
                        println(p.basicInfo)
                        if (p is Street && p.neighborhood.onePlayerOwnsAllStreets) {
                            println(getOwnedNeighborhoodMessage(otherPlayer))
                        }
                    }
                }
                
                wantedItems.numGetOffVacationCards.let {
                    if (it > 0) {
                        println()
                        println(
                            if (it == 1) "A get off Vacation free card"
                            else "$it get off Vacation free cards"
                        )
                    }
                }

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
                    "y" -> break@confirmTrade
                    
                    "n" -> {
                        while (true) {
                            println()
                            println(
                                """
                                ${initiatingPlayer.name}, type one of the following and press Enter:
                                st: Go to the start of the trade process
                                ex: Exit the trade process
                                """.trimIndent()
                            )
                            
                            when (getFormattedInput()) {
                                "st" -> continue@mainTradingLoop
                                
                                "ex" -> return
                                
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
                    
                    playerInfoValue -> println('\n' + playerManager.getPlayersInfo())
                    
                    else -> {
                        println()
                        printInvalidInput()
                    }
                }
            }
            
            
            // Part for the other player to accept or deny the trade and make the changes if accepted.

            acceptOrDenyTrade@ while (true) {
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
                        return
                    }
                    
                    propertyInfoValue -> {
                        println()
                        askAboutDisplayingPropertyInfo(playerWhoWantsToKnow = otherPlayer)
                    }
                    
                    playerInfoValue -> println('\n' + playerManager.getPlayersInfo())
                    
                    "y" -> {
                        // Give the other player what was agreed upon.

                        offeredItems.money.let {
                            if (it > 0) {
                                initiatingPlayer.decrementMoneyAndPrintUpdate(it)
                                otherPlayer.incrementMoneyAndPrintUpdate(it)
                            }
                        }
                        
                        for (property: Property in offeredItems.properties) {
                            if (property is Street && property.neighborhood.onePlayerOwnsAllStreets) {
                                val numNeighborhoodRestaurants: Int = property.neighborhood.numRestaurants
                                if (numNeighborhoodRestaurants > 0) {
                                    println()
                                    println(
                                        if (numNeighborhoodRestaurants == 1) {
                                            "There is 1 restaurant in the ${property.neighborhood.friendlyName} " +
                                            "neighborhood so this will be sold."
                                        } else {
                                            "There are $numNeighborhoodRestaurants restaurants in the " +
                                            "${property.neighborhood.friendlyName} neighborhood so these will be sold."
                                        }
                                    )
                                    
                                    // Sell all restaurants in the neighborhood.
                                    property.neighborhood.removeRestaurants()
                                    val moneyEarned: Int = numNeighborhoodRestaurants * property.restaurantRemoveGain
                                    initiatingPlayer.incrementMoneyAndPrintUpdate(moneyEarned)
                                }
                            }
                            
                            property.owner = otherPlayer
                        }
                        
                        initiatingPlayer.numGetOffVacationCards -= offeredItems.numGetOffVacationCards
                        otherPlayer.numGetOffVacationCards += offeredItems.numGetOffVacationCards
                        

                        // Now give the initiating player what was agreed upon.

                        wantedItems.money.let {
                            if (it > 0) {
                                otherPlayer.decrementMoneyAndPrintUpdate(it)
                                initiatingPlayer.incrementMoneyAndPrintUpdate(it)
                            }
                        }
                        
                        for (property: Property in wantedItems.properties) {
                            if (property is Street && property.neighborhood.onePlayerOwnsAllStreets) {
                                val numNeighborhoodRestaurants: Int = property.neighborhood.numRestaurants
                                if (numNeighborhoodRestaurants > 0) {
                                    println()
                                    println(
                                        if (numNeighborhoodRestaurants == 1) {
                                            "There is 1 restaurant in the ${property.neighborhood.friendlyName} " +
                                            "neighborhood so this will be sold."
                                        } else {
                                            "There are $numNeighborhoodRestaurants restaurants in the " +
                                            "${property.neighborhood.friendlyName} neighborhood so these will be sold."
                                        }
                                    )
                                    
                                    // Sell all restaurants in the neighborhood.
                                    property.neighborhood.removeRestaurants()
                                    val moneyEarned: Int = numNeighborhoodRestaurants * property.restaurantRemoveGain
                                    otherPlayer.incrementMoneyAndPrintUpdate(moneyEarned)
                                }
                            }
                            
                            property.owner = initiatingPlayer
                        }
                        
                        otherPlayer.numGetOffVacationCards -= wantedItems.numGetOffVacationCards
                        initiatingPlayer.numGetOffVacationCards += wantedItems.numGetOffVacationCards
                        return
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