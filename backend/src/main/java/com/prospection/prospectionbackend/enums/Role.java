package com.prospection.prospectionbackend.enums;

public enum Role {
    AGENT("Agent",1),
    CHEF_BRANCHE("Chef de branche", 2),
    SUPERVISEUR("Superviseur", 3),
    CHEF_ANIMATION_REGIONAL("Chef animation régional", 4),
    SIEGE("SIEGE", 5);

    private final String displayName;
    private final int hierarchyLevel;

    Role(String displayName, int hierarchyLevel) {
        this.displayName = displayName;
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getDisplayName() {
        return displayName;
    }
    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public boolean isHigherThan(Role role) {
        return hierarchyLevel > role.hierarchyLevel;
    }

    public boolean isLowerThan(Role role) {
        return hierarchyLevel < role.hierarchyLevel;
    }

    public Role[] getLowerRoles() {
        return java.util.Arrays.stream(Role.values())
                .filter(role -> role.hierarchyLevel < this.hierarchyLevel)
                .toArray(Role[]::new);
    }
}
