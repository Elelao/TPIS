package tiw.is.scheduler.controller;

@RestController
@RequestMapping("/produits")
public class ProduitController {

    @Autowired
    private ProduitService produitService;

    @PostMapping
    public ResponseEntity<ProduitDTO> creerProduit(@RequestBody ProduitDTO produitDTO) {
        Produit produit = produitService.creerProduit(produitDTO);
        ProduitDTO resultat = new ProduitDTO(produit.getId(), produit.getNom(), produit.getQuantite());
        return ResponseEntity.status(HttpStatus.CREATED).body(resultat);
    }
}
