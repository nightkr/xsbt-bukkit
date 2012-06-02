XSBT-Bukkit
===========
Simple plugin assisting with Bukkit plugin development.

Features
--------
* Automagic plugin manifest generation

Planned features
----------------
* Overriding `run` to launch a CraftBukkit server with the plugin loaded

Installation
------------
1. Add http://nexus.nullable.se/nexus/content/repositories/releases/ to the resolvers
2. Add `sbtVersion(v => "se.nullable.xsbt-bukkit" %% "xsbt-bukkit" % (v+"-0.0.1"))` to the plugin dependencies