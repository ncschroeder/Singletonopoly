package commandline

import java.lang.Exception
import kotlin.random.Random

/**
 * At the beginning of the game and after each turn, currentPlayer should be assigned to playerManager.currentPlayerCopy.
 * Throughout each player's turn, changes should be made to currentPlayer and then in the endTurn function which is
 * nested in the play function, playerManager.updatePlayer(currentPlayer) should be called to save all these changes.
 * There are some situations where changes are made to other players and in these cases, a copy of that player should
 * be taken from the playerManager and then changes should be made to that copy and then the updatePlayer method of
 * the playerManager should be called on the copy of that player.
 */
class Game {
    val playerManager = PlayerManager()
    var currentPlayer = playerManager.currentPlayerCopy
    val bank = Bank()
    val board = Board()
    val entropyDeck = EntropyDeck()

    var gameOver = false
    var turnOver = false
    var goAgain = false
    var numberOfDoubleRolls = 0

    init {
        Player.vacationPosition = board.getVacationPosition()
        play()
    }

    fun play() {

        // Testing area
        val player2 = playerManager.getPlayerCopy(2)
        board.setPropertyOwner(2, player2.number!!, player2.name)
        board.checkForNeighborhoodChanges(2)
        board.setPropertyOwner(3, player2.number!!, player2.name)
        board.checkForNeighborhoodChanges(3)
        board.setPropertyOwner(4, player2.number!!, player2.name)
        board.checkForNeighborhoodChanges(4)



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

            if (!turnOver) {
                rollDiceAndMove()
            }

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
        if (playerManager.onePlayerIsLeftInGame()) {
            println("The winner is ${playerManager.getWinnerName()}!")
        }
    }

    fun getDiceRoll() = Random.nextInt(1, 7)

    fun endTurn() {
        if (turnOver) {
            turnOver = false
        }
        if (goAgain) {
            goAgain = false
        } else {
            playerManager.updatePlayer(currentPlayer)
            playerManager.switchToNextPlayer()
            currentPlayer = playerManager.currentPlayerCopy
        }
        println("End of turn. Press enter to continue.")
        readLine()
    }

    fun askAboutPreRollAction() {
        var possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number!!)

        if (currentPlayer is HumanPlayer) {
            preRollAction@ while (true) {
                val playerCanAddRestaurant = possiblePropertyActions["add restaurant"]
                val playerCanRemoveRestaurant = possiblePropertyActions["remove restaurant"]
                val playerCanPawn = possiblePropertyActions["pawn"]
                val playerCanUnpawn = possiblePropertyActions["unpawn"]
                println(
                    """
                        ${currentPlayer.name}, type one of the following and press enter:
                        tt: Take your turn
                        i: View property info
                        p: View player positions
                        mt: Make a trade with another player
                        do: Drop out of the game
                        eg: End the game
                        """.trimIndent()
                )
                // None of these should be null so an exception should never be thrown.
                if (playerCanAddRestaurant!!) {
                    println("ar: Add a restaurant to one of your properties")
                }
                if (playerCanRemoveRestaurant!!) {
                    println("rr: Remove a restaurant from one of your properties")
                }
                if (playerCanPawn!!) {
                    println("pa: Pawn one of your properties")
                }
                if (playerCanUnpawn!!) {
                    println("u: Unpawn one of your properties")
                }

                when (readLine()!!.toLowerCase()) {
                    "tt" -> break@preRollAction

                    "i" -> board.displayPropertyInfo()

                    "p" -> playerManager.displayPositions()

                    "mt" -> {
                        askAboutTrading()
                        // Need to refresh the possible property actions.
                        possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number!!)
                    }

                    "do" -> {
                        currentPlayer.removeFromGame()
                        turnOver = true
                    }

                    "eg" -> {
                        return
                    }

                    "ar" -> {
                        if (playerCanAddRestaurant) {
                            askAboutAddingRestaurant()
                            // Need to refresh the possible property actions due to possible changes in the
                            // above function.
                            possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number!!)
                        } else {
                            println("Invalid input")
                        }
                    }

                    "rr" -> {
                        if (playerCanRemoveRestaurant) {
                            askAboutRemovingRestaurant()
                            // Need to refresh the possible property actions due to possible changes in the
                            // above function.
                            possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number!!)
                        } else {
                            println("Invalid input")
                        }
                    }

