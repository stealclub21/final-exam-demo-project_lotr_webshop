package hu.progmasters.webshop.domain.enumeration;

public enum Role {

    ROLE_GUEST("GUEST"),
    ROLE_BASICUSER("BASIC_USER"),
    ROLE_ADMIN("ADMIN"),
    ROLE_PREMIUM("PREMIUM_USER");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
