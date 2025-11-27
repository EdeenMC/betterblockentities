package betterblockentities.gui;

/* minecraft */
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

/*
    TODO: clean this shit up lol
*/

public class ConfigScreen extends GameOptionsScreen {
    private final ConfigHolder originalConfig;

    private SimpleOption<Boolean> masterToggle;
    private SimpleOption<Boolean>
            chestOpt,
            signOpt,
            shulkerOpt,
            bedOpt,
            bellOpt,
            potOpt,
            chestAnimOpt,
            signTextOpt,
            shulkerAnimOpt,
            bellAnimOpt,
            potAnimOpt;
    private SimpleOption<Integer>
            updateType,
            smoothness;
    private SimpleOption<Integer> signDistance;

    public ConfigScreen(Screen parent) {

        super(parent, MinecraftClient.getInstance().options, Text.translatable("config.betterblockentities.title"));
        this.originalConfig = ConfigManager.CONFIG.copy();
    }

    @Override
    protected void addOptions() {
        if (this.body == null) return;

        masterToggle = masterToggle();
        chestOpt = optimizeChests();
        signOpt = optimizeSigns();
        shulkerOpt = optimizeShulkers();
        bedOpt = optimizeBeds();
        bellOpt = optimizeBells();
        potOpt = optimizeDecoratedPots();
        updateType = updateType();
        smoothness = extraRenderPasses();
        signDistance = signTextRenderDistance();

        chestAnimOpt = chestsAnimations();
        signTextOpt = renderSignText();
        shulkerAnimOpt = shulkerAnimations();
        bellAnimOpt = bellAnimations();
        potAnimOpt = potAnimations();

        this.body.addSingleOptionEntry(masterToggle);
        this.body.addAll(
                chestOpt, chestAnimOpt,
                signOpt, signTextOpt,
                shulkerOpt, shulkerAnimOpt,
                bellOpt, bellAnimOpt,
                potOpt, potAnimOpt,
                bedOpt
        );
        this.body.addSingleOptionEntry(updateType);
        this.body.addSingleOptionEntry(smoothness);
        this.body.addSingleOptionEntry(signDistance);
        updateDependentOptions(masterToggle.getValue());
    }

    private SimpleOption<Boolean> masterToggle() {
        return new SimpleOption<>(
                "config.betterblockentities.enable_optimizations",
                value -> Tooltip.of(Text.translatable("config.betterblockentities.enable_optimizations.tooltip")),
                (text, value) -> value ? Text.translatable("config.betterblockentities.on") : Text.translatable("config.betterblockentities.off"),
                SimpleOption.BOOLEAN,
                ConfigManager.CONFIG.master_optimize,
                value -> {
                    ConfigManager.CONFIG.master_optimize = value;
                    updateDependentOptions(value);
                }
        );
    }

    private SimpleOption<Boolean> optimizeChests() {
        return new SimpleOption<>(
                "config.betterblockentities.optimize_chests",
                value -> Tooltip.of(Text.translatable("config.betterblockentities.optimize_chests.tooltip")),
                (text, value) -> value ? Text.translatable("config.betterblockentities.on") : Text.translatable("config.betterblockentities.off"),
                SimpleOption.BOOLEAN,
                ConfigManager.CONFIG.optimize_chests,
                v -> {
                    ConfigManager.CONFIG.optimize_chests = v;
                    setOptionActive(chestAnimOpt, v && masterToggle.getValue());
                }
        );
    }

    private SimpleOption<Boolean> chestsAnimations() {
        return booleanOption(
                "config.betterblockentities.chest_animations",
                ConfigManager.CONFIG.chest_animations,
                v -> ConfigManager.CONFIG.chest_animations = v
        );
    }

