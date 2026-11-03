<!--
  CurseForge description of Perfect Mob Farm Revived, same layout as the original Perfect Mob Farm page.
  The images use the raw GitHub URL of the repo, they show up once docs/images is pushed to the branch.
-->

![farms](https://raw.githubusercontent.com/SrNadien/Perfect-Mob-Farm-Revived/Neo/1.21.1/docs/images/farms.png)

![energy farms](https://raw.githubusercontent.com/SrNadien/Perfect-Mob-Farm-Revived/Neo/1.21.1/docs/images/energy-farms.png)

**Perfect Mob Farm Revived**
-----------------------------

Perfect Mob Farm Revived is an unofficial port of [Perfect Mob Farm](https://www.curseforge.com/minecraft/mc-mods/perfect-mob-farm) by [Sweetmimike](https://www.curseforge.com/members/sweetmimike) to NeoForge. The original mod is under the MIT license, so I am allowed to fork it. It adds new blocks called **Mob Farms** that make getting mob loot **easier** and **automatic**. I fixed everything that broke in the port and added a lot of new stuff on top.

#### **Recipes**

Every farm is crafted from the farm of the tier before it (the Iron one from a Mob Shard). The recipes also show up in JEI.

- **Mob Shard:** 4 blaze powder around a diamond
- **Advanced Mob Shard:** a diamond in the middle, a Mob Shard on each side, an eye of ender above and below
- **Ultimate Mob Shard:** a netherite ingot in the middle, an Advanced Mob Shard on each side, a nether star above and below
- **Iron Mob Farm:** Mob Shard in the middle, 4 iron blocks on the corners, rotten flesh above and below, a bone on each side
- **Gold Mob Farm:** Iron farm in the middle, 4 gold blocks on the corners, slime balls above and below, an ender pearl on each side
- **Diamond Mob Farm:** Gold farm in the middle, 4 diamond blocks on the corners, blaze rods above and below, a ghast tear on each side
- **Emerald Mob Farm:** Diamond farm in the middle, 4 emeralds on the corners, gunpowder above and below, a magma cream on each side
- **Netherite Mob Farm:** Emerald farm in the middle, 4 netherite ingots on the corners, a nether star above, an eye of ender below, a quartz on each side
- **Nether Mob Farm:** Netherite farm in the middle, 4 netherite ingots on the corners, blaze rods above and below, a wither skeleton skull on each side
- **End Mob Farm:** Nether farm in the middle, 4 end crystals on the corners, dragon breath above and below, a shulker shell on each side
- **Ultimate Mob Farm:** End farm in the middle, 4 nether stars on the corners, dragon heads above and below, an enchanted golden apple on each side
- **Energy Mob Farm:** the normal farm of the same tier in the middle and a redstone block above, below and on each side
- **Speed Upgrade:** a diamond in the middle, 4 gold ingots on the corners, redstone above, below and on each side
- **Mob Slot Upgrade:** a diamond block in the middle, 4 eyes of ender on the corners, an emerald above, below and on each side

### **Instructions**

**How to setup a Mob farm**

- Craft a Mob Farm and place it **with a container below** (chest, barrel, hopper...)
- Craft a Mob Shard and fill it (see below)
- Right click the Mob Farm and put the filled shard inside
- Wait for your loot to be generated

**How to fill up a Mob Shard**

- Right click the mob you want with the Mob Shard in your hand, the shard is now linked to that mob type
- Kill 10 mobs of the same type with the shard in your inventory
- The texture of the shard changes when it is full

Do not forget to check that the container is not full, the farm stops when it has no room for the loot.

### **Items and blocks**

- **Iron** Mob Farm (generates loot every 40 seconds)
- **Gold** Mob Farm (20 seconds)
- **Diamond** Mob Farm (10 seconds)
- **Emerald** Mob Farm (5 seconds) + Looting 1
- **Netherite** Mob Farm (2 seconds) + Looting 3
- **Nether** Mob Farm (1.5 seconds) + Looting 4
- **End** Mob Farm (1 second) + Looting 5
- **Ultimate** Mob Farm (0.5 seconds) + Looting 6
- **Mob Shard** (10 kills, it loses durability every time the farm generates loot, the config can make it unbreakable)
- **Advanced Mob Shard** (10 kills, always unbreakable)
- **Ultimate Mob Shard** (right clicking the mob is enough to fill it, unbreakable by default and the config can change that)
- **Speed Upgrade** and **Mob Slot Upgrade**

### **Energy farms**

Every farm has an **Energy** version. It does the same thing but it needs Forge Energy to work. It takes energy from any side through the NeoForge energy system, so it works with the usual energy mods. It uses 10 FE/t on the Iron tier, the amount doubles with every tier, and it stores a minute of energy. Both numbers are in the config. The creative tab also has every energy farm already filled with energy, with the green bar on the item.

### **Redstone**

Place a **lever** on a farm. While the lever is OFF the farm is stopped and when you turn it ON the farm works. A farm without a lever always works.

### **Upgrades**

- **Speed Upgrade:** up to 4 in its slot of the farm menu, every one lowers the cooldown by 20%. They only work in the Iron, Gold, Diamond and Emerald farms, normal and energy.
- **Mob Slot Upgrade:** adds a second Mob Shard slot to any farm. Every cycle generates the loot of both shards, and both mobs spin inside the farm.

### **Ultimate Armoury integration**

If you have [Ultimate Armoury: Tools And Weapons](https://www.curseforge.com/minecraft/mc-mods/ultimate-armoury-tools-and-weapons) (my other mod) loaded, you get three more tiers after the Ultimate farm, normal and energy versions. They are crafted with the blocks and ingots of that mod and they only exist when it is loaded. These three farms are only available in the **1.21.1** and **26.1.2** versions of Perfect Mob Farm Revived, because Ultimate Armoury is only released for those versions.

- **Mushashite** Mob Farm (0.3 seconds) + Looting 7
- **Joanfoite** Mob Farm (0.2 seconds) + Looting 8
- **Nadienite** Mob Farm (0.05 seconds) + Looting 10

### **Mob blacklist and whitelist**

You choose which mobs can be captured. There are two files in `config/perfectmobfarmrevived/`, `blacklist.json` and `whitelist.json`. The blacklist has the Ender Dragon, the Wither and the Elder Guardian by default and the whitelist is empty. An empty whitelist means every mob is allowed. The same can be done from a datapack with the entity type tags `perfectmobfarmrevived:blacklist` and `perfectmobfarmrevived:whitelist`, they are only used when they have something inside.

### **Other mods**

- **JEI:** description pages for the shards, the upgrades and every farm.
- **Jade:** shows the captured mob, the state of the farm (active, disabled, no container, no energy), a progress bar and the upgrades.

### **Configuration File**

The config file is `perfectmobfarmrevived-server.toml` and it is in the `config` folder (the blacklist and whitelist are in `config/perfectmobfarmrevived/`). On the 1.20.1 Forge version it is in the `serverconfig` folder of the world instead. You can change:

- The time every Mob Farm needs to generate loot
- The number of kills needed to fill the shards
- The durability of the shards and if they are unbreakable
- The energy usage of the energy farms
- How much every Speed Upgrade lowers the cooldown

**How it works**
----------------

The Mob Farm generates the loot of the mob linked to the shard, as if a player had killed it, and puts it in the container below. Every time it generates loot the Mob Shard loses durability until it breaks, unless it is an unbreakable one. A progress bar in the farm menu shows how long it is left for the next loot, and the mob spins inside the farm while it works.

If you find a bug or you have a suggestion, tell me in the comments. The source code is on [GitHub](https://github.com/SrNadien/Perfect-Mob-Farm-Revived).

**Credits**

- **[Sweetmimike](https://www.curseforge.com/members/sweetmimike):** author of the original Perfect Mob Farm
- **NadienDev:** port and new features

Perfect Mob Farm is inspired by Tiny Mob Farm. Feel free to use this mod in any modpack.
