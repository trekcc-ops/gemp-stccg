# Sample Game Loading #

The file <b>gameLibrary.json</b> can be used to create in-progress games. The purpose of this feature is to easily test UI features for actions that don't occur
until later parts of the game, or can only be performed if other actions with random results turned out
the way you needed them to.

All games in <b>gameLibrary.json</b> will be automatically loaded into the hall when Velara is booted up.
They will only be visible to the players involved and admin users.

## Rules enforcement ##

Currently all sample games will assume "1E Modern Complete" rules. That format's card pool is not enforced.
Random selections and card pile shuffles are enforced.

The rules of the game are not followed for any of the game setup specified in the JSON, but they will be
enforced for any player who joins the game once it's loaded into the hall. For example, if you don't have
a [AU]-enabling card in play in the JSON setup, you can still add [AU]-icon cards in play in the JSON. But
you wouldn't be able to play additional ones during the game once it is loaded.

For the most part, cards are added to the game using real gameplay mechanics. For example, if you add a mission
in the JSON, the game's action history will recognize that a "seed mission action" was performed.

## Syntax ##

The JSON used to load these games is simpler than a full game state JSON, to make it easier to "write"
new games. Only the "gameName" and "players" properties are required. However, you may end up with an
unplayable game if you don't include certain cards. For example, if you omit both the "missionsPile" and
"missions" properties, there will not be any missions populated in the game.

Every game is populated with 30 copies of Barber Pole in each player's draw deck.

### Basic properties ###
* <b>gameName</b> (string) - title of the game that will show up in the hall
* <b>players</b> (string array) - Player names that will be playing the game. The first name in the list will be the starting player.
* <b>format</b> (string) - Code for the game format to be used. If not provided, game builder assumes "st1emoderncomplete".
* <b>phase</b> (enumerated string, see Phase.java for options) - When the game loads, it will be at the start of the phase you specify here.
If not provided, the game builder assumes SEED_DOORWAY.

### Cards not at a location ###

Each of these properties is an array of string arrays representing card blueprint ids.
The first array is player #1's cards, the second array is player #2's. For example:

```
"seedDeck": {
    ["155_021", "155_022"],
    ["155_022"]
}
```

This represents two cards in player 1's seed deck and one card in player 2's seed deck. If only one
string array is included, it will be assumed it belongs to player 1. (If you want to include only cards
for player 2, just include an empty array for player 1.)

The properties the builder will accept are:
* <b>seedDeck</b> - Cards in the seed deck that haven't yet been seeded into play or under a mission
* <b>drawDeck</b> - Cards in a player's draw deck (in addition to the 30 Barber Poles that each player has by default)
* <b>missionsPile</b> - Missions that haven't yet been seeded
* <b>core</b> - Cards in play in a player's core
* <b>hand</b> - Cards in a player's hand
* <b>discard</b> - Cards in a player's discard pile

Note - Both "drawDeck" and "hand" are setup before players draw their starting hands. As a result,
your hand will include the cards specified in "hand" as well as 7 other cards. Some of the cards
in "drawDeck" may be drawn into your starting hand.

### Cards at locations ###

The optional "missions" property is an array of mission card objects. If "missions" is not included,
no cards will be in play. Only do this if you want to test seeding missions; otherwise, your game will be
mostly unplayable. Each card object in "missions" has two required properties:
* <b>blueprintId</b> (string) - the blueprint id of the mission card
* <b>owner</b> (string) - The name of the player who owns the card. "P1" or "P2" are also acceptable, and will map
to player 1's name and player 2's name respectively.

The optional boolean property <b>isShared</b> should be included and set to true if you want both players
to share the mission. The game builder will create a copy of that mission card for each player at the
same location.

Each mission may be given any number of "child cards" that are assigned using the child card relationship types
enumerated in ChildCardRelationshipType.java. Some of these have not yet been implemented
in the game builder. Each child card relationship type property for a mission will be an array of
non-mission card objects following the same general structure as missions (the only difference is that
non-missions do not have the "isShared" property).

In the example below, the first mission has one card on the planet, a facility in space
with two cards aboard, a ship in space, and one card seeded beneath it.

```
    "missions": [
      {
        "blueprintId": "101_154",
        "owner": "asdf",
        "isShared": true,
        "ON_PLANET": [
          {
            "blueprintId": "101_233",
            "owner": "asdf"
          }
        ],
        "IN_SPACE": [
          {
            "blueprintId": "101_104",
            "owner": "asdf",
            "ABOARD": [
              {
                "blueprintId": "155_054",
                "owner": "asdf"
              },
              {
                "blueprintId": "101_292",
                "owner": "asdf"
              }
            ]
          },
          {
            "blueprintId": "163_054",
            "owner": "asdf"
          }
        ],
        "SEEDED_UNDERNEATH": [
          {
            "blueprintId": "161_009",
            "owner": "qwer"
          }
        ]
      },
      {
        "blueprintId": "101_156",
        "owner": "qwer"
      }
    ]
```

There is limited enforcement on whether or not cards can be included
with illogical relationship types. For example, in a real game, you
cannot seed cards underneath a ship, or put cards aboard an event card.
In some cases, including these combinations will throw an error when the
server builds the game. In others, there will be no error, and your game
may include your setup.