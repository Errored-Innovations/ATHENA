# ATHENA
ATHENA is an easy-to-use and imformative event debugger for server owners and plugin developers alike, with aims at being end-user friendly and robust.

### Requirements
- Java 16 (or at least Java 9, but... it's built against Java 16)
- Minecraft 1.17 or newer
- Paper*

*This plugin has not been tested on other server types/forks. Please use this plugin on other server types at your own risk. Any problem that appears on different server types can be considered a bug, but only if said server type being used is Tuinity, Purpur or Airplane. Other server types are considered unsupported (including CraftBukkit and Spigot).

### Features
- See what plugins are listening to each event.
- See the changes an individual plugin makes to an event.
- See the amount of milliseconds it takes for a plugin to finish running their listener.
- See the listener class that is making changes to a given event.

### Important Notes
ATHENA is made with usability, stability and detail in mind. However, performance is not necessarily a priority with the plugin. Whilst I have applied the majority of my knowledge to make the plugin as optimised as possible, it is not recommended to run ATHENA when you don't need it. The plugin uses a considerable amount of reflection to function and collect data from events, which in itself can take a hit on performance. In addition, whilst ATHENA does not (or at least tries to not) interfere with individual plugin listeners, it may have an impact on how optimised they may appear in Paper's Timings. If you are measuring event performance using Timings and ATHENA, it is recommended to take separate measurements (e.g. /timings paste first, then use ATHENA to see completion times on one occasion).

If you are a server owner measuring listener durations, you should only be concerned about performance if a single listener exceeds 40ms of completion time. A single tick in Minecraft takes 50ms, hence why 20 TPS is an important number to aim for in servers. Please do not nag plugin developers if their listeners' time simply takes the longest; some plugins need to carry out more tasks than others. In addition, a spark/profiling report as well as timings may be more helpful than a single snippet from ATHENA.

If ATHENA is unable to listen to a specific event or crashes when you attempt to do so, please open an issue with the event. If it is an event from a plugin, please send the class of the event where possible.

If ATHENA experiences an error, it should still let the listeners it has control over run so it should not have an impact on other plugins' functionality.

PRs and suggestions on how to improve the plugin - including its performance - are more than welcome.
