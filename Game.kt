package commandline

import java.lang.Exception
import java.lang.NumberFormatException
import kotlin.random.Random

class Game {
    private fun getDiceRoll() = Random.nextInt(1, 7)

    init {
        play()
    }

    private fun play() {
        val playerManager = PlayerManager()
        val bank = Bank()
        val board = Board()
        val entropyDeck = EntropyDeck()
        var currentPlayer = playerManager.currentPlayerCopy
        var numDoubleRolls = 0
        var goAgain = false

        fun endTurn() {
            if (goAgain) goAgain = false
            else {
                playerManager.updateCurrentPlayer(currentPlayer)
                playerManager.switchToNextPlayer()
                currentPlayer = playerManager.currentPlayerCopy
            }
            println("End of turn. Press enter to continue.")
            readLine()
        }

        outer@ while (true) {
            println("It's ${currentPlayer.name}'s turn")
            println("They are a " + if (currentPlayer is HumanPlayer) "Human Player" else "AI Player")
            if (currentPlayer is HumanPlayer) {
                outerInputValidation@ while (true) {
                    println(
                        """
                            ${currentPlayer.name}, type one of the following and press enter:
                            nothing to take your turn
                            i: view property info
                            p: view player positions
                        """.trimIndent()
                    )
                    val currentPlayerCanAddRestaurant = board.playerCanAddRestaurant(currentPlayer.number)
                    if (currentPlayerCanAddRestaurant) println("r: add a restaurant to one of your properties")

                    val playerCanUnpawn = board.playerCanUnpawnProperty(currentPlayer.number)
                    if (playerCanUnpawn) println("u: Unpawn one of your properties")

                    when (readLine()) {
                        "" -> break@outerInputValidation
                        "i", "I" -> {
                            board.displayPropertyInfo()
                        }
                        "p", "P" -> {
                            println("Positions")
                            playerManager.displayPositions()
                        }
                        "r", "R" -> {
                            if (!currentPlayerCanAddRestaurant) {
                                println("Invalid input.")
                                continue@outerInputValidation
                            }
                            board.displayStreetsWhereRestaurantCanBeAdded(currentPlayer.number)
                            println(
                                "Enter the position of the street you would like to add a restaurant to or enter \"n\" " +
                                        "to signal that you changed your mind"
                            )
                            innerInputValidation@ while (true) {
                                val playerInput = readLine()!!
                                if (playerInput.equals("n") || playerInput.equals("N")) break@innerInputValidation
                                var streetPosition: Int
                                try {
                                    streetPosition = playerInput.toInt()
                                } catch (e: NumberFormatException) {
                                    println("Invalid input")
                                    continue@innerInputValidation
                                }
                                if (board.restaurantCanBeAddedToStreet(currentPlayer.number, streetPosition)) {
                                    val restaurantAddingFee = board.getRestaurantAddingPrice(streetPosition)
                                    if (currentPlayer.money >= restaurantAddingFee) {
                                        currentPlayer.money -= restaurantAddingFee
                                        board.addRestaurantToStreet(streetPosition)
                                    } else {
                                        // Not enough money
                                        TODO()
                                    }
                                    break@innerInputValidation
                                }
                                println("Invalid input")
                            }
                        }

                        "u", "U" -> {
                            if (!playerCanUnpawn) {
                                println("Invalid input")
                                continue@outerInputValidation
                            }
                            val unpawnableProperties = board.getUnpawnableProperties(currentPlayer.number)
                            val validPropertyPositions = mutableSetOf<Int>()
                            println("Your unpawnable properties")
                            for (property in unpawnableProperties) {
                                validPropertyPositions.add(property.position)
                                val propertyType = when (property) {
                                    is Street -> "Street"
                                    is GolfCourse -> "Golf Course"
                                    is SuperStore -> "Super Store"
                                    else -> {
                                        throw Exception(
                                            "Position ${property.position} doesn't contain " +
                                                    "a property but is supposed to"
                                        )
                                    }
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
                                if (playerInput.equals("b") || playerInput.equals("B")) break
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
                                    break
                                }
                                println("Invalid input")
                            }
                        }

                        else -> println("Invalid input.")
                    }
                }
            } else {
                // Block of code for AI players
                TODO()
            }

            var diceRoll1: Int
            var diceRoll2: Int

            if (currentPlayer.isOnVacation) {
                if (currentPlayer is HumanPlayer) {
                    print(
                        "You're on vacation and this is your " +
                                when (currentPlayer.numTurnsOnVacation) {
                                    0 -> "first"
                                    1 -> "second"
                                    2 -> "third (last)"
                                    else -> throw Exception("Shouldn't be on vacation at this point")
                                }
                                + " turn on vacation, would you like to (1) pay $50 to get out or (2) try to roll doubles? "
                    )
                    if (currentPlayer.hasAGetOffVacationCard) {
                        println("or (3) use a get off vacation card")
                    } else println()
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
                                diceRoll1 = getDiceRoll()
                                diceRoll2 = getDiceRoll()
                                println("You rolled a $diceRoll1 and a $diceRoll2")
                                if (diceRoll1 == diceRoll2) {
                                    println("These are doubles so you are off vacation")
                                    currentPlayer.removeFromVacation()
                                    currentPlayer.position += (diceRoll1 + diceRoll2)
                                } else currentPlayer.continueVacation()
                                endTurn()
                                continue@outer
                            }
                            "3" -> {
                                if (currentPlayer.hasAGetOffVacationCard) {
                                    println("${currentPlayer.name} has chosen to use a get off vacation card")
                                    currentPlayer.removeGetOffVacationCard()
                                    entropyDeck.insertGetOffVacationCardAtBottom()
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
                        diceRoll1 = getDiceRoll()
                        diceRoll2 = getDiceRoll()
                        println("${currentPlayer.name} will try to roll doubles\nThey rolled a $diceRoll1 and a $diceRoll2")
                        if (diceRoll1 == diceRoll2) {
                            println("These are doubles so ${currentPlayer.name} is off vacation")
                            currentPlayer.removeFromVacation()
                        } else {
                            println("These are not doubles so ${currentPlayer.name} will continue their vacation")
                            currentPlayer.continueVacation()
                            endTurn()
                            continue@outer
                        }
                    }
                }
            }
            diceRoll1 = getDiceRoll()
            diceRoll2 = getDiceRoll()
            println("${currentPlayer.name} rolled a $diceRoll1 and a $diceRoll2")
            if (diceRoll1 == diceRoll2) {
                numDoubleRolls++
                if (numDoubleRolls == 3) {
                    println("${currentPlayer.name} rolled doubles 3 times in a row so they go to vacation")
                    numDoubleRolls = 0
                    currentPlayer.sendToVacation(board.vacationPosition)
                    endTurn()
                    continue
                }
                println("${currentPlayer.name} rolled doubles so they get to go again")
                goAgain = true
            } else if (numDoubleRolls > 0) numDoubleRolls = 0

            if (currentPlayer.position + diceRoll1 + diceRoll2 > board.numSpaces) {
                currentPlayer.position = (currentPlayer.position + diceRoll1 + diceRoll2) % board.numSpaces + 1
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

            positionEvaluation@ while (true) {
                print("${currentPlayer.name} has landed on position ${currentPlayer.position} which is ")
                when (val currentBoardSpace = board.getBoardSpaceAt(currentPlayer.position)) {
                    is String -> {
                        println("\"$currentBoardSpace\".")
                        when (currentBoardSpace) {
                            "Start" -> {
                                println("This space has no effect")
                            }

                            "Go On Vacation" -> currentPlayer.sendToVacation(board.vacationPosition)

                            "Draw Entropy Card" -> {
                                var topCardShouldBeMovedToBottom = true
                                println("The card you drew says \"${entropyDeck.topCard.message}\"")
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
                                        val totalMoneyReceived =
                                            moneyReceivedFromEachPlayer * (playerManager.numPlayersInGame - 1)
                                        playerManager.removeMoneyFromAllPlayersBesidesCurrent(
                                            moneyReceivedFromEachPlayer
                                        )
                                        currentPlayer.money += totalMoneyReceived
                                    }

                                    "player to other players" -> {
                                        val moneyOwedToEachPlayer = entropyDeck.topCard.value!!
                                        val totalMoneyOwed =
                                            moneyOwedToEachPlayer * (playerManager.numPlayersInGame - 1)
                                        currentPlayer.money -= totalMoneyOwed
                                        playerManager.addMoneyToAllPlayersBesidesCurrent(moneyOwedToEachPlayer)
                                    }

                                    "absolute position change" -> {
                                        val newPosition = entropyDeck.topCard.value!!
                                        if (newPosition < currentPlayer.position) {
                                            println("${currentPlayer.name} has made a revolution")
                                            bank.money -= 200
                                            currentPlayer.money += 200
                                        }
                                        currentPlayer.position = newPosition
                                        continue@positionEvaluation
                                    }

                                    "relative position change" -> {
                                        val positionChange = entropyDeck.topCard.value!!
                                        val newPosition = currentPlayer.position + positionChange
                                        when {
                                            newPosition > board.numSpaces -> {
                                                currentPlayer.position = newPosition % board.numSpaces + 1
                                                println("${currentPlayer.name} has made a revolution")
                                                bank.money -= 200
                                                currentPlayer.money += 200
                                            }
                                            newPosition <= 0 -> currentPlayer.position = board.numSpaces + newPosition
                                            else -> currentPlayer.position = newPosition
                                        }
                                        continue@positionEvaluation
                                    }

                                    "get off vacation free" -> {
                                        topCardShouldBeMovedToBottom = false
                                        entropyDeck.removeGetOffVacationCardAtTop()
                                        currentPlayer.addGetOffVacationCard()
                                    }
                                    "property maintenance" -> {

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
//                                        val numGolfCoursesOwned =
//                                            players[currentBoardSpace.ownerNumber].numGolfCoursesOwned
                                            val numGolfCoursesOwned =
                                                playerManager.getPlayerCopy(currentBoardSpace.ownerNumber).numGolfCoursesOwned
                                            fee = GolfCourse.getFee(numGolfCoursesOwned)
                                            println(
                                                "$ownerName owns $numGolfCoursesOwned golf courses and the fee is $fee"
                                            )
                                        }
                                        is SuperStore -> {
                                            diceRoll1 = getDiceRoll()
                                            diceRoll2 = getDiceRoll()
                                            fee = SuperStore.getFee(diceRoll1 + diceRoll2)
                                            println(
                                                if (SuperStore.bothOwnedBySamePerson) {
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
                                    while (currentPlayer.money < fee) {
                                        if (currentPlayer is HumanPlayer) {
                                            println(
                                                """
                                                You don't have enough money to pay the fee, would you like to
                                                1: Drop out of the game
                                                2: Make a trade with another player
                                                """.trimIndent()
                                            )
                                            val playerCanPawn = board.playerCanPawnProperty(currentPlayer.number)
                                            if (playerCanPawn) println("3: Pawn one of your properties")
                                            lowMoneyInputValidation@ while (true) {
                                                when (readLine()) {
                                                    "1" -> {
                                                        currentPlayer.removeFromGame()
                                                        endTurn()
                                                        continue@outer
                                                    }
                                                    "2" -> {
                                                        println(
                                                            """
                                                            Enter one of the following:
                                                            "i" to see property info
                                                            "p" to see player info
                                                            "b" to go back
                                                            the number of the player you would like to make a trade with
                                                        """.trimIndent()
                                                        )
                                                        val validPlayerNumbers =
                                                            playerManager.getNumbersOfPlayersInGame()
                                                        var otherPlayerNumber: Int
                                                        tradeWithPlayerInputValidation1@ while (true) {
                                                            when (val input = readLine()!!) {
                                                                "i", "I" -> board.displayPropertyInfo()
                                                                "p", "P" -> playerManager.displayPositions()
                                                                "b", "B" -> continue@lowMoneyInputValidation
                                                                else -> {
                                                                    try {
                                                                        otherPlayerNumber = input.toInt()
                                                                    } catch (e: NumberFormatException) {
                                                                        println("Invalid input")
                                                                        continue@tradeWithPlayerInputValidation1
                                                                    }
                                                                    if (otherPlayerNumber in validPlayerNumbers) {
                                                                        break@tradeWithPlayerInputValidation1
                                                                    }
                                                                    println("Invalid input")
                                                                }
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
                                                        val otherPlayerHasGetOffVacationCard =
                                                            playerManager.getPlayerCopy(otherPlayerNumber)
                                                                .hasAGetOffVacationCard
                                                        if (otherPlayerHasGetOffVacationCard) {
                                                            println("3 for a get off vacation card")
                                                        }

                                                        var propertyWanted: Property
                                                        var propertyOffered: Property
                                                        var moneyAmount: Int

                                                        val whatPlayerWants: String
                                                        tradeWithPlayerInputValidation2@ while (true) {
                                                            when (readLine()) {
                                                                "1" -> {
                                                                    whatPlayerWants = "Property"
                                                                    val propertiesOwnedByOtherPlayer =
                                                                        board.getPropertiesOwnedBy(otherPlayerNumber)
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
                                                                    whatPlayerWants = "Money"
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
                                                                    if (otherPlayerHasGetOffVacationCard) {
                                                                        whatPlayerWants = "Get Off Vacation Card"
                                                                    } else {
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
                                                        if (!whatPlayerWants.equals("Money")) {
                                                            // Pointless to trade money for money
                                                            println("2 for money")
                                                        }
                                                        if (currentPlayer.hasAGetOffVacationCard) {
                                                            println("3 for a get off vacation card")
                                                        }
                                                        var whatPlayerOffers: String
                                                        tradeWithPlayerInputValidation3@ while (true) {
                                                            when (readLine()) {
                                                                "1" -> {
                                                                    whatPlayerOffers = "Property"
                                                                }
                                                                "2" -> {
                                                                    if (whatPlayerWants.equals("Money")) {
                                                                        println("Invalid input")
                                                                        continue@tradeWithPlayerInputValidation3
                                                                    } else {
                                                                        whatPlayerOffers = "Money"
                                                                    }
                                                                }
                                                                "3" -> {
                                                                    if (currentPlayer.hasAGetOffVacationCard) {
                                                                        whatPlayerOffers = "Get Off Vacation Card"
                                                                    } else {
                                                                        println("Invalid input")
                                                                        continue@tradeWithPlayerInputValidation3
                                                                    }
                                                                }
                                                                else -> {
                                                                    println("Invalid input")
                                                                    continue@tradeWithPlayerInputValidation3
                                                                }
                                                            }
                                                            break@tradeWithPlayerInputValidation3
                                                        }
                                                    }
//                                                    break@lowMoneyInputValidation
                                                    "3" -> {
                                                        if (!playerCanPawn) {
                                                            println("Invalid input")
                                                            continue@lowMoneyInputValidation
                                                        }
                                                        val pawnableProperties =
                                                            board.getPawnableProperties(currentPlayer.number)

                                                        val validPropertyNumbers = mutableSetOf<Int>()
                                                        println(
                                                            "Enter the position of the property you would like to pawn " +
                                                                    "or enter \"b\" to go back"
                                                        )
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
                                                        inner@ while (true) {
                                                            val playerInput = readLine()!!
                                                            if (playerInput.equals("b", true)) {
                                                                continue@lowMoneyInputValidation
                                                            }
                                                            val propertyPosition: Int
                                                            try {
                                                                propertyPosition = playerInput.toInt()
                                                            } catch (e: NumberFormatException) {
                                                                println("Invalid input")
                                                                continue@inner
                                                            }
                                                            if (propertyPosition in validPropertyNumbers) {
                                                                val pawnPrice =
                                                                    board.getProperty(propertyPosition).pawnPrice
                                                                bank.money -= pawnPrice
                                                                currentPlayer.money += pawnPrice
                                                                board.pawnProperty(propertyPosition)
                                                                break@lowMoneyInputValidation
                                                            } else {
                                                                println("Invalid input")
                                                            }
                                                        }
                                                    }
                                                    else -> {
                                                        println("Invalid input")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    currentPlayer.money -= fee
                                    playerManager.addMoney(currentBoardSpace.ownerNumber, fee)
                                }
                            }
                        } else {
                            // This block of code is for when a player lands on an unowned property
                            println(" that is unowned.")
                            if (currentPlayer is HumanPlayer) {
                                print("Would you like to buy it for $${currentBoardSpace.purchasePrice}? ")
                                propertyInputValidation@ while (true) {
                                    when (readLine()) {
                                        "y", "Y" -> {
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
                                            }
                                            break@propertyInputValidation
                                        }
                                        "n", "N" -> {
                                            break@propertyInputValidation
                                        }
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
                break@positionEvaluation
            }
            endTurn()
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