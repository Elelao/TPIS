package tiw.is.scheduler.model;

import jakarta.persistence.Embeddable;

@Embeddable
public record Coordonnees(int x, int y) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordonnees that = (Coordonnees) o;
        return x == that.x && y == that.y;
    }
}
