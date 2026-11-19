# Wizarding World: Hogwarts Legacy — Fabric 26.3

A multi-file Fabric mod project for Minecraft 26.3. Open the repository in GitHub and let the included Actions workflow build the playable JAR.

## Included content

- 49 wand varieties plus the Elder Wand (50 total).
- A 2,000-entry spell data archive with descriptions and spell selection commands.
- Wand casting with visible End Rod particle tracers; the Elder Wand bypasses spell-learning checks. There is no cast cooldown in the current implementation.
- 200 ingredient items and 100 potion items; potion items currently apply a restorative effect when used.
- Four brooms: Cleansweep 11, Nimbus 2000, Firebolt and Comet 260.
- Quidditch Quaffle, Bludger and Golden Snitch items.
- Galleons, Sickles and Knuts (1 Galleon = 17 Sickles; 1 Sickle = 29 Knuts).
- Cauldron placement, sorting quiz, simple vendors, and buildable Hogwarts grounds, Hagrid's hut, Forbidden Forest, Quidditch pitch, Platform 9¾, the Dursleys' house and the Quidditch World Cup stadium.

## Commands

- `/harrypotter hogwarts` — build Hogwarts grounds, Hagrid's hut, Forbidden Forest and a Quidditch pitch.
- `/harrypotter 9 3/4` — build a simple Platform 9¾ scene.
- `/harrypotter dursleys` — build the Dursleys' house.
- `/harrypotter quidditch world cup` — build the World Cup pitch and stand.
- `/harrypotter spells` — show spell archive page 1.
- `/harrypotter spellpage <1-100>` — browse all 2,000 spells, 20 at a time.
- `/harrypotter spell <1-2000>` — select a spell.
- `/harrypotter wand <1-50>` — receive one of the 50 wands.
- `/harrypotter elderwand` — receive the Elder Wand.
- `/harrypotter ingredient <1-200>` and `/harrypotter potion <1-100>` — receive catalog items.
- `/harrypotter brooms` — receive all four brooms.
- `/harrypotter quidditchballs` — receive the three Quidditch balls.
- `/harrypotter cauldron` — place a cauldron in front of you.
- `/harrypotter wallet` — show wizarding currency exchange rates.

The legacy `/hogwarts` commands are also retained.

## Build on GitHub

1. Create/open your GitHub repository (the previous project used `harry-potter`).
2. Upload the contents of this ZIP so that `build.gradle`, `gradle.properties`, `settings.gradle`, `src/`, and `.github/workflows/build.yml` are at the repository root.
3. Open **Actions** and run **Build Wizarding World Mod** (or push a commit).
4. Open the successful workflow run, scroll to **Artifacts**, and download `WizardingWorldFabric-JAR`.
5. Put the non-sources `.jar` in your Minecraft `mods` folder alongside Fabric API for Minecraft 26.3.

## Important current limitations

The spell archive browser is currently command/chat-based, not a dedicated clickable GUI screen. The cauldron is placeable but full ingredient-combination brewing is not implemented yet. The four broom items enable creative-style flight rather than a custom broom-riding entity. Structure builds are compact block-built approximations and require a clear area. This source has not been successfully compiled against a live Minecraft 26.3 toolchain in this environment; the GitHub Actions build is the verification step. If Actions fails, share the complete log and I can correct the exact error.


## Import this project into GitHub

1. Download and extract this ZIP on your computer.
2. On GitHub, create a **new empty repository** (do not add a README or .gitignore).
3. Open the extracted `WizardingWorldFabric` folder.
4. Upload the **contents of that folder**, including the hidden `.github` folder, into the GitHub repository's main page.
5. Open the repository's **Actions** tab. The workflow named **Build Wizarding World Mod** runs automatically after the files are pushed. You can also select it and choose **Run workflow**.
6. When the run finishes successfully, open that run, scroll to **Artifacts**, and download `WizardingWorldFabric-JAR`.

**Important:** The source project is packaged here, but a successful GitHub Actions build is still required before the JAR is ready to install. If the workflow fails, share the red error log so the build can be corrected.
