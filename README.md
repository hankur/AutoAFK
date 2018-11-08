# AutoAFK
Very simple plugin that implements AFK mode!

Feel free to contribute, since I no longer actively develop this plugin.

Available on BukkitDev: https://dev.bukkit.org/projects/autoafk

## Description
This simple AFK plugin aims to be very lightweight and easy to use. It allows players to put themselves in and out of AFK mode. There is also an option to set players into AFK mode automatically after configurable amount (permission based) of inactivity, which will be cancelled it if they become active again. Automatic teleport to specified location and kick after configurable time (permission based) is also built-in. AFK players can optionally be protected from moving or taking damage. Players in AFK mode can also have prefixes in display name, TAB name and player tag, they can also have particle effects. All messages and listeners are configurable! It does support /reload, but full restart is preferable.

**This plugin isn't supported anymore, but should work for versions 1.8.x to 1.12.x. For 1.13.x and forward, please consider using [AdvancedAFK](https://www.spigotmc.org/resources/advancedafk.60761/).**

## Dependencies
This plugin requires Java 8 and Spigot/Bukkit/Paper 1.8+.

If you want to use countdown and particle features:

- If you're using 1.8: [TitleAPI](https://www.spigotmc.org/resources/titleapi-1-8-1-9-1-10-1-11.1325/) and [ParticleAPI](https://www.spigotmc.org/resources/api-particleapi-1-7-1-8-1-9-1-10.2067/)
- If you're using 1.9 or 1.10: [TitleAPI](https://www.spigotmc.org/resources/titleapi-1-8-1-9-1-10-1-11.1325/)
- If you're using 1.11+: no extra requirements

## Setup
Just install the plugin as usual and let it generate the config file. Configuration options are explained below, under "Default config". You don't have to configure anything in-game, but if you want to use the teleport feature, you have to set a teleport location with the command /autoafk set.


## Commands and permissions
/command <required> [optional] (explanation) required.permission

- /afk
  - autoafk.afk (default)
  - If you are not AFK, then it puts you into AFK mode. If you already are AFK, then it cancels your AFK status.
- /autoafk
  - autoafk.main (OP)
  - Reloads the config.
- /autoafk set
  - autoafk.main (OP)
  - Sets the automatic teleport destination to command sender's location.
- autoafk.exempt (OP)
  - Players who have this permission will not be automatically put into AFK mode.
- autoafk.teleportexempt (OP)
  - Players who have this permission will not be automatically teleported to the specified location.
- autoafk.protection.move (OP)
  - Players who have this permission will not be protected from moving.
- autoafk.protection.damage (OP)
  - Players who have this permission will not be protected from taking damage.
- Other custom permissions specified in config
