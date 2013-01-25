Sneaky
===========

**Sneaky** is a small but versatile plugin that simply allows players to appear to be sneaking at all
times, without having to hold down the sneaking key. 

Many plugins offer a similar ability, but they may cause glitchy movement or trigger anti-cheating measures as 
they modify the player state on the server-side. To avoid these problems, Sneaky applies this effect on outgoing
packets destined for each player, ensuring that any modification is purely a client-side illusion. This approach
also permit us to selectively disable the sneaking effect, allowing (for instance) your operators or superusers 
to see through it completely.


Dependencies
------------
This plugin requires [ProtocolLib](https://github.com/aadnk/ProtocolLib) to function! 
Some features also require [Vault](http://dev.bukkit.org/server-mods/vault/).


Features
--------
# Toggle persistant sneaking on and off by calling `/sneak`
# Operators can toggle the sneaking of other players by calling `/sneak [player]`
# Operators are (by default) not affected by the sneak effect.
# Set the maximum duration a player can be sneaking in a single go, and a cooldown time
  until the sneak command is available after a use.
# Customize the duration and cooldown on a per player or per group basis. Requires Vault.


Commands
--------
There is presently only one command - `/sneak`. 

If no parameters are specified, the sneaking of the sender will be toggled provided the 
permission `sneaky.sneak.self` is set. 

Operators, or players with the permission `sneaky.sneak.other`, can specify a player in the 
second parameter other than themselves. The last parameter sets the desired state - either 
*on* or *off*. If not included, the state will be toggled.

Permissions
-----------
<table>
  <tr>
    <th>Permission</th>
    <th>Description</th>
    <th>Default</th>
  </tr>
  <tr>
    <td>sneaky.sneak.self</th>
    <td>Toggle automatic sneaking for your own player.</td>
    <td>true</td>
  </tr>
  <tr>
    <td>sneaky.sneak.other</td>
    <td>Toggle automatic sneaking of other players.</td>
    <td>op</td>
  </tr>
  <tr>
    <td>sneaky.hide.autosneak</td>
    <td>Whether or not to hide all automatic sneaking for this player.</td>
    <td>false</td>
  </tr>
  <tr>
    <td>sneaky.exempt</td>
    <td>If set, the player is not bound by any cooldown or duration limit.</td>
    <td>op</td>
  </tr>
</table>

It is also possible to customize the duration or cooldown of a player by setting the permission info nodes
(see the documentation of your permission plugin of choice) `sneaky_duration` and `sneaky_cooldown`
respectively. 

For PermissionsEx, this can be done like so:
```YAML
users:
  Player:
    group:
     - Members
    options:
      sneaky_duration: 15
      sneaky_cooldown: 5
```
	  
Building
--------
You can compile this project yourself by using the latest version of Maven.