    private SimpleOption<Boolean> optimizeSigns() {
        return new SimpleOption<>(
                "config.betterblockentities.optimize_signs",
                value -> Tooltip.of(Text.translatable("config.betterblockentities.optimize_signs.tooltip")),
                (text, value) -> value ? Text.translatable("config.betterblockentities.on") : Text.translatable("config.betterblockentities.off"),
                SimpleOption.BOOLEAN,
                ConfigManager.CONFIG.optimize_signs,
                v -> {
                    ConfigManager.CONFIG.optimize_signs = v;
                    setOptionActive(signTextOpt, v && masterToggle.getValue());
                }
        );
    }

    private SimpleOption<Boolean> renderSignText() {
        return booleanOption(
                "config.betterblockentities.render_sign_text",
                ConfigManager.CONFIG.render_sign_text,
                v -> ConfigManager.CONFIG.render_sign_text = v
        );
    }

    private SimpleOption<Boolean> optimizeShulkers() {
        return new SimpleOption<>(
                "config.betterblockentities.optimize_shulkers",
                value -> Tooltip.of(Text.translatable("config.betterblockentities.optimize_shulkers.tooltip")),
                (text, value) -> value ? Text.translatable("config.betterblockentities.on") : Text.translatable("config.betterblockentities.off"),
                SimpleOption.BOOLEAN,
                ConfigManager.CONFIG.optimize_shulkers,
                v -> {
                    ConfigManager.CONFIG.optimize_shulkers = v;
                    setOptionActive(shulkerAnimOpt, v && masterToggle.getValue());
                }
        );
    }

    private SimpleOption<Boolean> shulkerAnimations() {
        return booleanOption(
                "config.betterblockentities.shulker_animations",
                ConfigManager.CONFIG.shulker_animations,
                v -> ConfigManager.CONFIG.shulker_animations = v
        );
    }

    private SimpleOption<Boolean> optimizeBeds() {
        return new SimpleOption<>(
                "config.betterblockentities.optimize_beds",
                value -> Tooltip.of(Text.translatable("config.betterblockentities.optimize_beds.tooltip")),
                (text, value) -> value ? Text.translatable("config.betterblockentities.on") : Text.translatable("config.betterblockentities.off"),
                SimpleOption.BOOLEAN,
                ConfigManager.CONFIG.optimize_beds,
                v -> {
                    ConfigManager.CONFIG.optimize_beds = v;
                    setOptionActive(masterToggle, v && masterToggle.getValue());
                }
        );
    }

    private SimpleOption<Boolean> optimizeBells() {
        return new SimpleOption<>(
                "config.betterblockentities.optimize_bells",
                value -> Tooltip.of(Text.translatable("config.betterblockentities.optimize_bells.tooltip")),
                (text, value) -> value ? Text.translatable("config.betterblockentities.on") : Text.translatable("config.betterblockentities.off"),
                SimpleOption.BOOLEAN,
                ConfigManager.CONFIG.optimize_bells,
                v -> {
                    ConfigManager.CONFIG.optimize_bells = v;
                    setOptionActive(bellAnimOpt, v && masterToggle.getValue());
                }
        );
    }

    private SimpleOption<Boolean> bellAnimations() {
        return booleanOption(
                "config.betterblockentities.bell_animations",
                ConfigManager.CONFIG.bell_animations,
                v -> ConfigManager.CONFIG.bell_animations = v
        );
    }

    private SimpleOption<Boolean> optimizeDecoratedPots() {
        return new SimpleOption<>(
                "config.betterblockentities.optimize_decorated_pots",
                value -> Tooltip.of(Text.translatable("config.betterblockentities.optimize_decorated_pots.tooltip")),
                (text, value) -> value ? Text.translatable("config.betterblockentities.on") : Text.translatable("config.betterblockentities.off"),
                SimpleOption.BOOLEAN,
                ConfigManager.CONFIG.optimize_decoratedpots,
                v -> {
                    ConfigManager.CONFIG.optimize_decoratedpots = v;
                    setOptionActive(potAnimOpt, v && masterToggle.getValue());
                }
        );
    }

