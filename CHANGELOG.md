# Changelog
All notable changes to this project will be documented in this file.

## 5.0.0 - 2026-11-02

### Added
- Nether, End and Ultimate mob farms
- Energy version of every mob farm: it needs Forge Energy to work and the creative tab has them filled with energy
- Redstone control: a lever placed on a mob farm stops it when it is OFF and starts it when it is ON
- Progress bar in the mob farm menu
- Speed Upgrade (up to 4, valid for the Iron, Gold, Diamond and Emerald farms) and Mob Slot Upgrade (second mob shard slot, valid for every farm)
- Ultimate Mob Shard: right clicking the mob is enough to complete it, unbreakable by default
- Config to make the Mob Shard unbreakable. The Advanced Mob Shard is now always unbreakable
- Mob blacklist and whitelist (`config/perfectmobfarmrevived/blacklist.json` and `whitelist.json`, and the entity type tags `perfectmobfarmrevived:blacklist` and `perfectmobfarmrevived:whitelist`)
- The captured mob spins inside the farm and the farm throws particles when it generates loot
- JEI and Jade integration
- Spanish translation
- Data generation for models, blockstates, language files, recipes, loot tables and tags

### Changed
- This is the Minecraft 26.3 version of the mod (NeoForge 26.3.0.101)
- The mod id is now `perfectmobfarmrevived` (it was `perfectmobfarm`), so blocks and items of old worlds are not kept
- Mob shards now store their linked mob and kill count in a data component instead of NBT
- The five mob farms now share a single parametrized block and block entity
- Mob farms are opened and automated with the NeoForge capability system
- The mod now uses the NeoForge configuration system (server config)

### Fixed
- Resources and data packs updated to the 26.3 format (recipes, loot tables and tags)

## 1.2.0 - 07.07.2022

### Added
- Add possibility to customize Mob Shard durability

### Modified
- The mod now use a server configuration
  -  Config file is now located in the `saves` or `world` folder

### Fixed
- Fix crashes on dedicated servers caused by client setup


## 1.1.0 - 23.06.2022

### Added
- Emerald and Netherite mob farms
    - Emerald farms have a looting 1 effect
    - Netherite farms have a looting 3 effect
- New shard : Advanced mob shard
- Configuration file
    - Player can customize mob farms cooldown and the kill count needed to fill up a shard

### Modified
- Mob farms loot generation
    - They now act as if the player has killed the mob

### Fixed
- Mob shards were not damaged when Items.AIR were generated


## 1.0.0

- First release
