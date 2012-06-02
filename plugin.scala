import sbt._
import Keys._
import Cache._

import xsbt.api.Discovery

import org.yaml.snakeyaml
import snakeyaml.Yaml

import java.io.{FileOutputStream, BufferedOutputStream}

import scala.collection.JavaConverters._

object BukkitPlugin extends Plugin {
	sealed trait BukkitLoadTime
	case object BukkitLoadOnStartup extends BukkitLoadTime
	case object BukkitLoadPostWorld extends BukkitLoadTime

	case class BukkitCommand(aliases: Seq[String])

	val bukkitVersion = SettingKey[String]("bukkit-version", "Version of bukkit to compile against")
	val craftbukkitVersion = SettingKey[String]("craftbukkit-version", "Version of craftbukkit to test against")

	val bukkitPluginAuthors = SettingKey[Seq[String]]("bukkit-plugin-authors", "Authors of the bukkit plugin")
	val bukkitPluginLoadTime = SettingKey[BukkitLoadTime]("bukkit-plugin-load", "When the plugin should be loaded")
	val bukkitPluginClass = TaskKey[Option[String]]("bukkit-plugin-class", "Bukkit plugin main class")
	val bukkitPluginSoftDependencies = SettingKey[Seq[String]]("bukkit-plugin-soft-dependencies")
	val bukkitPluginDependencies = SettingKey[Seq[String]]("bukkit-plugin-dependencies")
	val bukkitPluginCommands = SettingKey[Map[String, BukkitCommand]]("bukkit-plugin-commands")

	val discoveredBukkitPluginClasses = TaskKey[Seq[String]]("bukkit-plugin-classes", "Discovered Bukkit plugin main classes")

	val bukkitPluginManifest = TaskKey[Map[String, Any]]("bukkit-plugin-manifest", "Bukkit plugin manifest")
	val generateBukkitPluginManifest = TaskKey[Seq[File]]("bukkit-plugin-generate-manifest")

	val bukkitSettings = Seq[Project.Setting[_]](
		bukkitVersion := "1.2.5-R3.0",
		craftbukkitVersion <<= bukkitVersion,

		bukkitPluginAuthors := Seq(),
		bukkitPluginLoadTime := BukkitLoadPostWorld,

		discoveredBukkitPluginClasses <<= compile in Compile map { analysis =>
			Discovery(Set("org.bukkit.plugin.java.JavaPlugin"), Set.empty)(Tests.allDefs(analysis)) collect { case (definition, discovered) if(discovered.baseClasses contains "org.bukkit.plugin.java.JavaPlugin") => definition.name }
		} storeAs discoveredBukkitPluginClasses triggeredBy (compile in Compile),
		bukkitPluginClass in run <<= discoveredBukkitPluginClasses.map(SelectMainClass(Some(SimpleReader readLine _), _)),
		bukkitPluginClass <<= discoveredBukkitPluginClasses.map(SelectMainClass(None, _)),
		bukkitPluginSoftDependencies := Seq(),
		bukkitPluginDependencies := Seq(),
		bukkitPluginCommands := Map(),

		bukkitPluginManifest <<= (name, version, bukkitPluginAuthors, bukkitPluginLoadTime, bukkitPluginClass, bukkitPluginSoftDependencies, bukkitPluginDependencies, bukkitPluginCommands) map {
			(name, version, authors, load, plugin, softDeps, deps, commands) => Map(
				"name" -> name,
				"version" -> version,
				"authors" -> authors.asJava,
				"load" -> (load match {
					case BukkitLoadOnStartup => "STARTUP"
					case BukkitLoadPostWorld => "POSTWORLD"
				}),
				"main" -> (plugin getOrElse sys.error("No Bukkit plugin main class detected.")),
				"softdepend" -> softDeps.asJava,
				"depend" -> deps.asJava,
				"commands" -> (commands map { case (name, metadata) =>
					name -> Map(
						"alias" -> metadata.aliases.asJava
					).asJava
				} asJava)
			)
		},
		generateBukkitPluginManifest <<= (bukkitPluginManifest, resourceManaged) map { (manifest, targetDir) =>
			val yaml = new Yaml
			val strManifest = yaml.dump(manifest.asJava)
			val target = targetDir / "plugin.yml"
			IO.write(target, strManifest)
			Seq[File](target)
		},

		resolvers += "Bukkit releases" at "http://repo.bukkit.org/content/repositories/releases/",
		libraryDependencies <+= bukkitVersion("org.bukkit" % "bukkit" % _),
		libraryDependencies <+= craftbukkitVersion("org.bukkit" % "craftbukkit" % _ % "runtime"),
		libraryDependencies <+= craftbukkitVersion("org.bukkit" % "craftbukkit" % _ % "test"),

		resourceGenerators in Compile <+= generateBukkitPluginManifest
	)
}