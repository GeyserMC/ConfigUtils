package org.geysermc.configutils.action.storage;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Storables {
  private final Map<Class<? extends Storable>, List<Storable>> storables = new HashMap<>();
  private final Map<Class<? extends Storable>, List<Unfinished>> notFinished = new HashMap<>();

  public boolean has(@NonNull Class<? extends Storable> storableType) {
    return first(storableType) != null;
  }

  public boolean hasMany(@NonNull Class<? extends Storable> storableType) {
    return all(storableType).size() > 1;
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public <T extends Storable> T first(@NonNull Class<T> storableType) {
    Objects.requireNonNull(storableType);

    List<Storable> items = storables.get(storableType);
    if (items != null && !items.isEmpty()) {
      return (T) items.get(0);
    }
    return null;
  }

  @NonNull
  @SuppressWarnings("unchecked")
  public <T extends Storable> List<T> all(@NonNull Class<T> storableType) {
    Objects.requireNonNull(storableType);

    List<Storable> items = storables.get(storableType);
    if (items != null && !items.isEmpty()) {
      return (List<T>) Collections.unmodifiableList(items);
    }
    return Collections.emptyList();
  }

  public boolean add(@NonNull Storable storable) {
    Objects.requireNonNull(storable);
    List<Storable> items = storables.computeIfAbsent(storable.getClass(), $ -> new ArrayList<>());

    if (!items.contains(storable) && (!(storable instanceof Singleton) || items.isEmpty())) {
      if (storable instanceof Unfinished) {
        notFinished.computeIfAbsent(storable.getClass(), $ -> new ArrayList<>())
            .add((Unfinished) storable);
      }
      return items.add(storable);
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <T extends Storable> T removeFirst(@NonNull Class<T> storableType) {
    Objects.requireNonNull(storableType);

    List<T> items = (List<T>) storables.get(storableType);
    if (items == null) {
      return null;
    }

    if (items.size() <= 1) {
      storables.remove(storableType);
    }

    T removed = items.remove(0);
    if (removed instanceof Unfinished) {
      List<?> unfinished = notFinished.get(storableType);
      unfinished.remove(removed);
      if (unfinished.isEmpty()) {
        notFinished.remove(storableType);
      }
    }
    return removed;
  }

  @SuppressWarnings("unchecked")
  public <T extends Storable> List<T> removeAll(@NonNull Class<T> storableType) {
    Objects.requireNonNull(storableType);

    notFinished.remove(storableType);

    List<T> items = (List<T>) storables.remove(storableType);
    if (items == null) {
      return Collections.emptyList();
    }
    return items;
  }

  public boolean hasUnfinished() {
    return !notFinished.isEmpty();
  }

  @SafeVarargs
  @Nullable
  public final Unfinished firstUnfinished(@NonNull Class<? extends Unfinished>... items) {
    Objects.requireNonNull(items);

    if (notFinished.isEmpty()) {
      return null;
    }

    if (items.length > 0) {
      int bestMatchIndex = Integer.MAX_VALUE;
      Unfinished bestMatch = null;

      for (Class<? extends Storable> unfinished : notFinished.keySet()) {
        List<Unfinished> unfinisheds = notFinished.get(unfinished);
        if (unfinisheds != null && !unfinisheds.isEmpty()) {
          for (int i = 0; i < items.length; i++) {
            Class<? extends Unfinished> item = items[i];
            if (item.equals(unfinished) && i < bestMatchIndex) {
              bestMatchIndex = i;
              bestMatch = unfinisheds.get(0);

              // we aren't going to find a better match
              if (i == 0) {
                return bestMatch;
              }
            }
          }
        }
      }

      // if we found any match: return it. Otherwise we'll find a different unfinished storable
      if (bestMatch != null) {
        return bestMatch;
      }
    }

    List<Unfinished> random = notFinished.values().stream().findFirst().orElse(null);
    if (random != null && !random.isEmpty()) {
      return random.get(0);
    }
    return null;
  }

  @SafeVarargs
  @NonNull
  public final List<Unfinished> allUnfinished(@NonNull Class<Unfinished>... items) {
    Objects.requireNonNull(items);

    if (notFinished.isEmpty()) {
      return Collections.emptyList();
    }

    List<IntObjectPair<Unfinished>> matches = new ArrayList<>();
    int nextMatchIndex = items.length;

    main: for (Entry<Class<? extends Storable>, List<Unfinished>> entry : notFinished.entrySet()) {
      List<Unfinished> unfinisheds = entry.getValue();
      if (unfinisheds != null && !unfinisheds.isEmpty()) {

        for (int i = 0; i < items.length; i++) {
          Class<? extends Unfinished> item = items[i];
          if (item.equals(entry.getKey())) {
            matches.add(new IntObjectImmutablePair<>(i, unfinisheds.get(0)));
            continue main;
          }
        }
        matches.add(new IntObjectImmutablePair<>(nextMatchIndex++, unfinisheds.get(0)));
      }
    }

    return matches.stream()
        .sorted(Comparator.comparing(Pair::left))
        .map(Pair::right)
        .collect(Collectors.toList());
  }
}
