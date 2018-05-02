package com.gameapi.rha.mechanics.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public abstract class GameObject {
    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    @NotNull
    private final Map<Class<?>, GamePart> parts = new HashMap<>();
    @NotNull
    private final Long id;

    public GameObject() {
        this.id = ID_GENERATOR.getAndIncrement();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends GamePart> T getPart(@NotNull Class<T> clazz) {
        return (T) parts.get(clazz);
    }

    @NotNull
    public <T extends GamePart> T claimPart(@NotNull Class<T> clazz) {
        @Nullable final T part = getPart(clazz);
        if (part == null) {
            throw new NullPointerException("Claimed part should not be null");
        }
        return part;
    }

    public <T extends GamePart> void addPart(@NotNull Class<T> clazz, @NotNull T gamePart) {
        parts.put(clazz, gamePart);
    }

    @NotNull
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

//    @NotNull
//    public List<Snap<? extends GamePart>> getPartSnaps() {
//        return parts.values().stream()
//                .filter(GamePart::shouldBeSnaped)
//                .map(GamePart::takeSnap)
//                .collect(Collectors.toList());
//    }

//    @NotNull
//    public abstract Snap<? extends GameObject> getSnap();
}