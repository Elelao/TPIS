package tiw.is.scheduler.model;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tiw.is.scheduler.services.EtagereService;
import tiw.is.scheduler.services.FullEtagereException;
import tiw.is.scheduler.services.MissingProduitException;

import java.util.*;

@Entity
public class Etagere {

    //region Declarations
    private static final Logger log = LoggerFactory.getLogger(Etagere.class);

    /**
     * Nombre maximal de produits qu'une étagère peut contenir
     */
    public static final int MAX_CAPACITY = 100;

    /**
     * Le numéro de série de l'étagère
     */
    @Id
    private String serialNumber;

    /**
     * Le "contenu" de l'étagère
     */
    @ElementCollection
    @CollectionTable(name="produit_quantites", joinColumns=@JoinColumn(name="produit_ID"))
    @Column(name="produit_quantite")
    @MapKeyJoinColumn(name="produit")
    private final Map<Produit, Integer> produits = new HashMap<>();

    /**
     * La position *courante* de l'étagère
     */
    private Coordonnees position;

    /**
     * La liste des positions auxquelles l'étagère doit se rendre
     */
    @ElementCollection
    private final List<Coordonnees> destinations = new ArrayList<>();

    /**
     * Les conflits de positions avec d'autres étagères
     */
    @ManyToMany
    private final Set<Etagere> conflits = new HashSet<>();

    @Transient
    private EtagereService service;
    //endregion

    //region Constructors
    // Pour JPA
    public Etagere() {
    }

    public Etagere(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    //endregion

    //region serialNumber
    public String getSerialNumber() {
        return serialNumber;
    }
    //endregion

    //region produits
    public int getQuantiteProduit(Produit produit) {
        return Objects.requireNonNullElse(produits.get(produit), 0);
    }

    public void setQuantiteProduit(Produit produit, int quantite) {
        if (quantite == 0) {
            produits.remove(produit);
        } else {
            produits.put(produit, quantite);
        }
        log.info("Etagere - setQuantiteProduit : " + serialNumber + " produit: " + produit + " quantite: " + quantite);
    }

    public Collection<Produit> getProduitList() {
        return produits.keySet();
    }

    public void addQuantiteProduit(Produit produit, int quantite) throws FullEtagereException {
        if (getOverallQuantite() + quantite > MAX_CAPACITY) {
            throw new FullEtagereException(serialNumber);
        }
        produits.put(produit, getQuantiteProduit(produit) + quantite);
    }

    public void removeQuantiteProduit(Produit produit, int quantite) throws MissingProduitException {
        if (getOverallQuantite() - quantite < 0) {
            throw new MissingProduitException(serialNumber, produit.code());
        }
        produits.put(produit, getQuantiteProduit(produit) - quantite);
    }

    public int getOverallQuantite() {
        return produits.values().stream().mapToInt(x -> x).sum();
    }
    //endregion

    //region position
    public Coordonnees getPosition() {
        return position;
    }

    public void setPosition(Coordonnees position) {
        this.position = position;
        log.info("Etagere " + serialNumber + " en position (" + position.x() + ", " + position.y() + ")");
    }
    //endregion

    //region destinations
    public List<Coordonnees> getDestinations() {
        return destinations;
    }

    public Coordonnees getNextDestination() {
        if (destinations.isEmpty()) {
            return null;
        } else {
            return destinations.getFirst();
        }
    }
    //endregion

    //region conflits
    public void addConflit(Etagere etagere) {
        // On force la symétrie en représentant deux fois le conflit, une dans chaque sens
        // Pas très élégant au niveau relationnel (duplication d'information), mais simplifie les requêtes.
        conflits.add(etagere);
        //etagere.conflits.add(this);
    }

    public void removeConflit(Etagere etagere) {
        conflits.remove(etagere);
        //etagere.conflits.remove(this);
    }

    public boolean hasConflit() {
        return !conflits.isEmpty();
    }

    public Set<Etagere> getConflits() {
        return conflits;
    }

    public boolean hasConflitWith(Etagere etagere) {
        return conflits.contains(etagere);
    }
    //endregion

    //region service
    public EtagereService getService() {
        if (service == null) {
            service = new EtagereService(this);
        }
        return service;
    }
    //endregion

    //region Object methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Etagere etagere = (Etagere) o;
        return Objects.equals(serialNumber, etagere.serialNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(serialNumber);
    }
    //endregion
}