                    "pa" -> {
                        if (playerCanPawn) {
                            askAboutPawningProperty()
                            // Need to refresh the possible property actions due to possible changes in the
                            // above function.
                            possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number!!)
                        } else {
                            println("Invalid input")
                        }
                    }

                    "u" -> {
                        if (playerCanUnpawn) {
                            askAboutUnpawningProperty()
                            // Need to refresh the possible property actions due to possible changes in the
                            // above function.
                            possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number!!)
                        } else {
                            println("Invalid input")
                        }
                    }

                    else -> println("Invalid input")
                }
            }
        } else {
            // Block of code for AI players
            TODO()
        }
    }

    fun doVacationAction() {
        if (currentPlayer is HumanPlayer) {
            println(
                "You're on vacation and this is your " +
                        when (currentPlayer.numberOfTurnsOnVacation) {
                            0 -> "first"
                            1 -> "second"
                            2 -> "third (last)"
                            else -> throw Exception("Shouldn't be on vacation at this point")
                        }
                        + " turn on vacation"
            )

            inputValidation@ while (true) {
                println("""
                    Would you like to:
                    1: Pay $50 to get off vacation
                    2: Try to roll doubles
                """.trimIndent())
                if (currentPlayer.hasAGetOffVacationCard) {
                    println("3: Use a get off vacation card")
                }

                when (readLine()) {
                    "1" -> {
                        if (currentPlayer.money >= 50) {
                            currentPlayer.money -= 50
                            bank.money += 50
                            return
                        }
                        println("Not enough money")
                    }

                    "2" -> {
                        val diceRoll1 = getDiceRoll()
                        val diceRoll2 = getDiceRoll()
                        println("You rolled a $diceRoll1 and a $diceRoll2")
                        if (diceRoll1 == diceRoll2) {
                            println("These are doubles so you are off vacation")
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
                            println("${currentPlayer.name} has chosen to use a get off vacation card")
                            currentPlayer.removeGetOffVacationCard()
                            entropyDeck.insertGetOffVacationCardAtBottom()
                            break@inputValidation
                        } else {
                            println("Invalid input")
                        }
                    }

                    else -> println("Invalid input")
                }
            }
        } else {
            // Block of code for AI players for deciding what to do when on vacation
            if (currentPlayer.money >= 50) {
                println("${currentPlayer.name} will pay $50 to get off vacation")
                currentPlayer.money -= 50
                currentPlayer.removeFromVacation()
            } else {
                val diceRoll1 = getDiceRoll()
                val diceRoll2 = getDiceRoll()
                println("${currentPlayer.name} will try to roll doubles\nThey rolled a $diceRoll1 and a $diceRoll2")
                if (diceRoll1 == diceRoll2) {
                    println("These are doubles so ${currentPlayer.name} is off vacation")
                    currentPlayer.removeFromVacation()
                } else {
                    println("These are not doubles so ${currentPlayer.name} will continue their vacation")
                    currentPlayer.continueVacation()
                    turnOver = true
                }
            }
        }
    }

    fun rollDiceAndMove() {
        val diceRoll1 = getDiceRoll()
        val diceRoll2 = getDiceRoll()
        println("${currentPlayer.name} rolled a $diceRoll1 and a $diceRoll2")
        if (diceRoll1 == diceRoll2) {
            numberOfDoubleRolls++
            if (numberOfDoubleRolls == 3) {
                println("${currentPlayer.name} rolled doubles 3 times in a row so they go to vacation")
                numberOfDoubleRolls = 0
                currentPlayer.sendToVacation()
                turnOver = true
            }
            println("${currentPlayer.name} rolled doubles so they get to go again")
            goAgain = true
        } else if (numberOfDoubleRolls > 0) {
            // Reset.
            numberOfDoubleRolls = 0
        }

        if (currentPlayer.position + diceRoll1 + diceRoll2 > board.numberOfSpaces) {
            currentPlayer.position = (currentPlayer.position + diceRoll1 + diceRoll2) % board.numberOfSpaces + 1
            println("${currentPlayer.name} has made a revolution")
            if (bank.money >= 200) {
                bank.money -= 200
                currentPlayer.money += 200
            } else {
                val moneyExchanged = bank.money
                bank.money = 0
                currentPlayer.money += moneyExchanged
            }
        } else {
            currentPlayer.position += (diceRoll1 + diceRoll2)
        }
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
                        // The top card should be moved to the bottom for most situations so this variable will
                        // be set true. For the situations where the top card does not get moved to the bottom,
                        // this variable will be changed to false.
                        var topCardShouldBeMovedToBottom = true

                        println("You draw a card from the entropy deck and it says \"${entropyDeck.topCard.message}\"")
                        when (entropyDeck.topCard.type) {
                            "bank to player" -> {
                                val moneyExchanged = entropyDeck.topCard.value!!
                                bank.money -= moneyExchanged
                                currentPlayer.money += moneyExchanged
                            }

                            "player to bank" -> {
                                val moneyExchanged = entropyDeck.topCard.value!!
                                currentPlayer.money -= moneyExchanged
                                bank.money += moneyExchanged
                            }

                            "other players to player" -> {
                                val moneyReceivedFromEachPlayer = entropyDeck.topCard.value!!
                                for (playerNumber in playerManager.getNumbersOfOtherPlayersInGame(currentPlayer.number!!)) {
                                    while (true) {
                                        val player = playerManager.getPlayerCopy(playerNumber)
                                        if (player.money >= moneyReceivedFromEachPlayer) {
                                            player.money -= moneyReceivedFromEachPlayer
                                            currentPlayer.money += moneyReceivedFromEachPlayer
                                            playerManager.updatePlayer(player)
                                            break
                                        }
                                        playerNeedsMoney(playerNumber, moneyReceivedFromEachPlayer - player.money)
                                        /*
                                        Following condition might be true depending on decisions of player in the
                                        playerNeedsMoney function. If it's true then that means that the player
                                        decided to drop out of the game and in that case we can skip to the next
                                        player number.
                                        */
                                        if (!playerManager.getPlayerCopy(playerNumber).isInGame) {
                                            break
                                        }
                                    }
                                }
//                                for (poorPlayer in playerManager.getPlayersThatDontHaveEnoughMoney(
//                                    moneyReceivedFromEachPlayer
//                                )) {
//                                    var moneyNeeded = moneyReceivedFromEachPlayer - poorPlayer.money
//                                    while (true) {
//                                        playerNeedsMoney(poorPlayer.number, moneyNeeded)
//                                        /**
//                                         * The changes from the playerNeedsMoney function should be reflected in the
//                                         * players copy of the playerManager.
//                                         */
//                                        val playerCopy = playerManager.getPlayerCopy(poorPlayer.number)
//                                        if (!playerCopy.isInGame || playerCopy.money >= moneyReceivedFromEachPlayer) {
//                                            break
//                                        }
//                                        moneyNeeded = moneyReceivedFromEachPlayer - playerCopy.money
//                                    }
//                                }
//                                playerManager.removeMoneyFromAllPlayersBesidesCurrent(
//                                    moneyReceivedFromEachPlayer
//                                )
//                                currentPlayer.money += totalMoneyReceived
                            }

                            "player to other players" -> {
                                val moneyOwedToEachPlayer = entropyDeck.topCard.value!!
                                val totalMoneyOwed =
                                    moneyOwedToEachPlayer * (playerManager.numberOfPlayersInGame - 1)
                                while (true) {
                                    if (currentPlayer.money >= totalMoneyOwed) {
                                        for (playerNumber in
                                        playerManager.getNumbersOfOtherPlayersInGame(currentPlayer.number!!)) {
                                            val player = playerManager.getPlayerCopy(playerNumber)
                                            player.money += moneyOwedToEachPlayer
                                            playerManager.updatePlayer(player)
                                        }
                                        break
                                    }
                                    playerNeedsMoney(moneyNeeded = totalMoneyOwed - currentPlayer.money)
                                    // This might be true depending on actions of the player in the playerNeedsMoney
                                    // function. If it's true then that means that the current player decided to drop
                                    // out and in that case, we can check for game over status and exit the function.
                                    if (!currentPlayer.isInGame) {
                                        if (playerManager.onePlayerIsLeftInGame()) {
                                            gameOver = true
                                        }
                                        return
                                    }
                                }
//                                while (currentPlayer.money < totalMoneyOwed) {
//                                    playerNeedsMoney(moneyNeeded = totalMoneyOwed - currentPlayer.money)
//                                    if (!currentPlayer.isInGame) {
//                                        // This should be true if the player decided to drop out of the game in
//                                        // the playerNeedsMoney function.
//                                        return
//                                    }
//                                }
//                                currentPlayer.money -= totalMoneyOwed
//                                playerManager.addMoneyToAllPlayersBesidesCurrent(moneyOwedToEachPlayer)
                            }

                            "absolute position change" -> {
                                val newPosition = entropyDeck.topCard.value!!
                                if (newPosition < currentPlayer.position) {
                                    println("${currentPlayer.name} has made a revolution")
                                    bank.money -= 200
                                    currentPlayer.money += 200
                                }
                                currentPlayer.position = newPosition
                                evaluatePosition()
                            }

                            "relative position change" -> {
                                val positionChange = entropyDeck.topCard.value!!
                                val newPosition = currentPlayer.position + positionChange
                                when {
                                    newPosition > board.numberOfSpaces -> {
                                        currentPlayer.position = newPosition % board.numberOfSpaces + 1
                                        println("${currentPlayer.name} has made a revolution")
                                        bank.money -= 200
                                        currentPlayer.money += 200
                                    }
                                    newPosition <= 0 -> currentPlayer.position = board.numberOfSpaces + newPosition
                                    else -> currentPlayer.position = newPosition
                                }
                                evaluatePosition()
                            }

                            "get off vacation free" -> {
                                topCardShouldBeMovedToBottom = false
                                entropyDeck.removeGetOffVacationCardAtTop()
                                currentPlayer.addGetOffVacationCard()
                            }

                            "property maintenance" -> {
                                val feePerRestaurant = entropyDeck.topCard.value!!
                                val restaurantCount = board.getRestaurantCount(currentPlayer.number!!)
                                val maintenanceFee = feePerRestaurant * restaurantCount
                                println(
                                    "${currentPlayer.name} has $restaurantCount restaurants and must pay " +
                                            "$$feePerRestaurant per restaurant so they owe $maintenanceFee."
                                )

                                while (currentPlayer.money < maintenanceFee) {
                                    playerNeedsMoney(moneyNeeded = maintenanceFee - currentPlayer.money)

                                    // The following condition should be true if the player decided to drop out of
                                    // the game in the playerNeedsMoney function.
                                    if (!currentPlayer.isInGame) {
                                        return
                                    }
                                }
                                currentPlayer.money -= maintenanceFee
                                bank.money += maintenanceFee
                            }
                        }
                        if (topCardShouldBeMovedToBottom) {
                            entropyDeck.moveTopCardToBottom()
                        }
                    }
                }
            }

            is Property -> {
                print(
                    "${currentBoardSpace.name}, which is a " +
                            when (currentBoardSpace) {
                                is Street -> "street in the ${currentBoardSpace.neighborhood} neighborhood"
                                is GolfCourse -> "golf course"
                                is SuperStore -> "super store"
                                else -> throw Exception("Invalid board space")
                            }
                )
                if (currentBoardSpace.isOwned) {
                    print(" that is owned ")
                    if (currentBoardSpace.ownerNumber == currentPlayer.number) {
                        println("by them")
                    } else {
                        val ownerName = currentBoardSpace.ownerName
                        println("by ${currentBoardSpace.ownerName}.")
                        if (currentBoardSpace.isPawned) {
                            println("This property is pawned so they don't have to pay a fee.")
                        } else {
                            val fee: Int
                            when (currentBoardSpace) {
                                is Street -> {
                                    fee = currentBoardSpace.fee
                                    println(
                                        "There " +
                                                when (currentBoardSpace.numberOfRestaurants) {
                                                    0 -> "are no restaurants"
                                                    1 -> "is a 1-star restaurant"
                                                    2 -> "are 1-star and 2-star restaurants"
                                                    3 -> "are 1-star, 2-star, and 3-star restaurants"
                                                    4 -> "are 1-star, 2-star, 3-star, and 4-star restaurants"
                                                    5 -> "are 1-star, 2-star, 3-star, 4-star, and 5-star restaurants"
                                                    else -> throw Exception("Invalid number of restaurants")
                                                }
                                                + " on this street and the fee is $fee."
                                    )
                                }

                                is GolfCourse -> {
                                    val numGolfCoursesOwned =
                                        playerManager.getPlayerCopy(currentBoardSpace.ownerNumber!!).numberOfGolfCoursesOwned
                                    fee = GolfCourse.getFee(numGolfCoursesOwned)
                                    println(
                                        "$ownerName owns $numGolfCoursesOwned golf courses and the fee is $fee"
                                    )
                                }

                                is SuperStore -> {
                                    val numberOfSuperStoresOwned =
                                        playerManager.getPlayerCopy(currentBoardSpace.ownerNumber!!).numberOfSuperStoresOwned
                                    val diceRoll1 = getDiceRoll()
                                    val diceRoll2 = getDiceRoll()
                                    fee = (diceRoll1 + diceRoll2) * if (numberOfSuperStoresOwned == 2) 10 else 5
                                    println(
                                        if (numberOfSuperStoresOwned == 2) {
                                            "$ownerName owns both super stores so you have to roll the dice " +
                                                    "and pay 10 times that amount."
                                        } else {
                                            "This is the only super store that $ownerName owns so you have " +
                                                    "to roll the dice and pay 5 times that amount"
                                        }
                                                + "\nYou rolled a $diceRoll1 and a $diceRoll2 so you have to pay $fee"
                                    )
                                }

                                else -> throw Exception("Invalid space")
                            }

                            while (true) {
                                if (currentPlayer.money >= fee) {
                                    val otherPlayer = playerManager.getPlayerCopy(currentBoardSpace.ownerNumber!!)
                                    currentPlayer.money -= fee
                                    otherPlayer.money += fee
                                    playerManager.updatePlayer(otherPlayer)
                                    break
                                }
                                playerNeedsMoney(moneyNeeded = fee - currentPlayer.money)
                                if (!currentPlayer.isInGame) break
                            }

//                            while (currentPlayer.money < fee) {
//                                if (currentPlayer is HumanPlayer) {
//                                    playerNeedsMoney(moneyNeeded = fee - currentPlayer.money)
//                                }
//                            }
//                            currentPlayer.money -= fee
//                            playerManager.addMoney(currentBoardSpace.ownerNumber, fee)
                        }
                    }
                } else {
                    // This block of code is for when a player lands on an unowned property
                    println(" that is unowned.")
                    if (currentPlayer is HumanPlayer) {
                        print("Would you like to buy it for $${currentBoardSpace.purchasePrice}? ")
                        propertyInputValidation@ while (true) {
                            when (readLine()!!.toLowerCase()) {
                                "y" -> {
                                    board.setPropertyOwner(
                                        currentPlayer.position,
                                        currentPlayer.number!!,
                                        currentPlayer.name
                                    )

                                    println()
                                    currentPlayer.money -= currentBoardSpace.purchasePrice
                                    bank.money += currentBoardSpace.purchasePrice
                                    println()

                                    when (currentBoardSpace) {
                                        is Street -> {
                                            board.checkForNeighborhoodChanges(currentPlayer.position)
//                                            if (currentBoardSpace.neighborhoodOwnedBySinglePlayer) {
//                                                 //This will be true if the player just bought a street and it turns
//                                                 //out that that player owns all streets in that neighborhood
//                                                print("Would you like to add any restaurants? ")
//                                            }
                                        }
                                        is GolfCourse -> currentPlayer.addGolfCourse()
                                        is SuperStore -> currentPlayer.addSuperStore()
                                    }
                                    break@propertyInputValidation
                                }
                                "n" -> break@propertyInputValidation
                                else -> print("\nInvalid input, must enter \"y\" or \"n\": ")
                            }
                        }
                    } else {
                        TODO()
                        // Block of code for AI players
                    }
                }
            }
        }
    }

    /**
     * This function could be used to affect the status of any player. If the current player is trying to pawn a
     * property then no argument is necessary. Otherwise, the player number should be passed as an argument.
     */
    fun askAboutPawningProperty(playerNumber: Int = currentPlayer.number!!) {
        val currentPlayerWantsToPawn = playerNumber == currentPlayer.number
        val player = if (!currentPlayerWantsToPawn) playerManager.getPlayerCopy(playerNumber) else null
        val pawnablePropertyInfo = board.getPawnablePropertyInfo(playerNumber)
        val infoString = pawnablePropertyInfo[0]
        val validPropertyPositions = pawnablePropertyInfo[1] as Set<*>

        while (true) {
            println("$infoString\nEnter the position of the property you would like to pawn or enter \"b\" to go back")
            val playerInput = readLine()!!.toLowerCase()
            if (playerInput == "b") {
                return
            }
            val propertyPosition = playerInput.toIntOrNull()
            if (propertyPosition == null || propertyPosition !in validPropertyPositions) {
                println("Invalid input")
            } else {
                val pawnPrice = board.getProperty(propertyPosition).pawnPrice
                bank.money -= pawnPrice
                if (currentPlayerWantsToPawn) currentPlayer.money += pawnPrice
                else {
                    if (player == null) throw Exception("Player should not be null but is")
                    player.money += pawnPrice
                    playerManager.updatePlayer(player)
                }
                board.pawnProperty(propertyPosition)
                return
            }
        }
    }

    /**
     * This function should only be used to change the status of the current player.
     */
    fun askAboutUnpawningProperty() {
        val unpawnablePropertyInfo = board.getUnpawnablePropertyInfo(currentPlayer.number!!)
        val infoString = unpawnablePropertyInfo[0]
        val validPropertyPositions = unpawnablePropertyInfo[1] as Set<*>
        println(
            "$infoString\nEnter the position of the property you would like " +
                    "to unpawn or enter \"b\" to go back"
        )
        while (true) {
            val playerInput = readLine()!!.toLowerCase()
            if (playerInput == "b") {
                return
            }
            val propertyPosition = playerInput.toIntOrNull()
            if (propertyPosition == null || propertyPosition !in validPropertyPositions) {
                println("Invalid input")
            } else {
                val unpawnPrice = board.getProperty(propertyPosition).unpawnPrice
                currentPlayer.money -= unpawnPrice
                bank.money += unpawnPrice
                board.unpawnProperty(propertyPosition)
                return
            }
        }
    }

    /**
     * This function should only be used to change the current player
     */
    fun askAboutAddingRestaurant() {
        val restaurantAdditionInfo = board.getRestaurantAdditionInfo(currentPlayer.number!!)
        val infoString = restaurantAdditionInfo[0]
        val validStreetPositions = restaurantAdditionInfo[1] as Set<*>

        while (true) {
            println(
                "$infoString\nEnter the position of the street you would like to add a restaurant to or enter " +
                        "\"b\" go back"
            )
            val input = readLine()!!.toLowerCase()
            if (input == "b") {
                return
            }
            val streetPosition = input.toIntOrNull()
            if (streetPosition == null || streetPosition !in validStreetPositions) {
                println("Invalid input")
            } else {
                val restaurantAddingFee = board.getStreet(streetPosition).restaurantAddPrice
                if (currentPlayer.money >= restaurantAddingFee) {
                    currentPlayer.money -= restaurantAddingFee
                    board.addRestaurantToStreet(streetPosition)
                } else {
                    // Not enough money
                    println("Not enough money")
                }
                return
            }
        }
    }

    /**
     * This function could be used to affect the status of any player. If the current player is trying to remove a
     * restaurant then no argument is necessary. Otherwise, the player number should be passed as an argument.
     */
    fun askAboutRemovingRestaurant(playerNumber: Int = currentPlayer.number!!) {
        val currentPlayerWantsToRemoveRestaurant = playerNumber == currentPlayer.number
        val restaurantRemovalInfo = board.getRestaurantRemovalInfo(playerNumber)
        val infoString = restaurantRemovalInfo[0]
        val validStreetNumbers = restaurantRemovalInfo[1] as Set<*>
        println(infoString)
        while (true) {
            println(
                "Enter the position of the street you would like to remove a restaurant from or enter " +
                        "\"b\" to go back"
            )
            val input = readLine()!!.toLowerCase()
            if (input == "b") {
                return
            }
            val streetPosition = input.toIntOrNull()
            if (streetPosition == null || streetPosition !in validStreetNumbers) {
                println("Invalid input")
            } else {
                board.removeRestaurantFromStreet(streetPosition)
                val moneyGained = board.getStreet(streetPosition).restaurantRemoveGain
                bank.money -= moneyGained
                if (currentPlayerWantsToRemoveRestaurant) {
                    currentPlayer.money += moneyGained
                } else {
                    val player = playerManager.getPlayerCopy(playerNumber)
                    player.money += moneyGained
                    playerManager.updatePlayer(player)
                }
                return
            }
        }
    }

    /**
     * This function might be used to allow any player to try to make a trade. If the current player is trying to make
     * a trade then no argument is necessary. Otherwise, the number of the player that wants to make a trade
     * should be passed in. The initiatingPlayerNeedsMoney should be set to true if this function was called as
     * a result of a player needing money.
     * @param initiatingPlayerNumber
     */
    fun askAboutTrading(
        initiatingPlayerNumber: Int = currentPlayer.number!!,
        initiatingPlayerNeedsMoney: Boolean = false
    ) {
        val currentPlayerWantsToTrade = initiatingPlayerNumber == currentPlayer.number
        val initiatingPlayer: Player? =
            if (currentPlayerWantsToTrade) playerManager.getPlayerCopy(initiatingPlayerNumber) else null

        val initiatingPlayerName = if (currentPlayerWantsToTrade) currentPlayer.name
        else initiatingPlayer!!.name

        val validPlayerNumbers = playerManager.getNumbersOfOtherPlayersInGame(initiatingPlayerNumber)
        println("The valid player numbers are ${validPlayerNumbers.joinToString(", ")}")

        mainLoop@ while (true) {
            println("You are at the beginning of the trade process")
            val otherPlayer: Player
            inputValidation1@ while (true) {
                println(
                    """
                    Enter one of the following and press enter:
                    "i" to see property info
                    "p" to see player numbers
                    "c" to cancel the trade
                    The number of the player you would like to make a trade with
                """.trimIndent()
                )
                when (val input = readLine()!!.toLowerCase()) {
                    "i" -> {
                        board.displayPropertyInfo()
                    }

                    "p" -> {
                        playerManager.displayNumbers()
                    }

                    "c" -> return

                    else -> {
                        val otherPlayerNumber = input.toIntOrNull()
                        if (otherPlayerNumber == null || otherPlayerNumber !in validPlayerNumbers) {
                            println("Invalid input")
                        } else {
                            otherPlayer = playerManager.getPlayerCopy(otherPlayerNumber)
                            break@inputValidation1
                        }
                    }
                }
            }

            val initiatingPlayerHasProperty = board.playerHasAProperty(initiatingPlayerNumber)
            val initiatingPlayerHasGetOffVacationCard =
                if (currentPlayerWantsToTrade) currentPlayer.hasAGetOffVacationCard
                else initiatingPlayer!!.hasAGetOffVacationCard

            val otherPlayerHasAProperty = board.playerHasAProperty(otherPlayer.number!!)
            val otherPlayerHasGetOffVacationCard = otherPlayer.hasAGetOffVacationCard

            if (!initiatingPlayerHasProperty && !initiatingPlayerHasGetOffVacationCard &&
                !otherPlayerHasAProperty && !otherPlayerHasGetOffVacationCard
            ) {
                println(
                    "There is nothing that these 2 players can trade with each other besides money." +
                            "The trade process will be restarted."
                )
                continue@mainLoop
            }

            val whatPlayerWants = mutableMapOf<String, Any>()
            selectWhatIsWanted@ while (true) {
                inputValidation1@ while (true) {
                    val otherPlayerMoney = otherPlayer.money
                    if (!initiatingPlayerNeedsMoney) {
                        print("Would you like any money from this person? (y/n): ")
                        when (readLine()!!.toLowerCase()) {
                            "y" -> {
                            }
                            "n" -> {
                                break@inputValidation1
                            }
                            else -> {
                                println("Invalid input")
                                continue@inputValidation1
                            }
                        }
                    }
                    inputValidation2@ while (true) {
                        println("This person has $$otherPlayerMoney. Enter an amount or enter \"b\" to go back:")
                        if (initiatingPlayerNeedsMoney) {
                            println("You need money so entering an amount is mandatory")
                        }
                        val input = readLine()!!.toLowerCase()
                        if (input == "b") {
                            break@inputValidation2
                        }
                        val moneyAmount = input.toIntOrNull()
                        if (moneyAmount == null || moneyAmount !in 0..otherPlayerMoney ||
                            (initiatingPlayerNeedsMoney && moneyAmount == 0)) {
                            println("Invalid input")
                        } else {
                            if (moneyAmount != 0) {
                                whatPlayerWants["money"] = moneyAmount
                            }
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
                                val otherPlayerPropertyInfo = board.getPropertyInfo(otherPlayer.number!!)
                                val infoString = otherPlayerPropertyInfo[0]
                                val validPropertyPositions = otherPlayerPropertyInfo[1] as Set<*>
                                println(infoString)
                                inputValidation2@ while (true) {
                                    println(
                                        "Enter the positions of all the properties you would like with each position " +
                                                "separated by a space or enter \"b\" to go back:"
                                    )
                                    input = readLine()!!.toLowerCase()
                                    if (input == "b") {
                                        continue@inputValidation1
                                    }
                                    val positionsEntered = input.split(" ")
                                    val positionsOfPropertiesWanted = mutableListOf<Int>()

                                    for (positionStr in positionsEntered) {
                                        val positionInt = positionStr.toIntOrNull()
                                        if (positionInt == null || positionInt !in validPropertyPositions) {
                                            println("Invalid input")
                                            continue@inputValidation2
                                        }

                                        // Avoid duplicates
                                        if (positionInt !in positionsOfPropertiesWanted) {
                                            positionsOfPropertiesWanted.add(positionInt)
                                        }
                                    }

                                    if (positionsOfPropertiesWanted.isNotEmpty()) {
                                        whatPlayerWants["properties"] = positionsOfPropertiesWanted
                                    }
                                    break@inputValidation1
                                }
                            }

                            "n" -> {
                                break@inputValidation1
                            }

                            else -> {
                                println("Invalid input")
                            }
                        }
                    }
                }

                if (otherPlayerHasGetOffVacationCard) {
                    inputValidation1@ while (true) {
                        print("Would you like any get off vacation cards? (y/n): ")
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                val numberOfGetOffVacationCards =
                                    otherPlayer.numberOfGetOffVacationCardsOwned
                                inputValidation2@ while (true) {
                                    println(
                                        "This player has $numberOfGetOffVacationCards get off vacation cards. How " +
                                                "many would you like? Enter an amount or \"b\" to go back:"
                                    )
                                    input = readLine()!!
                                    if (input == "b") {
                                        continue@inputValidation1
                                    }
                                    val amount = input.toIntOrNull()
                                    if (amount == null || amount !in 0..numberOfGetOffVacationCards) {
                                        println("Invalid input")
                                    } else {
                                        if (amount != 0) {
                                            whatPlayerWants["get off vacation cards"] = amount
                                        }
                                        break@inputValidation1
                                    }
                                }
                            }

                            "n" -> break@inputValidation1

                            else -> println("Invalid input")
                        }
                    }
                }

                if (whatPlayerWants.isEmpty()) {
                    println(
                        "$initiatingPlayerName, you didn't select anything for what you want. " +
                                "You must select something."
                    )
                } else {
                    break@selectWhatIsWanted
                }
            }

            // Part for having the initiating player select what they offer
            val whatPlayerOffers = mutableMapOf<String, Any>()
            selectWhatToOffer@ while (true) {
                inputValidation1@ while (true) {

                    // Only give the initiating player the choice of offering money if they are not in need of money.
                    if (!initiatingPlayerNeedsMoney) {
                        print("Would you like to offer this person any money? ")
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                val initiatingPlayerMoney = if (currentPlayerWantsToTrade) currentPlayer.money
                                else initiatingPlayer!!.money
                                inputValidation2@ while (true) {
                                    print("You have $initiatingPlayerMoney, how much of it would you like to offer? ")
                                    input = readLine()!!
                                    val moneyAmount = input.toIntOrNull()
                                    if (moneyAmount == null || moneyAmount !in 0..initiatingPlayerMoney) {
                                        println("Invalid input")
                                    } else {
                                        if (moneyAmount != 0) {
                                            whatPlayerOffers["money"] = moneyAmount
                                        }
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
                                val initiatingPlayerPropertyInfo = board.getPropertyInfo(initiatingPlayerNumber)
                                val infoString = initiatingPlayerPropertyInfo[0]
                                val validPropertyPositions = initiatingPlayerPropertyInfo[1] as Set<*>

                                println(infoString)
                                inputValidation2@ while (true) {
                                    println(
                                        "Enter the positions of the properties you would like to offer with each position " +
                                                "separated by a single space or enter \"b\" to go back: "
                                    )
                                    input = readLine()!!.toLowerCase()
                                    if (input == "b") {
                                        continue@inputValidation1
                                    }
                                    val positionsEntered = input.split(" ")
                                    val positionsOfPropertiesOffered = mutableListOf<Int>()

                                    for (positionStr in positionsEntered) {
                                        val positionInt = positionStr.toIntOrNull()
                                        if (positionInt == null || positionInt !in validPropertyPositions) {
                                            println("Invalid input")
                                            continue@inputValidation2
                                        }

                                        // Avoid duplicates
                                        if (positionInt !in positionsOfPropertiesOffered) {
                                            positionsOfPropertiesOffered.add(positionInt)
                                        }
                                    }

                                    if (positionsOfPropertiesOffered.isNotEmpty()) {
                                        whatPlayerOffers["properties"] = positionsOfPropertiesOffered
                                    }
                                    break@inputValidation1
                                }
                            }

                            "n" -> {
                                break@inputValidation1
                            }

                            else -> {
                                println("Invalid input")
                            }
                        }
                    }
                }

                if (initiatingPlayerHasGetOffVacationCard) {
                    inputValidation1@ while (true) {
                        print("Would you like to offer any get off vacation cards? (y/n) ")
                        var input = readLine()!!.toLowerCase()
                        when (input) {
                            "y" -> {
                                val numberOfGetOffVacationCards =
                                    if (currentPlayerWantsToTrade) currentPlayer.numberOfGetOffVacationCardsOwned
                                    else initiatingPlayer!!.numberOfGetOffVacationCardsOwned
                                inputValidation2@ while (true) {
                                    println(
                                        "You have $numberOfGetOffVacationCards get off vacation cards. How " +
                                                "many would you like to offer? Enter an amount or \"b\" to go back"
                                    )
                                    input = readLine()!!.toLowerCase()
                                    if (input == "b") {
                                        continue@inputValidation1
                                    }
                                    val amount = input.toIntOrNull()
                                    if (amount == null || amount !in 0..numberOfGetOffVacationCards) {
                                        println("Invalid input")
                                    } else {
                                        if (amount != 0) {
                                            whatPlayerWants["get off vacation cards"] = amount
                                        }
                                        break@inputValidation1
                                    }
                                }
                            }

                            "n" -> {
                                break@inputValidation1
                            }

                            else -> println("Invalid input")
                        }
                    }
                }

                if (whatPlayerOffers.isEmpty()) {
                    println(
                        "$initiatingPlayerName, you didn't offer anything. You must select something to offer."
                    )
                } else break@selectWhatToOffer
            }

            if (whatPlayerWants.size == 1 && whatPlayerWants.containsKey("money")
                && whatPlayerOffers.size == 1 && whatPlayerOffers.containsKey("money")
            ) {
                println(
                    "$initiatingPlayerName, you are trying to trade money for money, which is pointless. " +
                            "You must restart the trade process."
                )
                continue@mainLoop
            }

            // Section for initiating player to confirm their offer
            confirmTrade@ while (true) {
                println("\n$initiatingPlayerName, you have chosen to offer ${otherPlayer.name}:")
                if (whatPlayerOffers.containsKey("money")) {
                    val moneyAmount = whatPlayerOffers["money"] as Int
                    println("$$moneyAmount")
                }

                if (whatPlayerOffers.containsKey("properties")) {
                    val propertyPositions = whatPlayerOffers["properties"] as List<*>
                    for (propertyPosition in propertyPositions) {
                        if (propertyPosition !is Int) {
                            throw Exception(
                                "The \"properties\" key of the whatPlayerOffers map should contain a list of " +
                                        "Ints but apparently it doesn't"
                            )
                        }
                        val property = board.getProperty(propertyPosition)
                        println(property.name)
                        if (property is Street && property.neighborhoodOwnedBySinglePlayer) {
                            println(
                                "Note: the above property is a street that is in a neighborhood that is currently " +
                                        "owned by $initiatingPlayerName.\nIf they agree to the trade, that neighborhood " +
                                        "will no longer be owned by only them so all houses will be sold and the fees " +
                                        "will go back to the starting fees."
                            )
                        }
                    }
                }

                if (whatPlayerOffers.containsKey("get off vacation card")) {
                    val numberOfGetOffVacationCards = whatPlayerOffers["get off vacation card"] as Int
                    println(
                        if (numberOfGetOffVacationCards == 1) "A get off vacation card"
                        else "$numberOfGetOffVacationCards get off vacation cards"
                    )
                }

                println("In exchange for:")

                if (whatPlayerWants.containsKey("money")) {
                    val moneyAmount = whatPlayerWants["money"] as Int
                    println("$$moneyAmount")
                }

                if (whatPlayerWants.containsKey("properties")) {
                    val propertyPositionsList = whatPlayerWants["properties"] as List<*>
                    for (propertyPosition in propertyPositionsList) {
                        if (propertyPosition !is Int) {
                            throw Exception(
                                "The \"properties\" key of the whatPlayerWants map be mapped to a list of Ints " +
                                        "for it's value but apparently is not"
                            )
                        }
                        val property = board.getProperty(propertyPosition)
                        println(property.name)
                        if (property is Street && property.neighborhoodOwnedBySinglePlayer) {
                            println(
                                "Note: the above property is a street that is in a neighborhood that is currently " +
                                        "owned by ${otherPlayer.name}.\nIf they agree to the trade, that neighborhood " +
                                        "will no longer be owned by only them so all houses will be sold and the fees " +
                                        "will go back to the starting fees."
                            )
                        }
                    }
                }

                if (whatPlayerWants.containsKey("get off vacation card")) {
                    val numberOfGetOffVacationCards = whatPlayerWants["get off vacation card"] as Int
                    println(
                        if (numberOfGetOffVacationCards == 1) "A get off vacation card"
                        else "$numberOfGetOffVacationCards get off vacation cards"
                    )
                }

                println(
                    """
                        
                    $initiatingPlayerName, are you sure you want to make this offer to ${otherPlayer.name}?
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

            acceptOrDenyTrade@ while (true) {
                println("${otherPlayer.name}, do you accept the offer that was just confirmed by $initiatingPlayerName?")
                when (readLine()!!.toLowerCase()) {
                    "y" -> {
                        if (whatPlayerOffers.containsKey("money")) {
                            val moneyAmount = whatPlayerOffers["money"] as Int
                            if (currentPlayerWantsToTrade) currentPlayer.money -= moneyAmount
                            else initiatingPlayer!!.money -= moneyAmount
                            otherPlayer.money += moneyAmount
                        }

                        if (whatPlayerOffers.containsKey("properties")) {
                            val positionsOfPropertiesOffered = whatPlayerOffers["properties"] as List<*>

                            val propertiesActedUpon = mutableMapOf<Int, Boolean>()
                            for (position in positionsOfPropertiesOffered) {
                                if (position !is Int) {
                                    throw Exception(
                                        "The \"properties\" key of the whatPlayerOffers map be mapped to a list " +
                                                "of Ints for it's value but apparently is not"
                                    )
                                }
                                propertiesActedUpon[position] = false
                            }

                            for (position in positionsOfPropertiesOffered) {
                                if (position !is Int) {
                                    throw Exception(
                                        "The \"properties\" key of the whatPlayerOffers map be mapped to a list " +
                                                "of Ints for it's value but apparently is not"
                                    )
                                }

                                if (propertiesActedUpon[position]!! == true) {
                                    // Skip over properties already acted upon from earlier iterations. This will
                                    // happen for streets that were in the same neighborhood as streets in earlier
                                    // iterations.
                                    continue
                                }

                                board.setPropertyOwner(
                                    position,
                                    otherPlayer.number!!,
                                    otherPlayer.name
                                )

                                when (val property = board.getProperty(position)) {
                                    is Street -> {
                                        if (property.neighborhoodOwnedBySinglePlayer) {
                                            val numberOfNeighborhoodRestaurants =
                                                board.getNeighborhoodRestaurantCount(position)
                                            if (numberOfNeighborhoodRestaurants > 0) {
                                                // Sell all restaurants in the neighborhood
                                                board.removeRestaurantsFromNeighborhood(position)
                                                val moneyEarned = numberOfNeighborhoodRestaurants * property.restaurantRemoveGain
                                                bank.money -= moneyEarned
                                                if (currentPlayerWantsToTrade) currentPlayer.money += moneyEarned
                                                else initiatingPlayer!!.money += moneyEarned
                                            }
                                        }

                                        // Check if the neighbors of this street were part of the trade agreement.
                                        // If they are, change the ownership of them before checking for neighborhood
                                        // changes.
                                        for (neighborPosition in arrayOf(
                                            property.neighbor1Position,
                                            property.neighbor2Position)
                                        ) {
                                            if (neighborPosition in positionsOfPropertiesOffered) {
                                                board.setPropertyOwner(
                                                    neighborPosition,
                                                    otherPlayer.number!!,
                                                    otherPlayer.name
                                                )
                                                // Make it so this property gets skipped over in future iterations since
                                                // the work for it was just done.
                                                propertiesActedUpon[neighborPosition] = true
                                            }
                                        }

                                        board.checkForNeighborhoodChanges(position)
                                    }

                                    is GolfCourse -> {
                                        if (currentPlayerWantsToTrade) currentPlayer.removeGolfCourse()
                                        else initiatingPlayer!!.removeGolfCourse()
                                        otherPlayer.addGolfCourse()
                                    }

                                    is SuperStore -> {
                                        if (currentPlayerWantsToTrade) currentPlayer.removeSuperStore()
                                        else initiatingPlayer!!.removeSuperStore()
                                        otherPlayer.addSuperStore()
                                    }
                                }
                            }
                        }

                        if (whatPlayerOffers.containsKey("get off vacation card")) {
                            val numberOfGetOffVacationCards = whatPlayerOffers["get off vacation card"] as Int
                            for (i in 0 until numberOfGetOffVacationCards) {
                                if (currentPlayerWantsToTrade) currentPlayer.removeGetOffVacationCard()
                                else initiatingPlayer!!.removeGetOffVacationCard()
                                otherPlayer.addGetOffVacationCard()
                            }
                        }

                        if (whatPlayerWants.containsKey("money")) {
                            val moneyAmount = whatPlayerWants["money"] as Int
                            otherPlayer.money -= moneyAmount
                            if (currentPlayerWantsToTrade) currentPlayer.money += moneyAmount
                            else initiatingPlayer!!.money += moneyAmount
                        }

                        if (whatPlayerWants.containsKey("properties")) {
                            val positionsOfPropertiesWanted = whatPlayerWants["properties"] as List<*>
                            val propertiesActedUpon = mutableMapOf<Int, Boolean>()

                            for (position in positionsOfPropertiesWanted) {
                                if (position !is Int) {
                                    throw Exception(
                                        "The \"properties\" key of the whatPlayerOffers map be mapped to a list " +
                                                "of Ints for it's value but apparently is not"
                                    )
                                }
                                propertiesActedUpon[position] = false
                            }

                            for (position in positionsOfPropertiesWanted) {
                                if (position !is Int) {
                                    throw Exception(
                                        "The \"properties\" key of the whatPlayerWants map be mapped to a list " +
                                                "of Ints for it's value but apparently is not"
                                    )
                                }

                                board.setPropertyOwner(
                                    position,
                                    initiatingPlayerNumber,
                                    initiatingPlayerName
                                )

                                when (val property = board.getProperty(position)) {
                                    is Street -> {
                                        if (property.neighborhoodOwnedBySinglePlayer) {
                                            val numberOfNeighborhoodRestaurants =
                                                board.getNeighborhoodRestaurantCount(position)
                                            if (numberOfNeighborhoodRestaurants > 0) {
                                                // Sell all restaurants in the neighborhood
                                                board.removeRestaurantsFromNeighborhood(position)
                                                val moneyEarned = numberOfNeighborhoodRestaurants * property.restaurantRemoveGain
                                                bank.money -= moneyEarned
                                                if (currentPlayerWantsToTrade) currentPlayer.money += moneyEarned
                                                else initiatingPlayer!!.money += moneyEarned
                                            }
                                        }

                                        // Check if the neighbors of this street were part of the trade agreement.
                                        // If they are, change the ownership of them before checking for neighborhood
                                        // changes.
                                        for (neighborPosition in arrayOf(
                                            property.neighbor1Position,
                                            property.neighbor2Position)
                                        ) {
                                            if (neighborPosition in positionsOfPropertiesWanted) {
                                                board.setPropertyOwner(
                                                    neighborPosition,
                                                    otherPlayer.number!!,
                                                    otherPlayer.name
                                                )
                                                // Make it so this property gets skipped over in future iterations since
                                                // the work for it was just done.
                                                propertiesActedUpon[neighborPosition] = true
                                            }
                                        }

                                        board.checkForNeighborhoodChanges(position)
                                    }

                                    is GolfCourse -> {
                                        otherPlayer.removeGolfCourse()
                                        if (currentPlayerWantsToTrade) currentPlayer.addGolfCourse()
                                        else initiatingPlayer!!.addGolfCourse()
                                    }

                                    is SuperStore -> {
                                        otherPlayer.removeSuperStore()
                                        if (currentPlayerWantsToTrade) currentPlayer.addSuperStore()
                                        else initiatingPlayer!!.addSuperStore()
                                    }
                                }
                            }
                        }

                        if (whatPlayerWants.containsKey("get off vacation card")) {
                            val numberOfGetOffVacationCards = whatPlayerWants["get off vacation card"] as Int
                            for (i in 0 until numberOfGetOffVacationCards) {
                                otherPlayer.removeGetOffVacationCard()
                                if (currentPlayerWantsToTrade) currentPlayer.addGetOffVacationCard()
                                else initiatingPlayer!!.addGetOffVacationCard()
                            }
                        }

                        playerManager.updatePlayer(otherPlayer)
                        if (!currentPlayerWantsToTrade) playerManager.updatePlayer(initiatingPlayer!!)
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

    fun playerNeedsMoney(playerNumber: Int = currentPlayer.number!!, moneyNeeded: Int) {
        val currentPlayerNeedsMoney = playerNumber == currentPlayer.number

        val playerCanPawn = board.playerCanPawnProperty(playerNumber)

        inputValidation@ while (true) {
            println(
                """
                You need $moneyNeeded. Would you like to
                1: Drop out of the game
                2: Make a trade with a player
            """.trimIndent()
            )
            if (playerCanPawn) {
                println("3: Pawn one of your properties")
            }
            when (readLine()) {
                "1" -> {
                    if (currentPlayerNeedsMoney) {
                        currentPlayer.removeFromGame()
                    } else {
                        val player = playerManager.getPlayerCopy(playerNumber)
                        player.removeFromGame()
                        playerManager.updatePlayer(player)
                    }
                    if (playerManager.onePlayerIsLeftInGame()) {
                        gameOver = true
                    }
                    return
                }

                "2" -> {
                    askAboutTrading(playerNumber, true)
                }

                "3" -> {
                    if (playerCanPawn) {
                        askAboutPawningProperty(playerNumber)
                    } else {
                        println("Invalid input")
                        continue@inputValidation
                    }
                }

                else -> {
                    println("Invalid input")
                    continue@inputValidation
                }
            }
            break@inputValidation
        }
    }
}
