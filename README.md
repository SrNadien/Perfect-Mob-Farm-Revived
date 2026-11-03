# Perfect Mob Farm Revived
Perfect Mob Farm Revived is a port of **Perfect Mob Farm**, the original mod by **Sweetmimike**, to **Minecraft 26.1.2** and **NeoForge 26.1.2.126**.

## Presentation

Perfect Mob Farm Revived is a simple mod for Minecraft **26.1.2** (NeoForge) that adds new blocks called Mob Farms. These new blocks allow the player to generate loot passively after having correctly setup the mob farms.

To setup a mob farm, first you have to craft a **Mob Shard** and link it to a mob type by right clicking on the mob you want to generate loot. Then, when your mob shard is linked to a mob, you have to fill it by killing mob of the same type. Finally, just put the shard in the mob farm, put a container (chest, hopper...) right below it and enjoy ! Your mob farm will start **generating** loot.

**Curse forge link** : https://www.curseforge.com/minecraft/mc-mods/perfect-mob-farm

Feel free to use this mod in any modpack 😉

## Items and blocks

Every mob farm comes in two versions: the normal one, and an **Energy** version that needs Forge Energy (FE) to work.

| Mob farm | Generates loot every | Looting |
|---|---|---|
| **Iron** | 40 seconds | - |
| **Gold** | 20 seconds | - |
| **Diamond** | 10 seconds | - |
| **Emerald** | 5 seconds | 1 |
| **Netherite** | 2 seconds | 3 |
| **Nether** | 1.5 seconds | 4 |
| **End** | 1 second | 5 |
| **Ultimate** | 0.5 seconds | 6 |

Cooldowns, kill counts, durabilities and energy usage can be changed in the server config.

### Mob shards
- **Mob Shard**: needs 10 kills. It can be made unbreakable in the config.
- **Advanced Mob Shard**: needs 10 kills. Always unbreakable.
- **Ultimate Mob Shard**: right clicking the mob is enough to complete it. Unbreakable by default, the config can turn that off.

### Energy farms
- Craft an energy farm by surrounding a normal farm of the same tier with 4 redstone blocks.
- It uses `Energy base per tick` (10 FE/t) doubled for each tier, and holds one minute of energy. Both values are configurable.
- It accepts energy from any side.
- The creative tab also has every energy farm already filled with energy.

### Farm upgrades
The mob farm menu has slots for two upgrades:
- **Speed Upgrade**: up to 4 in the speed slot, each one lowers the cooldown by 20% (configurable). Valid for the **Iron, Gold, Diamond and Emerald** farms, normal and energy versions.
- **Mob Slot Upgrade**: adds a **second mob shard slot**. Valid for every farm, normal and energy. Each cycle generates the loot of both shards.

### Visuals
The captured mob spins inside the farm (both mobs when there are two shards) and the farm throws particles every time it generates loot.

### Redstone (lever)
Place a lever on a mob farm: while the lever is **OFF** the farm is stopped, while it is **ON** the farm works. A farm without a lever always works.

### Mob blacklist and whitelist
- `config/perfectmobfarmrevived/blacklist.json` (Ender Dragon, Wither and Elder Guardian by default) and `whitelist.json` (empty by default, an empty whitelist allows every mob) can be edited to choose which mobs can be captured.
- The entity type tags `perfectmobfarmrevived:blacklist` and `perfectmobfarmrevived:whitelist` do the same from a datapack. They are only used when they contain something.

### Integration with Ultimate Armoury: Tools And Weapons
When [Ultimate Armoury: Tools And Weapons](https://www.curseforge.com/minecraft/mc-mods/ultimate-armoury-tools-and-weapons) is loaded, three more tiers are available (normal and energy versions). They only exist in the **1.21.1** and **26.1.2** versions of this mod, the only ones Ultimate Armoury is released for:

| Mob farm | Generates loot every | Looting |
|---|---|---|
| **Mushashite** | 0.3 seconds | 7 |
| **Joanfoite** | 0.2 seconds | 8 |
| **Nadienite** | 0.05 seconds | 10 |

### Mod integrations
- **JEI**: description pages for the shards and every farm.
- **Jade**: shows the captured mob, the kills, the state of the farm, a progress bar and the energy.

## Incoming Feature
- [X] ~~More mob farm~~
- [X] ~~Advanced mob shard~~
- [X] ~~Custom config~~
- [X] ~~Nether, End and Ultimate mob farms~~
- [X] ~~Mob farm menu progress bar~~
- [X] ~~Ultimate mob shard and unbreakable shards~~
- [X] ~~Mob blacklist and whitelist~~
- [X] ~~Energy (energy versions of every farm)~~
- [X] ~~Redstone control with a lever~~
- [X] ~~Ultimate Armoury: Tools And Weapons integration~~
- [X] ~~JEI integration~~
- [X] ~~Jade integration~~
- [X] ~~Data generation (models, blockstates, lang, recipes, loot tables and tags)~~
- [X] ~~Farm upgrade (Speed, additional mob slot, ...)~~
- [X] ~~Mob farm visual improvement~~

## Usage
This project is under the MIT License.

## Credits
Perfect Mob Farm was created by **Sweetmimike**. This port to 26.1.2 NeoForge is maintained by **SrNadien**.

Perfect Mob farm is inspired by [Tiny Mob farm](https://www.curseforge.com/minecraft/mc-mods/tiny-mob-farm).

Thanks to all the people of the [Forge forum](https://forums.minecraftforge.net/) for the help.
