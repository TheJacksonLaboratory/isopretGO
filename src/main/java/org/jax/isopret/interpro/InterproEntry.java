package org.jax.isopret.interpro;

import org.jax.isopret.except.IsopretRuntimeException;

import java.util.Objects;

public class InterproEntry {
    private final int id;

    private final InterproEntryType entryType;

    private final String description;
    public InterproEntry(String id, InterproEntryType entryType, String description) {
        this.id = integerPart(id);
        this.entryType = entryType;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public InterproEntryType getEntryType() {
        return entryType;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entryType, description);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof InterproEntry))
            return false;
        InterproEntry that = (InterproEntry) obj;
        return this.id == that.id &&
                this.entryType == that.entryType &&
                this.description.equals(that.description);
    }




    public static int integerPart(String id) {
        if (! id.startsWith("IPR")) {
            throw new IsopretRuntimeException("Malformed interpro id: \"" + id + "\"");
        }
        return Integer.parseInt(id.substring(3));
    }


}