    private SimpleOption<Boolean> potAnimations() {
        return booleanOption(
                "config.betterblockentities.pot_animations",
                ConfigManager.CONFIG.pot_animations,
                v -> ConfigManager.CONFIG.pot_animations = v
        );
    }

    private SimpleOption<Integer> signTextRenderDistance() {
        return new SimpleOption<>(
                "config.betterblockentities.sign_text_render_distance",
                value -> Tooltip.of(Text.translatable("config.betterblockentities.sign_text_render_distance.tooltip")),

                (text, value) -> Text.translatable("config.betterblockentities.value_format", text, value),
                new SimpleOption.ValidatingIntSliderCallbacks(0, 64),
                ConfigManager.CONFIG.sign_text_render_distance,
                v -> ConfigManager.CONFIG.sign_text_render_distance = v
        );
    }

    private SimpleOption<Integer> updateType() {
        return new SimpleOption<>(
                "config.betterblockentities.update_type",
                value -> Tooltip.of(Text.translatable("config.betterblockentities.update_type.tooltip")),
                (text, value) -> switch (value) {
                    case 0 -> Text.translatable("config.betterblockentities.update_type.smart");
                    case 1 -> Text.translatable("config.betterblockentities.update_type.fast");
                    default -> Text.translatable("config.betterblockentities.update_type.fast");
                },
                new SimpleOption.MaxSuppliableIntCallbacks(0, () -> 1, 1),
                ConfigManager.CONFIG.updateType,
                value -> ConfigManager.CONFIG.updateType = value
        );
    }

    private SimpleOption<Integer> extraRenderPasses() {
        return new SimpleOption<>(
                "config.betterblockentities.extra_render_passes",
                value -> Tooltip.of(Text.translatable("config.betterblockentities.extra_render_passes.tooltip")),
                // 使用通用格式 "标题: 数值"
                (text, value) -> Text.translatable("config.betterblockentities.value_format", text, value),
                new SimpleOption.ValidatingIntSliderCallbacks(0, 50),
                ConfigManager.CONFIG.smoothness_slider,
                v -> ConfigManager.CONFIG.smoothness_slider = v
        );
    }

    private SimpleOption<Boolean> booleanOption(String key, boolean initial, java.util.function.Consumer<Boolean> onChange) {
        return new SimpleOption<>(
                key,
                SimpleOption.emptyTooltip(),
                (text, value) -> value ? Text.translatable("config.betterblockentities.on") : Text.translatable("config.betterblockentities.off"),
                SimpleOption.BOOLEAN,
                initial,
                onChange
        );
    }

    private void updateDependentOptions(boolean enabled) {
        setOptionActive(chestOpt, enabled);
        setOptionActive(signOpt, enabled);
        setOptionActive(shulkerOpt, enabled);
        setOptionActive(bedOpt, enabled);
        setOptionActive(bellOpt, enabled);
        setOptionActive(potOpt, enabled);

        setOptionActive(chestAnimOpt, enabled && chestOpt.getValue());
        setOptionActive(signTextOpt, enabled && signOpt.getValue());
        setOptionActive(shulkerAnimOpt, enabled && shulkerOpt.getValue());
        setOptionActive(bellAnimOpt, enabled && bellOpt.getValue());
        setOptionActive(potAnimOpt, enabled && potOpt.getValue());

        setOptionActive(smoothness, enabled);
        setOptionActive(updateType, enabled);
        setOptionActive(signDistance, enabled);
    }

    private void setOptionActive(SimpleOption<?> option, boolean active) {
        if (this.body == null) return;
        ClickableWidget widget = this.body.getWidgetFor(option);
        if (widget != null) widget.active = active;
    }

    @Override
    public void removed() {
        if (!ConfigManager.CONFIG.equals(originalConfig)) {
            ConfigManager.save();
            ConfigManager.refreshSupportedTypes();
            MinecraftClient.getInstance().reloadResources();
        }
    }
}