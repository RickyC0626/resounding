package dev.thedocruby.resounding.config.BlueTapePack;

import dev.thedocruby.resounding.Resounding;
import dev.thedocruby.resounding.config.MaterialData;
import dev.thedocruby.resounding.config.PrecomputedConfig;
import dev.thedocruby.resounding.config.ResoundingConfig;
import dev.thedocruby.resounding.config.presets.ConfigPresets;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigManager {

    private ConfigManager() {}

    private static ConfigHolder<ResoundingConfig> holder;

    public static String configVersion = "1.0.0-alpha.3";

    @Environment(EnvType.CLIENT)
    public static final ResoundingConfig DEFAULT = Resounding.env == EnvType.CLIENT ? new ResoundingConfig(){{
        Map<String, MaterialData> map = Resounding.nameToGroup.keySet().stream()
                .collect(Collectors.toMap(e -> e, e -> new MaterialData(e, 0.5, 0.5)));
        map.putIfAbsent("DEFAULT", new MaterialData("DEFAULT", 0.5, 0.5));
        materials.materialProperties = map;
    }} : null;

    public static void registerAutoConfig() {
        if (holder != null) {throw new IllegalStateException("Configuration already registered");}
        holder = AutoConfig.register(ResoundingConfig.class, JanksonConfigSerializer::new);

        if (Resounding.env == EnvType.CLIENT) try {GuiRegistryinit.register();} catch (Throwable ignored){
            Resounding.LOGGER.error("Failed to register config menu unwrappers. Edit config that isn't working in the config file");}

        holder.registerSaveListener((holder, config) -> onSave(config));
        holder.registerLoadListener((holder, config) -> onSave(config));
        reload(true);
    }

    public static ResoundingConfig getConfig() {
        if (holder == null) {return DEFAULT;}

        return holder.getConfig();
    }

    public static void reload(boolean load) {
        if (holder == null) {return;}

        if(load) holder.load();
        holder.getConfig().preset.setConfig();
        holder.save();
    }

    public static void save() { if (holder == null) {registerAutoConfig();} else {holder.save();} }

    @Environment(EnvType.CLIENT)
    public static void handleBrokenMaterials(@NotNull ResoundingConfig c ){
        Resounding.LOGGER.error("Critical materialProperties error. Resetting materialProperties");
        ResoundingConfig fallback = DEFAULT;
        ConfigPresets.RESET_MATERIALS.configChanger.accept(fallback);
        c.materials.materialProperties = fallback.materials.materialProperties;
        c.materials.blockWhiteList = List.of("block.minecraft.water");
    }

    public static void handleUnstableConfig( ResoundingConfig c ){
        Resounding.LOGGER.error("Error: Config file is not from a compatible version! Resetting the config...");
        // ConfigPresets.DEFAULT_PERFORMANCE.configChanger.accept(c); // TODO: add back
        ConfigPresets.RESET_MATERIALS.configChanger.accept(c);
        c.version = configVersion;
    }

    public static ActionResult onSave(ResoundingConfig c) {
        if (Resounding.env == EnvType.CLIENT && (c.materials.materialProperties == null || c.materials.materialProperties.get("DEFAULT") == null)) handleBrokenMaterials(c);
        if (Resounding.env == EnvType.CLIENT && c.preset != ConfigPresets.LOAD_SUCCESS) c.preset.configChanger.accept(c);
        if (c.version == null || !Objects.equals(c.version, configVersion)) handleUnstableConfig(c);
        if (PrecomputedConfig.pC != null) PrecomputedConfig.pC.deactivate();
        try {PrecomputedConfig.pC = new PrecomputedConfig(c);} catch (CloneNotSupportedException e) {e.printStackTrace(); return ActionResult.FAIL;}
        if (Resounding.env == EnvType.CLIENT && MinecraftClient.getInstance().getSoundManager() != null) {
            Resounding.updateRays();
            MinecraftClient.getInstance().getSoundManager().reloadSounds();
        }
        return ActionResult.SUCCESS;
    }
}
