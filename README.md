# Gravity-like Scalatron bot

This [Scalatron](https://github.com/scalatron/scalatron) bot implementation uses [gravity-like strategy](https://github.com/nurkiewicz/scalatron-bot/blob/master/src/main/scala/GravityLikeStrategy.scala) where good cells (e.g. Zugars and Fluppets) attract bot and bad cells repulse it. Resultant force guides the bot to good cells and keeps it away from bad.

By adjusting force strength (think: *electrostatic charge* in [Coulomb force](http://en.wikipedia.org/wiki/Coulomb_force)) different behaviours and priorities can be achieved.

## Mini bots

Mini bots are spawned when environment is "*attractive*", i.e. good cells or enemy bots are visible. By avoiding spawning bots in less interesting surroundings, we can spawn more in other places, maximizing efficiency of harvesting. This strategy assumes that both number of bots and control function run time to be limited.

Mini bots are driven using the same strategy but with different force multipliers: they are attracted by enemy (mini) bots and explode close to them, while normal bot tries to escape danger.

Another interesting behaviour is exploding when mini-bot's surrounding is not attractive (only other mini-bots, walls and bad plants/creatures) and energy low (< 80 EU). Mini-bots commit suicide early to allow spawning mini-bots in better neighbourhood.

## Efficiency

This implementation is better by few orders of magnitude compared to [`Reference` bot](https://github.com/scalatron/scalatron/blob/master/Scalatron/samples/Example%20Bot%2001%20-%20Reference/src/Bot.scala) (700K vs. 4K energy units, average after 20 rounds one-on-one; **924 915** record). Not bad for 350 lines of stateless code. Tested with the following options:

    $ java -jar Scalatron.jar -x 200 -y 200 -browser no -maxslaves 32

Scalatron 1.0 does not provide number of mini-bots alive and max number of slaves to control function. Thus limiting it to `32`. It takes about 30-35 millisecond for each step.

## Test drive

You'll find handful of tests, mostly experimenting and sanity checks (build status: [![Build Status](https://travis-ci.org/nurkiewicz/scalatron-bot.png?branch=master)](https://travis-ci.org/nurkiewicz/scalatron-bot)). Bulding:

	$ mvn package

Then copy `target/ScalatronBot.jar` to Scalatron's `bots` directory. Use parameters shown above to start Scalatron.

## License

[Apache License 2.0](https://github.com/nurkiewicz/scalatron-bot/blob/master/license.txt).
