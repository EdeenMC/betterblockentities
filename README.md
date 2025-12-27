# Better Block Entities

BBE is a **client side** mod that drastically improves block entity rendering, and we aim to improve and regularly update the mod between Minecraft versions.

**Will I lose animations like FastChest and similar mods?**
No you won’t, that’s the neat part. You get the **HUGE** performance increase without compromising animations! (If you want, you can turn this off if you don’t like it)
On top of that, we also optimize more than only chests, like:

- Shulker Boxes
- Signs
- Decorated Pots
- Beds
- Bells
- Chests (ender, trapped, and all the new copper variants)

**We are planning on adding even more block entities to that list in the future!**

## How does the mod work?

The mod works by rendering certain supported block entities conditionally in the regular terrain or in “meshes.” The mod switches between using Minecraft’s intended path of rendering for block entities when they are animating, and statically meshing them into the terrain when they are not. This improves FPS by a lot and scales depending on how many block entities you have. Expect at least a **3× increase!** We also offer options to turn off certain optimizations for each individual supported block through the mod menu options.

**Some added benefits :**
- Being able to see these block entities from much further away (just like regular blocks)
- Much nicer looking lighting / Ambient Occlusion (Smooth Lighting)

## Mod Compatibility
Currently, [Sodium](https://modrinth.com/mod/sodium) is a dependency, but we plan on making it optional in the future. [Fabric API](https://modrinth.com/mod/fabric-api) is needed as well.

**Mod Support:** We aim to support as many mods as possible, but as many other developers know, it’s hard to support them all and incompatibilities will occur. If your mod does not work with BBE and you feel like it should, please reach out to us in our [Discord](https://discord.gg/NdX9BYpTtz) server.
Out of the box we support nearly all your favorite optimization mods like:

- Sodium
- Lithium
- Nvidium
- ImmediatelyFast
- C2ME

And of course, the list goes on. Some known mods that do not work with BBE fully enabled are Traben’s [EMF] [Entity Model Features](https://modrinth.com/mod/entity-model-features)  / [ETF] [Entity Texture Features](https://modrinth.com/mod/entitytexturefeatures) mods. If you want these to work alongside BBE, you will have to turn off optimizations for the block entity you want EMF/ETF to modify through our config screen. The same goes for resource packs that modify any of the enabled block entities, like add new parts or custom animations. All texture/resource packs that only touch block entity textures will work.

## Before and Afters
![Performance Test: ~29000 chests](https://cdn.modrinth.com/data/cached_images/3599c5d4f5b67bfd46c03ae471bd0ad1ed82dc88.png)

## FAQ and Help
**Q- What Minecraft versions will the mod be supporting?**                                       
A- Minecraft versions from 1.21.6 and above

**Q- Are backports planned?**                                                                              
A- No backports are planned

**Q- My chests (or any other block entity) is invisible/glitched, what do I do?**                   
A- You are most likely using a resource pack that conflicts with BBE, either disable the block entity optimizations through the config screen or turn off the resource pack.

**Q- What other mods/dependencies do I need?**                                                                    
A- For now, Fabric API and Sodium.

**Q- Can I use any Sodium version with BBE?**                                                                 
A- No, we always update BBE to use the latest stable Sodium version out for a Minecraft version. And that version has to be used when loading the mod.

**Q- Are other mod loaders going to be supported?**                                                        
A- Yes, a NeoForge variant is planned.

**Q- Is BBE compatible with resource packs and shaders?**                                           
A- Yes! You can load most resource packs and shaders with BBE no problem. Check the "Mod Compatibility" section above for some more info on resource packs and EMF/ETF.



