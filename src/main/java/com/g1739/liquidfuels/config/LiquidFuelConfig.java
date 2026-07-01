package com.g1739.liquidfuels.config;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LiquidFuelConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> FUEL_BURN_TIMES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("fuels");
        FUEL_BURN_TIMES = builder
                .comment(
                        "Fluid fuel definitions. Value is burn ticks for 1000 mB. 200 ticks = 1 smelted item.",
                        "Use an exact fluid id: modid:fluid=burn_ticks_for_1000mb, for example minecraft:lava=20000.",
                        "Use a fluid tag with #: #namespace:tag=burn_ticks_for_1000mb, for example #c:biodiesel=12000.",
                        "Exact fluid ids take priority over tags. If multiple matching tags exist, the later config entry wins.",
                        "The tank consumes 100 mB each time, so each use burns for one tenth of the configured value.",
                        "Unknown exact fluids are ignored until their owning mod is loaded. Unknown tags are allowed and only match when a loaded fluid has that tag.",
                        "液体燃料配置。右侧数值是每 1000 mB 的燃烧 tick，200 tick = 1 个物品。",
                        "精确流体 ID 写法: modid:fluid=每1000mB燃烧tick，例如 minecraft:lava=20000。",
                        "流体标签写法需要以 # 开头: #namespace:tag=每1000mB燃烧tick，例如 #c:biodiesel=12000。",
                        "精确流体 ID 优先于标签；如果多个标签同时匹配同一种流体，靠后的配置项生效。",
                        "油桶每次作为燃料消耗 100 mB，所以单次燃烧时间是配置值的十分之一。",
                        "未加载的精确流体会被忽略；未加载的标签可以保留，只有实际加载的流体带有该标签时才会匹配。")
                .defineListAllowEmpty(List.of("burn_times_for_1000mb"), LiquidFuelConfig::defaultFuelEntries, LiquidFuelConfig::isFuelEntry);
        builder.pop();
        SPEC = builder.build();
    }

    private LiquidFuelConfig() {
    }

    public static int getBurnTimeForUnit(Fluid fluid) {
        return getBurnTimeFor1000mb(fluid)
                .map(burnTimeForBucket -> Math.max(1, burnTimeForBucket / 10))
                .orElse(0);
    }

    public static Map<ResourceLocation, Integer> getBurnTimesFor1000mb() {
        Map<ResourceLocation, Integer> resolved = new LinkedHashMap<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
            if (id == null) {
                continue;
            }
            getBurnTimeFor1000mb(fluid).ifPresent(burnTime -> resolved.put(id, burnTime));
        }
        return Collections.unmodifiableMap(resolved);
    }

    public static Optional<Integer> getBurnTimeFor1000mb(Fluid fluid) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id == null) {
            return Optional.empty();
        }
        FuelDefinitions definitions = readBurnTimes();
        Integer exactBurnTime = definitions.fluidBurnTimes().get(id);
        if (exactBurnTime != null) {
            return Optional.of(exactBurnTime);
        }
        return Optional.ofNullable(getMatchingTagBurnTime(fluid, definitions.tagBurnTimes()));
    }

    private static FuelDefinitions readBurnTimes() {
        Map<ResourceLocation, Integer> fluidBurnTimes = new LinkedHashMap<>();
        Map<TagKey<Fluid>, Integer> tagBurnTimes = new LinkedHashMap<>();
        for (String entry : FUEL_BURN_TIMES.get()) {
            parseEntry(entry, fluidBurnTimes, tagBurnTimes);
        }
        return new FuelDefinitions(fluidBurnTimes, tagBurnTimes);
    }

    private static void parseEntry(String entry, Map<ResourceLocation, Integer> fluidOutput, Map<TagKey<Fluid>, Integer> tagOutput) {
        int separator = entry.indexOf('=');
        if (separator <= 0 || separator >= entry.length() - 1) {
            return;
        }

        try {
            int burnTime = Integer.parseInt(entry.substring(separator + 1).trim());
            if (burnTime > 0) {
                parseFuelKey(entry.substring(0, separator).trim())
                        .ifPresent(key -> key.put(fluidOutput, tagOutput, burnTime));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private static boolean isFuelEntry(Object value) {
        if (!(value instanceof String entry)) {
            return false;
        }
        int separator = entry.indexOf('=');
        if (separator <= 0 || separator >= entry.length() - 1) {
            return false;
        }
        if (parseFuelKey(entry.substring(0, separator).trim()).isEmpty()) {
            return false;
        }
        try {
            return Integer.parseInt(entry.substring(separator + 1).trim()) > 0;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static Optional<FuelKey> parseFuelKey(String key) {
        if (key.startsWith("#")) {
            ResourceLocation tagId = ResourceLocation.tryParse(key.substring(1).trim());
            return tagId == null ? Optional.empty() : Optional.of(FuelKey.tag(TagKey.create(Registries.FLUID, tagId)));
        }
        ResourceLocation fluidId = ResourceLocation.tryParse(key);
        return fluidId == null ? Optional.empty() : Optional.of(FuelKey.fluid(fluidId));
    }

    private static Integer getMatchingTagBurnTime(Fluid fluid, Map<TagKey<Fluid>, Integer> tagBurnTimes) {
        Integer burnTime = null;
        for (Map.Entry<TagKey<Fluid>, Integer> entry : tagBurnTimes.entrySet()) {
            if (fluid.is(entry.getKey())) {
                burnTime = entry.getValue();
            }
        }
        return burnTime;
    }

    private static List<String> defaultFuelEntries() {
        return List.of(
                "minecraft:lava=20000",
                "tfc:olive_oil=8000",
                "immersiveengineering:creosote=3200",
                "immersiveengineering:ethanol=8000",
                "immersiveengineering:plantoil=8000",
                "immersiveengineering:biodiesel=12000",
                "immersiveengineering:high_power_biodiesel=13200",
                "#c:creosote=3200",
                "#c:ethanol=8000",
                "#c:plantoil=8000",
                "#c:plant_oil=8000",
                "#c:biodiesel=12000",
                "#c:high_power_biodiesel=13200",
                "#c:diesel=24000",
                "#c:diesel_sulfur=20000",
                "#c:kerosene=24000",
                "#c:gasoline=32000",
                "#forge:creosote=3200",
                "#forge:ethanol=8000",
                "#forge:plantoil=8000",
                "#forge:plant_oil=8000",
                "#forge:biodiesel=12000",
                "#forge:diesel=24000",
                "#forge:diesel_sulfur=20000",
                "#forge:kerosene=24000",
                "#forge:gasoline=32000",
                "createdieselgenerators:ethanol=8000",
                "createdieselgenerators:plant_oil=8000",
                "createdieselgenerators:biodiesel=12000",
                "createdieselgenerators:diesel=24000",
                "createdieselgenerators:gasoline=32000",
                "immersivepetroleum:diesel=24000",
                "immersivepetroleum:diesel_sulfur=20000",
                "immersivepetroleum:kerosene=24000",
                "immersivepetroleum:gasoline=32000"
        );
    }

    private record FuelDefinitions(Map<ResourceLocation, Integer> fluidBurnTimes, Map<TagKey<Fluid>, Integer> tagBurnTimes) {
    }

    private record FuelKey(ResourceLocation fluidId, TagKey<Fluid> tag) {
        private static FuelKey fluid(ResourceLocation id) {
            return new FuelKey(id, null);
        }

        private static FuelKey tag(TagKey<Fluid> tag) {
            return new FuelKey(null, tag);
        }

        private void put(Map<ResourceLocation, Integer> fluidOutput, Map<TagKey<Fluid>, Integer> tagOutput, int burnTime) {
            if (tag != null) {
                tagOutput.put(tag, burnTime);
            } else {
                fluidOutput.put(fluidId, burnTime);
            }
        }
    }
}
