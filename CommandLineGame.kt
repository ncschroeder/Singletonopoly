import kotlin.random.Random

fun main() {
    while (true) {
        CommandLineGame()
        playAgainAskLoop@ while (true) {
            print("Would you like to play again? (y/n) ")
            when (readLine()!!.toLowerCase()) {
                "y" -> {
                    println()
                    break@playAgainAskLoop
                }
                "n" -> return
                else -> println("Invalid input")
            }
        }
    }
}

/**
 * The CommandLineGame class consists of all the objects of a game and all the functions that have those objects
 * interact with each other. All that the main function needs to begin a game is a simple instantiation which doesn't
 * need to be stored to a variable or have any functions called.
 */
class CommandLineGame {
    val playerManager = PlayerManager()
    val board = Board()
    val actionDeck = ActionDeck()
    lateinit var currentPlayer: PlayerManager.Player
    var gameOver = false
    var turnOver = false
    var goAgain = false
    var numberOfDoubleRolls = 0

    init {
        println("You've started a game of Singletonopoly")
        setup()
        play()
    }

    fun setup() {
        var numberOfPlayers: Int?
        while (true) {
            print("How many players? ")
            numberOfPlayers = readLine()!!.toIntOrNull()
            if (numberOfPlayers == null || numberOfPlayers !in 2..8) {
                println("The number of players must be a number in the range of 2 to 8 inclusive")
            } else {
                break
            }
        }

        /**
         * The purpose of the PlayerOrdering class is to put instances of it in a list and sort that list based on
         * dice rolls in descending order (higher dice rolls are at the beginning).
         */
        class PlayerOrdering(val name: String, val totalDiceRoll: Int) : Comparable<PlayerOrdering> {
            override fun compareTo(other: PlayerOrdering) = other.totalDiceRoll - this.totalDiceRoll
        }

        val playerOrderingList = mutableListOf<PlayerOrdering>()
        val namesSet = mutableSetOf<String>()
        var playerNumber = 1
        while (playerNumber <= numberOfPlayers!!) {
            print("\nEnter a unique name for player $playerNumber or enter nothing for \"Player $playerNumber\": ")
            val input = readLine()!!
            val name: String
            if (input.isEmpty()) {
                name = "Player $playerNumber"
            } else if (input in namesSet) {
                println("$input is already a name for another player, you must select a unique name")
                continue
            } else {
                name = input
            }

            val diceRoll1 = getDiceRoll()
            val diceRoll2 = getDiceRoll()
            val totalDiceRoll = diceRoll1 + diceRoll2
            println("$name got a $diceRoll1 and a $diceRoll2 for their beginning dice roll for a total of $totalDiceRoll")

            playerOrderingList.add(PlayerOrdering(name = name, totalDiceRoll = totalDiceRoll))
            namesSet.add(name)

            playerNumber++
        }

        playerOrderingList.sort()

        println("\nThe order of the players is")
        for (playerOrdering in playerOrderingList) {
            println(playerOrdering.name)
            playerManager.addPlayer(playerOrdering.name)
        }
        println()

        currentPlayer = playerManager.currentPlayer
        playerManager.vacationPosition = board.getVacationPosition()

        val propertiesUsedByActionDeck = arrayOf("Knuth Street", "Lagos Avenue")
        val propertyPositions = board.getPropertyPositions(propertiesUsedByActionDeck)
        actionDeck.setPropertyPositions(propertyPositions)
        actionDeck.shuffle()
    }

    fun play() {
        gameLoop@ while (true) {
            // The gameOver and turnOver variables might change during the functions that get called so these
            // variables get checked and appropriate action is taken.
            println("It's ${currentPlayer.name}'s turn")
            askAboutPreRollAction()
            if (gameOver) {
                break@gameLoop
            }
            if (currentPlayer.isOnVacation && !turnOver) {
                doVacationAction()
            }
            if (!turnOver) {
                rollDiceAndMove()
            }
            if (!turnOver) {
                evaluatePosition()
            }
            if (gameOver) {
                break@gameLoop
            }
            endTurn()
        }

        // Following condition won't be true if the game was ended by choice abruptly.
        if (playerManager.onePlayerIsInGame) {
            println("The winner is ${playerManager.getWinnerName()}!\n")
        }
    }

    /**
     * @return An random Int that is greater than or equal to 1 and less than or equal to 6.
     */
    fun getDiceRoll() = Random.nextInt(1, 7)

    fun endTurn() {
        if (turnOver) {
            turnOver = false
        }
        if (goAgain) {
            goAgain = false
        } else {
            playerManager.switchToNextPlayer()
            currentPlayer = playerManager.currentPlayer
        }
        println("End of turn. Type \"c\" and press enter to continue.")
        while (readLine()!!.toLowerCase() != "c") {
            // Do nothing
        }
        println()
    }

    /**
     * Asks the current player about any actions they would like to do before they roll and take their turn. These
     * actions include viewing info about the board, properties on it, or other players; initiating a trade with
     * other players, dropping out of the game, ending the game, adding or removing restaurants from streets they
     * own, and pawning or unpawning properties they own.
     */
    fun askAboutPreRollAction() {
        var possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer)
        preRollActionAskLoop@ while (true) {
            val playerCanAddRestaurant = possiblePropertyActions.getValue("add restaurant")
            val playerCanRemoveRestaurant = possiblePropertyActions.getValue("remove restaurant")
            val playerCanPawn = possiblePropertyActions.getValue("pawn")
            val playerCanUnpawn = possiblePropertyActions.getValue("unpawn")

            println(
                    """
                    ${currentPlayer.name}, type one of the following and press enter:
                    
                    tt: Take your turn
                    s: View the board spaces
                    pr: View property info
                    pl: View player info
                    mt: Make a trade with another player
                    do: Drop out of the game
                    eg: End the game
                    """.trimIndent()
            )

            if (playerCanAddRestaurant) {
                println("ar: Add a restaurant to one of your properties")
            }
            if (playerCanRemoveRestaurant) {
                println("rr: Remove a restaurant from one of your properties")
            }
            if (playerCanPawn) {
                println("pa: Pawn one of your properties")
            }
            if (playerCanUnpawn) {
                println("u: Unpawn one of your properties")
            }

            val input = readLine()!!.toLowerCase()
            println()
            when (input) {
                // During some of these cases, possiblePropertyActions gets refreshed to check for changes
                "tt" -> return
                "s" -> println(board.spacesAndSimplePropertyInfo)
                "pr" -> askAboutDisplayingPropertyInfo(currentPlayer)
                "pl" -> println(playerManager)
                "mt" -> {
                    askAboutTrading(
                            initiatingPlayer = currentPlayer,
                            initiatingPlayerNeedsMoney = false
                    )
                    possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer)
                }

                "do" -> {
                    dropOutAskLoop@ while (true) {
                        print("${currentPlayer.name}, are you sure that you want to drop out? (y/n) ")
                        when (readLine()!!.toLowerCase()) {
                            "y" -> {
                                currentPlayer.removeFromGame()
                                if (playerManager.onePlayerIsInGame) {
                                    gameOver = true
                                } else {
                                    turnOver = true
                                    println("All of the properties of ${currentPlayer.name} are now unowned")
                                    board.makePropertiesUnowned(currentPlayer)
                                }
                                return
                            }
                            "n" -> break@dropOutAskLoop
                            else -> println("Invalid input")
                        }
                    }
                }

                "eg" -> {
                    endGameAskLoop@ while (true) {
                        print("Are you sure that you want to end the game? (y/n) ")
                        when (readLine()!!.toLowerCase()) {
                            "y" -> {
                                gameOver = true
                                return
                            }
                            "n" -> break@endGameAskLoop
                            else -> println("Invalid input")
                        }
                    }
                }

                "ar" -> {
                    if (playerCanAddRestaurant) {
                        askAboutAddingRestaurant()
                        possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer)
                    } else {
                        println("Invalid input")
                    }
                }

