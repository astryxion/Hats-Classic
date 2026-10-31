package com.astryxion.hats.client.compat;

import com.astryxion.hats.Hats;
import com.astryxion.hats.mixin.ModelPartAccessor;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fresh Animations empties vanilla {@code head} and draws the skull on CEM
 * {@code head2} (under {@code body} / {@code hat}). EMF writes those poses
 * during model render. Snapshot that matrix, then copy it onto the hat layer.
 */
public final class FreshAnimationsHatCompat {

    private static final ThreadLocal<Deque<Capture>> STACK = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Capture> LAST = new ThreadLocal<>();
    private static final Set<String> LOGGED = ConcurrentHashMap.newKeySet();

    private static Method emfIsModelMethod;
    private static Method emfRootMethod;
    private static Method emfVanillaPartsMethod;
    private static Method emfAnimationMapMethod;
    private static Method emfAnimateMethod;
    private static Field emfVariantField;
    private static Field childrenField;
    private static Field cubesField;
    private static Class<?> emfCustomClass;
    private static Field emfCustomIdField;
    private static boolean emfMethodsResolved;

    private FreshAnimationsHatCompat() {
    }

    public static void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager resourceManager) {
        LOGGED.clear();
    }

    public static void beginEntityRender() {
        Capture capture = new Capture();
        STACK.get().push(capture);
        LAST.set(capture);
    }

    public static void endEntityRender() {
        Deque<Capture> stack = STACK.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    public static void enterEntityModel(PoseStack poseStack) {
    }

    public static void leaveEntityModel() {
    }

    public static boolean isCapturing() {
        return activeCapture() != null;
    }

    public static void captureIfVisualHead(ModelPart part, PoseStack poseStack) {
        Capture capture = activeCapture();
        if (capture == null) {
            capture = new Capture();
            LAST.set(capture);
        }
        int priority = visualPriority(part, null);
        if (priority <= capture.priority) {
            return;
        }
        capture.priority = priority;
        capture.part = part;
        capture.headPose.set(poseStack.last().pose());
        capture.headNormal.set(poseStack.last().normal());
    }

    /**
     * @return the FA skull part for cube centering, or {@code null} to keep vanilla
     */
    public static ModelPart tryAttachAnimatedHead(PoseStack poseStack, EntityModel<?> model) {
        if (model == null) {
            return null;
        }
        Capture capture = activeCapture();
        if (capture != null && capture.part != null && capture.priority >= 2) {
            poseStack.last().pose().set(capture.headPose);
            poseStack.last().normal().set(capture.headNormal);
            logOnce(model, "capture p=" + capture.priority + " part=" + partName(capture.part, null));
            return capture.part;
        }

        Object emfRoot = emfRoot(model);
        if (emfRoot instanceof ModelPart rootPart) {
            animateEmf(emfRoot);
            ModelPart mapped = mappedVisualHead(emfRoot);
            if (mapped != null) {
                List<ModelPart> chain = chainFromVanillaParts(emfRoot, mapped);
                if (chain.isEmpty()) {
                    chain = chainTo(model, rootPart, mapped);
                }
                if (!chain.isEmpty()) {
                    for (ModelPart bone : chain) {
                        bone.translateAndRotate(poseStack);
                    }
                    logOnce(model, "emf-map chain=" + chain.size()
                            + " part=" + partName(mapped, null));
                    return mapped;
                }
                mapped.translateAndRotate(poseStack);
                logOnce(model, "emf-map part-only part=" + partName(mapped, null));
                return mapped;
            }
        }

        List<ModelPart> chain = findVisualHeadChain(model);
        if (chain.isEmpty() && emfRoot instanceof ModelPart rootPart) {
            chain = findVisualHeadChainFrom(rootPart);
        }
        if (chain.isEmpty()) {
            logOnce(model, "miss captureP=" + (capture == null ? -1 : capture.priority)
                    + " emf=" + (emfRoot != null)
                    + " keys=" + animationKeys(emfRoot));
            return null;
        }
        for (ModelPart bone : chain) {
            bone.translateAndRotate(poseStack);
        }
        logOnce(model, "walk chain=" + chain.size()
                + " part=" + partName(chain.get(chain.size() - 1), null));
        return chain.get(chain.size() - 1);
    }

    private static Capture activeCapture() {
        Capture capture = current();
        if (capture != null) {
            return capture;
        }
        return LAST.get();
    }

    private static void logOnce(EntityModel<?> model, String result) {
        String key = model.getClass().getSimpleName() + "|" + result;
        if (LOGGED.add(key)) {
            Hats.LOGGER.info("[FA hats] {} {}", model.getClass().getSimpleName(), result);
        }
    }

    private static Capture current() {
        return STACK.get().peek();
    }

    private static int visualPriority(ModelPart part, String nameHint) {
        String name = partName(part, nameHint);
        if (name == null || name.isEmpty()) {
            return 0;
        }
        if (name.contains("created_by_freshlx") || name.contains("freshlx")) {
            return 0;
        }
        if (name.equals("head2") || name.equals("head_2")) {
            return 3;
        }
        if (name.equals("head1_s") || name.equals("head1")) {
            return 3;
        }
        if (name.equals("nose") && isSkullSized(part)) {
            return 3;
        }
        if (isEmfCustom(part) && (name.equals("headwear") || name.equals("hat")) && isSkullSized(part)) {
            return 2;
        }
        if (isEmfCustom(part) && name.contains("head") && isSkullSized(part)) {
            return 2;
        }
        if (isEmfCustom(part) && name.equals("body2")) {
            return 2;
        }
        if (isEmfCustom(part) && name.equals("body") && isLargeBodyCube(part)) {
            return 2;
        }
        if (isEmfCustom(part) && name.equals("core")) {
            return 2;
        }
        return 0;
    }

    private static String partName(ModelPart part, String nameHint) {
        if (nameHint != null && !nameHint.isEmpty()) {
            return normalize(nameHint);
        }
        String named = emfCustomId(part);
        if (named == null) {
            named = readString(part, "id");
        }
        if (named == null) {
            named = readString(part, "name");
        }
        if (named == null) {
            named = idFromToString(part);
        }
        if (named == null) {
            named = scanStringFields(part);
        }
        return named == null ? null : normalize(named);
    }

    private static String normalize(String named) {
        String key = named.toLowerCase(Locale.ROOT).trim();
        int colon = key.lastIndexOf(':');
        if (colon >= 0 && colon + 1 < key.length()) {
            key = key.substring(colon + 1);
        }
        while (key.startsWith("emf_")) {
            key = key.substring(4);
        }
        return key;
    }

    private static boolean isEmfCustom(ModelPart part) {
        resolveEmfMethods();
        return emfCustomClass != null && emfCustomClass.isInstance(part);
    }

    private static String emfCustomId(ModelPart part) {
        resolveEmfMethods();
        if (emfCustomClass == null || emfCustomIdField == null || !emfCustomClass.isInstance(part)) {
            return null;
        }
        try {
            Object value = emfCustomIdField.get(part);
            return value instanceof String string ? string : null;
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    private static String idFromToString(ModelPart part) {
        String text = part.toString();
        if (text == null || text.isEmpty()) {
            return null;
        }
        String marker = "[custom part ";
        int start = text.indexOf(marker);
        if (start >= 0) {
            int end = text.indexOf(']', start);
            if (end > start) {
                return text.substring(start + marker.length(), end);
            }
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("head2")) {
            return "head2";
        }
        return null;
    }

    private static String scanStringFields(ModelPart part) {
        Class<?> search = part.getClass();
        while (search != null && search != Object.class) {
            for (Field field : search.getDeclaredFields()) {
                if (field.getType() != String.class) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object value = field.get(part);
                    if (value instanceof String string && !string.isEmpty()) {
                        String key = string.toLowerCase(Locale.ROOT);
                        if (key.contains("head2") || key.equals("nose") || key.contains("freshlx")) {
                            return string;
                        }
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
            search = search.getSuperclass();
        }
        return null;
    }

    private static String readString(Object owner, String fieldName) {
        Class<?> search = owner.getClass();
        while (search != null && search != Object.class) {
            try {
                Field field = search.getDeclaredField(fieldName);
                if (field.getType() == String.class) {
                    field.setAccessible(true);
                    Object value = field.get(owner);
                    return value instanceof String string ? string : null;
                }
            } catch (ReflectiveOperationException ignored) {
            }
            search = search.getSuperclass();
        }
        return null;
    }

    private static boolean isSkullSized(ModelPart part) {
        for (ModelPart.Cube cube : cubesOf(part)) {
            if (cube.maxX - cube.minX >= 6.0F && cube.maxY - cube.minY >= 6.0F) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLargeBodyCube(ModelPart part) {
        for (ModelPart.Cube cube : cubesOf(part)) {
            if (cube.maxX - cube.minX >= 12.0F
                    && cube.maxY - cube.minY >= 12.0F
                    && cube.maxZ - cube.minZ >= 12.0F) {
                return true;
            }
        }
        return false;
    }

    private static List<ModelPart> findVisualHeadChain(EntityModel<?> model) {
        List<ModelPart> best = new ArrayList<>();
        int[] bestPriority = {0};
        for (ModelPart start : modelStarts(model)) {
            walk(start, new ArrayList<>(), best, bestPriority, new IdentityHashMap<>());
        }
        return bestPriority[0] >= 2 ? best : List.of();
    }

    private static List<ModelPart> findVisualHeadChainFrom(ModelPart start) {
        List<ModelPart> best = new ArrayList<>();
        int[] bestPriority = {0};
        walk(start, new ArrayList<>(), best, bestPriority, new IdentityHashMap<>());
        return bestPriority[0] >= 2 ? best : List.of();
    }

    private static void walk(
            ModelPart part,
            List<ModelPart> chain,
            List<ModelPart> best,
            int[] bestPriority,
            Map<ModelPart, Boolean> seen
    ) {
        if (seen.put(part, Boolean.TRUE) != null) {
            return;
        }
        chain.add(part);
        consider(part, null, chain, best, bestPriority);
        for (Map.Entry<String, ModelPart> entry : childrenOf(part).entrySet()) {
            consider(entry.getValue(), entry.getKey(), append(chain, entry.getValue()), best, bestPriority);
            walk(entry.getValue(), chain, best, bestPriority, seen);
        }
        chain.remove(chain.size() - 1);
    }

    private static List<ModelPart> append(List<ModelPart> chain, ModelPart extra) {
        List<ModelPart> copy = new ArrayList<>(chain);
        copy.add(extra);
        return copy;
    }

    private static void consider(
            ModelPart part,
            String nameHint,
            List<ModelPart> chain,
            List<ModelPart> best,
            int[] bestPriority
    ) {
        int priority = visualPriority(part, nameHint);
        if (priority < 2) {
            return;
        }
        if (priority > bestPriority[0]
                || (priority == bestPriority[0] && !best.isEmpty() && chain.size() < best.size())) {
            bestPriority[0] = priority;
            best.clear();
            best.addAll(chain);
        }
    }

    private static List<ModelPart> chainTo(EntityModel<?> model, ModelPart emfRoot, ModelPart target) {
        for (ModelPart start : modelStarts(model)) {
            List<ModelPart> chain = new ArrayList<>();
            if (buildChain(start, target, chain, new IdentityHashMap<>())) {
                return chain;
            }
        }
        List<ModelPart> fromRoot = new ArrayList<>();
        if (buildChain(emfRoot, target, fromRoot, new IdentityHashMap<>())) {
            if (!fromRoot.isEmpty() && fromRoot.get(0) == emfRoot && fromRoot.size() > 1) {
                fromRoot.remove(0);
            }
            return fromRoot;
        }
        return List.of();
    }

    private static List<ModelPart> chainFromVanillaParts(Object emfRoot, ModelPart target) {
        resolveEmfMethods();
        if (emfVanillaPartsMethod == null) {
            return List.of();
        }
        try {
            Object value = emfVanillaPartsMethod.invoke(emfRoot);
            if (value instanceof Map<?, ?> map) {
                for (Object part : map.values()) {
                    if (part instanceof ModelPart start) {
                        List<ModelPart> chain = new ArrayList<>();
                        if (buildChain(start, target, chain, new IdentityHashMap<>())) {
                            return chain;
                        }
                    }
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return List.of();
    }

    private static boolean buildChain(
            ModelPart current,
            ModelPart target,
            List<ModelPart> chain,
            Map<ModelPart, Boolean> seen
    ) {
        if (seen.put(current, Boolean.TRUE) != null) {
            return false;
        }
        chain.add(current);
        if (current == target) {
            return true;
        }
        for (ModelPart child : childrenOf(current).values()) {
            if (buildChain(child, target, chain, seen)) {
                return true;
            }
        }
        chain.remove(chain.size() - 1);
        return false;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ModelPart> childrenOf(ModelPart part) {
        try {
            Map<String, ModelPart> children = ((ModelPartAccessor) (Object) part).hats$children();
            if (children != null) {
                return children;
            }
        } catch (Throwable ignored) {
        }
        Field field = typedField(childrenField, ModelPart.class, Map.class, "children", "f_104213_");
        childrenField = field;
        if (field == null) {
            return Map.of();
        }
        try {
            Object value = field.get(part);
            if (value instanceof Map<?, ?> map) {
                return (Map<String, ModelPart>) map;
            }
        } catch (IllegalAccessException ignored) {
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private static List<ModelPart.Cube> cubesOf(ModelPart part) {
        try {
            List<ModelPart.Cube> cubes = ((ModelPartAccessor) (Object) part).hats$cubes();
            if (cubes != null) {
                return cubes;
            }
        } catch (Throwable ignored) {
        }
        Field field = typedField(cubesField, ModelPart.class, List.class, "cubes", "f_104212_");
        cubesField = field;
        if (field == null) {
            return List.of();
        }
        try {
            Object value = field.get(part);
            if (value instanceof List<?> list) {
                return (List<ModelPart.Cube>) list;
            }
        } catch (IllegalAccessException ignored) {
        }
        return List.of();
    }

    private static Field typedField(Field cached, Class<?> owner, Class<?> type, String... names) {
        if (cached != null) {
            return cached;
        }
        for (String name : names) {
            try {
                Field field = owner.getDeclaredField(name);
                if (type.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return field;
                }
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private static List<ModelPart> modelStarts(EntityModel<?> model) {
        Set<ModelPart> starts = new LinkedHashSet<>();
        Object emfRoot = emfRoot(model);
        if (emfRoot instanceof ModelPart rootPart) {
            starts.add(rootPart);
            starts.addAll(vanillaParts(emfRoot));
        }
        starts.add(model.root());
        if (model instanceof HeadedModel headed) {
            starts.add(headed.getHead());
        }
        try {
            if (model instanceof HeadedModel headedParts) {
                ModelPart head = headedParts.getHead();
                if (head != null) {
                    starts.add(head);
                }
            }
            ModelPart root = model.root();
            if (root != null && root.hasChild("body")) {
                starts.add(root.getChild("body"));
            }
        } catch (Throwable ignored) {
        }
        Class<?> search = model.getClass();
        while (search != null && search != Object.class) {
            for (Field field : search.getDeclaredFields()) {
                if (!ModelPart.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value instanceof ModelPart part) {
                        starts.add(part);
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
            search = search.getSuperclass();
        }
        return new ArrayList<>(starts);
    }

    private static List<ModelPart> vanillaParts(Object emfRoot) {
        resolveEmfMethods();
        if (emfVanillaPartsMethod == null) {
            return List.of();
        }
        try {
            Object value = emfVanillaPartsMethod.invoke(emfRoot);
            if (value instanceof Map<?, ?> map) {
                List<ModelPart> parts = new ArrayList<>();
                for (Object part : map.values()) {
                    if (part instanceof ModelPart modelPart) {
                        parts.add(modelPart);
                    }
                }
                return parts;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return List.of();
    }

    private static Object emfRoot(EntityModel<?> model) {
        resolveEmfMethods();
        if (emfIsModelMethod == null || emfRootMethod == null) {
            return null;
        }
        try {
            if (!emfIsModelMethod.getDeclaringClass().isInstance(model)) {
                return null;
            }
            Object flagged = emfIsModelMethod.invoke(model);
            if (!(flagged instanceof Boolean bool) || !bool) {
                return null;
            }
            return emfRootMethod.invoke(model);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void animateEmf(Object root) {
        resolveEmfMethods();
        if (emfAnimateMethod == null) {
            return;
        }
        try {
            emfAnimateMethod.invoke(root);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static ModelPart mappedVisualHead(Object root) {
        Map<String, ?> map = animationMap(root);
        if (map == null) {
            return null;
        }
        ModelPart best = null;
        int bestPriority = 0;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!(entry.getValue() instanceof ModelPart part)) {
                continue;
            }
            int priority = visualPriority(part, entry.getKey());
            if (priority > bestPriority) {
                bestPriority = priority;
                best = part;
            }
        }
        return bestPriority >= 2 ? best : null;
    }

    private static String animationKeys(Object root) {
        Map<String, ?> map = animationMap(root);
        if (map == null || map.isEmpty()) {
            return "none";
        }
        List<String> keys = new ArrayList<>();
        for (String key : map.keySet()) {
            if (key != null && key.toLowerCase(Locale.ROOT).contains("head")) {
                keys.add(key);
            }
        }
        return keys.isEmpty() ? map.size() + "parts" : keys.toString();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ?> animationMap(Object root) {
        resolveEmfMethods();
        if (root == null || emfAnimationMapMethod == null) {
            return null;
        }
        try {
            int variant = 1;
            if (emfVariantField != null) {
                variant = emfVariantField.getInt(root);
            }
            Object value = emfAnimationMapMethod.invoke(root, "", variant, Map.of());
            if (value instanceof Map<?, ?> map) {
                return (Map<String, ?>) map;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    private static void resolveEmfMethods() {
        if (emfMethodsResolved) {
            return;
        }
        emfMethodsResolved = true;
        try {
            Class<?> iemf = Class.forName("traben.entity_model_features.models.IEMFModel");
            emfIsModelMethod = iemf.getMethod("emf$isEMFModel");
            emfRootMethod = iemf.getMethod("emf$getEMFRootModel");
        } catch (ReflectiveOperationException ignored) {
            return;
        }
        try {
            emfCustomClass = Class.forName("traben.entity_model_features.models.parts.EMFModelPartCustom");
            emfCustomIdField = emfCustomClass.getField("id");
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            Class<?> partClass = Class.forName("traben.entity_model_features.models.parts.EMFModelPart");
            emfAnimationMapMethod = partClass.getMethod(
                    "getAllChildPartsAsAnimationMap",
                    String.class,
                    int.class,
                    Map.class
            );
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            Class<?> rootClass = Class.forName("traben.entity_model_features.models.parts.EMFModelPartRoot");
            emfAnimateMethod = rootClass.getMethod("animate");
            emfVanillaPartsMethod = rootClass.getMethod("getAllVanillaPartsByNameEMF");
            try {
                emfVariantField = rootClass.getField("currentModelVariant");
            } catch (NoSuchFieldException missing) {
                emfVariantField = rootClass.getDeclaredField("currentModelVariant");
                emfVariantField.setAccessible(true);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static final class Capture {
        private final Matrix4f headPose = new Matrix4f();
        private final Matrix3f headNormal = new Matrix3f();
        private int priority;
        private ModelPart part;
    }
}
