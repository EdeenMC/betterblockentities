## Better Block Entities

BBE is a **client side** Minecraft mod for the <a href="https://fabricmc.net/use/">Fabric</a> mod loader which aims to increase block entity perfomance and enhancing their visuals, as well as providing support for block entity customisation.

**How does it work?** BBE makes some block entities use baked block models rather than laggy block entity models by implementing an hybrid renderer on Sodium's rendering pipeline. 

**Is it just an optimization mod?** EBE *isn't* just an optimization mod, it has some benefits such as:

- Smooth lightning (or ambient occlusion) on block entities.
- Toggling features like christmas chests.
- Being able to see block entites from much further away, just like regular blocks.

**What about animations? Do I lose animations like <a href="https://fabricmc.net/use/">FastChest</a> does?** No, additionally, the best part of BBE is that you still get to keep animations, and you can either enable them or disable them in the mod's settings! Most animated block entity models will only render with its usual block entity models when absolutely necessary.

## BBE 1.3.0-beta.1+ will work in <a href="https://modrinth.com/mod/sodium">Sodium</a> 0.8.0+ without additional dependencies.

**ONLY** for earlier BBE versions, use <a href="https://modrinth.com/mod/fabric-api">Fabric API</a> alongside Sodium 0.7.0 and below.

**Requires <a href="https://modrinth.com/mod/sodium">Sodium</a> for all versions of BBE, as BBE was made with Sodium, but in the future BBE will no longer require Sodium.**

## FAQ and help

**Q- What Minecraft versions will the mod be supporting?**

**A-** Minecraft versions from 1.21.6 and above.

**Q- Are backports planned?**

**A-** No backports are planned.

**Q- What other mods/dependencies do I need?**

**A-** For now, Fabric API and Sodium. Fabric API is no longer required since BBE 1.3.0-beta.1 and above.

**Q- Are other mod loaders going to be supported?**

**A-** Yes, a NeoForge variant is planned.

**Q- Is BBE compatible with resource packs and shaders?**

**A-** Yes, you can use resource packs and shaders without any problems, but keep in mind that ETF / EMF support is not 100% guaranteed and some resource packs might not work properly.

**Q- I need help with the mod/need to report a bug!**

**A-** Please report an issue in the <a href="https://github.com/ceeden/betterblockentities/issues">GitHub repository</a> of the mod for incompatibilities or crashes with other mods when using BBE. We recommend to join our <a href="https://discord.gg/NdX9BYpTtz">Discord server</a> and ask for help out there.

## FPS Boost

![An image of a player comparing the optimizations of Vanilla and BBE with a lot of chests rendering in a superflat world.](https://cdn.modrinth.com/data/cached_images/3599c5d4f5b67bfd46c03ae471bd0ad1ed82dc88.png)

## Compatibility

BBE is compatible with mods such as Sodium, Lithium, C2ME, ImmediatelyFast, ModernFix, etc; meaning that you can have BBE's perfomance and visual benefits while playing with your favourite optimization mods.

Since the release of <a href="https://modrinth.com/mod/better-block-entities/version/1.3.0-rc.1+1.21.11">BBE v1.3.0-rc.1+1.21.11</a>, BBE is compatible with <a href="https://modrinth.com/mod/entity-model-features">Entity Model Features</a> and <a href="https://modrinth.com/mod/entitytexturefeatures">Entity Texture Features</a>, but with limitations. Certain resource packs might cause to render out of place or being rotated weirdly. Not all resource packs that depend of these mods work with BBE and it's up to the resource pack creator to add compatibility with BBE's block model optimizations.
