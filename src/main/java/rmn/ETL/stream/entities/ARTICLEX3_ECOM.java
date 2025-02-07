package rmn.ETL.stream.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents extended e-commerce details for an ARTICLEX3 article.
 * <p>
 * This entity aggregates a comprehensive set of attributes related to the e-commerce
 * representation of an article, including product identifiers, language details, dimensions,
 * multimedia configurations, and distribution information.
 */
@Data
@NoArgsConstructor
public class ARTICLEX3_ECOM {
    private String codeLangue;
    private String referenceRMN;
    private String ean;
    private String clefAlice;
    private String tvaVente;
    private String referenceChapeau;
    private String statutX3;
    private String surCmd;
    private String marqueEditeur;
    private String designation1;
    private String designation2;
    private String produitPersonnalisable;
    private String matiereArticle;
    private String matiereOeuvre;
    private String codeLigneDediee;
    private String libelleLigneDediee;
    private List<String> codesManifestation = new ArrayList<>();
    private String museeDeConservationOeuvre;
    private String codeUniversX3;
    private String libelleUniversX3;
    private String codeArtiste;
    private String libelleArtiste;
    private String codeCourantEpoque;
    private String libelleCourantEpoque;
    private String cleGL;
    private String h1Secteur;
    private String h2Categorie;
    private String h3SousCategorie;
    private String codePaysMadeIn;
    private String codePaysDesignIn;
    private String consignesEntretien;
    private String codeTaille;
    private String libelleTaille;
    private String codeAge;
    private String libelleAge;
    private String sourdsMalentendants;
    private String longueurEmballee;
    private String largeurEmballee;
    private String hauteurEmballee;
    private String diametreEmballee;
    private String longueurNu;
    private String largeurNu;
    private String hauteurNu;
    private String diametreNu;
    private String epaisseurProduitNu;
    private String poidsNu;
    private String packEmballageSuremballage;
    private String poidsEmballe;
    private String diffuseur;
    private String distributeur;
    private String illustrateur;
    private String codeFormatLivre;
    private String libelleFormatLivre;
    private String nbPages;
    private String nombreIllustrations;
    private String dateParution;
    private String grammage;
    private String auteur;
    private String realisateur;
    private String dureeMultimedia;
    private String codeConfigMultimedia;
    private String libelleConfigMultimedia;
    private String produitCodeLangue1;
    private String produitCodeLangue2;
    private String datageGravure;
    private String dimensionFeuilleChalco;
    private String dimensionPlaqueCuivre;
    private String techniqueImpression;
    private String codeDouane;
    private String coEdition;
    private String licence;
    private String codeLicence;
    private String nomLicence;
    private String urlImageThb;
    private String canalDistribution;
}
