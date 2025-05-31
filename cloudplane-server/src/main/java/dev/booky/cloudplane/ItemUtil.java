package dev.booky.cloudplane;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.trading.ItemCost;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.minecraft.core.component.DataComponents.CUSTOM_DATA;

public final class ItemUtil {

    private static final Set<DataComponentType<?>> SAVEABLE_COMPONENT_TYPE_SET = Set.of(
            DataComponents.ITEM_NAME, DataComponents.CUSTOM_NAME, DataComponents.LORE
    );
    private static final List<Pair<String, DataComponentType<?>>> SAVEABLE_COMPONENT_TYPES = SAVEABLE_COMPONENT_TYPE_SET.stream()
            .<Pair<String, DataComponentType<?>>>map(type -> Pair.of(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type).toString(), type))
            .toList();
    private static final String SAVEABLE_COMPONENT_TAG_NAME = "CloudPlane$SavedData";

    private ItemUtil() {
    }

    @SuppressWarnings("unchecked") // not unchecked
    public static void unpackPatchSaves(ItemStack stack) {
        CustomData customData = stack.getOrDefault(CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty() || !(customData.getUnsafe().get(SAVEABLE_COMPONENT_TAG_NAME) instanceof CompoundTag savedTag)) {
            return; // nothing to unpack
        }
        for (Pair<String, DataComponentType<?>> type : SAVEABLE_COMPONENT_TYPES) {
            Tag encoded = savedTag.get(type.getFirst());
            if (encoded == null) {
                continue; // nothing saved for this type, skip
            }
            Object decoded = type.getSecond().codecOrThrow()
                    .decode(NbtOps.INSTANCE, encoded)
                    .getOrThrow().getFirst();
            stack.set((DataComponentType<Object>) type.getSecond(), decoded);
        }
        // generally unsafe to do, but this probably won't cause any issues at this place
        customData.getUnsafe().remove(SAVEABLE_COMPONENT_TAG_NAME);
    }

    @SuppressWarnings("unchecked") // not unchecked
    public static DataComponentPatch packPatchSaves(ItemStack stack) {
        Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch = stack.components.patch;
        if (patch == null || patch.isEmpty()) {
            return DataComponentPatch.EMPTY; // no patches, skip complex logic
        }

        CompoundTag savedTag = null; // lazy-loaded
        for (Pair<String, DataComponentType<?>> type : SAVEABLE_COMPONENT_TYPES) {
            Optional<?> value = patch.get(type.getSecond());
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (savedTag == null) {
                savedTag = new CompoundTag();
            }
            Codec<Object> codec = ((DataComponentType<Object>) type.getSecond()).codecOrThrow();
            Tag encoded = codec.encodeStart(NbtOps.INSTANCE, value.get()).getOrThrow();
            savedTag.put(type.getFirst(), encoded);
        }
        if (savedTag == null) { // nothing found to be saved, return original
            return new DataComponentPatch(patch);
        }

        // add saved data to custom data component
        CompoundTag tag = stack.getOrDefault(CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.put(SAVEABLE_COMPONENT_TAG_NAME, savedTag);

        // copy patch map, prevents modification of original item stack
        Reference2ObjectMap<DataComponentType<?>, Optional<?>> newPatch =
                new Reference2ObjectArrayMap<>(patch.size() + 1);
        newPatch.putAll(patch);
        newPatch.put(CUSTOM_DATA, Optional.of(CustomData.of(tag)));
        return new DataComponentPatch(newPatch);
    }

    @SuppressWarnings("unchecked") // not unchecked
    public static ItemCost unpackPatchSaves(ItemCost cost) {
        List<TypedDataComponent<?>> components = cost.components().expectedComponents;
        CustomData customData = null;
        for (TypedDataComponent<?> component : components) {
            if (component.type() == CUSTOM_DATA) {
                customData = (CustomData) component.value();
                break;
            }
        }
        if (customData == null || customData.isEmpty()
                || !(customData.getUnsafe().get(SAVEABLE_COMPONENT_TAG_NAME) instanceof CompoundTag savedTag)) {
            return cost; // nothing to unpack
        }
        components = new ArrayList<>(components);
        crying:
        for (Pair<String, DataComponentType<?>> type : SAVEABLE_COMPONENT_TYPES) {
            Tag encoded = savedTag.get(type.getFirst());
            if (encoded == null) {
                continue; // nothing saved for this type, skip
            }
            Object decoded = type.getSecond().codecOrThrow()
                    .decode(NbtOps.INSTANCE, encoded)
                    .getOrThrow().getFirst();
            TypedDataComponent<Object> newComponent = new TypedDataComponent<>(
                    (DataComponentType<Object>) type.getSecond(), decoded);

            for (int i = 0; i < components.size(); i++) {
                TypedDataComponent<?> component = components.get(i);
                if (component.type() == type) {
                    components.set(i, newComponent);
                    continue crying;
                }
            }
            components.add(newComponent);
        }
        // generally unsafe to do, but this probably won't cause any issues at this place
        customData.getUnsafe().remove(SAVEABLE_COMPONENT_TAG_NAME);
        cost.components().expectedComponents = components;
        return cost;
    }

    public static ItemCost packPatchSaves(ItemCost cost) {
        List<TypedDataComponent<?>> components = cost.components().expectedComponents;
        if (components == null || components.isEmpty()) {
            return cost; // no expected components, skip complex logic
        }

        int existingCustomDataIndex = -1;
        CustomData existingCustomData = null;
        CompoundTag savedTag = null; // lazy-loaded
        for (int i = 0; i < components.size(); i++) {
            TypedDataComponent<?> component = components.get(i);
            if (component.type() == CUSTOM_DATA) {
                existingCustomDataIndex = i;
                existingCustomData = (CustomData) component.value();
                continue;
            }
            if (!SAVEABLE_COMPONENT_TYPE_SET.contains(component.type())
                    || component.value() instanceof ItemLore lore && lore.lines().isEmpty()) {
                continue; // doesn't need to be saved
            }
            if (savedTag == null) {
                savedTag = new CompoundTag();
            }
            Tag encoded = component.encodeValue(NbtOps.INSTANCE).getOrThrow();
            String key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type()).toString();
            savedTag.put(key, encoded);
        }
        if (savedTag == null) { // nothing found to be saved, return original
            return cost;
        }

        // add saved data to custom data component
        CompoundTag tag = (existingCustomData == null ? CustomData.EMPTY : existingCustomData).copyTag();
        tag.put(SAVEABLE_COMPONENT_TAG_NAME, savedTag);

        // create new item cost predicate with packed data
        TypedDataComponent<CustomData> newComponent = new TypedDataComponent<>(
                CUSTOM_DATA, CustomData.of(tag));
        if (existingCustomDataIndex != -1) { // replace existing
            components = new ArrayList<>(components);
            components.set(existingCustomDataIndex, newComponent);
        } else { // add new
            List<TypedDataComponent<?>> prevComponents = components;
            components = new ArrayList<>(components.size() + 1);
            components.addAll(prevComponents);
            components.add(newComponent);
        }

        // components aren't made immutable here, but doesn't matter
        DataComponentExactPredicate componentsPredicate = new DataComponentExactPredicate(components);
        return new ItemCost(cost.item(), cost.count(), componentsPredicate, cost.itemStack());
    }

    private static List<Component> inlineComponent(Component component) {
        component = component.compact();
        if (component.children().isEmpty()) {
            return List.of(component);
        }

        List<Component> components = new ArrayList<>(5);
        inlineComponent0(components, component);
        return components;
    }

    private static void inlineComponent0(List<Component> components, Component component) {
        components.add(component.children(List.of()));
        for (Component child : component.children()) {
            inlineComponent0(components, child.applyFallbackStyle(component.style()));
        }
    }

    public static List<Component> getLines(Component component) {
        List<Component> parts = inlineComponent(component);

        List<Component> components = new ArrayList<>(parts.size());
        List<Component> currentComponents = new ArrayList<>(parts.size());

        for (Component part : parts) {
            if (!(part instanceof TextComponent textComp)
                    || textComp.content().indexOf('\n') == -1) {
                currentComponents.add(part);
                continue;
            }

            String[] strings = StringUtils.splitPreserveAllTokens(textComp.content(), '\n');
            for (int i = 0; i < strings.length; i++) {
                if (i != 0) {
                    components.add(Component.text().append(currentComponents).build());
                    currentComponents.clear();
                }

                String string = strings[i];
                if (!string.isEmpty()) {
                    currentComponents.add(Component.text(string, part.style()));
                }
            }
        }

        if (!currentComponents.isEmpty()) {
            components.add(Component.text().append(currentComponents).build());
        }
        return Collections.unmodifiableList(components);
    }
}
