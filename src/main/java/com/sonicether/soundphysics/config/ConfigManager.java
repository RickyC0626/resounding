package com.sonicether.soundphysics.config;

import com.sonicether.soundphysics.SPEfx;
import com.sonicether.soundphysics.SoundPhysicsMod;
import com.sonicether.soundphysics.config.BlueTapePack.GuiRegistryinit;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.util.ActionResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManager {
    private static ConfigHolder<SoundPhysicsConfig> holder;

    public static final SoundPhysicsConfig DEFAULT = new SoundPhysicsConfig(){{
        Map<String, MaterialData> map =
                SoundPhysicsMod.blockSoundGroups.entrySet().stream()
                        .collect(Collectors.toMap((e)-> e.getValue().getLeft(), (e) -> new MaterialData(0.5, 1, e.getValue().getRight())));
        map.putIfAbsent("DEFAULT", new MaterialData(0.5, 1, ""));
        Material_Properties.reflectivityMap = map;
    }};

    public static void registerAutoConfig() {
        if (holder != null) {
            throw new IllegalStateException("Configuration already registered");
        }

        holder = AutoConfig.register(SoundPhysicsConfig.class, JanksonConfigSerializer::new);
        try {
            GuiRegistryinit.register();
        } catch (@SuppressWarnings("CatchMayIgnoreException") Exception ignored){System.out.println(Arrays.toString(ignored.getStackTrace()));}
        holder.registerSaveListener((holder, config) -> onSave(config));
        holder.load();
        if (holder.getConfig().Material_Properties.reflectivityMap == null) {
            holder.getConfig().preset = ConfigPresets.THEDOCRUBY;
            holder.getConfig().Material_Properties.reflectivityMap = DEFAULT.Material_Properties.reflectivityMap;
        }
        reload(false);
    }

    public static SoundPhysicsConfig getConfig() {
        if (holder == null) {
            return DEFAULT;
        }

        return holder.getConfig();
    }

    public static void reload(boolean load) {
        if (holder == null) {
            return;
        }

        if(load) holder.load();
        holder.getConfig().preset.setConfig();
        SPEfx.syncReverbParams();
        holder.save();
    }

    public static void save() {
        if (holder == null) {
            registerAutoConfig();
        }

        holder.save();
    }

    public static void handleBrokenMaterials(){
        SoundPhysicsConfig fallback = new SoundPhysicsConfig();
        ConfigPresets.THEDOCRUBY.configChanger.accept(fallback);
        getConfig().Material_Properties.reflectivityMap = fallback.Material_Properties.reflectivityMap;
        getConfig().Material_Properties.blockWhiteList = List.of("block.minecraft.water_source");
    }

    @SuppressWarnings("SameReturnValue")
    public static ActionResult onSave(SoundPhysicsConfig c) {
        SPEfx.syncReverbParams();
        if (c.preset != ConfigPresets.LOAD_SUCCESS) {
            c.preset.configChanger.accept(c);
        }
        return ActionResult.SUCCESS;
    }
}
