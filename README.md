# PixelmonUtils
Server-side design utilities for pixelmon. Made for forge 1.16.5.

## Features
* Add dialogue to Pixelmon Statues when you interact with them!
* Assign commands to pokeloot that execute when they are claimed!
* Set required items in the inventory of players to interact with NPCs or battle Trainers!

## Commands:
Base command is `/pixelmonutils` which has the subcommands `get`, `set`, `remove`, or `npcbattle`.

`get` has the subcommands:
* `requireditem` to show an indexed list of required items on an NPC.
* `pokelootcommand` `[xyz]` to show an indexed list of commands to execute on a pokechest loot block.
* `dialogue` to show an indexed list of dialogue on a Statue.
* `npcstare` `<entityuuid>` to show the npc's current staring position.

`set` has the subcommands:
* `requireditem` to add the item in hand as a required item to interact or battle with the NPC.
* `pokelootcommand` `[xyz]` `command` to add a command to execute at the pokechest at XYZ coordinate.
* `dialogue` `<dialogue>` to add a line of dialogue to a Statue upon interaction.
* `npcstare` `<entityuuid>` `[xyz]` to set a position which the NPC will stare at when a player approaches within 8 blocks.

`remove` has the subcommands:
* `requireditem`
* `pokelootcommand` `[xyz]`
* `dialogue`
* `npcstare` `<entityuuid>`

The above `set` and `remove` must be followed by a whole-number `index` (seen through the `get` subcommand) to remove an entry from any of the above lists.

`npcbattle` takes a player selector and entity selector (must be an npc trainer) and lastly a true/false for whether to show the rules screen.
Then, it prompts the player into a battle against the NPC trainer.

`spectatebattle` is a remap to Pixelmon's `/spectate` command which can have issues overlapping with vanilla 1.16.5's `/spectate` command.

Command addendum:
* NPCs and Statues are targeted by looking at them in-game.
* The `dialogue` subcommand is for statues added by Pixelmon.
* `[xyz]` represents coordinates.
