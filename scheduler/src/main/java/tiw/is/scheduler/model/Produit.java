package tiw.is.scheduler.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Objects;

/**
 * Représente un produit stocké sur une ou plusieurs étagères
 */
@Entity
public record Produit (
    // Le code barre du produit
    @Id String code,

    // Description du produit
    String description) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produit produit = (Produit) o;
        return Objects.equals(code, produit.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
