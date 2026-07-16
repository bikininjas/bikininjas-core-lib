# core-lib API Reference

Modules utilisables par les mods enfants (`mod-*`). Tous les packages sont sous `com.bikininjas.corelib.*`.

---

## 1. TimeManager — `time/`

Contrôle du cycle jour/nuit serveur. Utility class finale, tout en static, pas de singleton.

```java
import com.bikininjas.corelib.time.TimeManager;
```

| Méthode | Description |
|---|---|
| `setTime(ServerLevel, long timeOfDay)` | Fixe l'heure absolue (0–23999) |
| `setDay(ServerLevel)` | Raccourci → midday (1000) |
| `setNight(ServerLevel)` | Raccourci → midnight (13000) |
| `addTime(ServerLevel, long ticks)` | Ajoute des ticks à l'heure actuelle |
| `setTimeRate(ServerLevel, float rate)` | Multiplicateur de temps (0=gELÉ, 1=normal, 72=72×) |
| `toggleTimeFreeze(ServerLevel)` | Bascule gel/reprise |
| `isTimeFrozen(ServerLevel)` → `boolean` | État gel |
| `getDayTime(ServerLevel)` → `long` | Heure actuelle |
| `computeExtraTicks(float rate, float tickDelta)` → `long` | Pure math, testable sans runtime |

**Bus event :** ✅ S'abonne à `ServerTickEvent.Post` via static block. Forcer le class load via `TimeManager.computeExtraTicks(1.0f, 1.0f)` dans initModules().

---

## 2. SpawnHelper — `entity/`

Utilitaires de spawn d'entités. Utility class, pas de bus.

```java
import com.bikininjas.corelib.entity.SpawnHelper;
```

| Méthode | Description |
|---|---|
| `spawnAt(ServerLevel, EntityType<T>, Vec3)` → `T` | Spawn à une position exacte |
| `spawnAt(ServerLevel, EntityType<T>, double x, double y, double z)` → `T` | Spawn à des coordonnées |
| `spawnWithConfig(...)` | Spawn avec configuration avancée |
| `spawnRandomNearby(ServerLevel, EntityType<T>, Vec3 center, double radius)` → `T` | Spawn aléatoire dans un rayon |
| `spawnAtPlayer(ServerPlayer, EntityType<T>, double distance)` | Spawn devant un joueur |
| `spawnAtPlayer(ServerPlayer, EntityType<T>)` | Spawn à 3 blocs |
| `spawnNearPlayer(ServerPlayer, EntityType<T>, double radius)` | Spawn aléatoire près d'un joueur |
| `spawnMobAtPlayer(ServerPlayer, EntityType<? extends Monster>)` | Spawn monstre près d'un joueur |
| `randomOffset(double radius, double angle, double fraction)` → `Vec3` | Pure math (uniform disk) |
| `circlePosition(Vec3 center, double radius, int index, int total)` → `Vec3` | Positions circulaires |

---

## 3. EnchantmentUtils — `enchantment/`

Application d'enchantements avec cap à 100. Utility class.

```java
import com.bikininjas.corelib.enchantment.EnchantmentUtils;
```

| Méthode | Description |
|---|---|
| `applyEnchantment(ItemStack, Holder<Enchantment>, int level)` | Applique un enchantement (level ≤ 100) |
| `applyEnchantments(ItemStack, Map<Holder<Enchantment>, Integer>)` | Applique plusieurs enchantements |
| `getMaxLevelForTool(Holder<Enchantment>, ItemStack)` → `int` | Max level pour cet outil (cap 100) |
| `canEnchantAtLevel(Holder<Enchantment>, int level)` → `boolean` | Vérifie level ≤ 100 |

**Constante :** `MAX_LEVEL = 100`

---

## 4. RandomEventManager — `randomevent/`

Moteur d'événements aléatoires avec cooldown et sélection pondérée. Singleton.

```java
import com.bikininjas.corelib.randomevent.*;
RandomEventManager mgr = RandomEventManager.getInstance();
```

