# BBE Sable Fix (fork)

Fork of **Better Block Entities (BBE)** `1.3.4-beta.3+mc1.21.1` for **NeoForge 1.21.1**,
fixing invisible block entities placed on **Sable / Create: Aeronautics** sublevels
(contraptions).

- Upstream: https://github.com/EdeenMC/betterblockentities
- Base: `1.21.1-backport` branch tip (`17b1ba7`, "mod version bump" = `1.3.4-beta.3+mc1.21.1`,
  the most recent NeoForge 1.21.1 release on Modrinth)
- Branch in this fork: `sable-fix`
- Built jar: `../bbe-neoforge-1.3.4-beta.3-sablefix+mc1.21.1.jar`
  (renamed copy of `build/mod/bbe-neoforge-1.3.4-beta.3+mc1.21.1.jar` for clarity;
  internal mod version is unchanged so it stays a drop-in replacement)

## The bug

BBE assumes every supported block entity lives in main-world terrain meshed by
Sodium. A new block entity defaults to `renderingMode=TERRAIN` +
`terrainMeshReady=true`, so BBE's `BlockEntityRenderDispatcher` mixin immediately
culls the vanilla block-entity renderer (BER) and waits for Sodium to bake the
model into the terrain chunk.

Sable sublevels are **not** meshed by Sodium's main-world pipeline. They live in
isolated plot chunks and are rendered by Sable's own `SubLevelRenderDispatcher`,
which collects block entities for vanilla BER rendering and never bakes BBE
static terrain models. Net effect: a block entity placed on a contraption is
culled from the BER path while no terrain mesh ever appears for it — invisible.
Interacting with it flips BBE into `IMMEDIATE` mode, re-enabling BER, which is
exactly the reported "culled until you interact with it" symptom.

## The fix

New helper `common/.../client/compat/SableCompat.java` detects (via reflection,
so Sable stays an **optional** dependency — no crash and zero behaviour change
when Sable isn't installed) whether a block entity is inside a Sable sublevel
(`Sable.HELPER.getContaining(BlockEntity)`), and such block entities are kept on
the vanilla path permanently:

1. `BlockEntityRenderDispatcherMixin.shouldManage()` returns `false` for sublevel
   BEs → vanilla BER is never culled for them.
2. `InstancedBlockEntityManager.run()/tick()/trigger()` pins sublevel BEs to
   `IMMEDIATE` + `terrainMeshReady=false` and never queues main-world section
   rebuilds for their plot positions.
3. `BBEBlockRenderer.emitBlockModel()` skips baking sublevel BEs into Sodium
   main-world terrain.
4. `ChunkBuilderMeshingTaskMixin` leaves sublevel BEs at their vanilla render
   shape instead of forcing `MODEL`.
5. `LidControllerSync.setImmediate()` skips the (useless) main-world rebuild for
   sublevel chests.

## Build

Requires JDK 21 (project targets Java 21; e.g. Temurin 21):

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
.\gradlew.bat :neoforge:modJar -Pbuild.release --console=plain
# -> build/mod/bbe-neoforge-1.3.4-beta.3+mc1.21.1.jar
```

Verified: `BUILD SUCCESSFUL`, and the jar contains
`betterblockentities/client/compat/SableCompat.class`.

## Install / test

1. Minecraft 1.21.1 + NeoForge 21.1.228 + Sodium 0.8.12 + Sable + Create: Aeronautics.
2. Remove upstream BBE, drop in this jar (client-side).
3. Place a chest/sign/barrel-class BE on a contraption sublevel → it should render
   immediately without needing interaction. Main-world BEs should behave exactly
   as upstream (meshed into terrain, animated via BER when opened).

## Notes

- No new dependencies; Sable detection is soft (reflection with fail-open to
  upstream behaviour).
- License: same as upstream (LGPL-3.0-or-later).
