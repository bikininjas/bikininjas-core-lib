# Child Mod Handbook — Guide pour créer un mod enfant

Ce document explique comment créer un nouveau mod enfant (`mod-*`) qui dépend de `core-lib` et s'intègre dans le workspace multi-repos.

---

## 1. Architecture du Workspace

```
BikininjasMCMods/                  # Racine (workspace-runner)
├── settings.gradle                # includeBuild() pour core-lib + mods
├── build.gradle                   # Configuration commune
├── gradle/
│   └── libs.versions.toml         # Catalogue de dépendances centralisé
│
├── core-lib/                      # Dépôt : bikininjas-mclib
│   ├── API.md                     # Référence complète des APIs
│   ├── src/main/java/.../         # com.bikininjas.corelib.*
│   └── build.gradle
│
├── mod-funny-mobs/                # Dépôt : bikininjas-mcmod-*
│   └── ...
│
└── mod-op-items/                  # Dépôt : bikininjas-mcmod-*
    └── ...
```

### Principe des Composite Builds

Chaque dépôt est indépendant (git remote séparé). Le `settings.gradle` du workspace-runner les lie via `includeBuild()` :

```groovy
// workspace-runner/settings.gradle
rootProject.name = 'BikininjasMCMods'

includeBuild('core-lib') {
    dependencySubstitution {
        substitute module('com.bikininjas:core-lib') using project(':')
    }
}

includeBuild('mod-funny-mobs')
includeBuild('mod-op-items')
```

**Ne PAS** inclure les mods dans le `settings.gradle` de `core-lib`. Cela casserait la boucle de dépendance.

---

## 2. Créer un nouveau mod enfant

### 2.1. Structure de fichiers minimale

```
mod-mon-nouveau-mod/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── src/main/
│   ├── java/com/bikinijas/mymod/
│   │   └── MyMod.java              # Classe @Mod
│   └── resources/
│       └── META-INF/
│           └── neoforge.mods.toml
```

### 2.2. settings.gradle

```groovy
rootProject.name = 'mod-mon-nouveau-mod'
```

### 2.3. gradle.properties

```properties
mod_id=mon_nouveau_mod
mod_name=Mon Nouveau Mod
mod_version=0.1.0
mod_license=Apache-2.0
mod_author=Bikininjas
mod_description=Description du mod
mod_credits=Bikininjas
```

### 2.4. build.gradle

```groovy
plugins {
    id 'net.neoforged.moddev' version '2.0.74-beta'
}

base {
    archivesName = "mon_nouveau_mod-${mod_version}"
}

neoForge {
    version = '21.1.212'
    mods {
        "${mod_id}" {
            sourceSet sourceSets.main
        }
    }
    runs {
        client {}
        server {}
        gameTestServer {}
        data {}
    }
    unitTest {
        enableAll()
    }
}

dependencies {
    // core-lib est une dépendance api — toutes ses classes sont visibles
    implementation project(':core-lib')
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named('test', Test) {
    useJUnitPlatform()
}
```

### 2.5. neoforge.mods.toml

```toml
modLoader="javafml"
loaderVersion="[4,)"
license="${mod_license}"

[[mods]]
modId="${mod_id}"
version="${mod_version}"
displayName="${mod_name}"
authors="${mod_author}"
description='''${mod_description}'''
credits="${mod_credits}"

[[dependencies.${mod_id}]]
modId="neoforge"
mandatory=true
versionRange="[21.1,)"
ordering="NONE"
side="BOTH"

[[dependencies.${mod_id}]]
modId="core_lib"
mandatory=true
versionRange="[1,)"
ordering="BEFORE"
side="BOTH"
```

### 2.6. Classe principale @Mod

```java
package com.bikininjas.mymod;

import com.bikininjas.corelib.kit.Kit;
import com.bikininjas.corelib.kit.KitManager;
import com.bikininjas.corelib.objective.*;
import com.bikininjas.corelib.restriction.RestrictionManager;
import com.bikininjas.corelib.restriction.RestrictionType;
import com.bikininjas.corelib.world.WorldUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(MyMod.MODID)
public final class MyMod {
    public static final String MODID = "mon_nouveau_mod";

    public MyMod(IEventBus modBus) {
        registerKits();
        registerChallenges();
        registerRestrictions();

        System.out.println("✅ " + MODID + " initialized");
    }

    private void registerKits() {
        KitManager.register(new Kit("starter",
            new ItemStack[] {
                new ItemStack(Items.STONE_SWORD),
                new ItemStack(Items.COOKED_BEEF, 16)
            },
            new ItemStack[4],    // no armor
            ItemStack.EMPTY,     // no offhand
            List.of()
        ));
    }

    private void registerChallenges() {
        ChallengeRegistry.register(new ChallengeDefinition(
            "kill_dragon",
            "§cDragon Slayer",
            List.of(
                new KillObjective("Kill 10 Endermen", EntityType.ENDERMAN, 10),
                new KillObjective("Kill the Ender Dragon", EntityType.ENDER_DRAGON, 1)
            ),
            3600,    // 1 hour
            List.of("minecraft", MODID)
        ));
    }

    private void registerRestrictions() {
        RestrictionManager.register(RestrictionType.PLACE_BLOCK, "minecraft", "tnt");
    }
}
```

---

## 3. Conventions de code