| Méthode | Description |
|---|---|
| `register(RandomEvent)` | Enregistre un événement |
| `register(RandomEvent, String name)` | Avec clé explicite |
| `remove(String name)` | Supprime un événement |
| `setEnabled(boolean)` | Active/désactive |
| `setInterval(int min, int max)` | Cooldown en ticks |
| `fireRandomEvent(ServerLevel)` | Déclenche un événement aléatoire |
| `fireEvent(String name, ServerLevel, Vec3 origin)` | Déclenche un événement nommé |
| `selectRandomEvent()` → `RandomEvent` | Sélection pondérée |
| `getAllEvents()` → `List<String>` | Liste des noms |
| `getEventCount()` → `int` | Nombre d'événements |
| `reset()` | Reset complet |

**Interface `RandomEvent` :**
```java
@FunctionalInterface
public interface RandomEvent {
    void execute(ServerLevel level, Vec3 origin);
    String name();
    int weight();        // 0 = jamais sélectionné
}
```

**Factory `RandomEvents` :** `announceEvent(String/Component)`, `spawnEntityEvent(EntityType, count)`, `randomExplosionEvent(power, fire)`, `clearWeatherEvent()`, `randomWeatherEvent()`.

**Bus event :** ✅ S'abonne à `ServerTickEvent.Post`. Instance créée via `getInstance()`.

---

## 5. PlayerStateManager — `player/`

Sauvegarde/restauration complète de l'état d'un joueur. Utility class.

```java
import com.bikininjas.corelib.player.PlayerState;
import com.bikininjas.corelib.player.PlayerStateManager;
```

| Méthode | Description |
|---|---|
| `save(ServerPlayer)` → `PlayerState` | Capture tout l'état |
| `load(ServerPlayer, PlayerState)` | Restaure l'état |
| `clear(ServerPlayer)` | Clear inventaire + stats |

**Record `PlayerState` :**
```java
record PlayerState(
    ItemStack[] mainInventory,     // 36 slots
    ItemStack[] armorInventory,    // 4 slots
    ItemStack offhand,
    float health,
    int food,
    float saturation,
    int xpLevel,
    float xpProgress,
    Collection<MobEffectInstance> effects,
    GameType gameType
)
```

**Bus event :** non.

---

## 6. KitManager — `kit/`

