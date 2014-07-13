CinnaHorse
==========

Tales of Dertinia Horse Plugin
This plugin allows you to summon individual horses with some customised stats.
The horses is killed when you logout so no need for manual slaying.

Commands
--------

/horse summon
Summons a horse or removes it if you already have one.

/horse rent
Rents a horse from the system. 
Plugin randomizes a "normal" horse and presents it to the user for a fee.
Horse is automatically returned at end of lease unless a renewal is issued.
Renewal is done by using /horse rent again while rental is active.

/horse list [Player]
Lists the statistics used for the horse summoned by /horse summon
A optional parameter is availiable to list statistics about another users horse.

/horse set [Player] {parameter} {value}
Sets a parameter for a players horse to the value.
If no player is specified, the users horse is edited.
Horse might need to be "reloaded" to update.

/horse reload
Reloads the configuration file, if you have done some manual editing.



Permissions
-----------

cinnahorse.spawn
Allows you to use the /horse command.
Default for Ops
