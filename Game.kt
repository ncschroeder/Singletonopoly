package commandline

import java.lang.Exception
import java.lang.NumberFormatException
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

    init {
        play()
    }

    fun play() {
        var goAgain = false
        var numberOfDoubleRolls = 0

        fun endTurn() {
            if (goAgain) goAgain = false
            else {
                playerManager.updatePlayer(currentPlayer)
                playerManager.switchToNextPlayer()
                currentPlayer = playerManager.currentPlayerCopy
            }
            println("End of turn. Press enter to continue.")
            readLine()
        }

        gameLoop@ while (true) {
            println("It's ${currentPlayer.name}'s turn")
//            println("They are a " + if (currentPlayer is HumanPlayer) "Human Player" else "AI Player")

            val possiblePropertyActions = board.getPossiblePropertyActions(currentPlayer.number)
            val playerCanAddRestaurant = possiblePropertyActions["add restaurant"]
            val playerCanRemoveRestaurant = possiblePropertyActions["remove restaurant"]
            val playerCanPawn = possiblePropertyActions["pawn"]
            val playerCanUnpawn = possiblePropertyActions["unpawn"]

            if (currentPlayer is HumanPlayer) {
                preRollAction@ while (true) {
                    println(
                        """
    ${currentPlayer.name}, type one of the following and press enter:
    nothing to take your turn
    i: View property info
    p: View player positions
    t: Make a trade with another player
    """.trimIndent()
                    )
                    // None of these should be null so an exception should never be thrown.
                    if (playerCanAddRestaurant!!) println("ar: Add a restaurant to one of your properties")
                    if (playerCanRemoveRestaurant!!) println("rr: Remove a restaurant from one of your properties")
                    if (playerCanPawn!!) println("pa: Pawn one of your properties")
                    if (playerCanUnpawn!!) println("u: Unpawn one of your properties")

                    when (readLine()!!.toLowerCase()) {
                        "" -> break@preRollAction

                        "i" -> board.displayPropertyInfo()

                        "p" -> {
                            println("Positions")
                            playerManager.displayPositions()
                        }

                        "t" -> askAboutTrading()

                        "ar" -> {
                            if (playerCanAddRestaurant) askAboutAddingRestaurant()
                            else println("Invalid input")
                        }

                        "rr" -> {
                            if (playerCanRemoveRestaurant) askAboutRemovingRestaurant()
                            else println("Invalid input")
                        }

                        "pa" -> {
                            if (playerCanPawn) askAboutPawningProperty()
                            else println("Invalid input")
                        }

                        "u" -> {
                            if (playerCanUnpawn) askAboutUnpawningProperty()
                            else println("Invalid input")
                        }

                        else -> println("Invalid input")
                    }
                }
            } else {
                // Block of code for AI players
                TODO()
            }

            if (currentPlayer.isOnVacation) {
                if (currentPlayer is HumanPlayer) {
                    print(
                        "You're on vacation and this is your " +
                                when (currentPlayer.numberOfTurnsOnVacation) {
                                    0 -> "first"
                                    1 -> "second"
                                    2 -> "third (last)"
                                    else -> throw Exception("Shouldn't be on vacation at this point")
                                }
                                + " turn on vacation, would you like to (1) pay $50 to get out or (2) try to roll doubles? "
                    )
                    if (currentPlayer.hasAGetOffVacationCard) println("or (3) use a get off vacation card")
                    else println()
                    vacationInputValidation@ while (true) {
                        when (readLine()) {
                            "1" -> {
                                if (currentPlayer.money >= 50) {
                                    currentPlayer.money -= 50
                                    bank.money += 50
                                    break@vacationInputValidation
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
                                } else currentPlayer.continueVacation()
                                endTurn()
                                continue@gameLoop
                            }
                            "3" -> {
                                if (currentPlayer.hasAGetOffVacationCard) {
                                    println("${currentPlayer.name} has chosen to use a get off vacation card")
                                    currentPlayer.removeGetOffVacationCard()
                                    entropyDeck.insertGetOffVacationCardAtBottom()
                                    break@vacationInputValidation
                                } else println("Invalid input")
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
                            endTurn()
                            continue@gameLoop
                        }
                    }
                }
            }
            val diceRoll1 = getDiceRoll()
            val diceRoll2 = getDiceRoll()
            println("${currentPlayer.name} rolled a $diceRoll1 and a $diceRoll2")
            if (diceRoll1 == diceRoll2) {
                numberOfDoubleRolls++
                if (numberOfDoubleRolls == 3) {
                    println("${currentPlayer.name} rolled doubles 3 times in a row so they go to vacation")
                    numberOfDoubleRolls = 0
                    currentPlayer.sendToVacation(board.vacationPosition)
                    endTurn()
                    continue
                }
                println("${currentPlayer.name} rolled doubles so they get to go again")
                goAgain = true
            } else if (numberOfDoubleRolls > 0) numberOfDoubleRolls = 0

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
            } else currentPlayer.position += (diceRoll1 + diceRoll2)
            evaluatePosition()
            endTurn()
        }
    }

    fun getDiceRoll() = Random.nextInt(1, 7)

    fun evaluatePosition() {
        print("${currentPlayer.name} has landed on position ${currentPlayer.position} which is ")
        when (val currentBoardSpace = board.getBoardSpace(currentPlayer.position)) {
            is String -> {
                println("\"$currentBoardSpace\".")
                when (currentBoardSpace) {
                    "Start" -> {
                        println("This space has no effect")
                    }

                    "Go On Vacation" -> currentPlayer.sendToVacation(board.vacationPosition)

                    "Draw Entropy Card" -> {
                        // The top card should be moved to the bottom for most situations so this variable will
                        // be set true. For the situations where the top card does not get moved to the bottom,
                        // this variable will be changed to false.
                        var topCardShouldBeMovedToBottom = true

                        println("You draw a card from the entropy deck and it says \"${entropyDeck.topCard.message}\"")
                        when (entropyDeck.topCard.type) {
                            "bank to player" -> {
                                bank.money -= entropyDeck.topCard.value!!
                                currentPlayer.money += entropyDeck.topCard.value!!
                            }

                            "player to bank" -> {
                                currentPlayer.money -= entropyDeck.topCard.value!!
                                bank.money += entropyDeck.topCard.value!!
                            }

                            "other players to player" -> {
                                val moneyReceivedFromEachPlayer = entropyDeck.topCard.value!!
                                for (playerNumber in playerManager.getNumbersOfOtherPlayersInGame(currentPlayer.number)) {
                                    while (true) {
                                        val player = playerManager.getPlayerCopy(playerNumber)
                                        if (player.money >= moneyReceivedFromEachPlayer) {
                                            player.money -= moneyReceivedFromEachPlayer
                                            currentPlayer.money += moneyReceivedFromEachPlayer
                                            playerManager.updatePlayer(player)
                                            break
                                        }
                                        playerNeedsMoney(playerNumber, moneyReceivedFromEachPlayer - player.money)
                                        if (!playerManager.getPlayerCopy(playerNumber).isInGame) break
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
                                        for (playerNumber in playerManager.getNumbersOfOtherPlayersInGame(currentPlayer.number)) {
                                            val player = playerManager.getPlayerCopy(playerNumber)
                                            player.money += moneyOwedToEachPlayer
                                            playerManager.updatePlayer(player)
                                        }
                                        break
                                    }
                                    playerNeedsMoney(moneyNeeded = totalMoneyOwed - currentPlayer.money)
                                    if (!currentPlayer.isInGame) {
                                        // This might be true depending on actions of the player in
                                        // the playerNeedsMoney function.
                                        break
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
                                val maintenanceFee = board.getMaintenancePrice(currentPlayer.number)
                                while (currentPlayer.money < maintenanceFee) {
                                    playerNeedsMoney(moneyNeeded = maintenanceFee - currentPlayer.money)
                                    if (!currentPlayer.isInGame) {
                                        // This should be true if the player decided to drop out of the game
                                        // in the playerNeedsMoney function.
                                        return
                                    }
                                }
                                currentPlayer.money -= maintenanceFee
                                bank.money += maintenanceFee
                            }
                        }
                        if (topCardShouldBeMovedToBottom) entropyDeck.moveTopCardToBottom()
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
                    if (currentBoardSpace.ownerNumber == currentPlayer.number) println("by them")
                    else {
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
                                                when (currentBoardSpace.numRestaurants) {
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
                                        playerManager.getPlayerCopy(currentBoardSpace.ownerNumber).numberOfGolfCoursesOwned
                                    fee = GolfCourse.getFee(numGolfCoursesOwned)
                                    println(
                                        "$ownerName owns $numGolfCoursesOwned golf courses and the fee is $fee"
                                    )
                                }
                                is SuperStore -> {
                                    val numberOfSuperStoresOwned =
                                        playerManager.getPlayerCopy(currentBoardSpace.ownerNumber).numberOfSuperStoresOwned
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
                                    val otherPlayer = playerManager.getPlayerCopy(currentBoardSpace.ownerNumber)
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
                                    board.setPropertyOwnerAndCheckForChanges(
                                        currentPlayer.position,
                                        currentPlayer.number,
                                        currentPlayer.name
                                    )
                                    currentPlayer.money -= currentBoardSpace.purchasePrice
                                    bank.money += currentBoardSpace.purchasePrice
                                    when (currentBoardSpace) {
                                        is Street -> {
                                            if (currentBoardSpace.neighborhoodOwnedBySinglePlayer) {
                                                // This will be true if the player just bought a street and it turns
                                                // out that that player owns all streets in that neighborhood
                                                print("Would you like to add any restaurants? ")
                                            }
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
    fun askAboutPawningProperty(playerNumber: Int = currentPlayer.number) {
        val currentPlayerWantsToPawn = playerNumber == currentPlayer.number
        var player: Player? = null
        if (!currentPlayerWantsToPawn) player = playerManager.getPlayerCopy(playerNumber)
        val pawnableProperties = board.getPawnableProperties(playerNumber)
        val validPropertyNumbers = mutableSetOf<Int>()
        for (property in pawnableProperties) {
            validPropertyNumbers.add(property.position)
            val propertyType = when (property) {
                is Street -> "Street"
                is GolfCourse -> "Golf Course"
                is SuperStore -> "Super Store"
                else -> throw IllegalArgumentException(
                    "Position ${property.position} is not a street, golf " +
                            "course nor super store so it's not a property " +
                            "but it's supposed to be"
                )
            }
            println(
                """
                Position: ${property.position}
                Type: $propertyType
                Name: ${property.name}
                Pawn price: ${property.pawnPrice}
            """.trimIndent()
            )
        }
        println("Enter the position of the property you would like to pawn or enter \"b\" to go back")
        while (true) {
            val playerInput = readLine()!!
            if (playerInput.equals("b", true)) return
            val propertyPosition: Int
            try {
                propertyPosition = playerInput.toInt()
            } catch (e: NumberFormatException) {
                println("Invalid input")
                continue
            }
            if (propertyPosition in validPropertyNumbers) {
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
            println("Invalid input")
        }
    }

    /**
     * This function should only be used to change the status of the current player
     */
    fun askAboutUnpawningProperty() {
        val unpawnableProperties = board.getUnpawnableProperties(currentPlayer.number)
        val validPropertyPositions = mutableSetOf<Int>()
        println("Your unpawnable properties")
        for (property in unpawnableProperties) {
            validPropertyPositions.add(property.position)
            val propertyType = when (property) {
                is Street -> "Street"
                is GolfCourse -> "Golf Course"
                is SuperStore -> "Super Store"
                else -> throw Exception("Position ${property.position} doesn't contain a property but is supposed to")
            }
            println(
                """
                                    Name: ${property.name}
                                    Type: $propertyType
                                    Position: ${property.position}
                                    Unpawn Price: ${property.unpawnPrice}
                                    
                                """.trimIndent()
            )
        }
        println(
            "Enter the position of the property you would like " +
                    "to unpawn or enter \"b\" to go back"
        )
        while (true) {
            val playerInput = readLine()!!
            if (playerInput.equals("b", true)) return
            val propertyPosition: Int
            try {
                propertyPosition = playerInput.toInt()
            } catch (e: NumberFormatException) {
                println("Invalid input")
                continue
            }
            if (propertyPosition in validPropertyPositions) {
                val unpawnPrice = board.getProperty(propertyPosition).unpawnPrice
                currentPlayer.money -= unpawnPrice
                bank.money += unpawnPrice
                board.unpawnProperty(propertyPosition)
                return
            }
            println("Invalid input")
        }
    }

    /**
     * This function should only be used to change the current player
     */
    fun askAboutAddingRestaurant() {
        val streetsWhereRestaurantsCanBeAdded = board.getStreetsWhereRestaurantCanBeAdded(currentPlayer.number)
        val validStreetNumbers = mutableSetOf<Int>()
        for (street in streetsWhereRestaurantsCanBeAdded) {
            validStreetNumbers.add(street.position)
            println(
                "Position: ${street.position}, Name: ${street.name}, Neighborhood: ${street.neighborhood}, " +
                        "Restaurant adding price: ${street.restaurantAddPrice}"
            )
        }
        println(
            "Enter the position of the street you would like to add a restaurant to or enter \"b\" go back"
        )
        while (true) {
            val playerInput = readLine()!!
            if (playerInput.equals("b", true)) return
            var streetPosition: Int
            try {
                streetPosition = playerInput.toInt()
            } catch (e: NumberFormatException) {
                println("Invalid input")
                continue
            }
            if (streetPosition in validStreetNumbers) {
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
            println("Invalid input")
        }
    }

    /**
     * This function could be used to affect the status of any player. If the current player is trying to remove a
     * restaurant then no argument is necessary. Otherwise, the player number should be passed as an argument.
     */
    fun askAboutRemovingRestaurant(playerNumber: Int = currentPlayer.number) {
        val currentPlayerWantsToRemoveRestaurant = playerNumber == currentPlayer.number
        val streetsWhereRestaurantCanBeRemoved = board.getStreetsWhereRestaurantCanBeRemoved(playerNumber)
        val validStreetNumbers = mutableSetOf<Int>()
        for (street in streetsWhereRestaurantCanBeRemoved) {
            validStreetNumbers.add(street.position)
            println(
                "Position: ${street.position}, Name: ${street.name}, Neighborhood: ${street.neighborhood}, " +
                        "Restaurant removal gain: ${street.restaurantRemovePrice}"
            )
        }
        println("Enter the position of the street you would like to remove a restaurant from or enter \"b\" to go back")
        while (true) {
            val input = readLine()!!
            if (input.equals("b", true)) return
            val streetPosition: Int
            try {
                streetPosition = input.toInt()
            } catch (e: NumberFormatException) {
                println("Invalid input")
                continue
            }
            if (streetPosition !in validStreetNumbers) {
                println("Invalid input")
                continue
            }
            board.removeRestaurantFromStreet(streetPosition)
            val moneyGained = board.getStreet(streetPosition).restaurantRemovePrice
            bank.money -= moneyGained
            if (currentPlayerWantsToRemoveRestaurant) currentPlayer.money += moneyGained
            else {
                val player = playerManager.getPlayerCopy(playerNumber)
                player.money += moneyGained
                playerManager.updatePlayer(player)
            }
            return
        }
    }

    /**
     * This function might be used to allow any player to try to make a trade. If the current player is trying to make
     * a trade then no argument is necessary. Otherwise, the number of the player that wants to make a trade
     * should be passed in.
     */
    fun askAboutTrading(
        initiatingPlayerNumber: Int = currentPlayer.number,
        initiatingPlayerNeedsMoney: Boolean = false
    ) {
        val currentPlayerWantsToTrade = initiatingPlayerNumber == currentPlayer.number
        val initiatingPlayer: Player? =
            if (currentPlayerWantsToTrade) playerManager.getPlayerCopy(initiatingPlayerNumber) else null

        val initiatingPlayerName = if (currentPlayerWantsToTrade) currentPlayer.name
        else initiatingPlayer!!.name

        val validPlayerNumbers = playerManager.getNumbersOfOtherPlayersInGame(initiatingPlayerNumber)
        mainLoop@ while (true) {
            println("You are at the beginning of the trade process")
            var otherPlayerNumber: Int? = null
            inputValidation1@ while (true) {
                println(
                    """
                                                                Enter one of the following and press enter:
                                                                "i" to see property info
                                                                "p" to see player numbers
                                                                "c" to cancel the trade
                                                                the number of the player you would like to make a trade with
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
                        try {
                            otherPlayerNumber = input.toInt()
                        } catch (e: NumberFormatException) {
                            println("Invalid input")
                            continue@mainLoop
                        }
                        if (otherPlayerNumber in validPlayerNumbers) break@inputValidation1
                        println("Invalid input")
                    }
                }
            }

            val otherPlayer = playerManager.getPlayerCopy(otherPlayerNumber!!)

            val initiatingPlayerHasProperty = board.playerHasAProperty(initiatingPlayerNumber)
            val initiatingPlayerHasGetOffVacationCard =
                if (currentPlayerWantsToTrade) currentPlayer.hasAGetOffVacationCard
                else playerManager.getPlayerCopy(initiatingPlayerNumber).hasAGetOffVacationCard

            val otherPlayerHasAProperty = board.playerHasAProperty(otherPlayerNumber)
            val otherPlayerHasGetOffVacationCard = otherPlayer.hasAGetOffVacationCard

            if (!initiatingPlayerHasProperty && !initiatingPlayerHasGetOffVacationCard &&
                !otherPlayerHasAProperty && !otherPlayerHasGetOffVacationCard
            ) {
                println("There is nothing that these 2 players can trade with each other besides money")
                return
            }

            val whatPlayerWants = mutableMapOf<String, Any>()

            inputValidation1@ while (true) {
                val otherPlayerMoney = otherPlayer.money
                if (!initiatingPlayerNeedsMoney) {
                    print("Would you like any money from this person? (y/n) ")
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
                    print(
                        if (initiatingPlayerNeedsMoney) "You need money so how much would you like from this person? "
                        else "How much money? "
                    )
                    print("This person has $$otherPlayerMoney. Enter an amount or enter \"b\" to go back")
                    val input = readLine()!!.toLowerCase()
                    if (input == "b") break@inputValidation2
                    val moneyAmount: Int
                    try {
                        moneyAmount = input.toInt()
                    } catch (e: NumberFormatException) {
                        println("Invalid input")
                        continue@inputValidation2
                    }
                    if (moneyAmount in 1..otherPlayerMoney) {
                        whatPlayerWants["money"] = moneyAmount
                        break@inputValidation1
                    } else {
                        println("Invalid input")
                    }
                }
            }

            if (otherPlayerHasAProperty) {
                inputValidation1@ while (true) {
                    print("Would you like any properties from this person? (y/n) ")
                    var input = readLine()!!.toLowerCase()
                    if (input == "y") {
                        val propertiesOwnedByOtherPlayer = board.getPropertiesOwnedBy(otherPlayerNumber)
                        val validPropertyPositions = mutableSetOf<Int>()
                        for (property in propertiesOwnedByOtherPlayer) {
                            validPropertyPositions.add(property.position)
                            println(property)
                        }
                        inputValidation2@ while (true) {
                            print(
                                "Enter the positions of all the properties you would like with each position " +
                                        "separated by a space or enter \"b\" to go back"
                            )
                            input = readLine()!!.toLowerCase()

                            if (input.isEmpty()) {
                                println("Invalid input")
                                continue@inputValidation2
                            }

                            if (input == "b") continue@inputValidation1
                            val positionsEntered = input.split(" ")
                            val propertiesWanted = mutableListOf<Property>()

                            for (positionStr in positionsEntered) {
                                val positionInt: Int
                                try {
                                    positionInt = positionStr.toInt()
                                } catch (e: NumberFormatException) {
                                    println("Invalid input")
                                    continue@inputValidation2
                                }

                                if (positionInt in validPropertyPositions) {
                                    propertiesWanted.add(board.getProperty(positionInt))
                                } else {
                                    println("Invalid input")
                                    continue@inputValidation2
                                }
                            }

                            if (propertiesWanted.isNotEmpty()) {
                                whatPlayerWants["properties"] = propertiesWanted
                            }
                            break@inputValidation2
                        }
                    } else if (input != "n") {
                        println("Invalid input")
                        continue@inputValidation1
                    }
                    break@inputValidation1
                }
            }

            if (otherPlayerHasGetOffVacationCard) {
                inputValidation1@ while (true) {
                    print("Would you like any get off vacation cards? (y/n) ")
                    var input = readLine()!!.toLowerCase()
                    if (input == "y") {
                        val numberOfGetOffVacationCards =
                            playerManager.getPlayerCopy(otherPlayerNumber).numberOfGetOffVacationCardsOwned
                        inputValidation2@ while (true) {
                            println(
                                "This player has $numberOfGetOffVacationCards get off vacation cards. How " +
                                        "many would you like? Enter an amount or \"b\" to go back"
                            )
                            input = readLine()!!
                            if (input == "b") continue@inputValidation1
                            val amount: Int
                            try {
                                amount = input.toInt()
                            } catch (e: NumberFormatException) {
                                println("Invalid input")
                                continue@inputValidation2
                            }
                            if (amount in 1..numberOfGetOffVacationCards) {
                                whatPlayerWants["get off vacation cards"] = amount
                            } else if (amount != 0) {
                                println("Invalid input")
                                continue@inputValidation2
                            }
                            break@inputValidation2
                        }
                    } else if (input != "n") {
                        println("Invalid input")
                        continue
                    }
                    break@inputValidation1
                }
            }

            if (whatPlayerWants.isEmpty()) {
                println(
                    "$initiatingPlayerName, you didn't select anything for what you want. " +
                            "You must restart the trade process"
                )
                continue@mainLoop
            }

            // Part for having the initiating player select what they offer
            val whatPlayerOffers = mutableMapOf<String, Any>()
            inputValidation1@ while (true) {
                print("Would you like to offer this person any money? ")
                var input = readLine()!!.toLowerCase()
                if (input == "y") {
                    val initiatingPlayerMoney = if (currentPlayerWantsToTrade) currentPlayer.money
                    else initiatingPlayer!!.money
                    inputValidation2@ while (true) {
                        print("You have $initiatingPlayerMoney, how much of it would you like to offer? ")
                        input = readLine()!!
                        val moneyAmount: Int
                        try {
                            moneyAmount = input.toInt()
                        } catch (e: NumberFormatException) {
                            println("Invalid input")
                            continue@inputValidation2
                        }
                        if (moneyAmount in 1..initiatingPlayerMoney) {
                            whatPlayerOffers["money"] = moneyAmount
                        } else if (moneyAmount != 0) {
                            println("Invalid input")
                            continue@inputValidation2
                        }
                        break@inputValidation2
                    }
                } else if (input != "n") {
                    println("Invalid input")
                    continue@inputValidation1
                }
                break@inputValidation1
            }

            if (initiatingPlayerHasProperty) {
                inputValidation1@ while (true) {
                    print("Would you like to offer this player any properties? (y/n) ")
                    var input = readLine()!!.toLowerCase()
                    when (input) {
                        "y" -> {
                            val validPropertyPositions = mutableSetOf<Int>()
                            val propertiesOwnedByInitiatingPlayer = board.getPropertiesOwnedBy(initiatingPlayerNumber)

                            for (property in propertiesOwnedByInitiatingPlayer) {
                                validPropertyPositions.add(property.position)
                                println(property)
                            }
                            inputValidation2@ while (true) {
                                println(
                                    "Enter the positions of the properties you would like to offer with each position " +
                                            "separated by a single space or enter \"b\" to go back"
                                )
                                input = readLine()!!.toLowerCase()
                                if (input == "b") continue@inputValidation1
                                val positionsEntered = input.split(" ")
                                val propertiesOffered = mutableListOf<Property>()
                                for (positionStr in positionsEntered) {
                                    val positionInt: Int
                                    try {
                                        positionInt = positionStr.toInt()
                                    } catch (e: NumberFormatException) {
                                        println("Invalid input")
                                        continue@inputValidation2
                                    }

                                    if (positionInt in validPropertyPositions) {
                                        propertiesOffered.add(board.getProperty(positionInt))
                                    } else {
                                        println("Invalid input")
                                        continue@inputValidation2
                                    }
                                }
                                if (propertiesOffered.isNotEmpty()) {
                                    whatPlayerOffers["properties"] = propertiesOffered
                                }
                                break@inputValidation2
                            }
                            break@inputValidation1
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
                                input = readLine()!!
                                if (input == "b") continue@inputValidation1
                                val amount: Int
                                try {
                                    amount = input.toInt()
                                } catch (e: NumberFormatException) {
                                    println("Invalid input")
                                    continue@inputValidation2
                                }
                                if (amount in 1..numberOfGetOffVacationCards) {
                                    whatPlayerWants["get off vacation cards"] = amount
                                } else if (amount != 0) {
                                    println("Invalid input")
                                    continue@inputValidation2
                                }
                                break@inputValidation2
                            }
                            break@inputValidation1
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
                    "$initiatingPlayerName, you didn't offer anything. You must restart the trade process."
                )
                continue@mainLoop
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
                println("$initiatingPlayerName, you have chosen to offer:")
                if (whatPlayerOffers.containsKey(""))
            }
        }



        println(
            """
                                                            What would you like to get from this person?
                                                            Enter one of the following
                                                            1 for a property
                                                            2 for money
                                                        """.trimIndent()
        )
        val otherPlayerHasGetOffVacationCard = playerManager.getPlayerCopy(otherPlayerNumber).hasAGetOffVacationCard
        if (otherPlayerHasGetOffVacationCard) println("3 for a get off vacation card")

        var propertyWanted: Property? = null
        var propertyOffered: Property? = null
        var moneyAmount: Int? = null

        val whatPlayerWants: String
        tradeWithPlayerInputValidation2@ while (true) {
            when (readLine()) {
                "1" -> {
                    whatPlayerWants = "property"
                    val propertiesOwnedByOtherPlayer = board.getPropertiesOwnedBy(otherPlayerNumber)
                    val validPropertyPositions = mutableSetOf<Int>()
                    for (property in propertiesOwnedByOtherPlayer) {
                        validPropertyPositions.add(property.position)
                        println(property)
                    }
                    println("Select the position of the property you would like")
                    while (true) {
                        val propertyPosition: Int
                        try {
                            propertyPosition = readLine()!!.toInt()
                        } catch (e: NumberFormatException) {
                            println("Must enter one of the property position numbers")
                            continue
                        }
                        if (propertyPosition in validPropertyPositions) {
                            propertyWanted = board.getProperty(propertyPosition)
                            break
                        }
                        println("Must enter one of the property position numbers")
                    }
                }
                "2" -> {
                    whatPlayerWants = "money"
                    while (true) {
                        print("How much money? ")
                        try {
                            moneyAmount = readLine()!!.toInt()
                        } catch (e: NumberFormatException) {
                            println("Must enter a number")
                            continue
                        }
                        if (moneyAmount > 0) break
                        println("Amount must be greater than 0")
                    }
                }
                "3" -> {
                    if (otherPlayerHasGetOffVacationCard) whatPlayerWants = "Get Off Vacation Card"
                    else {
                        println("Invalid input")
                        continue@tradeWithPlayerInputValidation2
                    }
                }
                else -> {
                    println("Invalid input")
                    continue@tradeWithPlayerInputValidation2
                }
            }
            break@tradeWithPlayerInputValidation2
        }

        println(
            """
                                                            What would you like to offer this player?
                                                            Enter one of the following
                                                            1 for a property
                                                        """.trimIndent()
        )
        if (whatPlayerWants != "money") {
            // Pointless to trade money for money
            println("2 for money")
        }
        if (currentPlayer.hasAGetOffVacationCard) {
            println("3 for a get off vacation card")
        }
        val whatPlayerOffers: String
        selectOffer@ while (true) {
            when (readLine()) {
                "1" -> {
                    whatPlayerOffers = "property"
                    val propertiesOwned = board.getPropertiesOwnedBy(initiatingPlayerNumber)
                    val validPropertyPositions = mutableSetOf<Int>()
                    for (property in propertiesOwned) {
                        validPropertyPositions.add(property.position)
                        println(property)
                    }
                    while (true) {
                        println("Enter the position of the property you would like to offer")
                        val propertyPosition: Int
                        try {
                            propertyPosition = readLine()!!.toInt()
                        } catch (e: NumberFormatException) {
                            println("Must enter one of the property positions")
                            continue
                        }
                        if (propertyPosition in validPropertyPositions) {
                            propertyOffered = board.getProperty(propertyPosition)
                            break
                        }
                        println("Must enter one of the property positions")
                    }
                }
                "2" -> {
                    if (whatPlayerWants == "money") {
                        println("Invalid input")
                        continue@selectOffer
                    } else {
                        whatPlayerOffers = "money"
                        while (true) {
                            print("How much money? ")
                            try {
                                moneyAmount = readLine()!!.toInt()
                            } catch (e: NumberFormatException) {
                                println("Must enter a number greater than 0")
                                continue
                            }
                            if (moneyAmount > 0) break
                            println("Must enter a number greater than 0")
                        }
                    }
                }
                "3" -> {
                    if (currentPlayer.hasAGetOffVacationCard) whatPlayerOffers = "get off vacation card"
                    else {
                        println("Invalid input")
                        continue@selectOffer
                    }
                }
                else -> {
                    println("Invalid input")
                    continue@selectOffer
                }
            }
            break@selectOffer
        }
        if (otherPlayer is HumanPlayer) {
            print("${otherPlayer.name}, ${currentPlayer.name} would like to give you ")
            print(
                when (whatPlayerOffers) {
                    "property" -> {
                        propertyOffered!!.name
                    }
                    "money" -> "$$moneyAmount"
                    "get off vacation card" -> "a get off vacation card"
                    else -> {
                        throw Exception("Player is somehow not offering a property, money, nor get off vacation card")
                    }
                }
            )
            print(" in exchange for you giving them ")
            print(
                when (whatPlayerWants) {
                    "property" -> {
                        // propertyWanted shouldn't be null if whatPlayerWants equals "Property"
                        propertyWanted!!.name
                    }
                    "money" -> "$$moneyAmount"
                    "get off vacation card" -> "a get off vacation card"
                    else -> {
                        throw Exception(
                            "Player somehow wants something other than a property, money, or a get off vacation card"
                        )
                    }
                }
            )
            print("Do you accept the trade? (y/n) ")
            acceptOrDenyTrade@ while (true) {
                when (readLine()!!.toLowerCase()) {
                    "y" -> {
                        // Make all necessary changes
                        when (whatPlayerWants) {
                            "property" -> {
                                val playerName = if (currentPlayerWantsToTrade) currentPlayer.name
                                else initiatingPlayer!!.name
                                board.setPropertyOwnerAndCheckForChanges(
                                    propertyWanted!!.position,
                                    initiatingPlayerNumber,
                                    playerName
                                )
                                when (propertyWanted) {
                                    is GolfCourse -> {
                                        otherPlayer.removeGolfCourse()
                                        playerManager.updatePlayer(otherPlayer)
                                        if (currentPlayerWantsToTrade) currentPlayer.addGolfCourse()
                                        else {
                                            initiatingPlayer!!.addGolfCourse()
                                            playerManager.updatePlayer(initiatingPlayer)
                                        }
                                    }

                                    is SuperStore -> {
                                        otherPlayer.removeSuperStore()
                                        playerManager.updatePlayer(otherPlayer)
                                        if (currentPlayerWantsToTrade) currentPlayer.addSuperStore()
                                        else {
                                            initiatingPlayer!!.addSuperStore()
                                            playerManager.updatePlayer(initiatingPlayer)
                                        }
                                    }
                                }
                            }

                            "money" -> {
                                // moneyAmount shouldn't be null under this condition
                                otherPlayer.money -= moneyAmount!!
                                playerManager.updatePlayer(otherPlayer)
                                if (currentPlayerWantsToTrade) currentPlayer.money += moneyAmount
                                else {
//                                    playerManager.addMoney(playerNumber, moneyAmount)
                                    initiatingPlayer!!.money += moneyAmount
                                    playerManager.updatePlayer(initiatingPlayer!!)
                                }
                            }

                            "get off vacation card" -> {
                                otherPlayer.removeGetOffVacationCard()
                                playerManager.updatePlayer(otherPlayer)
                                if (currentPlayerWantsToTrade) currentPlayer.addGetOffVacationCard()
                                else {
                                    initiatingPlayer!!.addGetOffVacationCard()
                                    playerManager.updatePlayer(initiatingPlayer)
                                }
                            }

                            else -> {
                                throw Exception(
                                    "Player somehow wants something that isn't a property, money, or a " +
                                            "get off vacation card"
                                )
                            }
                        }
                        when (whatPlayerOffers) {
                            "property" -> {
                                board.setPropertyOwnerAndCheckForChanges(
                                    propertyOffered!!.position,
                                    otherPlayerNumber,
                                    otherPlayer.name
                                )
                                when (propertyOffered) {
                                    is GolfCourse -> {
                                        if (currentPlayerWantsToTrade) currentPlayer.removeGolfCourse()
                                        else {
                                            initiatingPlayer!!.removeGolfCourse()
                                            playerManager.updatePlayer(initiatingPlayer)
                                        }
                                        otherPlayer.addGolfCourse()
                                        playerManager.updatePlayer(otherPlayer)
                                    }

                                    is SuperStore -> {
                                        if (currentPlayerWantsToTrade) currentPlayer.removeSuperStore()
                                        else {
                                            initiatingPlayer!!.removeSuperStore()
                                            playerManager.updatePlayer(initiatingPlayer)
                                        }
                                        otherPlayer.addSuperStore()
                                        playerManager.updatePlayer(otherPlayer)
                                    }
                                }
                            }

                            "money" -> {
                                if (currentPlayerWantsToTrade) currentPlayer.money -= moneyAmount!!
                                else {
                                    initiatingPlayer!!.money -= moneyAmount!!
                                    playerManager.updatePlayer(initiatingPlayer!!)
                                }
                                otherPlayer.money += moneyAmount
                                playerManager.updatePlayer(otherPlayer)
                            }

                            "get off vacation card" -> {
                                if (currentPlayerWantsToTrade) currentPlayer.removeGetOffVacationCard()
                                else {
                                    initiatingPlayer!!.removeGetOffVacationCard()
                                    playerManager.updatePlayer(initiatingPlayer)
                                }
                                otherPlayer.addGetOffVacationCard()
                                playerManager.updatePlayer(otherPlayer)
                            }

                            else -> {
                                throw Exception(
                                    "Player somehow offered something that isn't a property, money, or a " +
                                            "get off vacation card"
                                )
                            }
                        }
                    }
                    "n" -> {
                        println("${otherPlayer.name} denies the trade")
                    }
                    else -> {
                        println("Invalid input. Must enter \"y\" or \"n\"")
                        continue@acceptOrDenyTrade
                    }
                }
                break@acceptOrDenyTrade
            }
        }
    }

    fun playerNeedsMoney(playerNumber: Int = currentPlayer.number, moneyNeeded: Int) {
        val currentPlayerNeedsMoney = playerNumber == currentPlayer.number
        println(
            """
you need $moneyNeeded. Would you like to
             1: Drop out of the game
             2: Make a trade with a player
        """.trimIndent()
        )
        val playerCanPawn = board.playerCanPawnProperty(playerNumber)
        if (playerCanPawn) println("3: Pawn one of your properties")
        lowMoneyInputValidation@ while (true) {
            when (readLine()) {
                "1" -> {
                    if (currentPlayerNeedsMoney) currentPlayer.removeFromGame()
                    else playerManager.removePlayerFromGame(playerNumber)
                }

                "2" -> {
                    askAboutTrading(playerNumber)
                }

                "3" -> {
                    if (playerCanPawn) askAboutPawningProperty(playerNumber)
                    else {
                        println("Invalid input")
                        continue@lowMoneyInputValidation
                    }
                }

                else -> {
                    println("Invalid input")
                    continue@lowMoneyInputValidation
                }
            }
            break@lowMoneyInputValidation
        }
    }
}


//        var currentPlayerIndex = 0
//                // Update players array since currentPlayer is just a copy of one of the players from the players list
//                players.set(currentPlayerIndex, currentPlayer)
//
//                // Skip over players that are out of the game
//                do {
//                    currentPlayer = players[(currentPlayer.number + 1) % players.size]
//                    currentPlayerIndex = (currentPlayerIndex + 1) % players.size
//                } while (!players[currentPlayerIndex].isInGame)
//                currentPlayer = players[currentPlayerIndex]
//            }

//    fun setup() {
//        while (true) {
//            while (true) {
//                print("How many human players? ")
//                playerInput = readLine()!!
//                try {
//                    numHumanPlayers = playerInput.toInt()
//                } catch (e: NumberFormatException) {
//                    println("Invalid input")
//                    continue
//                }
//                if (numHumanPlayers in 0..8) break
//                println("The number of human players can be a minimum of 0 and a maximum of 8")
//            }
//
//            while (true) {
//                print("How many AI Players? ")
//                playerInput = readLine()!!
//                try {
//                    numAIPlayers = playerInput.toInt()
//                } catch (e: NumberFormatException) {
//                    println("Invalid input")
//                    continue
//                }
//                if (numAIPlayers in 0..8) break
//                println("The number of AI Players can be a minimum of 0 and a maximum of 8")
//            }
//            if (numHumanPlayers + numAIPlayers in 2..8) break
//            println("Total number of players can be a minimum of 2 and a maximum of 8")
//        }
//
//        // Get player's names
//        val playerOrderingArray = arrayOfNulls<PlayerOrdering>(numHumanPlayers + numAIPlayers)
//        var name: String
//        for (i in 0 until numHumanPlayers) {
//            print("Enter name for player ${i + 1} (Human commandline.Player) or enter nothing for \"Human commandline.Player ${i + 1}\": ")
//            playerInput = readLine()!!
//            name = if (playerInput == "") "Human commandline.Player ${i + 1}" else playerInput
//            playerOrderingArray.set(
//                i,
//                PlayerOrdering(name, getDiceRoll() + getDiceRoll(), "Human")
//            )
//        }
//        for (i in numHumanPlayers until numHumanPlayers + numAIPlayers) {
//            print(
//                "Enter a name for player ${i + 1} (AI commandline.Player) or enter nothing for \"AI commandline.Player " +
//                        "${i - numHumanPlayers + 1}\": "
//            )
//            playerInput = readLine()!!
//            name = if (playerInput == "") "AI commandline.Player ${i - numHumanPlayers + 1}" else playerInput
//            playerOrderingArray.set(
//                i,
//                PlayerOrdering(name, getDiceRoll() + getDiceRoll(), "AI")
//            )
//        }
//        playerOrderingArray.sort()
//
//        // Create and fill array of players
//        for (i in 0 until numHumanPlayers + numAIPlayers) {
//            players.add(
//                if (playerOrderingArray[i]!!.type.equals("Human")) HumanPlayer(
//                    playerOrderingArray[i]!!.name
//                )
//                else AIPlayer(playerOrderingArray[i]!!.name)
//            )
//        }
//
//        println("The order of the players is ")
//        for (player in players) println(player.name)
//
//        // Set index numbers
//        for (i in players.indices) {
//            players[i].number = i
//            println("The index number for ${players[i].name} is ${players[i].number}")
//        }
//        println()
//    }

//
//when (board.getTypeAt(currentPlayer.position)) {
//    "property" -> {
//        val propertyType = board.getPropertyType(currentPlayer.position)
//        print("a $propertyType ")
//        print("named ${board.getPropertyName(currentPlayer.position)}")
//        if (propertyType.equals("street")) {
//            println("in the ${board.getNeighborhood(currentPlayer.position)} neighborhood ")
//        } else println()
//        if (board.propertyIsOwned(currentPlayer.position)) {
//            print(", which is owned ")
//            val ownerIndexNumber = board.getPropertyOwnerIndexNumber(currentPlayer.position)
//            if (ownerIndexNumber == currentPlayerIndex) println("by you")
////                        when {
////                            ownerIndexNumber == currentPlayerIndex -> {
////                                println("by you")
////                            }
////                            board.propertyIsPawned(currentPlayer.position) -> {
////
////                            }
////                            else -> {
////
////                            }
////                        }
//            else {
//                println("by ${players[ownerIndexNumber].name}")
//                if (board.propertyIsPawned(currentPlayer.position)) {
//                    println("this property is pawned so you don't have to pay a fee")
//                } else {
//                    var fee: Int
//                    when (propertyType) {
//                        "street" -> {
//
//                        }
//                        "golf course" -> {
//
//                        }
//                        "super store" -> {
//                            if (board.bothSuperStoresOwnedBySamePerson) {
//                                println("They own both super stores so you must roll the dice and pay 10 times the amount")
//                            } else {
//                                println(
//                                    "This is the only super store they own so you must roll the dice and pay 5 " +
//                                            "times that amount"
//                                )
//                            }
//                            diceRoll1 = getDiceRoll()
//                            diceRoll2 = getDiceRoll()
//                            fee = board.getSuperStoreFee(diceRoll1 + diceRoll2)
//                            println("You rolled a $diceRoll1 and a $diceRoll2 so you must pay $fee")
//                        }
//                    }
//                    currentPlayer.money -= fee
//                    players[ownerIndexNumber].money += fee
//                }
//            }
//        } else {
//
//        }
//    }
//
//    "Start" -> {
//        // Nothing
//    }
//
//    "Draw entropy card" -> {
//        println("a \"Draw entropy card\" space")
//    }
//
//    "Go on vacation" -> {
//        println("the \"Go on vacation\" space")
//        currentPlayer.sendToVacation(board.vacationPosition)
//    }
//}


//when {
//    board.hasStreetAt(currentPlayer.position) -> {
//
//    }
//    board.hasGolfCourseAt(currentPlayer.position) -> {
//
//    }
//    board.hasSuperStoreAt(currentPlayer.position) -> {
//
//    }
//    board.hasDrawEntropyCardSpaceAt(currentPlayer.position) -> {
//
//    }
//    board.hasEffectlessSpaceAt(currentPlayer.position) -> {
//
//    }
//}