Registre statique de kits (ensembles d'items). Thread-safe.

```java
import com.bikininjas.corelib.kit.Kit;
import com.bikininjas.corelib.kit.KitManager;
```

| Méthode | Description |
|---|---|
| `register(Kit)` | Enregistre un kit |
| `get(String name)` → `Kit` | Récupère un kit |
| `getAll()` → `List<String>` | Liste des noms |
| `remove(String name)` | Supprime un kit |
| `clear()` | Vide le registre |
| `give(ServerPlayer, String name)` → `boolean` | Donne le kit (false si inconnu) |

**Record `Kit` :**
```java
record Kit(String name, ItemStack[] items, ItemStack[] armor, ItemStack offhand, Collection<MobEffectInstance> effects)
```

**Bus event :** non.

---

## 7. ObjectiveTracker — `objective/`

Système de challenges/objectifs événementiel. Utility class avec event handler.

```java
import com.bikininjas.corelib.objective.*;
```

| Méthode | Description |
|---|---|
| `startChallenge(ServerPlayer, Challenge)` | Démarre un challenge |
| `stopChallenge(ServerPlayer)` | Stoppe le challenge actif |
| `addObjective(ServerPlayer, Objective)` | Ajoute un objectif |
| `removeObjective(ServerPlayer, String description)` | Supprime un objectif |
| `isTracking(ServerPlayer)` → `boolean` | Challenge actif ? |
| `getProgress(ServerPlayer)` → `float` | Progression (0.0–1.0) |
| `getElapsedSeconds(ServerPlayer)` → `long` | Temps écoulé |
| `currentTick()` → `long` | Tick serveur actuel |
| `getActiveChallengeName(ServerPlayer)` → `String` | Nom du challenge |
| `getObjectives(ServerPlayer)` → `List<Objective>` | Objectifs du challenge |
| `isChallengeComplete(ServerPlayer)` → `boolean` | Challenge fini ? |
| `saveToPlayer(ServerPlayer)` | Persiste dans persistent data |
| `loadFromPlayer(ServerPlayer)` | Restaure depuis persistent data |

**Interface scellée `Objective` :**
```java
sealed interface Objective permits KillObjective, CollectObjective, ReachObjective, SurvivalObjective {
    String description();
    boolean isComplete(ServerPlayer);
    float progress(ServerPlayer);       // 0.0 – 1.0
    int progressValue(ServerPlayer);     // valeur actuelle
    int target();                        // seuil de complétion
    ObjectiveType type();                // KILL | COLLECT | REACH | SURVIVE
}
```

**Implémentations :** `KillObjective(description, EntityType, target)`, `CollectObjective(description, Item, target)`, `ReachObjective(description, BlockPos pos, double radius)`, `SurvivalObjective(description, int durationTicks)`.

**Record `Challenge` :** `Challenge(String name, List<Objective>, int timeLimitSeconds)`

**Bus event :** ✅ S'abonne à `LivingDeathEvent`, `ItemEntityPickupEvent$Post`, `ServerTickEvent$Post` via static inner `ObjectiveHandler`. Forcer le class load via `ObjectiveTracker.currentTick()`.

---

## 8. ChallengeRegistry — `objective/`

Registre de définitions de challenges (templates). Filters par mods chargés.

```java
import com.bikininjas.corelib.objective.ChallengeDefinition;
import com.bikininjas.corelib.objective.ChallengeRegistry;
```

| Méthode | Description |
|---|---|
| `register(ChallengeDefinition)` | Enregistre une définition |
| `get(String name)` → `ChallengeDefinition` | Récupère par nom |
| `getAll()` → `List<ChallengeDefinition>` | Toutes les définitions |
| `getAvailable()` → `List<ChallengeDefinition>` | Définitions dont les mods requis sont chargés |
| `areModsLoaded(ChallengeDefinition)` → `boolean` | Vérifie les mods requis |
| `clear()` | Vide le registre |

**Record `ChallengeDefinition` :**
```java
record ChallengeDefinition(
    String name,
    String displayName,
    List<? extends Objective> objectives,
    int timeLimitSeconds,
    List<String> requiredMods    // modids requis (ex: ["minecraft", "core_lib"])
)
```

**Bus event :** non.

---

## 9. MessageHelper — `message/`

Utilitaires de messagerie (chat, title, actionbar, broadcast). Utility class.

```java
import com.bikininjas.corelib.message.MessageHelper;
```

**Chat :** `chat(ServerPlayer, String|Component)`

**Title :** `title(ServerPlayer, title, subtitle[, fadeIn, stay, fadeOut])` — defaults 10/70/20 ticks

**Action bar :** `actionBar(ServerPlayer, String|Component)`

**Broadcast (via MinecraftServer) :** `broadcastChat(String|Component, server)`, `broadcastTitle(title, subtitle, ... , server)`, `broadcastActionBar(String, server)`

**Formatage :**
| Méthode | Description |
|---|---|
| `text(String)` → `Component` | Component littéral |
| `red(String)`, `green(String)`, `blue(String)`, `gold(String)` ... | 14 couleurs |
| `format(String)` → `Component` | Parse `&`-codes (`&c`, `&l`, `&a`...) |

**Bus event :** non.

---

## 10. PlayerStatsManager — `stats/`

Compteurs automatiques (morts, kills, blocs cassés, crafts). Utility class.

```java
import com.bikininjas.corelib.stats.PlayerStats;
import com.bikininjas.corelib.stats.PlayerStatsManager;
```

| Méthode | Description |
|---|---|
| `getStats(ServerPlayer)` → `PlayerStats` | Stats complètes |
| `getStats(UUID)` → `PlayerStats` | Par UUID |
| `getDeaths(ServerPlayer)` → `int` | |
| `getKills(ServerPlayer)` → `int` | |
| `getBlocksBroken(ServerPlayer)` → `int` | |
| `getCrafts(ServerPlayer)` → `int` | |
| `resetStats(ServerPlayer)` | Remet à zéro |
| `init()` | Force le class load (appelé par CoreLib) |

**Record `PlayerStats` :** `PlayerStats(int deaths, int kills, int blocksBroken, int crafts)`, `PlayerStats.EMPTY`

**Événements écoutés :** `LivingDeathEvent` (morts + kills), `BlockEvent.BreakEvent` (blocs), `PlayerEvent.ItemCraftedEvent` (crafts)

**Bus event :** ✅ S'abonne via `StatsHandler` inner class. Forcer le class load via `PlayerStatsManager.init()`.

---

## 11. RestrictionManager — `restriction/`

API pour bloquer des actions (placement/bloc, utilisation item, spawn entité, entrée dimension).

```java
import com.bikininjas.corelib.restriction.RestrictionType;
import com.bikininjas.corelib.restriction.RestrictionManager;
```

**`RestrictionType` enum :** `PLACE_BLOCK`, `BREAK_BLOCK`, `USE_ITEM`, `SPAWN_ENTITY`, `ENTER_DIMENSION`

| Méthode | Description |
|---|---|
| `register(RestrictionType, ResourceLocation)` | Bloque une action |
| `register(RestrictionType, String namespace, String path)` | Raccourci |
| `isRestricted(RestrictionType, ResourceLocation)` → `boolean` | Vérifie si bloqué |
| `unregister(RestrictionType, ResourceLocation)` | Débloque |
| `clear(RestrictionType)` | Débloque tout d'un type |
| `clear()` | Débloque tout |
| `getAll(RestrictionType)` → `Set<ResourceLocation>` | Liste des restrictions |
| `init()` | Force le class load |

**Événements écoutés :** `BlockEvent.EntityPlaceEvent`, `BlockEvent.BreakEvent`, `PlayerInteractEvent.RightClickItem`, `EntityJoinLevelEvent`, `EntityTravelToDimensionEvent`

**Exemple :**
```java
// Bloquer le placement de TNT
RestrictionManager.register(RestrictionType.PLACE_BLOCK, "minecraft", "tnt");

// Bloquer l'entrée dans le Nether
RestrictionManager.register(RestrictionType.ENTER_DIMENSION, "minecraft", "the_nether");

// Vérifier
boolean blocked = RestrictionManager.isRestricted(RestrictionType.PLACE_BLOCK, 
    ResourceLocation.parse("minecraft:tnt"));
```

**Bus event :** ✅ S'abonne via `RestrictionHandler`. Forcer le class load via `RestrictionManager.init()`.

---

## 12. RecipeAPI — `recipe/`

Création et suppression programmatique de recettes. Utility class.

```java
import com.bikininjas.corelib.recipe.RecipeAPI;
import com.bikininjas.corelib.recipe.RecipeBuilder;
```

| Méthode | Description |
|---|---|
| `addRecipe(String id, RecipeHolder<?>)` | Ajoute une recette |
| `removeRecipe(String id)` | Supprime une recette |
| `syncToPlayer(ServerPlayer)` | Synchronise les recettes modifiées à un joueur |
| `syncToAll(MinecraftServer)` | Synchronise à tous les joueurs |

**Fluent `RecipeBuilder` :**
```java
// Shaped (3×3)
RecipeAPI.addRecipe("my_mod:custom_sword",
    RecipeBuilder.shaped(new ItemStack(Items.DIAMOND_SWORD))
        .pattern(" D ", " D ", " S ")
        .where('D', Items.DIAMOND_BLOCK)
        .where('S', Items.STICK)
        .build().orElseThrow()
);

// Shapeless
RecipeAPI.addRecipe("my_mod:stuff",
    RecipeBuilder.shapeless(new ItemStack(Items.NETHERITE_INGOT))
        .requires(Items.DIAMOND, 4)
        .requires(Items.EMERALD, 4)
        .build().orElseThrow()
);

// Smelting
RecipeAPI.addRecipe("my_mod:special_ingot",
    RecipeBuilder.smelting(new ItemStack(Items.GOLD_INGOT), Items.IRON_INGOT, 1.0f, 200)
        .build().orElseThrow()
);
```

**Bus event :** non.

---

## 13. WorldUtils — `world/`

Utilitaires de manipulation du monde. Utility class.

```java
import com.bikininjas.corelib.world.WorldUtils;
```

| Méthode | Description |
|---|---|
| `setBlock(Level, BlockPos, BlockState)` → `boolean` | Pose un bloc |
| `getBlock(Level, BlockPos)` → `BlockState` | Récupère un bloc |
| `isAir(Level, BlockPos)` → `boolean` | Vérifie si air |
| `fillArea(Level, BlockPos from, BlockPos to, BlockState)` → `int` | Remplit une zone (nb blocs changés) |
| `getChunk(Level, BlockPos)` → `LevelChunk` | Récupère un chunk |
| `isLoaded(Level, BlockPos)` → `boolean` | Chunk chargé ? |
| `getEntities(Level, Class<T>)` → `List<T>` | Toutes les entités d'un type |
| `getEntitiesInRange(Level, BlockPos, double radius, Class<T>)` → `List<T>` | Entités dans un rayon |
| `getPlayersInRange(Level, BlockPos, double radius)` → `List<ServerPlayer>` | Joueurs dans un rayon |
| `getNearestPlayer(Level, BlockPos, double radius)` → `ServerPlayer` | Joueur le plus proche |

**Bus event :** non.

---

## 14. Stats HUD Overlay — `client/` & `stats/` & `network/`

Overlay HUD stats toggleable et persistant côté client. Configuré automatiquement.

```java
import com.bikininjas.corelib.stats.StatsDisplayPrefs;
```

| Méthode | Description |
|---|---|
| `StatsDisplayPrefs.toggle(ServerPlayer)` | Active/désactive l'overlay |
| `StatsDisplayPrefs.setVisibleFields(ServerPlayer, Set<String>)` | Champs visibles (deaths, kills, blocksBroken, crafts) |
| `StatsDisplayPrefs.getVisibleFields(ServerPlayer)` → `Set<String>` | Champs actifs |
| `StatsDisplayPrefs.isEnabled(ServerPlayer)` → `boolean` | Overlay visible ? |

**Network :** paquet `StatsSyncPayload` (core_lib:stats_sync) envoyé automatiquement toutes les 20 ticks.

**Rendu :** `StatsOverlayRenderer` dessine un panneau semi-transparent à droite de l'écran via `RenderGuiEvent.Post`.

---

## 15. Registers — `registry/`

DeferredRegisters centralisés (Items, Blocks, BlockEntityTypes, EntityTypes).

```java
import com.bikininjas.corelib.registry.Registers;
```

| Registre | Type |
|---|---|
| `Registers.ITEMS` | `DeferredRegister.Items` |
| `Registers.BLOCKS` | `DeferredRegister.Blocks` |
| `Registers.BLOCK_ENTITY_TYPES` | `DeferredRegister<BlockEntityType<?>>` |
| `Registers.ENTITY_TYPES` | `DeferredRegister<EntityType<?>>` |

Tous sont enregistrés sur le mod bus dans `CoreLib(IEventBus modBus)`.

---

## Intégration minimale dans un mod enfant

```java
// build.gradle
dependencies {
    implementation project(':core-lib')  // composite build
}

// Dans votre classe @Mod
public MyMod(IEventBus modBus) {
    // Vos DeferredRegisters
    MY_ITEMS.register(modBus);
    
    // Utiliser les APIs core-lib directement :
    KitManager.register(new Kit("starter", ...));
    ChallengeRegistry.register(new ChallengeDefinition("my_challenge", ...));
    RestrictionManager.register(RestrictionType.PLACE_BLOCK, "minecraft", "tnt");
}
```
