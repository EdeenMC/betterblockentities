# BBE Renderer Registration API

The Renderer Registration API allows mods to register their own Block Entity Renderers to run alongside BBE. This is especially useful
for mods that need to add extra model geometry to a block entity we seek out to optimize.

If there is functionality missing from the API, please contact us and let us know of what you like to be added. We would appreciate not
resorting to using mixins to add functionality that isn't there.

## Example use cases



    - You have a mod that needs to render a party hat on top of a shulkerbox.
    - You have a mod that needs to render some text anywhere on a chest

....

Any use case where additional stuff needs to be rendered alongside the original block entity model
is a perfect match for this.

## Overview

All that is need from you as a developer is a block entity renderer that implements our
provided interfaces, an entrypoint that is declared in your `fabric.mod.json` and one
or several calls to register your renderer(s)

Each renderer can be "configured" as preferred and allows you as a user to control basically every aspect within
your own context. We also provide some function that you can implement into your renderer which you can use to turn off
BBE rendering, control when your renderer is allowed to run, etc...

## Getting Started

### *1) Including The API In Your Project

We publish all our build artifacts to our maven repository, to find a suitible api or mod build for your project you can go over to
`https://maven.edeenmc.net` which presents all our current uploaded builds via easy to use graphical interface

Step one is to add an `modImplementation` or an `implementation` (depending on what fabric loom version you are using) 
into your dependencies in your mods build script.


Example dependencies block:

```kotlin
dependencies {
    // ... other dependencies
    
    implementation ( "net.edeenmc:bbe-fabric:{BUILD_NAME}" )
}
```


Make sure you have our maven repository declared:

```kotlin
maven {
    // ... other maven repositories
    
    maven ( "https://maven.edeenmc.net/snapshots" ) //for snapshot builds
    maven ( "https://maven.edeenmc.net/releases" ) //for release builds
}
```

### *2) Creating an Entrypoint

Step two: after you have imported the api, simply create an entrypoint class (can be named
whatever you want) and make it implement `BBEApiEntryPoint`

Make sure you implement the required function `registerRenderers(...)`. Inside the implemented
registration function, you will be handed a context which you then later will use to register as many renderer(s) as you need.

After you have your entrypoint setup, make sure you refrence this entrypoint in your `fabric.mod.json`
file under the entrypoints section

Example:
```json5
{
  "entrypoints": {
    // ... other entrypoints

    "bbe_alt_renderers": [
      "com.example.examplemod.compact.BBEEntryPoint" //your entrypoint class
    ]
  }
}
```

### *3) Creating An Registrable Renderer
Now that we have our entrypoint where we handle renderer registration, we do need a renderer.
The registered renderer is setup much like how any other `BlockEntityRenderer` is.
Instead of explaining exactly how to setup your renderer we will provide you with an example, including
comments explaining what elements needs changing.

Example skeleton renderer for a Copper-Golem-Statue:
```java
package com.example.examplemod;

//api imports
import betterblockentities.api.render.AltRenderer;
import betterblockentities.api.render.AltRendererProvider;

//minecraft; mojang imports
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.CopperGolemStatueRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;

//make sure you implement AltRenderer here
public class SuperCoolCGSRenderer implements AltRenderer<CopperGolemStatueBlockEntity, CopperGolemStatueRenderState> {
    public SuperCoolCGSRenderer(final AltRendererProvider.Context context) {
        //... make sure that each constructor that takes in a provider
        //    context uses AltRendererProvider.Context and not the vanilla
        //    equivalent BlockEntityRendererProvider.Context
    }

    public CopperGolemStatueRenderState createRenderState() {
        return new CopperGolemStatueRenderState();
    }

    public void extractRenderState(
            final CopperGolemStatueBlockEntity blockEntity,
            final CopperGolemStatueRenderState state,
            final float partialTicks,
            final Vec3 cameraPosition,
            final ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        //make sure you don't forget to call the super class function here
        //also make sure AltRenderer is used
        AltRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        //... do your renderstate setup
    }

    public void submit(
            final CopperGolemStatueRenderState state,
            final PoseStack poseStack,
            final SubmitNodeCollector submitNodeCollector,
            final CameraRenderState camera
    ) {
        //... do rendering logic, submit with submitNodeCollector...
    }
    
    //implement shouldRender(...) for user controlled renderer management
    //implement dedicatedRenderer(...) to stop BBE rendering
}
```

For simplicity, we recommend to take a look at a vanilla `BlockEntityRenderer`, copying it, and changing out
the parts the need to change to be able to register said renderer:
- `implements BlockEntityRenderer` needs to be changed to `implements AltRenderer`
- Any references to `BlockEntityRendererProvider.Context` needs to be changed to `AltRendererProvider.Context`
- And any `BlockEntityRenderer.super` calls needs to be swaped out for `AltRenderer.super` equivalents instead

The superclass `AltRenderer` provides the function `shouldRender(...)` which can be implemented into your renderer to control when the renderer is allowed
to run (user controlled via the logic you put inside, returns a boolean)

We also expose a function called `dedicatedRenderer()` which you can override
and set to `true` to stop BBE from rendering anything related to said block entity. Something worth mentioning is that this function
only takes effect once a resource reload has happened and all the renderers are recreated (meaning you can't set this at runtime dynamically
and make it take effect immediately, without first triggering a resource reload or renderer recreation).
This override is also global and locked after being set, meaning once you set it, no other mod can unset it as long as you override the function

### *4) Registering Your Renderer

Cool, we are nearly done, the hard parts are over :)

Once you have your renderer(s) and your entrypoint class setup, all you have to do
is revisit `registerRenderers()` and use the given context to register your renderer(s)
with `registerRenderer(...)`

Something to note is that we support multiple registrations to the same BlockEntityType and
as many registrations to different block entity types as your heart desires, so go crazy!

When registering a renderer you will need to pass in two arguments:
- `SupportedBlockEntityTypes blockEntityType` - will be the `BlockEntityType` that your renderer belongs to (aka the BlockEntity you want to render additional stuff to)
- `AltRendererProvider rendererProvider` - this will be your renderer that implements `AltRenderer`

Here is a full example of renderer registration;

```java
public class BBEEntryPoint implements BBEApiEntryPoint {
    @Override
    public void registerRenderers(AltRendererRegistration context) {
        context.registerRenderer(SupportedBlockEntityTypes.COPPER_GOLEM_STATUE, SuperCoolCGSRenderer::new);
    }
}
```

## API Notes

We currently do not provide a way to include model geometry into terrain meshes, meaning what you render won't be optimized by BBE.
Making sure your registered renderer is as optimized as it can be will be up to you. We simply provide a way to render your stuff the "regular"
way via the immediate rendering pipeline. As a result of this, performance will be around the same as running any other BlockEntityRender and can
therefor give the illusion of that BBE is not doing its job correctly even though we are

### Notice
We assume no responsibility or liability for any actions taken by developers using this API. All use of the API is at the developer’s own discretion 
and risk. This includes, but is not limited to, instances where the API is used to create, distribute, or render content that may be deemed illegal, 
offensive, or otherwise in violation of applicable international or domestic laws. Developers are solely responsible for ensuring that their use of the 
API complies with all relevant laws and regulations






