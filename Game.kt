package commandline

import kotlin.random.Random

/**
 * The Game class consists of all the objects of a game and all the methods that have those objects interact with
 * each other. All that the main file needs to begin a game is a simple instantiation which doesn't even need to
 * be stored to a variable or have any methods called.
 */
class Game {
    val playerManager = PlayerManager()
    val board = Board()
    val actionDeck = ActionDeck()

    var currentPlayer = playerManager.currentPlayer
    var gameOver = false
    var turnOver = false
    var goAgain = false
    var numberOfDoubleRolls = 0

    init {
        setup()
        play()
    }

    fun setup() {
        playerManager.vacationPosition = board.getVacationPosition()

        val propertiesUsedByActionDeck = arrayOf("Knuth Street", "Lagos Avenue")
        val propertyPositions = board.getPropertyPositions(propertiesUsedByActionDeck)
        actionDeck.setPropertyPositions(propertyPositions)
        actionDeck.shuffle()
    }

    fun play() {

        // Testing area
//        val player2 = playerManager.getPlayerCopy(2)
//        board.setPropertyOwner(2, player2.number, player2.name)
//        board.checkForNeighborhoodChanges(2)
//        board.setPropertyOwner(3, player2.number, player2.name)
//        board.checkForNeighborhoodChanges(3)
//        board.setPropertyOwner(4, player2.number, player2.name)
//        board.checkForNeighborhoodChanges(4)


        gameLoop@ while (true) {
            println("It's ${currentPlayer.name}'s turn")

            askAboutPreRollAction()

            // Following condition might be true depending on actions in askAboutPreRollAction().
            if (gameOver) {
                break@gameLoop
            }

            if (currentPlayer.isOnVacation && !turnOver) {
                doVacationAction()
            }

            if (!turnOver) rollDiceAndMove()
            if (!turnOver) {
                evaluatePosition()
            }

            // Following condition might be true depending on actions in evaluatePosition().
            if (gameOver) {
                break@gameLoop
            }

            endTurn()
        }

        // Following condition won't be true if the game was ended by choice.
        if (playerManager.onePlayerIsInGame) {
            println("The winner is ${playerManager.getWinnerName()}!\n")
        }
    }

    fun generateDiceRoll() = Random.nextInt(1, 7)

    fun endTurn() {
        if (turnOver) {
            turnOver = false
        }
        if (goAgain) {
            goAgain = false
        } else {
//            playerManager.updatePlayer(currentPlayer)
            playerManager.switchToNextPlayer()
            currentPlayer = playerManager.currentPlayer
        }
        println("End of turn. Press enter to continue.")
        readLine()
    }

