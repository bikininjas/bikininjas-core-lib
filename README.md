# core-lib

Bibliothèque NeoForge partagée pour tous les mods Minecraft Bikininjas (1.21.1+).

## Modules

| Module | Package | Description |
|---|---|---|
| **CoreLib** | `com.bikininjas.corelib` | Point d'entrée `@Mod`, enregistrement des `DeferredRegister`, initialisation |
| **ModLogger** | `.log` | Logger structuré avec préfixe `[modId][ClassName]` + `ErrorBuilder` fluent |
| **Registers** | `.registry` | `DeferredRegister` centralisés (ITEMS, BLOCKS, BLOCK_ENTITY_TYPES, ENTITY_TYPES) |
| **TimeManager** | `.time` | Contrôle tick rate, freeze temps, ratio jour/nuit |
| **SpawnHelper** | `.entity` | Spawn d'entités avec offset mathématique |
| **EnchantmentUtils** | `.enchantment` | Cap enchantement niveau 100 |
| **MessageHelper** | `.message` | Chat, title, actionbar, broadcast, formatting |
| **WorldUtils** | `.world` | Remplissage blocs, requêtes entités dans zone |
| **PlayerState** | `.player` | Record état joueur + manager thread-safe |
| **Kit** | `.kit` | Kits nommés (items, armure, offhand, effets) + manager thread-safe |
| **RandomEvent** | `.randomevent` | Interface événement aléatoire + manager singleton (pondéré, cooldown) |
| **Restriction** | `.restriction` | 5 types d'actions restreignables, handlers NeoForge |
| **Recipe** | `.recipe` | Builder fluent shaped/shapeless/smelting + API add/remove/sync |
| **Stats** | `.stats` | Stats joueur (morts, kills, blocs, crafts) + HUD overlay + préférences |
| **Network** | `.network` | `CustomPacketPayload` + `StreamCodec` pour sync stats client |
| **Challenge** | `.objective` | Système d'objectifs sealed + challenges + tracker |
| **Command** | `.command` | Enregistrement commandes (`/kit list`, `/kit give`) |
| **GameTest** | `.gametest` | 40 tests in-game via `@GameTestHolder` |

## Stack

- **Minecraft 1.21.1**, **NeoForge 21.1.238**, **Java 21**, **Gradle 9.6.1**
- Mappings Parchment 2024.11.17
- NeoGradle moddev 2.0.142

## Build

```bash
./gradlew build test   # Build + 17 tests JUnit 5
./gradlew runServer    # Lancement serveur avec GameTests
```

## CI/CD

| Workflow | Déclencheur | Action |
|---|---|---|
| **CI** | Push/PR sur master | `./gradlew build test` (Java 21, setup-gradle@v6) |
| **CD** | Après succès CI (`workflow_run`) | Auto-version semver → build JAR → GitHub Release |

**Règle : la CD ne se lance JAMAIS en parallèle du push — elle dépend de la CI via `workflow_run`.**

## Installation (mod enfant)

```gradle
// settings.gradle
includeBuild('../core-lib')

// build.gradle
dependencies {
    implementation 'com.bikininjas.corelib:core_lib:1.0.+'
}
```
