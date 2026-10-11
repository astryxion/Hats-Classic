package com.astryxion.hats.common.tabula;

import com.astryxion.hats.Hats;
import com.astryxion.hats.common.hat.HatItem;
import com.astryxion.hats.common.hat.HatRarity;

import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

public final class TabulaHats {

    public static final String ROOT = "assets/" + Hats.MODID + "/legacy_tabula/hats";

    private static final int[] RARITY_WEIGHTS = {21, 13, 8, 5, 3};
    private static final HatRarity[] RARITIES = HatRarity.values();

    private static final Map<String, TabulaHatDefinition> BY_ID = new LinkedHashMap<>();
    private static Path hatsRoot;

    private TabulaHats() {}

    public static List<TabulaHatDefinition> discover(Set<String> reservedIds) {
        BY_ID.clear();
        hatsRoot = locateRoot();

        List<String> relatives = listTblRelatives();
        if (relatives.isEmpty()) {
            Hats.LOGGER.warn("legacy_tabula hats folder not found");
            return List.of();
        }

        Set<String> reservedCompact = new HashSet<>();
        for (String reserved : reservedIds) {
            reservedCompact.add(compact(reserved));
        }

        List<TabulaHatDefinition> accessories = new ArrayList<>();
        for (String relative : relatives) {
            String tblName = tblFileName(relative);
            List<String> notes = List.of();
            try {
                notes = readNotes(relative);
            } catch (IOException e) {
                Hats.LOGGER.warn("Could not read notes for tabula hat {}", relative);
            }

            String accessoryFor = noteValue(notes, "hats-accessory:");
            String accessoryParent = noteValue(notes, "hats-accessory-parent:");
            List<String> layers = noteValues(notes, "hats-accessory-layer:");
            List<String> hideParts = noteValues(notes, "hats-accessory-hide-parent-part:");
            boolean accessory = accessoryFor != null;

            boolean nameTaken = reservedCompact.contains(compact(tblName));
            String preferred = snake(tblName);
            String id;
            String display;
            if (accessory) {
                id = uniqueAccessoryId(preferred, snake(accessoryFor));
                display = tblName;
            } else if (nameTaken || !isFree(preferred, reservedIds)) {
                id = uniqueParentId("classic_" + preferred, reservedIds);
                display = "Classic " + tblName;
            } else {
                id = preferred;
                display = tblName;
            }
            reservedIds.add(id);

            HatRarity rarity = rarityFrom(notes, relative, id);
            Boolean cull = cullFrom(notes);
            TabulaHatDefinition def = new TabulaHatDefinition(
                    id, tblName, relative, display, rarity, cull,
                    accessoryFor, accessoryParent, layers, hideParts
            );
            BY_ID.put(id, def);
            if (accessory) {
                accessories.add(def);
            }
        }

        List<TabulaHatDefinition> parents = new ArrayList<>();
        for (TabulaHatDefinition def : BY_ID.values()) {
            if (!def.isAccessory()) {
                parents.add(def);
            }
        }
        for (TabulaHatDefinition parent : parents) {
            attachAccessories(parent, accessories, parent.tblName);
        }
        for (TabulaHatDefinition leftover : accessories) {
            Hats.LOGGER.warn("Accessory '{}' could not find its parent '{}'", leftover.tblName, leftover.accessoryFor);
        }

        Hats.LOGGER.info("Discovered {} legacy tabula hats ({} accessories)", parents.size(), BY_ID.size() - parents.size());
        return parents;
    }

    public static TabulaHatDefinition get(String id) {
        return BY_ID.get(id);
    }

