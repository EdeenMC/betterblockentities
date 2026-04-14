package betterblockentities.client.gui.config.builder;

/* local */
import betterblockentities.client.BBE;
import betterblockentities.client.gui.option.EnumTypes;
import betterblockentities.client.gui.storage.SodiumConfigStorage;

/* sodium */
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.option.Range;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls;

/* minecraft */
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SodiumConfigBuilder implements ConfigEntryPoint {
    private final SodiumConfigStorage bbeSodiumConfigStorage = new SodiumConfigStorage();
    private final StorageEventHandler saveMainConfigStorageObject = bbeSodiumConfigStorage::save;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        OptionPageBuilder BBEPage = builder.createOptionPage().setName(Component.translatable("bbe.config.sodium.pagetext"));

        BBEPage.addOptionGroup(builder.createOptionGroup()
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:master"))
                            .setName(Component.translatable("bbe.config.storage.main.optimize.master"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.optimize.master.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.HIGH)
                            .setBinding(
                                value -> BBE.GlobalScope.CONFIG.MAIN.setOption("optimize.master", value),
                                () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("optimize.master").getValue()
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            ));
        BBEPage.addOptionGroup(builder.createOptionGroup()
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:optimize.chest"))
                            .setName(Component.translatable("bbe.config.storage.main.optimize.chest"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.optimize.chest.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.HIGH)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("optimize.chest", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("optimize.chest").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")), Identifier.parse("bbe:master")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
            .addOption(
                   builder.createBooleanOption(Identifier.parse("bbe:animation.chest"))
                            .setName(Component.translatable("bbe.config.storage.main.animation.chest"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.animation.chest.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.LOW)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("animation.chest", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("animation.chest").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:optimize.chest")),
                                    Identifier.parse("bbe:master"), Identifier.parse("bbe:optimize.chest")
                            )
                           .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                           .setStorageHandler(this.saveMainConfigStorageObject)
            )
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:misc.christmas_chest"))
                            .setName(Component.translatable("bbe.config.storage.main.misc.christmas_chest"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.misc.christmas_chest.tooltip"))
                            .setDefaultValue(false)
                            .setImpact(OptionImpact.LOW)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("misc.christmas_chest", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("misc.christmas_chest").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:optimize.chest")),
                                    Identifier.parse("bbe:master"), Identifier.parse("bbe:optimize.chest")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
        );

        BBEPage.addOptionGroup(builder.createOptionGroup()
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:optimize.shulker"))
                            .setName(Component.translatable("bbe.config.storage.main.optimize.shulker"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.optimize.shulker.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.HIGH)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("optimize.shulker", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("optimize.shulker").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")), Identifier.parse("bbe:master")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:animation.shulker"))
                            .setName(Component.translatable("bbe.config.storage.main.animation.shulker"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.animation.shulker.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.LOW)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("animation.shulker", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("animation.shulker").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:optimize.shulker")),
                                    Identifier.parse("bbe:master"), Identifier.parse("bbe:optimize.shulker")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
        );

        BBEPage.addOptionGroup(builder.createOptionGroup()
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:optimize.sign"))
                            .setName(Component.translatable("bbe.config.storage.main.optimize.sign"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.optimize.sign.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.HIGH)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("optimize.sign", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("optimize.sign").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")), Identifier.parse("bbe:master")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:misc.sign_text"))
                            .setName(Component.translatable("bbe.config.storage.main.misc.sign_text"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.misc.sign_text.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.MEDIUM)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("misc.sign_text", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("misc.sign_text").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:optimize.sign")),
                                    Identifier.parse("bbe:master"), Identifier.parse("bbe:optimize.sign")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
            .addOption(
                    builder.createIntegerOption(Identifier.parse("bbe:misc.sign_text_distance"))
                            .setName(Component.translatable("bbe.config.storage.main.misc.sign_text_distance"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.misc.sign_text_distance.tooltip"))
                            .setDefaultValue(16)
                            .setImpact(OptionImpact.MEDIUM)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("misc.sign_text_distance", value),
                                    () -> (int) BBE.GlobalScope.CONFIG.MAIN.getOption("misc.sign_text_distance").getValue()
                            )
                            .setRange(new Range(1, 64, 1))
                            .setValueFormatter(ControlValueFormatterImpls.number())
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:optimize.sign")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:misc.sign_text")),
                                    Identifier.parse("bbe:master"), Identifier.parse("bbe:optimize.sign"),
                                    Identifier.parse("bbe:misc.sign_text")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:misc.sign_text_culling"))
                            .setName(Component.translatable("bbe.config.storage.main.misc.sign_text_culling"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.misc.sign_text_culling.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.MEDIUM)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("misc.sign_text_culling", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("misc.sign_text_culling").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:optimize.sign")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:misc.sign_text")),
                                    Identifier.parse("bbe:master"), Identifier.parse("bbe:optimize.sign"),
                                    Identifier.parse("bbe:misc.sign_text")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
        );

        BBEPage.addOptionGroup(builder.createOptionGroup()
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:optimize.decoratedpot"))
                            .setName(Component.translatable("bbe.config.storage.main.optimize.decoratedpot"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.optimize.decoratedpot.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.HIGH)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("optimize.decoratedpot", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("optimize.decoratedpot").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")), Identifier.parse("bbe:master")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:animation.decoratedpot"))
                            .setName(Component.translatable("bbe.config.storage.main.animation.decoratedpot"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.animation.decoratedpot.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.LOW)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("animation.decoratedpot", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("animation.decoratedpot").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:optimize.decoratedpot")),
                                    Identifier.parse("bbe:master"), Identifier.parse("bbe:optimize.decoratedpot")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
        );

        BBEPage.addOptionGroup(builder.createOptionGroup()
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:optimize.banner"))
                            .setName(Component.translatable("bbe.config.storage.main.optimize.banner"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.optimize.banner.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.HIGH)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("optimize.banner", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("optimize.banner").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")), Identifier.parse("bbe:master")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
            .addOption(
                    builder.createIntegerOption(Identifier.parse("bbe:misc.banner_pose"))
                            .setName(Component.translatable("bbe.config.storage.main.misc.banner_pose"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.misc.banner_pose.tooltip"))
                            .setDefaultValue(16)
                            .setImpact(OptionImpact.MEDIUM)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("misc.banner_pose", value),
                                    () -> (int) BBE.GlobalScope.CONFIG.MAIN.getOption("misc.banner_pose").getValue()
                            )
                            .setRange(new Range(1, 9, 1))
                            .setValueFormatter(ControlValueFormatterImpls.number())
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:optimize.banner")),
                                    Identifier.parse("bbe:master"), Identifier.parse("bbe:optimize.banner")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
            .addOption(
                    builder.createEnumOption(Identifier.parse("bbe:misc.banner_graphics"), EnumTypes.BannerGraphicsType.class)
                            .setName(Component.translatable("bbe.config.storage.main.misc.banner_graphics"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.misc.banner_graphics.tooltip"))
                            .setDefaultValue(EnumTypes.BannerGraphicsType.FANCY)
                            .setImpact(OptionImpact.VARIES)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("misc.banner_graphics", EnumTypes.BannerGraphicsType.map(value)),
                                    () -> EnumTypes.BannerGraphicsType.map((int)BBE.GlobalScope.CONFIG.MAIN.getOption("misc.banner_graphics").getValue())
                            )
                            .setElementNameProvider(e -> new Component[]{
                                    Component.translatable("bbe.config.storage.main.misc.banner_graphics.type.fast"),
                                    Component.translatable("bbe.config.storage.main.misc.banner_graphics.type.fancy"),
                            }[e.ordinal()])
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:optimize.banner")),
                                    Identifier.parse("bbe:master"), Identifier.parse("bbe:optimize.banner")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
        );

        BBEPage.addOptionGroup(builder.createOptionGroup()
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:optimize.bell"))
                            .setName(Component.translatable("bbe.config.storage.main.optimize.bell"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.optimize.bell.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.HIGH)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("optimize.bell", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("optimize.bell").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")), Identifier.parse("bbe:master")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:animation.bell"))
                            .setName(Component.translatable("bbe.config.storage.main.animation.bell"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.animation.bell.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.LOW)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("animation.bell", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("animation.bell").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")) &&
                                    c.readBooleanOption(Identifier.parse("bbe:optimize.bell")),
                                    Identifier.parse("bbe:master"), Identifier.parse("bbe:optimize.bell")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
        );

        BBEPage.addOptionGroup(builder.createOptionGroup()
            .addOption(
                    builder.createBooleanOption(Identifier.parse("bbe:optimize.bed"))
                            .setName(Component.translatable("bbe.config.storage.main.optimize.bed"))
                            .setTooltip(Component.translatable("bbe.config.storage.main.optimize.bed.tooltip"))
                            .setDefaultValue(true)
                            .setImpact(OptionImpact.HIGH)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("optimize.bed", value),
                                    () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("optimize.bed").getValue()
                            )
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")), Identifier.parse("bbe:master")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
        );

        BBEPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(Identifier.parse("bbe:optimize.copper_golem_statue"))
                                .setName(Component.translatable("bbe.config.storage.main.optimize.copper_golem_statue"))
                                .setTooltip(Component.translatable("bbe.config.storage.main.optimize.copper_golem_statue.tooltip"))
                                .setDefaultValue(true)
                                .setImpact(OptionImpact.HIGH)
                                .setBinding(
                                        value -> BBE.GlobalScope.CONFIG.MAIN.setOption("optimize.copper_golem_statue", value),
                                        () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("optimize.copper_golem_statue").getValue()
                                )
                                .setEnabledProvider(c ->
                                        c.readBooleanOption(Identifier.parse("bbe:master")), Identifier.parse("bbe:master")
                                )
                                .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                .setStorageHandler(this.saveMainConfigStorageObject)
                )
        );

        BBEPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(Identifier.parse("bbe:optimize.shelf"))
                                .setName(Component.translatable("bbe.config.storage.main.optimize.shelf"))
                                .setTooltip(Component.translatable("bbe.config.storage.main.optimize.shelf.tooltip"))
                                .setDefaultValue(true)
                                .setImpact(OptionImpact.HIGH)
                                .setBinding(
                                        value -> BBE.GlobalScope.CONFIG.MAIN.setOption("optimize.shelf", value),
                                        () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("optimize.shelf").getValue()
                                )
                                .setEnabledProvider(c ->
                                        c.readBooleanOption(Identifier.parse("bbe:master")), Identifier.parse("bbe:master")
                                )
                                .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                .setStorageHandler(this.saveMainConfigStorageObject)
                )
        );

        BBEPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(Identifier.parse("bbe:optimize.campfire"))
                                .setName(Component.translatable("bbe.config.storage.main.optimize.campfire"))
                                .setTooltip(Component.translatable("bbe.config.storage.main.optimize.campfire.tooltip"))
                                .setDefaultValue(true)
                                .setImpact(OptionImpact.HIGH)
                                .setBinding(
                                        value -> BBE.GlobalScope.CONFIG.MAIN.setOption("optimize.campfire", value),
                                        () -> (boolean) BBE.GlobalScope.CONFIG.MAIN.getOption("optimize.campfire").getValue()
                                )
                                .setEnabledProvider(c ->
                                        c.readBooleanOption(Identifier.parse("bbe:master")), Identifier.parse("bbe:master")
                                )
                                .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                                .setStorageHandler(this.saveMainConfigStorageObject)
                )
        );

        BBEPage.addOptionGroup(builder.createOptionGroup()
            .addOption(
                    builder.createEnumOption(Identifier.parse("bbe:misc.update_scheduler"), EnumTypes.UpdateSchedulerType.class)
                            .setName(Component.translatable("bbe.config.storage.main.misc.update_scheduler"))
                            .setTooltip(
                                    BBE.ModCompact.isSchedulerOptionLimited() ?
                                            Component.translatable("bbe.config.storage.main.misc.update_scheduler.tooltip_notavailable") :
                                            Component.translatable("bbe.config.storage.main.misc.update_scheduler.tooltip")
                            )
                            .setDefaultValue(EnumTypes.UpdateSchedulerType.SMART)
                            .setImpact(OptionImpact.VARIES)
                            .setBinding(
                                    value -> BBE.GlobalScope.CONFIG.MAIN.setOption("misc.update_scheduler", EnumTypes.UpdateSchedulerType.map(value)),
                                    () -> EnumTypes.UpdateSchedulerType.map((int)BBE.GlobalScope.CONFIG.MAIN.getOption("misc.update_scheduler").getValue())
                            )
                            .setElementNameProvider(e -> new Component[]{
                                    Component.translatable("bbe.config.storage.main.misc.update_scheduler.type.fast"),
                                    Component.translatable("bbe.config.storage.main.misc.update_scheduler.type.smart"),
                            }[e.ordinal()])
                            .setEnabledProvider(c ->
                                    c.readBooleanOption(Identifier.parse("bbe:master")) && !BBE.ModCompact.isSchedulerOptionLimited(), Identifier.parse("bbe:master")
                            )
                            .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                            .setStorageHandler(this.saveMainConfigStorageObject)
            )
        );

        builder.registerOwnModOptions()
                .setNonTintedIcon(Identifier.parse("betterblockentities:icon.png"))
                .setColorTheme(builder.createColorTheme().setBaseThemeRGB(0xc68d46)) //old 0x603900
                .addPage(BBEPage);
    }
}