### 3.1. Enregistrement (Registries)
- Utilisez EXCLUSIVEMENT `DeferredRegister` de NeoForge
- N'utilisez JAMAIS `Registry.register` directement

### 3.2. Client/Serveur
- Isolez le code client dans des packages `client/`
- Utilisez `@EventBusSubscriber(value = Dist.CLIENT, ...)` ou enregistrement explicite via `FMLClientSetupEvent`

### 3.3. Data-Driven
- Préférez les Data Maps, Tags et datapacks plutôt que des valeurs en dur

### 3.4. Mixins
- Dernier recours uniquement si aucun event NeoForge ne permet la modification

### 3.5. Logs
- Utilisez SLF4J (`org.slf4j.Logger`)

### 3.6. Tests
- **GameTest Framework** : tests d'intégration in-game pour objets/entités
- **JUnit 5** : tests unitaires pour la logique pure (via `unitTest { enableAll() }`)

---

## 4. Utiliser les APIs core-lib

### 4.1. TimeManager
```java
// Dans un event handler serveur
TimeManager.setNight(level);
TimeManager.toggleTimeFreeze(level);
```

### 4.2. MessageHelper
```java
MessageHelper.title(player, "§6Boss Arrives", "§cPrepare for battle!");
MessageHelper.actionBar(player, "§aHP: §f" + health);
MessageHelper.broadcastChat("§eServer event!", server);
Component msg = MessageHelper.format("&c&lWARNING! &rA &adragon &rappears!");
```

### 4.3. SpawnHelper
```java
SpawnHelper.spawnAtPlayer(player, EntityType.ZOMBIE);
SpawnHelper.spawnRandomNearby(level, EntityType.WITHER_SKELETON, pos, 10.0);
```

### 4.4. PlayerStateManager
```java
PlayerState state = PlayerStateManager.save(player);
// ... plus tard ...
PlayerStateManager.load(player, state);
```

### 4.5. RecipeAPI
```java
// Créer une recette shapeless
RecipeAPI.addRecipe("mymod:stuff",
    RecipeBuilder.shapeless(new ItemStack(Items.DIAMOND))
        .requires(Items.IRON_INGOT, 9)
        .build().orElseThrow()
);

// Synchroniser avec un joueur
RecipeAPI.syncToPlayer(player);
```

### 4.6. WorldUtils
```java
int changed = WorldUtils.fillArea(level, pos1, pos2, Blocks.STONE.defaultBlockState());
List<ServerPlayer> nearby = WorldUtils.getPlayersInRange(level, center, 20.0);
ServerPlayer nearest = WorldUtils.getNearestPlayer(level, pos, 50.0);
```

### 4.7. ObjectiveTracker
```java
// Démarrer un challenge
ObjectiveTracker.startChallenge(player, new Challenge(
    "Dragon Rush",
    List.of(new KillObjective("Kill Dragon", EntityType.ENDER_DRAGON, 1)),
    1800   // 30 minutes
));

// Vérifier progression
float progress = ObjectiveTracker.getProgress(player);
boolean done = ObjectiveTracker.isChallengeComplete(player);
```

### 4.8. RestrictionManager
```java
// Bloquer l'utilisation d'items
RestrictionManager.register(RestrictionType.USE_ITEM, "minecraft", "ender_pearl");
// Bloquer l'entrée dans une dimension
RestrictionManager.register(RestrictionType.ENTER_DIMENSION, "minecraft", "the_end");
```

---

## 5. Commandes disponibles

| Commande | Description | Fournie par |
|---|---|---|
| `/challenge list` | Liste les challenges disponibles | core-lib |
| `/challenge start <name>` | Démarre un challenge | core-lib |
| `/challenge status` | Progression du challenge actif | core-lib |
| `/challenge abort` | Annule le challenge actif | core-lib |
| `/stats` | Toggle l'overlay HUD stats | core-lib |
| `/stats <fields...>` | Sélectionne les champs affichés | core-lib |

---

## 6. Build & Test

```bash
# Build complet + tests
cd /chemin/vers/BikininjasMCMods/mod-mon-nouveau-mod
./gradlew build test

# Lancer le client Minecraft (dev)
./gradlew client

# Lancer le serveur Minecraft (dev)
./gradlew server

# Tests unitaires uniquement
./gradlew test
```

Les événements NeoForge utilisés par core-lib sont automatiquement disponibles :
- `ServerTickEvent.Post` — TimeManager, ObjectiveTracker, PlayerStatsManager
- `RegisterCommandsEvent` — ChallengeCommand, StatsCommand
- `LivingDeathEvent` — ObjectiveTracker, PlayerStatsManager
- `BlockEvent.BreakEvent` — PlayerStatsManager, RestrictionManager
- `PlayerEvent.ItemCraftedEvent` — PlayerStatsManager
- `EntityJoinLevelEvent` — RestrictionManager
- `EntityTravelToDimensionEvent` — RestrictionManager

---

## 7. Dépendance Gradle

Dans `build.gradle` du mod enfant :

```groovy
dependencies {
    implementation project(':core-lib')
}
```

Le `settings.gradle` du workspace-runner doit inclure `includeBuild('core-lib')`. Ainsi, Gradle résout `:core-lib` automatiquement via le composite build, sans publication Maven nécessaire en développement.

Pour la production, core-lib publie un JAR sur GitHub Releases (via CI/CD). Le mod enfant peut aussi le référencer comme dépendance Maven une fois publié.