    public static TabulaHatDefinition from(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof HatItem hat) || !hat.isTabula()) {
            return null;
        }
        return BY_ID.get(hat.getHatId());
    }

    public static boolean isTabula(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && stack.getItem() instanceof HatItem hat
                && hat.isTabula();
    }

    public static Iterable<String> ids() {
        return BY_ID.keySet();
    }

    public static InputStream open(String relativePath) throws IOException {
        String full = ROOT + "/" + relativePath.replace('\\', '/');
        var contents = modContents();
        if (contents != null) {
            InputStream in = contents.openFile(full);
            if (in != null) {
                return in;
            }
        }
        Path path = resolve(relativePath);
        if (path != null && Files.isRegularFile(path)) {
            return Files.newInputStream(path);
        }
        return null;
    }

    private static List<String> readNotes(String relative) throws IOException {
        try (InputStream in = open(relative)) {
            if (in == null) {
                return List.of();
            }
            return TabulaTblReader.readNotes(in);
        }
    }

    private static List<String> listTblRelatives() {
        List<String> relatives = new ArrayList<>();
        var contents = modContents();
        if (contents != null) {
            String prefix = ROOT + "/";
            contents.visitContent(prefix, (path, resource) -> {
                if (path.startsWith(prefix) && path.endsWith(".tbl")) {
                    relatives.add(path.substring(prefix.length()));
                }
            });
        }
        if (relatives.isEmpty() && hatsRoot != null && Files.exists(hatsRoot)) {
            try (Stream<Path> walk = Files.walk(hatsRoot)) {
                walk.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".tbl"))
                        .sorted()
                        .map(TabulaHats::relativePath)
                        .forEach(relatives::add);
            } catch (IOException e) {
                throw new RuntimeException("Failed to scan legacy tabula hats", e);
            }
        }
        relatives.sort(String::compareTo);
        return relatives;
    }

    private static net.neoforged.fml.jarcontents.JarContents modContents() {
        try {
            var info = net.neoforged.fml.ModList.get().getModFileById(Hats.MODID);
            if (info == null) {
                return null;
            }
            return info.getFile().getContents();
        } catch (Exception e) {
            Hats.LOGGER.warn("Could not read hats mod jar", e);
            return null;
        }
    }

    public static Path resolve(String relativePath) {
        if (hatsRoot == null) {
            hatsRoot = locateRoot();
        }
        if (hatsRoot == null) {
            return null;
        }
        Path resolved = hatsRoot;
        for (String part : relativePath.replace('\\', '/').split("/")) {
            if (!part.isEmpty()) {
                resolved = resolved.resolve(part);
            }
        }
        return resolved;
    }

    public static String uniqueId(String relative, Set<String> reserved) {
        String preferred = snake(tblFileName(relative));
        if (isFree(preferred, reserved)) {
            return preferred;
        }
        return uniqueParentId("classic_" + preferred, reserved);
    }

    private static String uniqueParentId(String preferred, Set<String> reserved) {
        if (isFree(preferred, reserved)) {
            return preferred;
        }
        int n = 2;
        String candidate;
        do {
            candidate = preferred + "_" + n++;
        } while (!isFree(candidate, reserved));
        return candidate;
    }

    private static String uniqueAccessoryId(String preferred, String parentSnake) {
        if (isFree(preferred, Set.of()) && !BY_ID.containsKey(preferred)) {
            return preferred;
        }
        String withParent = parentSnake + "_" + preferred;
        if (!BY_ID.containsKey(withParent)) {
            return withParent;
        }
        int n = 2;
        String candidate;
        do {
            candidate = withParent + "_" + n++;
        } while (BY_ID.containsKey(candidate));
        return candidate;
    }

    private static void attachAccessories(
            TabulaHatDefinition parent,
            List<TabulaHatDefinition> candidates,
            String rootName
    ) {
        List<TabulaHatDefinition> claimed = new ArrayList<>();
        for (TabulaHatDefinition candidate : candidates) {
            if (!rootName.equals(candidate.accessoryFor)) {
                continue;
            }
            boolean parentMatch = candidate.accessoryParent == null
                    || candidate.accessoryParent.equals(parent.tblName);
            if (parentMatch) {
                parent.accessories.add(candidate);
                claimed.add(candidate);
            }
        }
        candidates.removeAll(claimed);
        for (TabulaHatDefinition accessory : parent.accessories) {
            attachAccessories(accessory, candidates, rootName);
        }
    }

    private static String tblFileName(String relative) {
        String noExt = stripTbl(relative);
        int slash = noExt.lastIndexOf('/');
        return slash < 0 ? noExt : noExt.substring(slash + 1);
    }

    private static String compact(String value) {
        StringBuilder out = new StringBuilder(value.length());
        String lower = value.toLowerCase(Locale.ROOT);
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String noteValue(List<String> notes, String prefix) {
        for (String note : notes) {
            if (note.startsWith(prefix)) {
                String value = note.substring(prefix.length()).trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    private static List<String> noteValues(List<String> notes, String prefix) {
        List<String> values = new ArrayList<>();
        for (String note : notes) {
            if (note.startsWith(prefix)) {
                String value = note.substring(prefix.length()).trim();
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
        }
        return values;
    }

    private static String snakePath(String relative, boolean dropDuplicateLast) {
        String noExt = stripTbl(relative);
        String[] parts = noExt.split("/");
        List<String> snakes = new ArrayList<>();
        for (String part : parts) {
            String snake = snake(part);
            if (!snake.isEmpty()) {
                snakes.add(snake);
            }
        }
        if (dropDuplicateLast && snakes.size() >= 2
                && snakes.get(snakes.size() - 1).equals(snakes.get(snakes.size() - 2))) {
            snakes.remove(snakes.size() - 1);
        }
        String joined = String.join("_", snakes);
        return joined.isEmpty() ? "hat" : joined;
    }

    private static String snake(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT).replace("'", "");
        StringBuilder out = new StringBuilder(lower.length());
        boolean underscore = false;
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                out.append(c);
                underscore = false;
            } else if (!underscore && out.length() > 0) {
                out.append('_');
                underscore = true;
            }
        }
        int len = out.length();
        if (len > 0 && out.charAt(len - 1) == '_') {
            out.setLength(len - 1);
        }
        return out.toString();
    }

    private static boolean isFree(String id, Set<String> reserved) {
        return id != null && !id.isEmpty() && !reserved.contains(id) && !BY_ID.containsKey(id);
    }

    private static String stripTbl(String relative) {
        String path = relative.replace('\\', '/');
        if (path.toLowerCase(Locale.ROOT).endsWith(".tbl")) {
            return path.substring(0, path.length() - 4);
        }
        return path;
    }

    private static String relativePath(Path tbl) {
        String relative = hatsRoot.relativize(tbl).toString().replace('\\', '/');
        while (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        return relative;
    }

    private static HatRarity rarityFrom(List<String> notes, String relative, String id) {
        for (String note : notes) {
            if (note.startsWith("hats-rarity:")) {
                return HatRarity.fromString(note.substring("hats-rarity:".length()).trim());
            }
        }
        if (relative.replace('\\', '/').toLowerCase(Locale.ROOT).startsWith("contributors/")) {
            return HatRarity.LEGENDARY;
        }
        return seededRarity(id);
    }

    private static Boolean cullFrom(List<String> notes) {
        for (String note : notes) {
            if (note.startsWith("hats-cull:")) {
                String v = note.substring("hats-cull:".length()).trim().toLowerCase(Locale.ROOT);
                if ("true".equals(v) || "false".equals(v)) {
                    return "true".equals(v);
                }
            }
        }
        return null;
    }

    private static HatRarity seededRarity(String id) {
        long seed = Math.abs((long) id.hashCode()) * 154041013L;
        Random rand = new Random(seed);
        double chance = rand.nextDouble();
        int total = 0;
        for (int weight : RARITY_WEIGHTS) {
            total += weight;
        }
        double cumulative = 0;
        for (int i = 0; i < RARITIES.length; i++) {
            cumulative += RARITY_WEIGHTS[i] / (double) total;
            if (chance <= cumulative) {
                return RARITIES[i];
            }
        }
        return HatRarity.LEGENDARY;
    }

    private static Path locateRoot() {
        try {
            URL url = TabulaHats.class.getClassLoader().getResource(ROOT);
            if (url == null) {
                return modContentPath(ROOT);
            }
            if ("jar".equals(url.getProtocol())) {
                URI uri = url.toURI();
                String uriStr = uri.toString();
                int sep = uriStr.indexOf("!/");
                URI jarUri = URI.create(uriStr.substring(0, sep));
                FileSystem fs;
                try {
                    fs = FileSystems.getFileSystem(jarUri);
                } catch (FileSystemNotFoundException e) {
                    try {
                        fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
                    } catch (FileSystemAlreadyExistsException exists) {
                        fs = FileSystems.getFileSystem(jarUri);
                    }
                }
                return fs.getPath(ROOT);
            }
            return Path.of(url.toURI());
        } catch (IOException | URISyntaxException e) {
            Hats.LOGGER.error("Failed to locate legacy tabula hats", e);
            return null;
        }
    }

    private static Path modContentPath(String path) {
        var info = net.neoforged.fml.ModList.get().getModFileById(Hats.MODID);
        if (info == null) {
            return null;
        }
        for (Path root : info.getFile().getContents().getContentRoots()) {
            Path candidate = root.resolve(path);
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
