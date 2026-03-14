# Action results
<i>Last updated 11 March 2026</i>

The serialized game state property "actionResults" contains
information about game actions that have been initiated and/or completed.
On the server side, these objects belong to the class ActionResult.

Some actions may have multiple results, for example:
* An action result is created at both the start and end of a mission attempt action.
* "Group actions" (such as playing multiple cards at the same time) will create a result for each subaction.

All action result objects have these four properties:
* <b>resultId</b> (integer) - unique id number for the action result
* <b>performingPlayerId</b> (string) - Name of the player who initiated the action. All action results
will have one of these, but it may not be intuitive to display it in the UI. For example, if you encounter
a dilemma that you seeded, and that dilemma kills one of your personnel, you are the performing player of that
kill action.
* <b>timestamp</b> (string) - ISO-8601 representation of the time 
* <b>type</b> (string) - type of action result; these are enumerated in ActionResultType.java and below.
Most correspond to a specific class inherited from ActionResult.

## Properties of specific action result types ##

Unless specified below, every property listed should be included in every
action result of the type with a non-null value. Card id numbers are visible
in every player's game state unless specified below.

### ACTIVATED_TRIBBLE_POWER ###
* no additional properties at this time
### ADDED_PRESEEDS ###
A card is "preseeded" when a player selects a mission for it to be seeded under.
* <b>targetCardIds</b> (integer array) - cardId numbers for cards selected to be seeded.
These id numbers will be made anonymous to all players except the one seeding these cards.
### BEAMED ###
The beam action always has both a destination and origin. For example, if beaming from a Runabout
at a mission to the planet there, the origin will be the Runabout card, and the destination
will be the bottom mission card at that location.
* <b>targetCardIds</b> (integer array) - cardId numbers for cards that were beamed
* <b>destinationCardId</b> (integer) - cardId for destination
* <b>originCardId</b> (integer) - cardId for origin
### CHANGED_AFFILIATION ###
* <b>targetCardId</b> (integer) - cardId number for card whose affiliation was changed
* <b>newAffiliation</b> (string) - affiliation the card was changed to; these are enumerated in Affiliation.java
### DILEMMA_PLACED_ON_CARD ###
This result is created when an encountered dilemma is placed on another card, for example Punishment Box or Friendly Fire.
* <b>targetCardId</b> (integer) - cardId number for the dilemma that was placed
* <b>cardPlacedOnId</b> (integer) - cardId number for the card the dilemma was placed on
### DISCARDED ###
The "destination" property here indicates when a card is directly discarded to somewhere other than the discard pile.
There are separate action
results for cards placed directly in one of those piles. For example, a dilemma that scores points
when overcome is discarded to the point area, whereas cards like Scientific Diplomacy directly place
cards in the point area.
* <b>targetCardId</b> (integer) - cardId number for the card discarded
* <b>destination</b> (string) - either "DISCARD", "POINT_AREA", or "REMOVED"
### DOCKED ###
* <b>targetCardId</b> (integer) - cardId number for the ship that docked
* <b>dockedAtCardId</b> (integer) - cardId 
### DREW_CARDS ###
* <b>drawnCardIds</b> (integer array) - cardId numbers for the cards drawn; these will be anonymous for all players except the one who drew them
* <b>[optional] performingCardId</b> (integer) - cardId for the card that allowed or required the card draw;
this will not be included if there was no such card (for example, a player's end-of-turn normal card draw, or a player
drawing their starting hand)
* <b>isStartingHand</b> (boolean) - TRUE if this is the result of a player drawing their starting hand; FALSE otherwise
### ENDED_TURN ###
* No additional properties at this time.
### FLEW_SHIP ###
The "origin" and "destination" represent the cards the ship is flying from and to. For
example, if a ship flies from a mission to Bajoran Wormhole, the origin will be the bottom mission
card at the mission's location, and the destination will be the Bajoran Wormhole card.
* <b>targetCardId</b> (integer) - cardId number for the flying ship
* <b>originCardId</b> (integer) - cardId number for origin
* <b>destinationCardId</b> (integer) - cardId number for destination
### KILLED ###
Typically when cards are killed, they are also discarded. That discard action has a separate result.
* <b>killedCardIds</b> (integer array) - cardId number for cards killed
* <b>performingCardId</b> (integer) - cardId number for the card whose gametext caused the kill
### MISSION_ATTEMPT_ENDED ###
* <b>targetCardId</b> (integer) - cardId number for the mission that was attempted
* <b>wasSuccessful</b> (boolean) - TRUE if the mission was solved; FALSE otherwise
### MISSION_ATTEMPT_STARTED ###
* <b>targetCardId</b> (integer) - cardId number for the mission being attempted
### NULLIFIED ###
Typically when cards are nullified, they are also discarded. That discard action has a separate result.
* <b>targetCardId</b> (integer) - cardId number for card that was nullified
* <b>performingCardId</b> (integer) - cardId number for the card whose gametext caused the nullify action.
It may not always make sense to include this in the UI. For example, if you nullify a dilemma using its
gametext, the dilemma will be both the target and performing card in this result.
### PLACED_CARD_IN_POINT_AREA ###
Typically when cards are placed in a player's point area, the player scores points. Scoring those points has a separate result.
* <b>targetCardId</b> (integer) - cardId number for card placed in point area
### PLACED_CARDS_IN_DRAW_DECK ###
* <b>targetCardIds</b> (integer array) - cardId numbers for cards placed in deck; these will be
anonymous to players who couldn't see the cards before this action (for example, the result of Masaka Transformations)
* <b>placement</b> (string) - either "BOTTOM" (placed beneath deck), "SHUFFLE" (shuffled into deck), or "TOP" (placed on top of deck)
### PLAYED_CARD ###
* <b>playedCardId</b> (integer) - cardId for card played
* <b>[optional] destinationCardId</b> (integer) - In most cases, this will be included; the main exception is
when playing a card "on the table" aka to a player's core. In those cases, this property will not be included.
When included, this represents the played card's "parent card". Examples:
  * When playing a personnel aboard a ship, the ship is the destination.
  * When playing Venus Drug on a location, the destination is the card representing that location (like Bajoran Wormhole or the bottom mission card at a mission).
  * When downloading Equipment to a site, the site is the destination.
