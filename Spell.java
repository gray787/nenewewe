package com.example.wizardingworld;

public record Spell(String id, String name, String description, Effect effect) {
    public enum Effect {
        LIGHT, EXTINGUISH, PULL_ITEMS, SHIELD, LEVITATE, FIRE, WATER, HEAL,
        STUN, FREEZE, PATRONUS, CONFUSE, REVEAL, DISARM, REPAIR, TELEPORT,
        SLOW, GLOW, KNOCKBACK, CLEANSE
    }
}
