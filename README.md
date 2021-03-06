# Singletonopoly
This is a game with a command line interface made using Kotlin and IntelliJ IDEA. All of the Kotlin files in this repo are copies of the 
source files for an IntelliJ IDEA project on my computer.

### History and future of this project
During the spring of 2020, I was thinking that it would be interesting to make coding implementations of some board games. I was also planning
on doing an independent study course over the summer. I came up with the idea of making coding implementations of some board games for the 
independent study course. I proposed my idea to one of my professors who would be a sponsor for the course and they accepted. I decided that
Monopoly would be one of the games I would implement. I decided to make a parody, which in time would be called Singletonopoly. I thought this 
game would be challenging but not overwhelming to implement. I decided to use Kotlin because I had a little experience by using it for an 
Android development course, the professor of which was the professor I proposed my idea to. I was originally planning on first making a command line version of Singletonopoly and then
a graphical user interface version and then do a different board game. However, even after working on a command line version of Singletonopoly for a good portion
of June and all of July, I still was not done with this version. The time period for the independent study course had ended so I turned in all
the source code I had to my professor and then got an A for the course and then got my Bachelor's degree in computer science. I would then 
spend all of August and some of September finishing up this project up and then making minor changes in the following months. 
I might make a graphical user interface version of this project in the future.

### Thoughts on this project
I'm glad I used Kotlin because it has some useful features, some of which I was not familiar with at the start. Some features I like include
smart casting, custom getters, string interpolation, and if statements that are also expressions. I enjoyed this project and 1 reason is because 
this project was a big problem solving exercise and required a bit of thinking to implement all features. Another reason I enjoyed it is because 
all the features were fun to implement because this project is a parody of a board game that I sometimes enjoy playing. The hardest part was 
the trading feature. This part also takes up between 500 and 600 lines of code of the approximately total 2500 lines of code. One problem I 
had with this feature is that I originally made it so that if a player wanted to trade a property with another player, they would only be 
able to trade 1 property per trade. I realized that it would be ideal to let players be able to trade however many properties they want in 
a single trade because what if a player wanted to trade 2 of their properties for 1 of another player's. I had to do some modification to 
allow for this.

## Rules and how to play

### Objective
Make all the money you can by moving around a board and buying properties. Get money from other players when they land
on your properties. Remove other players from the game by making them lose all their money and properties. Be the last
person remaining in the game. In other words, make the set of players in the game a singleton set that consists of
only you.


### Beginning
At the beginning of each game, the program will ask for how many players and the user will type the amount.
The program will then ask for the name of each player. After each name is typed and confirmed, a random roll of 2 dice
will be generated for them. This dice roll will determine their order. Higher rolls go first. In the event of a tie,
the player who entered their name first will be the first of everybody who tied. All players start with $4096 (2<sup>12</sup>) 
and on position 1 of the board, which is a position labeled with "Start". Along with the amount of money that players 
start with, there are other references to powers of 2 throughout the game.


### The Board
The board consists of spaces that may or may not have an effect when a player lands on them. On each player's turn,
they roll 2 dice and their position gets advanced by the value of the dice and they move to the corrresponding space
on the board. If the values of both dice are the same, also called rolling doubles, that player gets to go again. When
the player goes completely around the board, this is called a revolution and that player gets $512 (2<sup>9</sup>). Some
of the spaces consist of properties. At the beginning of a game, all properties are unowned. When a player lands on an
unowned property, they have the option to buy it. When a player lands on a property owned by someone other than
themself and this property is not pawned, they must pay that person a fee. How much this fee is depends on multiple
factors explained in the properties section.


### Properties
The 3 types of properties are streets, super stores, and golf courses.

####     Streets
There are 8 neighborhoods around the board, which consist of 3 streets each. Each street has it's own board space.
Each neighborhood has a number and name associated with it. The number for each neighborhood is equal to the order
they're in from the start of the board. So the neighborhood that is closest to the start of the board has a number
of 1 and the neighborhood that is second closest to the start of the board has a number of 2 and so on. The
purchase price of a street is equal to it's neighborhood number multiplied by 128 (2<sup>7</sup>). The starting fee is equal to the
purchase price divided by 8. This is what the fee is if the owner owns 1 or 2 of the streets in the corresponding
neighborhood. If a player owns all 3 of the streets in that neighborhood, the fee for the streets there
(with no restaurants on them) is the starting fee multiplied by 2, or the purchase price divided by 4. Those streets can have
restaurants added to them, which increases the fee. There can be a maximum of 5 restaurants on a street. The price
to add a restaurant to a street is equal to half of the purchase price. The money that a player gains when selling
restaurants back is half of the price that was payed to put those restaurants there in the first place. When 1
restaurant is on a street, the fee for that street is the starting fee multiplied by 4, or the purchase price
divided by 2. For 2 restaurants, the fee is the starting fee multiplied by 6. For 3 restaurants, the fee is the
starting fee multiplied by 8, which is equal to the purchase price. For 4 restaurants, the fee is the starting fee
multiplied by 10. For 5 restaurants, the fee is the starting fee multiplied by 12.

####     Super Stores
There are 2 super stores. Each one has a purchase price of $512 (2<sup>9</sup>). If a super store gets landed on by someone who
doesn't own it, the player that landed there will need to roll 2 dice and then pay the value of those dice
multiplied by either 8 (2<sup>3</sup>) or 16 (2<sup>4</sup>), 8 for if the player who owns that super store doesn't own the other super store
and 16 for if they do.

####     Golf Courses
There are 4 golf courses around the board. Each one has a purchase price of $512 (2<sup>9</sup>). The fee for an owned golf
course depends on how many golf courses the owner of that golf course owns. The fee for 1 owned golf course
is $(512 / 8) or $64 (2<sup>6</sup>). For 2, it's $(512 / 4) or $128 (2<sup>7</sup>). For 3, it's $(512 / 2) or $256 (2<sup>8</sup>). For 4, it's $512 (2<sup>9</sup>).

####     Pawning Properties
If a player pawns one of their properties, that player will get some money from the bank but anybody who lands on
that property will not need to pay a fee until that property is unpawned. For all properties, the money that a
player gains from pawning is equal to half of that property's purchase price. The money needed to unpawn is equal
to the pawn price plus a 10.24% (2<sup>10</sup> / 100) fee.


### Action Deck
The action deck is a deck of cards with messages on them. When a player lands on a board space that says
"Draw Action Card", they draw a card from the action deck and then do what the card says. The card will then be
placed at the bottom of the deck, unless it's a "Get Off Vacation Free" card. In this case, the card gets removed from
the deck and goes to the player that drew it. When the player uses that card, it goes at the bottom of the action deck.


### Vacation
When a player lands on the "Go On Vacation" space on the board, draws a card from the action deck with this same
message, or rolls doubles 3 times in a row, that player's position will be changed to a designated "Vacation" space
on the board. The player is stuck on this space for as long as they are on vacation. On the player's next turn, they
will have the choice of either trying to roll doubles, paying $128 (2<sup>7</sup>) to get off vacation, or using a
"Get Off Vacation Free" card if they drew one from the action deck. If they choose to roll doubles and then succeed,
the player is off vacation. A player can be on vacation for a maximum of 3 turns by choosing to roll doubles each time.
After failing to roll doubles for 3 turns in a row, they get off vacation.


### Trading
At the beginning of a player's turn, they can choose to initiate a trade with another player. Trading can also happen
when a player does not have enough money to pay a fee and that player chooses to trade something of theirs for money.
They follow the prompts given by the program about what they want to trade. The other player can accept or deny the
trade. If the trade is accepted, then the necessary changes are made.
