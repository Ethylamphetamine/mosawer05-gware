/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.accounts;

public class UuidToProfileResponse {
    public Property[] properties;

    public String getPropertyValue(String name) {
        for (Property property : this.properties) {
            if (!property.name.equals(name)) continue;
            return property.value;
        }
        return null;
    }

    public static class Property {
        public String name;
        public String value;
    }
}