* <b>toCore</b> (boolean) - TRUE if the card is played to a player's core; FALSE otherwise
* <b>isDownload</b> (boolean) - TRUE if the card was downloaded; FALSE otherwise
* <b>isReport</b> (boolean) - TRUE if the card was considered to be "reported for duty"; FALSE otherwise
* <b>performingCardId</b> (integer) - cardId for the card whose gametext caused the card play. If this doesn't apply (for
example, a card played using a player's normal card play), performingCardId is equal to playedCardId.
### PLAYER_WENT_OUT ###
* No additional properties at this time
### RANDOM_SELECTION_INITIATED ###
* No additional properties at this time
### REMOVED_CARD_FROM_GAME ###
(see "DISCARDED")
### REMOVED_PRESEEDS ###
* <b>targetCardIds</b> (integer array) - cardId numbers for cards removed from seed selection.
  These id numbers will be made anonymous to all players except the cards' owners.
### REVEALED_SEED_CARD ###
* <b>targetCardId</b> (integer) - cardId for the card being revealed
### SCORED_POINTS ###
* <b>performingCardId</b> (integer) - card whose gametext created the points
* <b>pointsScored</b> (integer)
* <b>pointsAreBonus</b> (boolean) - TRUE if the points are "bonus points" by rule; FALSE otherwise
### SEEDED_INTO_PLAY ###
For example, seeding a facility or card like Continuing Mission. This result is not created when seeding cards under missions.
* <b>seededCardId</b> (integer) - cardId for card seeded
* <b>destinationCardId</b> (integer) - see "PLAYED_CARD" for additional details; this property works the same way
* <b>toCore</b> (boolean) - TRUE if seeded to a player's core; FALSE otherwise
### STARTED_TURN ###
Example: If "player1" is the first player of the game, their first turn will be both gameTurnNumber = 1 and playerTurnNumber = 1.
When the next player starts their first turn, it will be gameTurnNumber = 2, but still playerTurnNumber = 1.
Assuming no players are skipped, the next one will be game=3, player=2; next one game=4, player=2, etc.
* <b>gameTurnNumber</b> (integer)
* <b>playerTurnNumber</b> (integer)
### STOPPED_CARDS ###
* <b>targetCardIds</b> (integer array) - cardId numbers for cards that were stopped
### UNDOCKED ###
* <b>targetCardId</b> (integer) - cardId for ship that undocked
* <b>undockingFromCardId</b> (integer) - cardId for site or facility the ship undocked from
### VOLUNTEERED_FOR_SELECTION ###
Card "volunteered" to be selected by a random selection. Created by cards like Harry Kim and Korris.
* <b>volunteeringCardId</b> (integer) - cardId for card that volunteered
* <b>[optional] selectingCardId</b> (integer) - cardId for the card that is requiring the selection; will not be included
if the selection was not caused by a card (for example, casualties at the end of personnel battle)
### WALKED ###
Same concepts as the "BEAMED" result.
* <b>targetCardIds</b> (integer array)
* <b>destinationCardId</b> (integer)
* <b>originCardId</b> (integer)