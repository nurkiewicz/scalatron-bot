# Gravity-like Scalatron bot

This Scalatron bot implementation uses gravity-like strategy where good cells (e.g. Zugars and Fluppets) attract bot and bad cells repulse it. Resultant force guides the bot to good cells and keeps it away from bad.

By adjusting force strength (think: *electrostatic charge* in [Coulomb force](http://en.wikipedia.org/wiki/Coulomb_force)) different behaviours and priorities can be achieved.

## Mini bots

Mini bots are spawned in two situations:

* when enemy mini bot is approaching, we fire mini bot to track it and explode close to it
* when plenty of good cells are around (especially moving blue Fluppets) and we want to harvest them quickly

Mini bots are driven using the same strategy but with different force multipliers: they are attracted by enemy (mini) bots and explode close to them, while normal bot tries to escape danger.

## Efficiency

Not tested with real bots, but this implementation is better by an order of magnitude compared to [`Reference` bot](https://github.com/scalatron/scalatron/blob/master/Scalatron/samples/Example%20Bot%2001%20-%20Reference/src/Bot.scala) (60K vs. 4K energy units, average after 20 rounds one-on-one). Not bad for 200 lines of completely stateless code (double that if parsing input and support classes added).

## Test drive

Whole source code is composed of one file in default package. Highly discouraged practices, but helps interactive testing. You'll find handful of tests, mostly experimenting and sanity checks. Run:

	$ mvn package

and copy `target/ScalatronBot.jar` to Scalatron `bots` directory. Alternatively paste `ControlFunctionFactory.scala` contents to the GUI.

## License

Apache License 2.0.