    fun askAboutPreRollAction() {
        var possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number)
        preRollAction@ while (true) {
            val playerCanAddRestaurant = possiblePropertyActions.getValue("add restaurant")
            val playerCanRemoveRestaurant = possiblePropertyActions.getValue("remove restaurant")
            val playerCanPawn = possiblePropertyActions.getValue("pawn")
            val playerCanUnpawn = possiblePropertyActions.getValue("unpawn")

            println(
                """
                    ${currentPlayer.name}, type one of the following and press enter:
                    
                    tt: Take your turn
                    s: View the board spaces
                    i: View property info
                    p: View player info
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

            when (readLine()!!.toLowerCase()) {
                "tt" -> return

                "s" -> board.displaySpacesAndSimplePropertyInfo()

                "i" -> {
                    propertyInfoLoop@ while (true) {
                        println(
                            """
                            Enter one of the following and press enter
                        
                            mp: View moderately detailed info for all properties
                            ms: View moderately detailed street info
                            hs: View highly detailed street info
                            gc: View moderately detailed golf course info
                            ss: View moderately detailed super store info
                            b: Go back
                        """.trimIndent()
                        )

                        when (readLine()!!.toLowerCase()) {
                            "mp" -> board.displayPropertyInfo()
                            "ms" -> board.displayModeratelyDetailedStreetInfo()
                            "hs" -> board.displayHighlyDetailedStreetInfo()
                            "gc" -> board.displayGolfCourseInfo()
                            "ss" -> board.displaySuperStoreInfo()
                            "b" -> continue@preRollAction
                            else -> println("Invalid input")
                        }
                    }
                }

                "p" -> playerManager.displayPlayerInfo()

                "mt" -> {
                    askAboutTrading(
                        initiatingPlayerNumber = currentPlayer.number,
                        initiatingPlayerNeedsMoney = false
                    )

                    // Need to refresh the possible property actions.
                    possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number)
                }

                "do" -> {
//                    println(playerManager.numberOfPlayersInGame)
                    currentPlayer.removeFromGame()
//                    println(playerManager.numberOfPlayersInGame)
                    if (playerManager.onePlayerIsInGame) {
                        gameOver = true

                        // currentPlayer normally gets updated in the endTurn function but for this situation,
                        // we'll update it here since we won't be going to the endTurn function.
//                        playerManager.updatePlayer(currentPlayer)
                    } else {
                        turnOver = true
                        board.makePropertiesUnowned(currentPlayer.number)
                    }
                    return
                }

                "eg" -> {
                    inputValidation@ while (true) {
                        print("Are you sure that you want to end the game? (y/n) ")
                        when (readLine()!!.toLowerCase()) {
                            "y" -> {
                                gameOver = true
                                return
                            }

                            "n" -> continue@preRollAction

                            else -> println("Invalid input")
                        }
                    }

                }

                "ar" -> {
                    if (playerCanAddRestaurant) {
                        askAboutAddingRestaurant()
                        // Need to refresh the possible property actions due to possible changes in the
                        // above function.
                        possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number)
                    } else {
                        println("Invalid input")
                    }
                }

                "rr" -> {
                    if (playerCanRemoveRestaurant) {
                        askAboutRemovingRestaurant(currentPlayer.number)

                        // Need to refresh the possible property actions due to possible changes in the
                        // above function.
                        possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number)
                    } else {
                        println("Invalid input")
                    }
                }

                "pa" -> {
                    if (playerCanPawn) {
                        askAboutPawningProperty(currentPlayer.number)

                        // Need to refresh the possible property actions due to possible changes in the
                        // above function.
                        possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number)
                    } else {
                        println("Invalid input")
                    }
                }

                "u" -> {
                    if (playerCanUnpawn) {
                        askAboutUnpawningProperty()

                        // Need to refresh the possible property actions due to possible changes in the
                        // above function.
                        possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number)
                    } else {
                        println("Invalid input")
                    }
                }

                else -> println("Invalid input")
            }
        }
    }

    fun doVacationAction() {
        println(
            "${currentPlayer.name}, you're on vacation and this is your " +
                    when (currentPlayer.numberOfTurnsOnVacation) {
                        0 -> "first"
                        1 -> "second"
                        2 -> "third (last)"
                        else -> throw IllegalStateException("Shouldn't be on vacation at this point")
                    }
                    + " turn on vacation"
        )

        val feeToGetOffVacation = 50
        inputValidation@ while (true) {
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
                            playerNumber = currentPlayer.number,
                            moneyNeeded = feeToGetOffVacation,
                            playerNeedsMoney = false
                        )

                        // The following condition will be true if the player couldn't gather the money or
                        // changed their mind.
                        if (currentPlayer.money < feeToGetOffVacation) {
                            continue@inputValidation
                        }
                    }
                    println("${currentPlayer.name} has chosen to pay so they will get to take their turn")
                    currentPlayer.money -= feeToGetOffVacation
                    currentPlayer.removeFromVacation()
                    return
                }

                "2" -> {
                    val diceRoll1 = generateDiceRoll()
                    val diceRoll2 = generateDiceRoll()
                    println("${currentPlayer.name} rolled a $diceRoll1 and a $diceRoll2")
                    if (diceRoll1 == diceRoll2) {
                        println("These are doubles so they are off vacation")
                        currentPlayer.removeFromVacation()
                        currentPlayer.position += (diceRoll1 + diceRoll2)
                    } else {
                        currentPlayer.continueVacation()
                        turnOver = true
                    }
                    return
                }

                "3" -> {
                    if (currentPlayer.hasAGetOffVacationCard) {
                        println(
                            "${currentPlayer.name} has chosen to use a get off vacation card so they " +
                                    "get to take their turn"
                        )
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

    fun rollDiceAndMove() {
        val diceRoll1 = generateDiceRoll()
        val diceRoll2 = generateDiceRoll()
        println("${currentPlayer.name} rolled a $diceRoll1 and a $diceRoll2")
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
            // Reset numberOfDoubleRolls
            numberOfDoubleRolls = 0
        }

        if (currentPlayer.position + diceRoll1 + diceRoll2 > board.numberOfSpaces) {
            currentPlayer.position = (currentPlayer.position + diceRoll1 + diceRoll2) % board.numberOfSpaces
            currentPlayerHasMadeRevolution()
        } else {
            currentPlayer.position += (diceRoll1 + diceRoll2)
        }
    }

    fun currentPlayerHasMadeRevolution() {
        println("${currentPlayer.name} has made a revolution")
        currentPlayer.money += 512
    }

    fun evaluatePosition() {
        print("${currentPlayer.name} has landed on position ${currentPlayer.position} which is ")
        when (val currentBoardSpace = board.getBoardSpace(currentPlayer.position)) {
            is String -> {
                println("\"$currentBoardSpace\".")
                when (currentBoardSpace) {
                    "Start", "Vacation", "Break Time" -> {
                        println("This space has no effect")
                    }

                    "Go On Vacation" -> currentPlayer.sendToVacation()

                    "Draw Entropy Card" -> {
                        println("You draw a card from the entropy deck and it says \"${actionDeck.topCard.message}\"")
                        when (actionDeck.topCard.type) {
                            "money gain" -> {
                                val moneyOwedToPlayer = actionDeck.topCard.value
                                actionDeck.moveTopCardToBottom()
                                currentPlayer.money += moneyOwedToPlayer
                            }

                            "money loss" -> {
                                val moneyOwedToBank = actionDeck.topCard.value
                                actionDeck.moveTopCardToBottom()
                                if (currentPlayer.money < moneyOwedToBank) {
                                    askAboutGettingMoney(
                                        playerNumber = currentPlayer.number,
                                        moneyNeeded = moneyOwedToBank,
                                        playerNeedsMoney = true
                                    )

                                    // Following condition is true if the player decided to drop out of the game
                                    // in the playerNeedsMoney function.
                                    if (!currentPlayer.isInGame) {
                                        if (!gameOver) {
                                            board.makePropertiesUnowned(currentPlayer.number)
                                        }
                                        return
                                    }
                                }
                                currentPlayer.money -= moneyOwedToBank
                            }

                            "other players to player" -> {
                                val moneyReceivedFromEachPlayer = actionDeck.topCard.value
                                actionDeck.moveTopCardToBottom()
                                for (otherPlayerNumber in playerManager.getNumbersOfOtherPlayersInGame(
                                    excludingPlayerNumber = currentPlayer.number
                                )) {
                                    val otherPlayer = playerManager.getPlayer(otherPlayerNumber)

                                    if (otherPlayer.money < moneyReceivedFromEachPlayer) {
                                        askAboutGettingMoney(
                                            playerNumber = otherPlayerNumber,
                                            moneyNeeded = moneyReceivedFromEachPlayer,
                                            playerNeedsMoney = true
                                        )
                                        if (!otherPlayer.isInGame) {
                                            if (gameOver) {
                                                return
                                            }
                                            board.transferOwnership(
                                                currentOwnerNumber = otherPlayerNumber,
                                                newPlayerNumber = currentPlayer.number,
                                                newPlayerName = currentPlayer.name
                                            )
                                            continue
                                        }
                                    }

                                    otherPlayer.money -= moneyReceivedFromEachPlayer
                                    currentPlayer.money += moneyReceivedFromEachPlayer
                                }
                            }

                            "player to other players" -> {
                                val moneyOwedToEachPlayer = actionDeck.topCard.value
                                actionDeck.moveTopCardToBottom()
                                val totalMoneyOwed =
                                    moneyOwedToEachPlayer * (playerManager.numberOfPlayersInGame - 1)

                                if (currentPlayer.money < totalMoneyOwed) {
                                    askAboutGettingMoney(
                                        playerNumber = currentPlayer.number,
                                        moneyNeeded = totalMoneyOwed,
                                        playerNeedsMoney = true
                                    )

                                    if (!currentPlayer.isInGame) {
                                        if (!gameOver) {
                                            board.makePropertiesUnowned(currentPlayer.number)
                                        }
                                        return
                                    }
                                }

                                for (otherPlayerNumber in playerManager.getNumbersOfOtherPlayersInGame(
                                    excludingPlayerNumber = currentPlayer.number
                                )) {
                                    val otherPlayer = playerManager.getPlayer(otherPlayerNumber)
                                    currentPlayer.money -= moneyOwedToEachPlayer
                                    otherPlayer.money += moneyOwedToEachPlayer
                                    playerManager.updatePlayer(otherPlayer)
                                }
                            }

                            "absolute position change" -> {
                                val newPosition = actionDeck.topCard.value
                                actionDeck.moveTopCardToBottom()
                                if (newPosition < currentPlayer.position) {
                                    currentPlayerHasMadeRevolution()
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
                                        currentPlayerHasMadeRevolution()
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

                            "property maintenance" -> {
                                val restaurantCount = board.getRestaurantCount(currentPlayer.number)
                                val feePerRestaurant = actionDeck.topCard.value
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
                                            playerNumber = currentPlayer.number,
                                            moneyNeeded = maintenanceFee,
                                            playerNeedsMoney = true
                                        )

                                        // The following condition should be true if the player decided to drop out of
                                        // the game in the playerNeedsMoney function.
                                        if (!currentPlayer.isInGame) {
                                            if (!gameOver) {
                                                board.makePropertiesUnowned(currentPlayer.number)
                                            }
                                            // TODO top card doesn't get moved to bottom
                                            return
                                        }
                                    }

                                    currentPlayer.money -= maintenanceFee
                                }
                            }
                        }
                    }
                }
            }

            is Board.Property -> {
                print("${currentBoardSpace.name}, which is a ${currentBoardSpace.typeStringLowerCase}")
                if (currentBoardSpace is Board.Street) {
                    print(" in the ${currentBoardSpace.neighborhoodName}")
                }
                if (currentBoardSpace.isOwned) {
                    print(" that is owned ")
                    if (currentBoardSpace.ownerNumber == currentPlayer.number) {
                        println("by themself")
                        return
                    }
                    val ownerName = currentBoardSpace.ownerName
                    println("by $ownerName.")
                    if (currentBoardSpace.isPawned) {
                        println("This property is pawned so ${currentPlayer.name} doesn't have to pay a fee.")
                        return
                    }
                    val fee: Int
                    when (currentBoardSpace) {
                        is Board.Street -> {
                            fee = currentBoardSpace.getCurrentFee()
                            println(
                                "There " +
                                        when (currentBoardSpace.numberOfRestaurants) {
                                            0 -> "are no restaurants"
                                            1 -> "is a restaurant"
                                            in 2..5 -> "are ${currentBoardSpace.numberOfRestaurants} restaurants"
                                            else -> throw IllegalArgumentException(
                                                "Number of restaurants must be between 0 and 5 inclusive."
                                            )
                                        }
                                        + " on this street and the fee is $$fee."
                            )
                        }

                        is Board.GolfCourse -> {
                            val feeInfo = currentBoardSpace.getFeeInfo()
                            val numberOfGolfCoursesOwned = feeInfo[0]
                            fee = feeInfo[1]
                            println(
                                "$ownerName owns $numberOfGolfCoursesOwned golf courses and the fee is $$fee"
                            )
                        }

                        is Board.SuperStore -> {
                            val diceRoll1 = generateDiceRoll()
                            val diceRoll2 = generateDiceRoll()

                            val feeInfo = currentBoardSpace.getFeeInfo(totalDiceRoll = diceRoll1 + diceRoll2)
                            val bothSuperStoresOwnedBySamePerson = feeInfo.component1() as Boolean
                            val multiplier = feeInfo.component2()
                            fee = feeInfo.component3() as Int

                            println(
                                //TODO
                                if (bothSuperStoresOwnedBySamePerson) {
                                    "$ownerName owns both super stores so you have to roll the dice " +
                                            "and pay $multiplier times that amount."
                                } else {
                                    "This is the only super store that $ownerName owns so you have " +
                                            "to roll the dice and pay $multiplier times that amount"
                                }
                                        + "\nYou rolled a $diceRoll1 and a $diceRoll2 so you have to pay $$fee"
                            )
                        }

                        else -> throw IllegalArgumentException(
                            "Board space is not a street, golf course, nor super store."
                        )
                    }

                    val otherPlayer = playerManager.getPlayer(currentBoardSpace.ownerNumber)
                    if (currentPlayer.money < fee) {
                        askAboutGettingMoney(
                            playerNumber = currentPlayer.number,
                            moneyNeeded = fee,
                            playerNeedsMoney = true
                        )
                        // Following condition might be true depending on actions in the above function
                        if (!currentPlayer.isInGame) {
                            if (!gameOver) {
                                // Give all money and properties to the owner of the property.
                                otherPlayer.money += currentPlayer.money
                                board.transferOwnership(
                                    currentOwnerNumber = currentPlayer.number,
                                    newPlayerNumber = otherPlayer.number,
                                    newPlayerName = otherPlayer.name
                                )
                            }
                            return
                        }
                    }
                    currentPlayer.money -= fee
                    otherPlayer.money += fee

                } else {
                    // This block of code is for when a player lands on an unowned property
                    print(" that is unowned.\nWould you like to buy it for $${currentBoardSpace.purchasePrice}? ")
                    inputValidation@ while (true) {
                        when (readLine()!!.toLowerCase()) {
                            "y" -> {
                                if (currentPlayer.money < currentBoardSpace.purchasePrice) {
                                    askAboutGettingMoney(
                                        playerNumber = currentPlayer.number,
                                        moneyNeeded = currentBoardSpace.purchasePrice,
                                        playerNeedsMoney = false
                                    )

                                    // The following condition will be true if the player originally decided
                                    // that they would buy the property but either couldn't gather the money
                                    // for it or changed their mind.
                                    if (currentPlayer.money < currentBoardSpace.purchasePrice) {
                                        println("${currentPlayer.name} is not buying this property")
                                        return
                                    }
                                }

                                currentBoardSpace.setOwner(
                                    ownerNumber = currentPlayer.number,
                                    ownerName = currentPlayer.name
                                )

                                currentPlayer.money -= currentBoardSpace.purchasePrice

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
     * Asks the player whose number is the playerNumber argument about which properties they would like to pawn.
     */
    fun askAboutPawningProperty(playerNumber: Int) {
        val validPropertiesMap = board.getPawnablePropertyMap(playerNumber)
        for (property in validPropertiesMap.values) {
            println(property.pawnInfo)
        }
        while (true) {
            println("Enter the position of the property you would like to pawn or enter \"b\" to go back")
            val input = readLine()!!.toLowerCase()
            if (input == "b") return
            val property = validPropertiesMap[input]
            if (property == null) {
                println("Invalid input")
            } else {
                val player = playerManager.getPlayer(playerNumber)
                player.money += property.pawnPrice
                property.pawn()
                return
            }
        }
    }

    /**
     * Asks the current player about which properties they would like to unpawn.
     */
    fun askAboutUnpawningProperty() {
        val validPropertiesMap = board.getUnpawnablePropertyMap(playerNumber = currentPlayer.number)
        for (property in validPropertiesMap.values) {
            println(property.unpawnInfo)
        }
        while (true) {
            println("Enter the position of the property you would like to unpawn or enter \"b\" to go back")
            val input = readLine()!!.toLowerCase()
            if (input == "b") return
            val property = validPropertiesMap[input]
            if (property == null) {
                // This will be true if there was no such key in validPropertiesMap
                println("Invalid input")
            } else {
                if (currentPlayer.money < property.unpawnPrice) {
                    askAboutGettingMoney(
                        playerNumber = currentPlayer.number,
                        moneyNeeded = property.unpawnPrice,
                        playerNeedsMoney = false
                    )

                    // Following condition will be true if the player couldn't get enough money or changed their mind.
                    if (currentPlayer.money < property.unpawnPrice) {
                        return
                    }
                }
                currentPlayer.money -= property.unpawnPrice
                property.unpawn()
                return
            }
        }
    }

    /**
     * Asks the current player about which streets they would like to add a restaurant to.
     */
    fun askAboutAddingRestaurant() {
        val validStreetsMap = board.getStreetsWhereRestaurantCanBeAdded(currentPlayer.number)
        for (street in validStreetsMap.values) {
            println(street.restaurantAddInfo)
        }
        while (true) {
            println("Enter the position of the street you would like to add a restaurant to or enter \"b\" go back")
            val input = readLine()!!.toLowerCase()
            if (input == "b") {
                return
            }
            val street = validStreetsMap[input]
            if (street == null) {
                // This will be true if there was no such key in validStreetsMap
                println("Invalid input")
            } else {
                val restaurantAddingFee = street.restaurantAddPrice
                if (currentPlayer.money < restaurantAddingFee) {
                    askAboutGettingMoney(
                        playerNumber = currentPlayer.number,
                        moneyNeeded = restaurantAddingFee,
                        playerNeedsMoney = false
                    )

                    // Following condition will be true if the player couldn't get enough money or changed their mind.
                    if (currentPlayer.money < restaurantAddingFee) {
                        return
                    }
                }
                currentPlayer.money -= restaurantAddingFee
                street.addRestaurant()
                return
            }
        }
    }

    /**
     * Asks the player whose number is the playerNumber argument about which streets they would like to remove a
     * restaurant from.
     */
    fun askAboutRemovingRestaurant(playerNumber: Int) {
        val validStreetsMap = board.getStreetsWhereRestaurantCanBeRemoved(playerNumber)
        for (street in validStreetsMap.values) {
            println(street.restaurantRemoveInfo)
        }
        while (true) {
            println(
                "Enter the position of the street you would like to remove a restaurant from or enter " +
                        "\"b\" to go back"
            )
            val input = readLine()!!.toLowerCase()
            if (input == "b") return
            val street = validStreetsMap[input]
            if (street == null) {
                // This will be true if there was no such key in validStreetsMap
                println("Invalid input")
            } else {
                val player = playerManager.getPlayer(playerNumber)
                player.money += street.restaurantRemoveGain
                street.removeRestaurant()
                return
            }
        }
    }

    /**
     * Asks a player what they want to do if they need money immediately and don't have it. If the player decides
     * to drop out, then this function exits. If they decide to do something else, the amount of money they have
     * afterwards is checked and if this amount of money is still not enough, this function loops again.
     *
     * @param playerNumber The number of the player that needs money.
     * @param moneyNeeded The total amount of money that the player needs.
     * @param playerNeedsMoney If this is false, the player has the option to exit the function without getting all
     * the money that was required for the situation they were in.
     */
    fun askAboutGettingMoney(playerNumber: Int, moneyNeeded: Int, playerNeedsMoney: Boolean) {
        val player = playerManager.getPlayer(playerNumber)
        var moneyGainOptions = board.getMoneyGainOptions(playerNumber)

        moneyGettingLoop@ while (true) {
            val playerCanPawn = moneyGainOptions.getValue("pawn")
            val playerCanRemoveRestaurant = moneyGainOptions.getValue("remove restaurant")
            println("${player.name}, you need $$moneyNeeded and you currently have $${player.money}.")

            if (!playerNeedsMoney) {
                println("You don't need this money so you have the option of going back.")
            }

            println(
                if (playerNeedsMoney) {
                    "1: Drop out of the game"
                } else {
                    "1: Go back"
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
                    if (playerNeedsMoney) {
                        player.removeFromGame()
                        if (playerManager.onePlayerIsInGame) {
                            gameOver = true
                        }
                    }
                    return
                }

                "2" -> askAboutTrading(initiatingPlayerNumber = playerNumber, initiatingPlayerNeedsMoney = true)

                "3" -> {
                    if (playerCanPawn) {
                        askAboutPawningProperty(playerNumber)
                    } else {
                        println("Invalid input")
                        continue@moneyGettingLoop
                    }
                }

                "4" -> {
                    if (playerCanRemoveRestaurant) {
                        askAboutRemovingRestaurant(playerNumber)
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

            if (player.money >= moneyNeeded) {
                return
            }

            // Refresh the money gain options since they could've changed in one of the functions.
            moneyGainOptions = board.getMoneyGainOptions(playerNumber = playerNumber)
        }
    }

    /**
     * This function could be used to allow any player to try to make a trade.
     * @param initiatingPlayerNumber
     * @param initiatingPlayerNeedsMoney If this is true, the initiating player will need to select a money amount
     * they want from the player they're offering a trade to and they won't be able to offer money themselves.
     */
    fun askAboutTrading(initiatingPlayerNumber: Int, initiatingPlayerNeedsMoney: Boolean) {
        /**
         * When a player wants to make a trade, they will have to select what they want to offer and what they want to
         * receive. These can include money, properties, and get off vacation free cards. This class is used as a
         * data structure for those.
         */
        class TradeData {
            var money = 0
                set(value) {
                    if (value < 0) {
                        throw Exception("Trade data money cannot be negative")
                    }
                    field = value
                }

            private val _properties = mutableListOf<Board.Property>()

            /**
             * Is a read-only list of properties that were selected.
             */
            val properties: List<Board.Property> get() = _properties

            fun addPropertyIfAbsent(property: Board.Property) {
                if (property !in _properties) {
                    _properties.add(property)
                }
            }

            var getOffVacationFreeCards = 0
                set(value) {
                    if (value < 0) {
                        throw Exception("Trade data amount of get off vacation free cards cannot be negative")
                    }
                    field = value
                }

            /**
             * Is true when the amount of money, properties, and amount of get off vacation free cards are the same
             * as when this object was instantiated.
             */
            val nothingHasBeenChanged get() = money == 0 && _properties.isEmpty() && getOffVacationFreeCards == 0
            val onlyMoneyHasBeenChanged get() = money != 0 && _properties.isEmpty() && getOffVacationFreeCards == 0
        }

        val initiatingPlayer = playerManager.getPlayer(initiatingPlayerNumber)
        val mapOfPlayersInGame = playerManager.getMapOfOtherPlayersInGame(
            excludingPlayerNumber = initiatingPlayerNumber
        )

        mainLoop@ while (true) {
            println("You are at the beginning of the trade process")
            val otherPlayer: PlayerManager.Player
            inputValidation1@ while (true) {
                println(
                    """
                    Enter one of the following and press enter:
                    i: See property info
                    p: See player info
                    c: Cancel the trade
                """.trimIndent()
                )
                for (player in mapOfPlayersInGame) {
                    val playerNumber = player.key
                    val playerName = player.value.name
                    println("$playerNumber to initiate a trade with $playerName")
                }
                when (val input = readLine()!!.toLowerCase()) {
                    "i" -> {
                        board.displayPropertyInfo()
                    }

                    "p" -> {
                        playerManager.displayPlayerInfo()
                    }

                    "c" -> return

                    else -> {
                        val otherPlayerNumber = input.toIntOrNull()
                        if (otherPlayerNumber == null || otherPlayerNumber !in mapOfPlayersInGame.keys) {
                            println("Invalid input")
                        } else {
                            otherPlayer = mapOfPlayersInGame[otherPlayerNumber]!!
                            break@inputValidation1
                        }
                    }
                }
            }

            val initiatingPlayerHasProperty = board.playerHasAProperty(initiatingPlayerNumber)
            val otherPlayerHasAProperty = board.playerHasAProperty(otherPlayer.number)

            if (!initiatingPlayerHasProperty && !initiatingPlayer.hasAGetOffVacationCard &&
                !otherPlayerHasAProperty && !otherPlayer.hasAGetOffVacationCard
            ) {
                println(
                    "There is nothing that ${initiatingPlayer.name} and ${otherPlayer.name} can trade with each other " +
                            "besides money. The trade process will be restarted."
                )
                continue@mainLoop
            }

            val whatPlayerWants = TradeData()

            selectWhatIsWanted@ while (true) {
                inputValidation1@ while (true) {
                    // Only ask the initiating player the following question if they don't need money.
                    if (!initiatingPlayerNeedsMoney) {
                        print("Would you like any money from this person? (y/n): ")
                        val input = readLine()!!.toLowerCase()
                        if (input == "n") break@inputValidation1
                        else if (input != "y") {
                            println("Invalid input")
                            continue@inputValidation1
                        }
                    }

                    inputValidation2@ while (true) {
                        println("This person has $${otherPlayer.money}. Enter an amount or enter \"b\" to go back:")
                        if (initiatingPlayerNeedsMoney) {
                            println("You need money so entering an amount is mandatory")
                        }
                        val input = readLine()!!.toLowerCase()
                        if (input == "b") {
                            break@inputValidation2
                        }
                        val moneyAmount = input.toIntOrNull()
                        if (moneyAmount == null || moneyAmount !in 0..otherPlayer.money ||
                            (initiatingPlayerNeedsMoney && moneyAmount == 0)
                        ) {
                            println("Invalid input")
                        } else {
                            whatPlayerWants.money = moneyAmount
                            break@inputValidation1
                        }
                    }
                }

                if (otherPlayerHasAProperty) {
                    inputValidation1@ while (true) {
                        print("Would you like any properties from this person? (y/n): ")
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                val otherPlayerPropertyMap = board.getPropertyInfoMap(otherPlayer.number)
                                for (property in otherPlayerPropertyMap.values) {
                                    println(property.lowDetailInfo)
                                }
                                inputValidation2@ while (true) {
                                    println(
                                        "Enter the positions of all the properties you would like with each position " +
                                                "separated by a space or enter \"b\" to go back:"
                                    )
                                    input = readLine()!!.toLowerCase().trim()
                                    if (input == "b") {
                                        continue@inputValidation1
                                    }
                                    val positionsEntered = input.split(" ")

                                    for (positionStr in positionsEntered) {
                                        val property = otherPlayerPropertyMap[positionStr]
                                        if (property == null) {
                                            // This will be true if there was no such key in otherPlayerPropertyMap
                                            println("Invalid input")
                                            continue@inputValidation2
                                        }

                                        whatPlayerWants.addPropertyIfAbsent(property)
                                    }
                                    break@inputValidation1
                                }
                            }

                            "n" -> break@inputValidation1

                            else -> println("Invalid input")
                        }
                    }
                }

                if (otherPlayer.hasAGetOffVacationCard) {
                    inputValidation1@ while (true) {
                        print("Would you like any get off vacation cards? (y/n): ")
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                inputValidation2@ while (true) {
                                    println(
                                        "${otherPlayer.name} has ${otherPlayer.numberOfGetOffVacationCardsOwned} get " +
                                                "off vacation cards. How many would you like? Enter an amount or " +
                                                "\"b\" to go back:"
                                    )
                                    input = readLine()!!
                                    if (input == "b") continue@inputValidation1
                                    val amount = input.toIntOrNull()
                                    if (amount == null || amount !in 0..otherPlayer.numberOfGetOffVacationCardsOwned) {
                                        println("Invalid input")
                                    } else {
                                        whatPlayerWants.getOffVacationFreeCards = amount
                                        break@inputValidation1
                                    }
                                }
                            }

                            "n" -> break@inputValidation1

                            else -> println("Invalid input")
                        }
                    }
                }

                if (whatPlayerWants.nothingHasBeenChanged) {
                    println(
                        "${initiatingPlayer.name}, you didn't select anything for what you want. " +
                                "You must select something."
                    )
                } else {
                    break@selectWhatIsWanted
                }
            }

            // Part for having the initiating player select what they offer
            val whatPlayerOffers = TradeData()
            selectWhatToOffer@ while (true) {
                inputValidation1@ while (true) {
                    // Only give the initiating player the choice of offering money if they are not in need of money
                    // and they chose that they didn't want any money.
                    if (!initiatingPlayerNeedsMoney || whatPlayerWants.money == 0) {
                        print("Would you like to offer this person any money? ")
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                inputValidation2@ while (true) {
                                    print("You have $${initiatingPlayer.money}, how much of it would you like to offer? ")
                                    input = readLine()!!
                                    val moneyAmount = input.toIntOrNull()
                                    if (moneyAmount == null || moneyAmount !in 0..initiatingPlayer.money) {
                                        println("Invalid input")
                                    } else {
                                        whatPlayerOffers.money = moneyAmount
                                        break@inputValidation1
                                    }
                                }
                            }

                            "n" -> break@inputValidation1

                            else -> println("Invalid input")
                        }
                    }
                }

                if (initiatingPlayerHasProperty) {
                    inputValidation1@ while (true) {
                        print("Would you like to offer this player any properties? (y/n): ")
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                val currentPlayerPropertyMap = board.getPropertyInfoMap(currentPlayer.number)
                                for (property in currentPlayerPropertyMap.values) {
                                    println(property.lowDetailInfo)
                                }
                                inputValidation2@ while (true) {
                                    println(
                                        "Enter the positions of the properties you would like to offer with each position " +
                                                "separated by a single space or enter \"b\" to go back: "
                                    )
                                    input = readLine()!!.toLowerCase().trim()
                                    if (input == "b") {
                                        continue@inputValidation1
                                    }
                                    val positionsEntered: List<String> = input.split(" ")

                                    for (positionStr in positionsEntered) {
                                        val property = currentPlayerPropertyMap[positionStr]
                                        if (property == null) {
                                            // This will be true if there was no such key in currentPlayerPropertyMap
                                            println("Invalid input")
                                            continue@inputValidation2
                                        }
                                        whatPlayerOffers.addPropertyIfAbsent(property)
                                    }
                                    break@inputValidation1
                                }
                            }

                            "n" -> break@inputValidation1

                            else -> println("Invalid input")
                        }
                    }
                }

                if (initiatingPlayer.hasAGetOffVacationCard) {
                    inputValidation1@ while (true) {
                        print("Would you like to offer any get off vacation cards? (y/n) ")
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                inputValidation2@ while (true) {
                                    println(
                                        "You have ${initiatingPlayer.numberOfGetOffVacationCardsOwned} get " +
                                                "off vacation cards. How many would you like to offer? " +
                                                "Enter an amount or \"b\" to go back"
                                    )
                                    input = readLine()!!.toLowerCase()
                                    if (input == "b") continue@inputValidation1
                                    val amount = input.toIntOrNull()
                                    if (amount == null || amount !in
                                        0..initiatingPlayer.numberOfGetOffVacationCardsOwned
                                    ) {
                                        println("Invalid input")
                                    } else {
                                        whatPlayerOffers.getOffVacationFreeCards = amount
                                        break@inputValidation1
                                    }
                                }
                            }

                            "n" -> break@inputValidation1

                            else -> println("Invalid input")
                        }
                    }
                }

                if (whatPlayerOffers.nothingHasBeenChanged) {
                    println(
                        "${initiatingPlayer.name}, you didn't offer anything. You must select something to offer."
                    )
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

                for (property in whatPlayerOffers.properties) {
                    println(property.lowDetailInfo)
                    if (property is Board.Street && property.neighborhoodIsOwnedBySinglePlayer) {
                        println(
                            "Note: the above property is a street that is in a neighborhood that is currently " +
                                    "owned by ${initiatingPlayer.name}.\nIf they agree to the trade, that neighborhood " +
                                    "will no longer be owned by only them so all houses will be sold and the fees " +
                                    "will go back to the starting fees."
                        )
                    }
                }

                if (whatPlayerOffers.getOffVacationFreeCards > 0) {
                    println(
                        if (whatPlayerOffers.getOffVacationFreeCards == 1) {
                            "A get off vacation card"
                        } else {
                            "${whatPlayerOffers.getOffVacationFreeCards} get off vacation free cards"
                        }
                    )
                }

                println("In exchange for:")

                if (whatPlayerWants.money > 0) {
                    println("$${whatPlayerWants.money}")
                }

                for (property in whatPlayerWants.properties) {
                    println(property.lowDetailInfo)
                    if (property is Board.Street && property.neighborhoodIsOwnedBySinglePlayer) {
                        println(
                            "Note: the above property is a street that is in a neighborhood that is currently " +
                                    "owned by ${otherPlayer.name}.\nIf they agree to the trade, that neighborhood " +
                                    "will no longer be owned by only them so all houses will be sold and the fees " +
                                    "will go back to the starting fees."
                        )
                    }
                }

                if (whatPlayerWants.getOffVacationFreeCards > 0) {
                    println(
                        if (whatPlayerWants.getOffVacationFreeCards == 1) {
                            "A get off vacation card"
                        } else {
                            "${whatPlayerWants.getOffVacationFreeCards} get off vacation cards"
                        }
                    )
                }

                println(
                    """
                        
                    ${initiatingPlayer.name}, are you sure you want to make this offer to ${otherPlayer.name}?
                    Enter "y", "n", or "c" to cancel the trade process
                """.trimIndent()
                )
                when (readLine()!!.toLowerCase()) {
                    "y" -> break@confirmTrade
                    "n" -> continue@mainLoop
                    "c" -> return
                    else -> println("Invalid input")
                }
            }

            // TODO make sure whatPlayerOffers and whatPlayerWants are not mixed up anywhere.
            acceptOrDenyTrade@ while (true) {
                println("${otherPlayer.name}, do you accept the offer that was just confirmed by ${initiatingPlayer.name}?")
                when (readLine()!!.toLowerCase()) {
                    "y" -> {
                        // Give the other player what was agreed upon
                        initiatingPlayer.money -= whatPlayerOffers.money
                        otherPlayer.money += whatPlayerOffers.money

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

                            property.setOwner(ownerNumber = otherPlayer.number, ownerName = otherPlayer.name)
                        }

                        for (i in 0 until whatPlayerOffers.getOffVacationFreeCards) {
                            initiatingPlayer.removeGetOffVacationCard()
                            otherPlayer.addGetOffVacationCard()
                        }

                        // Now give the initiating player what was agreed upon.
                        otherPlayer.money -= whatPlayerWants.money
                        initiatingPlayer.money += whatPlayerWants.money

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

                            property.setOwner(ownerNumber = currentPlayer.number, ownerName = currentPlayer.name)
                        }

                        for (i in 0 until whatPlayerWants.getOffVacationFreeCards) {
                            otherPlayer.removeGetOffVacationCard()
                            initiatingPlayer.addGetOffVacationCard()
                        }

                        return
                    }

                    "n" -> {
                        println("${otherPlayer.name} denies the trade offer")
                        return
                    }

                    else -> {
                        println("Invalid input")
                    }
                }
            }
        }
    }
}