                "rr" -> {
                    if (playerCanRemoveRestaurant) {
                        askAboutRemovingRestaurant(currentPlayer)
                        possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer)
                    } else {
                        println("Invalid input")
                    }
                }

                "pa" -> {
                    if (playerCanPawn) {
                        askAboutPawningProperty(currentPlayer)
                        possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer)
                    } else {
                        println("Invalid input")
                    }
                }

                "u" -> {
                    if (playerCanUnpawn) {
                        askAboutUnpawningProperty()
                        possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer)
                    } else {
                        println("Invalid input")
                    }
                }

                else -> println("Invalid input")
            }
            println()
        }
    }

    /**
     * Asks a player about which types of properties they would like to see info for and how much detail
     * they would like to see the info in.
     *
     * @param playerWhoWantsToKnow can be the current player if a player wants to view property info at the beginning
     * of their turn. Can also be one of the 2 players involved in a trade: the initiating player and the player the
     * initiating player wants to trade with.
     */
    fun askAboutDisplayingPropertyInfo(playerWhoWantsToKnow: PlayerManager.Player) {
        while (true) {
            println(
                    """
                ${playerWhoWantsToKnow.name}, enter one of the following and press enter
                        
                mp: View moderately detailed info for all properties
                yp: View moderately detailed info for your properties
                ms: View moderately detailed street info
                hs: View highly detailed street info
                gc: View moderately detailed golf course info
                ss: View moderately detailed super store info
                b: Go back
            """.trimIndent()
            )

            val input = readLine()!!.toLowerCase()
            if (input == "b") {
                return
            }

            println()
            println(
                    when (input) {
                        "mp" -> board.moderateDetailPropertyInfo
                        "yp" -> board.getModerateDetailPropertyInfoOfPlayer(playerWhoWantsToKnow)
                        "ms" -> board.moderateDetailStreetInfo
                        "hs" -> board.highDetailStreetInfo
                        "gc" -> board.golfCourseInfo
                        "ss" -> board.superStoreInfo
                        else -> "Invalid input"
                    }
            )
            println()
        }
    }

    /**
     * Asks the current player what they would like to do for their turn when they are on vacation.
     * @throws IllegalStateException if the current player is not on vacation.
     */
    fun doVacationAction() {
        if (!currentPlayer.isOnVacation) {
            throw IllegalStateException("Current player is not on vacation")
        }
        println(
                "${currentPlayer.name}, you're on vacation and this is your " +
                        when (currentPlayer.numberOfTurnsOnVacation) {
                            0 -> "first"
                            1 -> "second"
                            2 -> "third (last)"
                            else -> throw IllegalStateException(
                                    "Shouldn't be on vacation at this point. " +
                                            "Number of turns on vacation: ${currentPlayer.numberOfTurnsOnVacation}"
                            )
                        }
                        + " turn on vacation"
        )

        val feeToGetOffVacation = 128
        vacationActionAskLoop@ while (true) {
            println(
                    """
                Would you like to:
                1: Pay $$feeToGetOffVacation to get off vacation
                2: Try to roll doubles
            """.trimIndent()
            )
            if (currentPlayer.hasAGetOffVacationCard) {
                println("3: Use a get off vacation card")
            }

            when (readLine()) {
                "1" -> {
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
                    println("${currentPlayer.name} has chosen to pay so they will get to take their turn")
                    currentPlayer.money -= feeToGetOffVacation
                    printMoneyLossUpdate(player = currentPlayer, moneyLoss = feeToGetOffVacation)
                    currentPlayer.removeFromVacation()
                    return
                }

                "2" -> {
                    val diceRoll1 = getDiceRoll()
                    val diceRoll2 = getDiceRoll()
                    println("${currentPlayer.name} rolled a $diceRoll1 and a $diceRoll2")
                    if (diceRoll1 == diceRoll2) {
                        println("These are doubles so they are off vacation")
                        currentPlayer.removeFromVacation()
                        currentPlayer.position += (diceRoll1 + diceRoll2)
                        evaluatePosition()
                    } else {
                        currentPlayer.numberOfTurnsOnVacation++
                        if (currentPlayer.numberOfTurnsOnVacation == 3) {
                            println("${currentPlayer.name} has spent 3 turns on vacation so it is now over")
                            currentPlayer.removeFromVacation()
                        }
                    }
                    turnOver = true
                    return
                }

                "3" -> {
                    if (currentPlayer.hasAGetOffVacationCard) {
                        println(
                                "${currentPlayer.name} has chosen to use a get off vacation card so they " +
                                        "get to take their turn"
                        )
                        currentPlayer.removeFromVacation()
                        currentPlayer.removeGetOffVacationCard()
                        actionDeck.insertGetOffVacationCardAtBottom()
                        return
                    } else {
                        println("Invalid input")
                    }
                }

                else -> println("Invalid input")
            }
        }
    }

    fun printMoneyGainUpdate(player: PlayerManager.Player, moneyGain: Int) {
        println("${player.name} has gained $$moneyGain and now has $${player.money}")
    }

    fun printMoneyLossUpdate(player: PlayerManager.Player, moneyLoss: Int) {
        println("${player.name} has lost $$moneyLoss and now has $${player.money}")
    }

    fun currentPlayerMadeRevolution() {
        println("${currentPlayer.name} has made a revolution!")
        currentPlayer.money += 512
        printMoneyGainUpdate(player = currentPlayer, moneyGain = 512)
    }

    fun rollDiceAndMove() {
        val diceRoll1 = getDiceRoll()
        val diceRoll2 = getDiceRoll()
        val totalDiceRoll = diceRoll1 + diceRoll2
        println("${currentPlayer.name} rolled a $diceRoll1 and a $diceRoll2, for a total of $totalDiceRoll")
        if (diceRoll1 == diceRoll2) {
            numberOfDoubleRolls++
            if (numberOfDoubleRolls == 3) {
                println("${currentPlayer.name} rolled doubles 3 times in a row so they go to vacation")
                numberOfDoubleRolls = 0
                currentPlayer.sendToVacation()
                turnOver = true
                return
            }
            println("${currentPlayer.name} rolled doubles so they get to go again")
            goAgain = true
        } else if (numberOfDoubleRolls > 0) {
            numberOfDoubleRolls = 0
        }

        if (currentPlayer.position + totalDiceRoll > board.numberOfSpaces) {
            currentPlayer.position = (currentPlayer.position + totalDiceRoll) % board.numberOfSpaces
            currentPlayerMadeRevolution()
        } else {
            currentPlayer.position += totalDiceRoll
        }
    }

    /**
     * Evaluates the position that the current player has landed on and takes appropriate action.
     */
    fun evaluatePosition() {
        print("${currentPlayer.name} has landed on position ${currentPlayer.position} which is ")
        when (val currentBoardSpace = board.getBoardSpace(currentPlayer.position)) {
            is String -> {
                println("\"$currentBoardSpace\".")
                when (currentBoardSpace) {
                    "Start", "Vacation", "Break Time" -> println("This space has no effect")

                    "Go On Vacation" -> {
                        currentPlayer.sendToVacation()
                        // Following condition will be true if the player rolled doubles. The player will not get to
                        // go again for this case.
                        if (goAgain) {
                            goAgain = false
                        }
                    }

                    "Draw Action Card" -> {
                        println(
                                "${currentPlayer.name} draws a card from the action deck and it says " +
                                        "\"${actionDeck.topCard.message}\""
                        )
                        when (actionDeck.topCard.type) {
                            "money gain" -> {
                                val moneyGain = actionDeck.topCard.value
                                actionDeck.moveTopCardToBottom()
                                currentPlayer.money += moneyGain
                                printMoneyGainUpdate(player = currentPlayer, moneyGain = moneyGain)
                            }

                            "money loss" -> {
                                val moneyLoss = actionDeck.topCard.value
                                actionDeck.moveTopCardToBottom()
                                if (currentPlayer.money < moneyLoss) {
                                    askAboutGettingMoney(
                                            player = currentPlayer,
                                            moneyNeeded = moneyLoss,
                                            playerHasChoice = false
                                    )

                                    // Following condition is true if the player decided to drop out of the game
                                    // in the askAboutGettingMoney function.
                                    if (!currentPlayer.isInGame) {
                                        if (!gameOver) {
                                            println(
                                                    "${currentPlayer.name} has dropped out so their properties are " +
                                                            "now unowned"
                                            )
                                            board.makePropertiesUnowned(currentPlayer)
                                        }
                                        return
                                    }
                                }
                                currentPlayer.money -= moneyLoss
                                printMoneyLossUpdate(player = currentPlayer, moneyLoss = moneyLoss)
                            }

                            "other players to player" -> {
                                val moneyReceivedFromEachPlayer = actionDeck.topCard.value
                                actionDeck.moveTopCardToBottom()
                                for (otherPlayer in playerManager.getListOfOtherPlayersInGame(
                                        excludingPlayer = currentPlayer
                                )) {
                                    if (otherPlayer.money < moneyReceivedFromEachPlayer) {
                                        askAboutGettingMoney(
                                                player = otherPlayer,
                                                moneyNeeded = moneyReceivedFromEachPlayer,
                                                playerHasChoice = false
                                        )
                                        // Following condition is true if the other player decided to drop out in the
                                        // askAboutGettingMoney function
                                        if (!otherPlayer.isInGame) {
                                            if (gameOver) {
                                                return
                                            }
                                            println(
                                                    "${otherPlayer.name} has dropped out so their money and properties " +
                                                            "will go to ${currentPlayer.name}"
                                            )
                                            currentPlayer.money += otherPlayer.money
                                            printMoneyGainUpdate(player = currentPlayer, moneyGain = otherPlayer.money)
                                            board.transferOwnership(
                                                    currentOwner = otherPlayer,
                                                    newOwner = currentPlayer
                                            )
                                            continue
                                        }
                                    }
                                    otherPlayer.money -= moneyReceivedFromEachPlayer
                                    printMoneyLossUpdate(player = otherPlayer, moneyLoss = moneyReceivedFromEachPlayer)
                                    currentPlayer.money += moneyReceivedFromEachPlayer
                                    printMoneyGainUpdate(player = currentPlayer, moneyGain = moneyReceivedFromEachPlayer)
                                }
                            }

                            "player to other players" -> {
                                val moneyOwedToEachPlayer = actionDeck.topCard.value
                                actionDeck.moveTopCardToBottom()
                                val totalMoneyOwed =
                                        moneyOwedToEachPlayer * (playerManager.numberOfPlayersInGame - 1)
                                if (currentPlayer.money < totalMoneyOwed) {
                                    askAboutGettingMoney(
                                            player = currentPlayer,
                                            moneyNeeded = totalMoneyOwed,
                                            playerHasChoice = false
                                    )
                                    // Following condition is true if the current player decided to drop out in the
                                    // askAboutGettingMoney function
                                    if (!currentPlayer.isInGame) {
                                        if (!gameOver) {
                                            println(
                                                    "${currentPlayer.name} has dropped out so their properties are " +
                                                            "now unowned"
                                            )
                                            board.makePropertiesUnowned(currentPlayer)
                                        }
                                        return
                                    }
                                }

                                for (otherPlayer in playerManager.getListOfOtherPlayersInGame(
                                        excludingPlayer = currentPlayer
                                )) {
                                    currentPlayer.money -= moneyOwedToEachPlayer
                                    printMoneyLossUpdate(player = currentPlayer, moneyLoss = moneyOwedToEachPlayer)
                                    otherPlayer.money += moneyOwedToEachPlayer
                                    printMoneyGainUpdate(player = otherPlayer, moneyGain = moneyOwedToEachPlayer)
                                }
                            }

                            "absolute position change" -> {
                                val newPosition = actionDeck.topCard.value
                                actionDeck.moveTopCardToBottom()
                                if (newPosition < currentPlayer.position) {
                                    currentPlayerMadeRevolution()
                                }
                                currentPlayer.position = newPosition
                                evaluatePosition()
                            }

                            "relative position change" -> {
                                val positionChange = actionDeck.topCard.value
                                actionDeck.moveTopCardToBottom()
                                if (positionChange > 0) {
                                    if (currentPlayer.position + positionChange > board.numberOfSpaces) {
                                        currentPlayer.position =
                                                (currentPlayer.position + positionChange) % board.numberOfSpaces
                                        currentPlayerMadeRevolution()
                                    } else {
                                        currentPlayer.position += positionChange
                                    }
                                } else {
                                    if (currentPlayer.position + positionChange < 1) {
                                        currentPlayer.position =
                                                board.numberOfSpaces + (currentPlayer.position + positionChange)
                                    } else {
                                        // Move the player back since positionChange is negative.
                                        currentPlayer.position += positionChange
                                    }
                                }
                                evaluatePosition()
                            }

                            "get off vacation free" -> {
                                actionDeck.removeGetOffVacationCardAtTop()
                                currentPlayer.addGetOffVacationCard()
                            }

                            "go on vacation" -> {
                                actionDeck.moveTopCardToBottom()
                                currentPlayer.sendToVacation()
                                // Following condition will be true if doubles were rolled. The player will not get
                                // to go again for this case.
                                if (goAgain) {
                                    goAgain = false
                                }
                            }

                            "property maintenance" -> {
                                val feePerRestaurant = actionDeck.topCard.value
                                val restaurantCount = board.getRestaurantCount(currentPlayer)
                                actionDeck.moveTopCardToBottom()
                                if (restaurantCount == 0) {
                                    println(
                                            "${currentPlayer.name} doesn't have any restaurants so they don't have to " +
                                                    "pay anything"
                                    )
                                } else {
                                    val maintenanceFee = feePerRestaurant * restaurantCount
                                    println(
                                            "${currentPlayer.name} has $restaurantCount restaurants and must pay " +
                                                    "$$feePerRestaurant per restaurant so they owe $$maintenanceFee."
                                    )
                                    if (currentPlayer.money < maintenanceFee) {
                                        askAboutGettingMoney(
                                                player = currentPlayer,
                                                moneyNeeded = maintenanceFee,
                                                playerHasChoice = false
                                        )
                                        // The following condition is true if the player decided to drop out of
                                        // the game in the askAboutGettingMoney function.
                                        if (!currentPlayer.isInGame) {
                                            if (!gameOver) {
                                                println(
                                                        "${currentPlayer.name} has dropped out so their properties are " +
                                                                "now unowned"
                                                )
                                                board.makePropertiesUnowned(currentPlayer)
                                            }
                                            return
                                        }
                                    }
                                    currentPlayer.money -= maintenanceFee
                                    printMoneyLossUpdate(player = currentPlayer, moneyLoss = maintenanceFee)
                                }
                            }
                        }
                    }
                }
            }

            is Board.Property -> {
                print("${currentBoardSpace.name}, which is a ${currentBoardSpace.type.toLowerCase()}")
                if (currentBoardSpace is Board.Street) {
                    print(" in the ${currentBoardSpace.neighborhoodName} neighborhood")
                }
                if (currentBoardSpace.isOwned) {
                    val owner = currentBoardSpace.owner!!
                    print(" that is owned ")
                    if (owner == currentPlayer) {
                        println("by themself.")
                        return
                    }
                    println("by ${owner.name}.")
                    if (currentBoardSpace.isPawned) {
                        println("This property is pawned so ${currentPlayer.name} doesn't have to pay a fee.")
                        return
                    }
                    val fee: Int
                    when (currentBoardSpace) {
                        is Board.Street -> {
                            fee = currentBoardSpace.currentFee
                            println(
                                    "There " +
                                            when (currentBoardSpace.numberOfRestaurants) {
                                                0 -> "are no restaurants"
                                                1 -> "is a restaurant"
                                                in 2..5 -> "are ${currentBoardSpace.numberOfRestaurants} restaurants"
                                                else -> throw IllegalArgumentException(
                                                        "Invalid number of restaurants: ${currentBoardSpace.numberOfRestaurants}"
                                                )
                                            }
                                            + " on this street and the fee is $$fee."
                            )
                        }

                        is Board.GolfCourse -> {
                            val feeData = currentBoardSpace.feeData
                            val numberOfGolfCoursesOwned = feeData.getValue("number of golf courses owned")
                            fee = feeData.getValue("fee")
                            println(
                                    "${owner.name} owns $numberOfGolfCoursesOwned " +
                                            (if (numberOfGolfCoursesOwned == 1) "golf course" else "golf courses") +
                                            " and the fee is $$fee"
                            )
                        }

                        is Board.SuperStore -> {
                            val diceRoll1 = getDiceRoll()
                            val diceRoll2 = getDiceRoll()
                            val feeData = currentBoardSpace.getFeeData(totalDiceRoll = diceRoll1 + diceRoll2)
                            val bothSuperStoresOwnedBySamePerson =
                                    feeData.getValue("both super stores owned by same person") as Boolean
                            val multiplier = feeData.getValue("multiplier")
                            fee = feeData.getValue("fee") as Int

                            println(
                                    //TODO
                                    if (bothSuperStoresOwnedBySamePerson) {
                                        "${owner.name} owns both super stores so you have to roll the dice " +
                                                "and pay $multiplier times that amount."
                                    } else {
                                        "This is the only super store that ${owner.name} owns so you have " +
                                                "to roll the dice and pay $multiplier times that amount"
                                    }
                                            + "\nYou rolled a $diceRoll1 and a $diceRoll2 for a total of " +
                                            "${diceRoll1 + diceRoll2} so you have to pay $$fee"
                            )
                        }

                        else -> throw IllegalArgumentException(
                                "Board space is not a street, golf course, nor super store."
                        )
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
                            // Following condition is true if there are at least 2 players remaining after the current
                            // player decided to drop out.
                            if (!gameOver) {
                                // Give all money and properties to the owner of the property.
                                println(
                                        "${currentPlayer.name} has dropped out of the game so their money and " +
                                                "properties will go to ${owner.name}"
                                )
                                owner.money += currentPlayer.money
                                printMoneyGainUpdate(player = owner, moneyGain = currentPlayer.money)
                                board.transferOwnership(
                                        currentOwner = currentPlayer,
                                        newOwner = owner
                                )
                            }
                            return
                        }
                        println(
                                "${currentPlayer.name} has gathered the $$fee that they need to pay the " +
                                        "fee to ${owner.name}"
                        )
                    }
                    currentPlayer.money -= fee
                    printMoneyLossUpdate(currentPlayer, fee)
                    owner.money += fee
                    printMoneyGainUpdate(owner, fee)

                } else {
                    // This block of code is for when a player lands on an unowned property
                    print(
                            " that is unowned.\n${currentPlayer.name}, you have $${currentPlayer.money}. Would you " +
                                    "like to buy it for $${currentBoardSpace.purchasePrice}? (y/n) "
                    )
                    while (true) {
                        when (readLine()!!.toLowerCase()) {
                            "y" -> {
                                if (currentPlayer.money < currentBoardSpace.purchasePrice) {
                                    askAboutGettingMoney(
                                            player = currentPlayer,
                                            moneyNeeded = currentBoardSpace.purchasePrice,
                                            playerHasChoice = true
                                    )

                                    // The following condition will be true if the player originally decided
                                    // that they would buy the property but either couldn't gather the money
                                    // for it or changed their mind.
                                    if (currentPlayer.money < currentBoardSpace.purchasePrice) {
                                        println("${currentPlayer.name} is not buying this property")
                                        return
                                    }

                                    println(
                                            "${currentPlayer.name} has gathered the $${currentBoardSpace.purchasePrice} " +
                                                    "that they need to buy ${currentBoardSpace.name}"
                                    )
                                }

                                currentBoardSpace.owner = currentPlayer
                                currentPlayer.money -= currentBoardSpace.purchasePrice
                                printMoneyLossUpdate(currentPlayer, currentBoardSpace.purchasePrice)
                                return
                            }

                            "n" -> return

                            else -> print("\nInvalid input, must enter \"y\" or \"n\": ")
                        }
                    }
                }
            }
        }
    }

    /**
     * Asks the player that was passed in as an argument about which properties they would like to pawn.
     */
    // TODO displayMoneyGain and displayMoneyLoss
    fun askAboutPawningProperty(player: PlayerManager.Player) {
        val validPropertiesMap = board.getPawnablePropertyMap(player)
        for (property in validPropertiesMap.values) {
            println(property.pawnInfo)
        }
        while (true) {
            println("Enter the position of the property you would like to pawn or enter \"b\" to go back")
            val input = readLine()!!.toLowerCase()
            if (input == "b") {
                return
            }
            val property = validPropertiesMap[input]
            if (property == null) {
                println("Invalid input")
            } else {
                player.money += property.pawnPrice
                printMoneyGainUpdate(player = player, moneyGain = property.pawnPrice)
                property.pawn()
                return
            }
        }
    }

    /**
     * Only the current player is able to choose to unpawn their property so this function asks the current player
     * about which properties they would like to unpawn.
     */
    fun askAboutUnpawningProperty() {
        val validPropertiesMap = board.getUnpawnablePropertyMap(currentPlayer)
        for (property in validPropertiesMap.values) {
            println(property.unpawnInfo)
        }
        while (true) {
            println(
                    """
                ${currentPlayer.name}, you have $${currentPlayer.money}.
                Enter the position of the property you would like to unpawn or enter "b" to go back.
            """.trimIndent()
            )
            val input = readLine()!!.toLowerCase()
            if (input == "b") {
                return
            }
            val property = validPropertiesMap[input]
            if (property == null) {
                println("Invalid input")
            } else {
                if (currentPlayer.money < property.unpawnPrice) {
                    askAboutGettingMoney(
                            player = currentPlayer,
                            moneyNeeded = property.unpawnPrice,
                            playerHasChoice = true
                    )
                    // Following condition will be true if the player couldn't get enough money or changed their mind.
                    if (currentPlayer.money < property.unpawnPrice) {
                        return
                    }
                }
                currentPlayer.money -= property.unpawnPrice
                printMoneyLossUpdate(player = currentPlayer, moneyLoss = property.unpawnPrice)
                property.unpawn()
                return
            }
        }
    }

    /**
     * Only the current player is able to choose to add a restaurant to a street they own so this function asks the
     * current player about which street they would like to add a restaurant to.
     */
    fun askAboutAddingRestaurant() {
        val validStreetsMap = board.getStreetsWhereRestaurantCanBeAdded(currentPlayer)
        for (street in validStreetsMap.values) {
            println(street.restaurantAddInfo)
        }
        while (true) {
            println(
                    """
                ${currentPlayer.name}, you have $${currentPlayer.money}.
                Enter the position of the street you would like to add a restaurant to or enter "b" go back.
            """.trimIndent()
            )
            val input = readLine()!!.toLowerCase()
            if (input == "b") {
                return
            }
            val street = validStreetsMap[input]
            if (street == null) {
                println("Invalid input")
            } else {
                val restaurantAddingFee = street.restaurantAddPrice
                if (currentPlayer.money < restaurantAddingFee) {
                    askAboutGettingMoney(
                            player = currentPlayer,
                            moneyNeeded = restaurantAddingFee,
                            playerHasChoice = true
                    )

                    // Following condition will be true if the player couldn't get enough money or changed their mind.
                    if (currentPlayer.money < restaurantAddingFee) {
                        return
                    }
                    println(
                            "${currentPlayer.name} has gathered the $$restaurantAddingFee that they need to add a " +
                                    "restaurant to ${street.name}"
                    )
                }
                currentPlayer.money -= restaurantAddingFee
                printMoneyLossUpdate(player = currentPlayer, moneyLoss = restaurantAddingFee)
                street.addRestaurant()
                return
            }
        }
    }

    /**
     * Asks the player that was passed in as an argument about which streets they would like to remove a
     * restaurant from.
     */
    fun askAboutRemovingRestaurant(player: PlayerManager.Player) {
        val validStreetsMap = board.getStreetsWhereRestaurantCanBeRemoved(player)
        for (street in validStreetsMap.values) {
            println(street.restaurantRemoveInfo)
        }
        while (true) {
            println(
                    "Enter the position of the street you would like to remove a restaurant from or enter " +
                            "\"b\" to go back"
            )
            val input = readLine()!!.toLowerCase()
            if (input == "b") {
                return
            }
            val street = validStreetsMap[input]
            if (street == null) {
                println("Invalid input")
            } else {
                player.money += street.restaurantRemoveGain
                printMoneyGainUpdate(player = player, moneyGain = street.restaurantRemoveGain)
                street.removeRestaurant()
                return
            }
        }
    }

    /**
     * Asks a player what they want to do if they need money for the situation they're in and don't have it.
     *
     * @param player The player that needs money.
     * @param moneyNeeded The total amount of money that the player needs.
     * @param playerHasChoice Should be true if a player chooses to do something and they don't have enough money to
     * do that thing. For example, adding a restaurant to one of their streets. For this situation, this function can
     * be exited either by having the player either choosing to exit or by gathering all the money that is needed.
     * Should be false if the player is in a situation where they need money and they didn't choose to be in that
     * situation. For example, if the player lands on an owned property and doesn't currently have all the money they
     * need to pay the fee. For this situation, this function can be exited by having the player either gather the
     * money needed or drop out of the game.
     */
    fun askAboutGettingMoney(player: PlayerManager.Player, moneyNeeded: Int, playerHasChoice: Boolean) {
        var moneyGainOptions = board.getMoneyGainOptions(player)
        moneyGettingLoop@ while (true) {
            val playerCanPawn = moneyGainOptions.getValue("pawn")
            val playerCanRemoveRestaurant = moneyGainOptions.getValue("remove restaurant")
            println(
                    "${player.name}, you need $$moneyNeeded and you currently have $${player.money}. " +
                            "Select one of the following options to get more money."
            )
            if (playerHasChoice) {
                println("You don't need this money so you have the option of going back.")
            }
            println(
                    if (playerHasChoice) {
                        "1: Go back"
                    } else {
                        "1: Drop out of the game"
                    }
                            + "\n2: Make a trade with a player"
            )
            if (playerCanPawn) {
                println("3: Pawn one of your properties")
            }
            if (playerCanRemoveRestaurant) {
                println("4: Remove a restaurant from one of your properties")
            }
            when (readLine()) {
                "1" -> {
                    if (!playerHasChoice) {
                        player.removeFromGame()
                        if (playerManager.onePlayerIsInGame) {
                            gameOver = true
                        }
                    }
                    return
                }

                "2" -> askAboutTrading(
                        initiatingPlayer = player,
                        initiatingPlayerNeedsMoney = true
                )

                "3" -> {
                    if (playerCanPawn) {
                        askAboutPawningProperty(player)
                    } else {
                        println("Invalid input")
                        continue@moneyGettingLoop
                    }
                }

                "4" -> {
                    if (playerCanRemoveRestaurant) {
                        askAboutRemovingRestaurant(player)
                    } else {
                        println("Invalid input")
                        continue@moneyGettingLoop
                    }
                }

                else -> {
                    println("Invalid input")
                    continue@moneyGettingLoop
                }
            }

            // If this part is reached, then the player must have done something to get more money. Check if they
            // have enough.
            if (player.money >= moneyNeeded) {
                return
            }

            // Before another iteration, refresh the money gain options since they could've changed in
            // one of the functions.
            moneyGainOptions = board.getMoneyGainOptions(player)
        }
    }

    /**
     * This function could be used to allow any player to try to make a trade.
     * @param initiatingPlayerNeedsMoney If this is true, the initiating player will need to select a money amount
     * they want from the player they want to make a trade with and they won't be able to offer money themselves.
     */
    fun askAboutTrading(initiatingPlayer: PlayerManager.Player, initiatingPlayerNeedsMoney: Boolean) {
        /**
         * When a player wants to make a trade, they will have to select what they want to offer and what they want to
         * receive. These can include money, properties, and Get Off Vacation Free cards. This class is used as a
         * data structure for those.
         */
        class TradeData {
            var money = 0
                set(value) {
                    if (value < 0) {
                        throw IllegalArgumentException("Trade data money cannot be negative")
                    }
                    field = value
                }

            val properties = mutableListOf<Board.Property>()

            /**
             * properties list should not contain duplicates so this function prevents that.
             */
            fun addPropertyIfAbsent(property: Board.Property) {
                if (property !in properties) {
                    properties.add(property)
                }
            }

            var getOffVacationFreeCards = 0
                set(value) {
                    if (value < 0) {
                        throw Exception("Trade data amount of Get Off Vacation Free cards cannot be negative")
                    }
                    field = value
                }

            /**
             * Is true when the amount of money, properties, and amount of get off vacation free cards are the same
             * as when this object was instantiated.
             */
            val nothingHasBeenChanged get() = money == 0 && properties.isEmpty() && getOffVacationFreeCards == 0
        }

        val otherPlayersList = playerManager.getListOfOtherPlayersInGame(excludingPlayer = initiatingPlayer)

        mainTradingLoop@ while (true) {
            println("${initiatingPlayer.name}, you are at the beginning of the trade process")
            val otherPlayer: PlayerManager.Player
            tradeBeginningLoop@ while (true) {
                println(
                        """
                    Enter one of the following and press enter:
                    pr: See property info
                    pl: See player info
                    c: Cancel the trade
                """.trimIndent()
                )
                for ((index, player) in otherPlayersList.withIndex()) {
                    println("${index + 1}: Initiate a trade with ${player.name}")
                }
                when (val input = readLine()!!.toLowerCase()) {
                    "pr" -> askAboutDisplayingPropertyInfo(playerWhoWantsToKnow = initiatingPlayer)
                    "pl" -> println(playerManager)
                    "c" -> return
                    else -> {
                        // Check if a valid number was entered. If so, set otherPlayer to the correct player.
                        val inputNumber = input.toIntOrNull()
                        if (inputNumber == null || inputNumber !in 1..otherPlayersList.size) {
                            println("Invalid input")
                        } else {
                            otherPlayer = otherPlayersList[inputNumber - 1]
                            break@tradeBeginningLoop
                        }
                    }
                }
            }

            val initiatingPlayerHasAProperty = board.playerHasAProperty(initiatingPlayer)
            val otherPlayerHasAProperty = board.playerHasAProperty(otherPlayer)
            // Prevent the player from continuing if there is nothing that can be traded besides money.
            if (!initiatingPlayerHasAProperty && !initiatingPlayer.hasAGetOffVacationCard &&
                    !otherPlayerHasAProperty && !otherPlayer.hasAGetOffVacationCard
            ) {
                println(
                        "There is nothing that ${initiatingPlayer.name} and ${otherPlayer.name} can trade with each other " +
                                "besides money. The trade process will be restarted."
                )
                continue@mainTradingLoop
            }

            val whatPlayerWants = TradeData()

            selectWhatIsWanted@ while (true) {
                moneyAskLoop1@ while (true) {
                    // Only ask the initiating player the following question if they don't need money, because if they
                    // do need money then it's an automatic yes.
                    if (!initiatingPlayerNeedsMoney) {
                        print("${initiatingPlayer.name}, would you like any money from ${otherPlayer.name}? (y/n): ")
                        val input = readLine()!!.toLowerCase()
                        if (input == "n") {
                            break@moneyAskLoop1
                        } else if (input != "y") {
                            println("Invalid input")
                            continue@moneyAskLoop1
                        }
                    }

                    moneyAskLoop2@ while (true) {
                        print("This person has $${otherPlayer.money}. Enter an amount")
                        if (initiatingPlayerNeedsMoney) {
                            println(". You need money so entering an amount is mandatory")
                        } else {
                            println(" or enter \"b\" to go back")
                        }
                        val input = readLine()!!.toLowerCase()
                        if (input == "b" && !initiatingPlayerNeedsMoney) {
                            continue@moneyAskLoop1
                        }
                        val moneyAmount = input.toIntOrNull()
                        if (moneyAmount == null || moneyAmount !in 0..otherPlayer.money ||
                                (initiatingPlayerNeedsMoney && moneyAmount == 0)
                        ) {
                            println("Invalid input")
                        } else {
                            whatPlayerWants.money = moneyAmount
                            break@moneyAskLoop1
                        }
                    }
                }

                if (otherPlayerHasAProperty) {
                    propertyAskLoop1@ while (true) {
                        print("${initiatingPlayer.name}, would you like any properties from ${otherPlayer.name}? (y/n): ")
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                val otherPlayerPropertyMap = board.getPropertyMap(otherPlayer)
                                println("Properties owned by ${otherPlayer.name}:")
                                for (property in otherPlayerPropertyMap.values) {
                                    println(property.moderateDetailInfo)
                                }
                                propertyAskLoop2@ while (true) {
                                    println(
                                            "Enter the positions of all the properties you would like with each position " +
                                                    "separated by a single space or enter \"b\" to go back:"
                                    )
                                    input = readLine()!!.toLowerCase().trim()
                                    if (input == "b") {
                                        continue@propertyAskLoop1
                                    }
                                    val positionsEntered = input.split(" ")
                                    for (position in positionsEntered) {
                                        val property = otherPlayerPropertyMap[position]
                                        if (property == null) {
                                            // This will be true if there was no such key in otherPlayerPropertyMap
                                            println("Invalid input")
                                            // Clear the properties list since properties may have been added to it
                                            // from previous iterations of this for loop.
                                            whatPlayerWants.properties.clear()
                                            continue@propertyAskLoop2
                                        }
                                        whatPlayerWants.addPropertyIfAbsent(property)
                                    }
                                    break@propertyAskLoop1
                                }
                            }

                            "n" -> break@propertyAskLoop1

                            else -> println("Invalid input")
                        }
                    }
                }

                if (otherPlayer.hasAGetOffVacationCard) {
                    getOffVacationFreeCardsAskLoop1@ while (true) {
                        print(
                                "${initiatingPlayer.name}, would you like any get off vacation cards from " +
                                        "${otherPlayer.name}? (y/n): "
                        )
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                getOffVacationFreeCardsAskLoop2@ while (true) {
                                    println(
                                            "${otherPlayer.name} has ${otherPlayer.numberOfGetOffVacationCardsOwned} get " +
                                                    "off vacation cards. How many would you like? Enter an amount or " +
                                                    "enter \"b\" to go back:"
                                    )
                                    input = readLine()!!
                                    if (input == "b") {
                                        continue@getOffVacationFreeCardsAskLoop1
                                    }
                                    val amount = input.toIntOrNull()
                                    if (amount == null || amount !in 0..otherPlayer.numberOfGetOffVacationCardsOwned) {
                                        println("Invalid input")
                                    } else {
                                        whatPlayerWants.getOffVacationFreeCards = amount
                                        break@getOffVacationFreeCardsAskLoop1
                                    }
                                }
                            }

                            "n" -> break@getOffVacationFreeCardsAskLoop1

                            else -> println("Invalid input")
                        }
                    }
                }

                // Following condition will be true if the initiating player didn't select anything for what they want.
                if (whatPlayerWants.nothingHasBeenChanged) {
                    while (true) {
                        println(
                                """
                                
                            ${initiatingPlayer.name}, you didn't select anything for what you want
                            Type one of the following and press enter
                            ta: Try again
                            c: Cancel the trade
                        """.trimIndent()
                        )
                        when (readLine()!!.toLowerCase()) {
                            "ta" -> continue@selectWhatIsWanted
                            "c" -> return
                            else -> println("Invalid input")
                        }
                    }
                } else {
                    break@selectWhatIsWanted
                }
            }

            // Part for having the initiating player select what they offer
            val whatPlayerOffers = TradeData()
            selectWhatToOffer@ while (true) {
                // Only give the initiating player the choice of offering money if they are not in need of money
                // and they chose that they didn't want any money.
                if (!initiatingPlayerNeedsMoney && whatPlayerWants.money == 0) {
                    moneyAskLoop1@ while (true) {
                        print("${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} any money? (y/n): ")
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                moneyAskLoop2@ while (true) {
                                    println(
                                            "${initiatingPlayer.name}, you have $${initiatingPlayer.money}, how much " +
                                                    "of it would you like to offer? Enter an amount or enter \"b\" " +
                                                    "to go back"
                                    )
                                    input = readLine()!!
                                    if (input == "b") {
                                        continue@moneyAskLoop1
                                    }
                                    val moneyAmount = input.toIntOrNull()
                                    if (moneyAmount == null || moneyAmount !in 0..initiatingPlayer.money) {
                                        println("Invalid input")
                                    } else {
                                        whatPlayerOffers.money = moneyAmount
                                        break@moneyAskLoop1
                                    }
                                }
                            }

                            "n" -> break@moneyAskLoop1

                            else -> println("Invalid input")
                        }
                    }
                }

                if (initiatingPlayerHasAProperty) {
                    propertyAskLoop1@ while (true) {
                        print(
                                "${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} " +
                                        "any properties? (y/n): "
                        )
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                val initiatingPlayerPropertyMap = board.getPropertyMap(initiatingPlayer)
                                println("Properties owned by ${initiatingPlayer.name}:")
                                for (property in initiatingPlayerPropertyMap.values) {
                                    println(property.moderateDetailInfo)
                                }
                                propertyAskLoop2@ while (true) {
                                    println(
                                            "Enter the positions of the properties you would like to offer with each " +
                                                    "position separated by a single space or enter \"b\" to go back: "
                                    )
                                    input = readLine()!!.toLowerCase().trim()
                                    if (input == "b") {
                                        continue@propertyAskLoop1
                                    }
                                    val positionsEntered = input.split(" ")
                                    for (position in positionsEntered) {
                                        val property = initiatingPlayerPropertyMap[position]
                                        if (property == null) {
                                            // This will be true if there was no such key in initiatingPlayerPropertyMap
                                            println("Invalid input")
                                            // Clear the properties list since properties could have been added to it
                                            // from previous iterations of this for loop.
                                            whatPlayerOffers.properties.clear()
                                            continue@propertyAskLoop2
                                        }
                                        whatPlayerOffers.addPropertyIfAbsent(property)
                                    }
                                    break@propertyAskLoop1
                                }
                            }

                            "n" -> break@propertyAskLoop1

                            else -> println("Invalid input")
                        }
                    }
                }

                // Only ask the initiating player if they would like to offer any get off vacation free cards
                // if they have one and they chose that they didn't want any.
                if (initiatingPlayer.hasAGetOffVacationCard && whatPlayerWants.getOffVacationFreeCards == 0) {
                    getOffVacationFreeCardsAskLoop1@ while (true) {
                        print(
                                "${initiatingPlayer.name}, would you like to offer ${otherPlayer.name} any Get " +
                                        "Off Vacation Free cards? (y/n): "
                        )
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                getOffVacationFreeCardsAskLoop2@ while (true) {
                                    println(
                                            "${initiatingPlayer.name}, you have " +
                                                    initiatingPlayer.numberOfGetOffVacationCardsOwned +
                                                    " Get Off Vacation Free cards. How many would you like to offer? " +
                                                    "Enter an amount or \"b\" to go back"
                                    )
                                    input = readLine()!!.toLowerCase()
                                    if (input == "b") {
                                        continue@getOffVacationFreeCardsAskLoop1
                                    }
                                    val amount = input.toIntOrNull()
                                    if (amount == null || amount !in
                                            0..initiatingPlayer.numberOfGetOffVacationCardsOwned
                                    ) {
                                        println("Invalid input")
                                    } else {
                                        whatPlayerOffers.getOffVacationFreeCards = amount
                                        break@getOffVacationFreeCardsAskLoop1
                                    }
                                }
                            }

                            "n" -> break@getOffVacationFreeCardsAskLoop1

                            else -> println("Invalid input")
                        }
                    }
                }

                // Following condition will be true if the initiating player didn't offer anything
                if (whatPlayerOffers.nothingHasBeenChanged) {
                    while (true) {
                        println(
                                """
                                
                                ${initiatingPlayer.name}, you didn't offer anything.
                                Type one of the following and press enter
                                ta: Try again
                                c: Cancel the trade
                                """.trimIndent()
                        )
                        when (readLine()!!.toLowerCase()) {
                            "ta" -> continue@selectWhatToOffer
                            "c" -> return
                            else -> println("Invalid input")
                        }
                    }
                } else {
                    break@selectWhatToOffer
                }
            }

            // Section for initiating player to confirm their offer
            confirmTrade@ while (true) {
                println("\n${initiatingPlayer.name}, you have chosen to offer ${otherPlayer.name}:")
                if (whatPlayerOffers.money > 0) {
                    println("$${whatPlayerOffers.money}")
                }
                if (whatPlayerOffers.properties.isNotEmpty()) {
                    println("The following properties:")
                    for (property in whatPlayerOffers.properties) {
                        println(property.moderateDetailInfo)
                        if (property is Board.Street && property.neighborhoodIsOwnedBySinglePlayer) {
                            println(
                                    "Note: the above property is a street that is in a neighborhood that is currently " +
                                            "owned by ${initiatingPlayer.name}.\nIf they agree to the trade, that " +
                                            "neighborhood will no longer be owned by only them so all restaurants will " +
                                            "be sold and the fees will go back to the starting fees."
                            )
                        }
                    }
                }

                if (whatPlayerOffers.getOffVacationFreeCards > 0) {
                    println(
                            if (whatPlayerOffers.getOffVacationFreeCards == 1) {
                                "A Get Off Vacation Free card"
                            } else {
                                "${whatPlayerOffers.getOffVacationFreeCards} Get Off Vacation Free cards"
                            }
                    )
                }

                println("And you have chosen that you want:")

                if (whatPlayerWants.money > 0) {
                    println("$${whatPlayerWants.money}")
                }

                if (whatPlayerWants.properties.isNotEmpty()) {
                    println("The following properties:")
                    for (property in whatPlayerWants.properties) {
                        println(property.moderateDetailInfo)
                        if (property is Board.Street && property.neighborhoodIsOwnedBySinglePlayer) {
                            println(
                                    "Note: the above property is a street that is in a neighborhood that is currently " +
                                            "owned by ${otherPlayer.name}.\nIf they agree to the trade, that neighborhood " +
                                            "will no longer be owned by only them so all houses will be sold and the fees " +
                                            "will go back to the starting fees."
                            )
                        }
                    }
                }

                if (whatPlayerWants.getOffVacationFreeCards > 0) {
                    println(
                            if (whatPlayerWants.getOffVacationFreeCards == 1) {
                                "A Get Off Vacation Free card"
                            } else {
                                "${whatPlayerWants.getOffVacationFreeCards} Get Off Vacation Free cards"
                            }
                    )
                }
                println(
                        """
                        
                    ${initiatingPlayer.name}, are you sure you want to make this offer to ${otherPlayer.name}?
                    Type "y", "n", or one of the following and press enter
                    pr: See property info
                    pl: See player info
                """.trimIndent()
                )
                when (readLine()!!.toLowerCase()) {
                    "y" -> break@confirmTrade
                    "n" -> {
                        while (true) {
                            println(
                                    """
                                ${initiatingPlayer.name}, type one of the following and press enter
                                1: Go to the start of the trade process
                                2: Exit the trade process
                            """.trimIndent()
                            )
                            when (readLine()) {
                                "1" -> continue@mainTradingLoop
                                "2" -> return
                                else -> println("Invalid input")
                            }
                        }
                    }
                    "pr" -> askAboutDisplayingPropertyInfo(playerWhoWantsToKnow = initiatingPlayer)
                    "pl" -> println(playerManager)
                    else -> println("Invalid input")
                }
            }

            acceptOrDenyTrade@ while (true) {
                println(
                        """
                        ${otherPlayer.name}, do you accept the offer that was just confirmed by ${initiatingPlayer.name}?
                        Type "y", "n", or one of the following and press enter
                        pr: See property info
                        pl: See player info
                    """.trimIndent()
                )
                when (readLine()!!.toLowerCase()) {
                    "n" -> {
                        println("${otherPlayer.name} denies the trade offer")
                        return
                    }

                    "pr" -> askAboutDisplayingPropertyInfo(playerWhoWantsToKnow = otherPlayer)

                    "pl" -> println(playerManager)

                    "y" -> {
                        // Give the other player what was agreed upon
                        if (whatPlayerOffers.money > 0) {
                            initiatingPlayer.money -= whatPlayerOffers.money
                            printMoneyLossUpdate(player = initiatingPlayer, moneyLoss = whatPlayerOffers.money)
                            otherPlayer.money += whatPlayerOffers.money
                            printMoneyGainUpdate(player = otherPlayer, moneyGain = whatPlayerOffers.money)
                        }

                        for (property in whatPlayerOffers.properties) {
                            if (property is Board.Street && property.neighborhoodIsOwnedBySinglePlayer) {
                                val numberOfNeighborhoodRestaurants = property.neighborhoodRestaurantCount
                                if (numberOfNeighborhoodRestaurants > 0) {
                                    println(
                                            "There are $numberOfNeighborhoodRestaurants in the " +
                                                    "${property.neighborhoodName} neighborhood so these will be sold"
                                    )
                                    // Sell all restaurants in the neighborhood
                                    property.removeRestaurantsFromNeighborhood()
                                    val moneyEarned =
                                            numberOfNeighborhoodRestaurants * property.restaurantRemoveGain
                                    initiatingPlayer.money += moneyEarned
                                }
                            }
                            property.owner = otherPlayer
                        }

                        initiatingPlayer.numberOfGetOffVacationCardsOwned -= whatPlayerOffers.getOffVacationFreeCards
                        otherPlayer.numberOfGetOffVacationCardsOwned += whatPlayerOffers.getOffVacationFreeCards

                        // Now give the initiating player what was agreed upon.
                        if (whatPlayerWants.money > 0) {
                            otherPlayer.money -= whatPlayerWants.money
                            printMoneyLossUpdate(player = otherPlayer, moneyLoss = whatPlayerWants.money)
                            initiatingPlayer.money += whatPlayerWants.money
                            printMoneyGainUpdate(player = initiatingPlayer, moneyGain = whatPlayerWants.money)
                        }

                        for (property in whatPlayerWants.properties) {
                            if (property is Board.Street && property.neighborhoodIsOwnedBySinglePlayer) {
                                val numberOfNeighborhoodRestaurants = property.neighborhoodRestaurantCount
                                if (numberOfNeighborhoodRestaurants > 0) {
                                    println(
                                            "There are $numberOfNeighborhoodRestaurants restaurants in the " +
                                                    "${property.neighborhoodName} neighborhood so these will be sold"
                                    )
                                    // Sell all restaurants in the neighborhood
                                    property.removeRestaurantsFromNeighborhood()
                                    val moneyEarned = numberOfNeighborhoodRestaurants * property.restaurantRemoveGain
                                    otherPlayer.money += moneyEarned
                                }
                            }
                            property.owner = initiatingPlayer
                        }

                        otherPlayer.numberOfGetOffVacationCardsOwned -= whatPlayerWants.getOffVacationFreeCards
                        initiatingPlayer.numberOfGetOffVacationCardsOwned += whatPlayerWants.getOffVacationFreeCards
                        return
                    }

                    else -> println("Invalid input")
                }
            }
        }
    }
}