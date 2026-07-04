package com.g1739.liquidfuels.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LiquidFuelConfig {
    public static final int DEFAULT_SMALL_FUEL_TANK_CAPACITY = 1000;
    public static final int DEFAULT_FUEL_TANK_CAPACITY = 5000;
    public static final int DEFAULT_LARGE_FUEL_TANK_CAPACITY = 20000;

    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.ConfigValue<?> FUEL_BURN_TIMES;
    private static final ForgeConfigSpec.IntValue SMALL_FUEL_TANK_CAPACITY;
    private static final ForgeConfigSpec.IntValue FUEL_TANK_CAPACITY;
    private static final ForgeConfigSpec.IntValue LARGE_FUEL_TANK_CAPACITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("fuels");
        FUEL_BURN_TIMES = builder
                .comment(
                        "Fluid fuel definitions. Value is burn ticks for 1000 mB. 200 ticks = 1 smelted item.",
                        "Use an exact fluid id: modid:fluid=burn_ticks_for_1000mb, for example minecraft:lava=20000.",
                        "Use a fluid tag with #: #namespace:tag=burn_ticks_for_1000mb, for example #forge:diesel=24000 or #c:biodiesel=12000.",
                        "Exact fluid ids take priority over tags. If multiple matching tags exist, the later config entry wins.",
                        "The tank consumes 100 mB each time, so each use burns for one tenth of the configured value.",
                        "Unknown exact fluids are ignored until their owning mod is loaded. Unknown tags are allowed and only match when a loaded fluid has that tag.",
                        "Default format is a TOML table with one fuel per line. Old string arrays like [\"modid:fluid=20000\"] are still accepted.",
                        "In table format, keep quotes around keys that contain : or #.",
                        "液体燃料配置。右侧数值是每 1000 mB 的燃烧 tick，200 tick = 1 个物品。",
                        "精确流体 ID 写法: modid:fluid=每1000mB燃烧tick，例如 minecraft:lava=20000。",
                        "流体标签写法需要以 # 开头: #namespace:tag=每1000mB燃烧tick，例如 #forge:diesel=24000 或 #c:biodiesel=12000。",
                        "精确流体 ID 优先于标签；如果多个标签同时匹配同一种流体，靠后的配置项生效。",
                        "油桶每次作为燃料消耗 100 mB，所以单次燃烧时间是配置值的十分之一。",
                        "未加载的精确流体会被忽略；未加载的标签可以保留，只有实际加载的流体带有该标签时才会匹配。",
                        "默认格式是 TOML 子表，每种燃料单独一行；旧版 [\"modid:fluid=20000\"] 字符串数组仍兼容。",
                        "子表格式中，带 : 或 # 的键需要保留英文引号。")
                .define(List.of("burn_times_for_1000mb"), LiquidFuelConfig::defaultFuelDefinitions, LiquidFuelConfig::isFuelDefinitions);
        builder.pop();
        builder.push("tanks");
        SMALL_FUEL_TANK_CAPACITY = builder
                .comment(
                        "Capacity of the small fuel tank, in mB.",
                        "The tank still consumes 100 mB each time it is used as fuel.",
                        "小油桶容量，单位 mB。",
                        "油桶每次作为燃料仍消耗 100 mB。")
                .defineInRange("small_fuel_tank_capacity", DEFAULT_SMALL_FUEL_TANK_CAPACITY, 1, Integer.MAX_VALUE);
        FUEL_TANK_CAPACITY = builder
                .comment(
                        "Capacity of the medium fuel tank, in mB.",
                        "中油桶容量，单位 mB。")
                .defineInRange("fuel_tank_capacity", DEFAULT_FUEL_TANK_CAPACITY, 1, Integer.MAX_VALUE);
        LARGE_FUEL_TANK_CAPACITY = builder
                .comment(
                        "Capacity of the large fuel tank, in mB.",
                        "大油桶容量，单位 mB。")
                .defineInRange("large_fuel_tank_capacity", DEFAULT_LARGE_FUEL_TANK_CAPACITY, 1, Integer.MAX_VALUE);
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

    public static int getSmallFuelTankCapacity() {
        return SMALL_FUEL_TANK_CAPACITY.get();
    }

    public static int getFuelTankCapacity() {
        return FUEL_TANK_CAPACITY.get();
    }

    public static int getLargeFuelTankCapacity() {
        return LARGE_FUEL_TANK_CAPACITY.get();
    }

    public static int getTankCapacity(int defaultCapacity) {
        return switch (defaultCapacity) {
            case DEFAULT_SMALL_FUEL_TANK_CAPACITY -> getSmallFuelTankCapacity();
            case DEFAULT_FUEL_TANK_CAPACITY -> getFuelTankCapacity();
            case DEFAULT_LARGE_FUEL_TANK_CAPACITY -> getLargeFuelTankCapacity();
            default -> Math.max(1, defaultCapacity);
        };
    }

    public static Map<ResourceLocation, Integer> getBurnTimesFor1000mb() {
        Map<ResourceLocation, Integer> resolved = new LinkedHashMap<>();
        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
            if (id == null) {
                continue;
            }
            getBurnTimeFor1000mb(fluid).ifPresent(burnTime -> resolved.put(id, burnTime));
        }
        return Collections.unmodifiableMap(resolved);
    }

    public static Optional<Integer> getBurnTimeFor1000mb(Fluid fluid) {
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
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
        readBurnTimeEntries(FUEL_BURN_TIMES.get(), fluidBurnTimes, tagBurnTimes);
        return new FuelDefinitions(fluidBurnTimes, tagBurnTimes);
    }

    private static void readBurnTimeEntries(Object value, Map<ResourceLocation, Integer> fluidOutput, Map<TagKey<Fluid>, Integer> tagOutput) {
        if (value instanceof UnmodifiableConfig config) {
            for (Map.Entry<String, Object> entry : config.valueMap().entrySet()) {
                parseEntry(entry.getKey(), entry.getValue(), fluidOutput, tagOutput);
            }
            return;
        }
        if (value instanceof List<?> entries) {
            for (Object entry : entries) {
                if (entry instanceof String text) {
                    parseEntry(text, fluidOutput, tagOutput);
                }
            }
        }
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

    private static void parseEntry(String key, Object value, Map<ResourceLocation, Integer> fluidOutput, Map<TagKey<Fluid>, Integer> tagOutput) {
        Integer burnTime = readPositiveInt(value);
        if (burnTime != null) {
            parseFuelKey(key.trim())
                    .ifPresent(fuelKey -> fuelKey.put(fluidOutput, tagOutput, burnTime));
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

    private static boolean isFuelDefinitions(Object value) {
        if (value instanceof UnmodifiableConfig config) {
            return config.valueMap().entrySet().stream()
                    .allMatch(entry -> parseFuelKey(entry.getKey().trim()).isPresent() && readPositiveInt(entry.getValue()) != null);
        }
        return value instanceof List<?> entries && entries.stream().allMatch(LiquidFuelConfig::isFuelEntry);
    }

    private static Integer readPositiveInt(Object value) {
        try {
            int parsed;
            if (value instanceof Number number) {
                long longValue = number.longValue();
                if (longValue <= 0 || longValue > Integer.MAX_VALUE) {
                    return null;
                }
                parsed = (int) longValue;
            } else if (value instanceof String string) {
                parsed = Integer.parseInt(string.trim());
            } else {
                return null;
            }
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ignored) {
            return null;
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

    private static Config defaultFuelDefinitions() {
        Config defaults = Config.of(LinkedHashMap::new, InMemoryFormat.withUniversalSupport());
        addDefault(defaults, "minecraft:lava", 20000);
        addDefault(defaults, "tfc:olive_oil", 8000);
        addDefault(defaults, "immersiveengineering:creosote", 3200);
        addDefault(defaults, "immersiveengineering:ethanol", 8000);
        addDefault(defaults, "immersiveengineering:plantoil", 8000);
        addDefault(defaults, "immersiveengineering:biodiesel", 12000);
        addDefault(defaults, "immersiveengineering:high_power_biodiesel", 13200);
        addDefault(defaults, "#c:creosote", 3200);
        addDefault(defaults, "#c:ethanol", 8000);
        addDefault(defaults, "#c:plantoil", 8000);
        addDefault(defaults, "#c:plant_oil", 8000);
        addDefault(defaults, "#c:biodiesel", 12000);
        addDefault(defaults, "#c:high_power_biodiesel", 13200);
        addDefault(defaults, "#c:diesel", 24000);
        addDefault(defaults, "#c:diesel_sulfur", 20000);
        addDefault(defaults, "#c:kerosene", 24000);
        addDefault(defaults, "#c:gasoline", 32000);
        addDefault(defaults, "#forge:creosote", 3200);
        addDefault(defaults, "#forge:ethanol", 8000);
        addDefault(defaults, "#forge:plantoil", 8000);
        addDefault(defaults, "#forge:plant_oil", 8000);
        addDefault(defaults, "#forge:biodiesel", 12000);
        addDefault(defaults, "#forge:diesel", 24000);
        addDefault(defaults, "#forge:diesel_sulfur", 20000);
        addDefault(defaults, "#forge:kerosene", 24000);
        addDefault(defaults, "#forge:gasoline", 32000);
        addDefault(defaults, "createdieselgenerators:ethanol", 8000);
        addDefault(defaults, "createdieselgenerators:plant_oil", 8000);
        addDefault(defaults, "createdieselgenerators:biodiesel", 12000);
        addDefault(defaults, "createdieselgenerators:diesel", 24000);
        addDefault(defaults, "createdieselgenerators:gasoline", 32000);
        addDefault(defaults, "immersivepetroleum:diesel", 24000);
        addDefault(defaults, "immersivepetroleum:diesel_sulfur", 20000);
        addDefault(defaults, "immersivepetroleum:kerosene", 24000);
        addDefault(defaults, "immersivepetroleum:gasoline", 32000);
        return defaults;
    }

    private static void addDefault(Config config, String key, int burnTime) {
        config.add(List.of(key), burnTime);
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
