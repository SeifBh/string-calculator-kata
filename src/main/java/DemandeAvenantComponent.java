package fr.aba.prevoyance.api.component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fr.aba.prevoyance.api.converter.ContratConverter;
import fr.aba.prevoyance.api.converter.DemandeAvenantConverter;
import fr.aba.prevoyance.postgresql.entity.DemandeAvenantContratEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.aba.prevoyance.api.service.DemandeAvenantService;
import com.aviva.prevoyance.pivot.adhesion.contrat.avenant.DemandeAvenantPivot;
import com.aviva.prevoyance.pivot.adhesion.contrat.v2.ContratPivot;
import com.aviva.prevoyance.pivot.couverture.v2.CouverturePivot;
import com.aviva.prevoyance.pivot.couverture.v2.GarantiePivot;
import com.aviva.prevoyance.pivot.finance.PrimePivot;
import com.aviva.prevoyance.pivot.finance.SurprimePivot;
import com.aviva.prevoyance.pivot.personne.v2.RolePersonnePivot;
import com.aviva.prevoyance.pivot.v2.DossierPivot;
import com.aviva.prevoyance.pivotparametrage.IntervalleMontantsPivot;
import com.aviva.prevoyance.pivotparametrage.avenant.DemandeAvenantConstantes;
import com.aviva.prevoyance.pivotparametrage.avenant.DemandeAvenantConstantes.EtatDemandeAvenant;
import com.aviva.prevoyance.pivottechnical.exceptions.v2.ComponentCoherenceDatasException;
import com.aviva.prevoyance.pivottechnical.exceptions.v2.ComponentValidationException;
import com.aviva.prevoyance.pivottechnical.exceptions.v2.EnumNotFoundException;
import fr.aba.prevoyance.postgresql.entity.DemandeAvenantEntity;
import fr.aba.prevoyance.postgresql.repository.DemandeAvenantContratRepository;
import fr.aba.prevoyance.postgresql.repository.DemandeAvenantRepository;
import com.aviva.prevoyance.utils.component.BigDecimalComponent;
import com.aviva.prevoyance.utilsmicroservice.component.contrat.APIGarantie;

@Component
public class DemandeAvenantComponent {

	@Autowired private DemandeAvenantRepository repositoryDemandeAvenant;
	@Autowired private DemandeAvenantContratRepository repositoryDemandeAvenantContrat;
	@Autowired private DemandeAvenantConverter convDemandeAvenant;
	@Autowired private DemandeAvenantService sDemandeAvenant;
	@Autowired private APIGarantie apiGarantie;

	@Autowired private GarantieComponent cGarantie;
	@Autowired private PersonneComponent cPersonne;
	@Autowired private SurprimesComponent surprimesComponent;
	@Autowired private ContratConverter convContrat;
	@Autowired private BigDecimalComponent cBigDecimal;

	public Optional<DemandeAvenantPivot> demandeOuvertureNouvelleDemandeAvenantV2(DossierPivot dossier, UUID uuidTrace) {
		BigInteger idDemandeAvenant = sDemandeAvenant.createNewDemandeAvenantV2(dossier, uuidTrace);
		return getDemandeAvenant(idDemandeAvenant);
	}

	private Predicate<? super CouverturePivot> nePasPrendreLesCouverturesRefusees() {
		return couverture -> !StringUtils.equals("5", couverture.getEtat().getCodeEtat());
	}

	public Optional<DemandeAvenantPivot> createDemandeAvenantAcceptee(DossierPivot dossier, BigInteger idDemandeAvenant, BigInteger idDemandeAvenantPhoto, UUID uuidTrace) {
		// Obtenir les garanties du contrat
		List<GarantiePivot> garantiesContrat = apiGarantie.getGarantiesV3(dossier.getContrat().getRéférenceCommerciale(), Arrays.asList("ParametrageCouverture"), uuidTrace);

		if (dossier != null && dossier.getContrat() != null && dossier.getContrat().getInformations() != null && dossier.getContrat().getInformations().getGaranties() != null) {
			// mettre le montant assuré du contrat dans la garantie acceptée
			fillMontantAssureContratDansDemandeAvenantAcceptee(dossier, garantiesContrat);
			// mettre les surprimes du contrat dans la garantie acceptée
			fillSurprimesApresAcceptation(garantiesContrat, dossier.getContrat().getInformations().getGaranties());

		}
		BigInteger idDemandeAvenantAcceptee = sDemandeAvenant.createDemandeAvenantAcceptee(dossier, idDemandeAvenant, idDemandeAvenantPhoto, uuidTrace);
		return getDemandeAvenant(idDemandeAvenantAcceptee);
	}

	public void fillSurprimesContratDansGaranties(List<GarantiePivot> garantiesPhoto, List<GarantiePivot> garantiesSouhaitees) {
		Map<String, SurprimePivot> surprimes = surprimesComponent.determinerSurprimesDemandeAvenantAPartirDeLaPhotoParTypeDeGarantie(garantiesSouhaitees, garantiesPhoto);
		if (surprimes != null && !surprimes.isEmpty()) {
			garantiesSouhaitees.stream().filter(Objects::nonNull).forEach(garantieSouhaitee -> {
				SurprimePivot surprime = surprimes.get(garantieSouhaitee.getCode());
				if (surprime != null) {
					garantieSouhaitee.getCouvertures().stream().filter(c -> c.getNumCouvertureFils() == null).forEach(couverture -> {
						if (couverture.getPrime() == null) {
							PrimePivot prime = new PrimePivot();
							couverture.setPrime(prime);
						}
						couverture.getPrime().setSurprimes(surprime);
					});
				}
			});
		}
	}

	public void fillSurprimesApresAcceptation(List<GarantiePivot> garantiesContrat, List<GarantiePivot> garantiesSouhaitees) {
		Map<Integer, SurprimePivot> surprimes = surprimesComponent.extractSurprimesParNumeroCouverture(garantiesContrat);
		if (surprimes != null && !surprimes.isEmpty()) {
			garantiesSouhaitees.stream().filter(Objects::nonNull).filter(g -> g.getCouvertures() != null).forEach(garantieSouhaitee -> {
				garantieSouhaitee.getCouvertures().stream().filter(c -> c.getNumeroCouverture() != null).forEach(couverture -> {
					SurprimePivot surprime = surprimes.get(couverture.getNumeroCouverture());
					if (surprime != null) {
						if (couverture.getPrime() == null) {
							PrimePivot prime = new PrimePivot();
							couverture.setPrime(prime);
						}
						couverture.getPrime().setSurprimes(surprime);
					}
				});
			});
		}
	}

	public void fillMontantAssureContratDansDemandeAvenantAcceptee(DossierPivot dossier, List<GarantiePivot> garantiesContrat) {
		for (GarantiePivot garantieContrat : garantiesContrat) {
			GarantiePivot garantieDemandeAvenantAcceptee = getGarantieDemandeAvenantAcceptee(dossier, garantieContrat);
			if (garantieDemandeAvenantAcceptee != null && !StringUtils.equalsAny(garantieContrat.getCode(), "RE", "RC")) {
				if (!garantieContrat.getCouvertures().isEmpty()) {
					CouverturePivot couverture = garantieDemandeAvenantAcceptee.getCouvertures().get(0);
					BigDecimal montantTotal = getMontantTotal(garantieContrat, garantieDemandeAvenantAcceptee);
					couverture.setMontantAssure(montantTotal);
					garantieDemandeAvenantAcceptee.setCouvertures(Collections.singletonList(couverture));
				}
			}
		}
	}

	private GarantiePivot getGarantieDemandeAvenantAcceptee(DossierPivot dossier, GarantiePivot garantieContrat) {
		return dossier.getContrat().getInformations().getGaranties().stream()
				.filter(g -> g.getCodeCombinaisonFils() != null && g.getCodeCombinaisonFils().getCodePlan().equals(garantieContrat.getCodeCombinaisonFils().getCodePlan())).findFirst().orElse(null);
	}

	private BigDecimal getMontantTotal(GarantiePivot garantieContrat, GarantiePivot garantieDemandeAvenant) {
		List<CouverturePivot> couvertures = montantAssureDemandeAvenantEditeeInferieurAMontantAsurreContrat(garantieContrat, garantieDemandeAvenant) ? garantieDemandeAvenant.getCouvertures()
				: garantieContrat.getCouvertures();
		return couvertures.stream().filter(nePasPrendreLesCouverturesRefusees()).map(c -> c.getMontantAssure() != null ? c.getMontantAssure() : BigDecimal.ZERO).reduce(BigDecimal.ZERO,
				BigDecimal::add);
	}

	private boolean montantAssureDemandeAvenantEditeeInferieurAMontantAsurreContrat(GarantiePivot garantieContrat, GarantiePivot garantieDemandeAvenant) {
		List<CouverturePivot> couverturesContrat = garantieContrat.getCouvertures();
		List<CouverturePivot> couverturesDemandeAvenant = garantieDemandeAvenant.getCouvertures();

		if (!couverturesContrat.isEmpty() && !couverturesDemandeAvenant.isEmpty()) {
			BigDecimal montantAssureContrat = couverturesContrat.get(0).getMontantAssure();
			BigDecimal montantAssureDemandeAvenant = couverturesDemandeAvenant.get(0).getMontantAssure();

			if (montantAssureContrat != null && montantAssureDemandeAvenant != null) {
				return montantAssureDemandeAvenant.compareTo(montantAssureContrat) < 0;
			}
		}

		return false;
	}

	public void suspendreDemandeAvenant(final BigInteger idDemandeAvenant) {
		sDemandeAvenant.suspendreDemandeAvenant(idDemandeAvenant);
	}

	public void enregistrerTarif(Optional<DemandeAvenantPivot> demandeAvenant) {
		sDemandeAvenant.enregistrerTarif(demandeAvenant);
	}

	public Optional<Boolean> enregistrerNumeroTravail(BigInteger idDemandeAvenant, String numeroTravail) {
		if (idDemandeAvenant != null) {
			Optional<DemandeAvenantEntity> oDemandeAvenant = repositoryDemandeAvenant.findById(idDemandeAvenant);
			if (oDemandeAvenant.isPresent()) {
				oDemandeAvenant.get().setNumeroTravail(numeroTravail);
				repositoryDemandeAvenant.save(oDemandeAvenant.get());
				return Optional.of(Boolean.TRUE);
			}
		}
		return Optional.of(Boolean.FALSE);
	}

	public Optional<DemandeAvenantPivot> getDemandeAvenantAcceptee(String refContrat) {
		if (StringUtils.isBlank(refContrat)) {
			throw new ComponentValidationException("Impossible de récuperer la demande d'avenant pour la référence contrat [" + refContrat + "]");
		}
		Optional<DemandeAvenantPivot> oDemandeEnCours = getDemandeAvenantEnCours(refContrat);
		if (oDemandeEnCours.isPresent() && oDemandeEnCours.get().getIdPhotoDemandeAvenantAcceptee() != null) {
			return getDemandeAvenant(oDemandeEnCours.get().getIdPhotoDemandeAvenantAcceptee());
		}
		return Optional.empty();
	}

	private List<DemandeAvenantEntity> getEntitiesDemandesAvenantsDunContrat(String refContrat) {
		if (StringUtils.isBlank(refContrat)) {
			throw new ComponentValidationException("Impossible de récuperer les demandes d'avenants pour la référence contrat [" + refContrat + "]");
		}
		return repositoryDemandeAvenant.customFindByReferenceAdhesion(refContrat);
	}

	public List<DemandeAvenantPivot> getDemandesAvenantsDunContrat(String refContrat) {

		List<DemandeAvenantEntity> entities = getEntitiesDemandesAvenantsDunContrat(refContrat);

		// @formatter:off
		return entities.stream().filter(Objects::nonNull)
				.filter(nePasPrendreLaPhotoContrat())
				.filter(nePasPrendreLaPhotoAvenantAccepte())
				.map(e -> convDemandeAvenant.convertEntityToPivot(e))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sorted(Comparator.comparing(DemandeAvenantPivot::getId)).collect(Collectors.toList());
		// @formatter:on
	}

	private Predicate<? super DemandeAvenantEntity> nePasPrendreLaPhotoContrat() {
		return e -> !StringUtils.equals(EtatDemandeAvenant.PHOTO.getCode(), e.getCodeEtat());
	}

	private Predicate<? super DemandeAvenantEntity> nePasPrendreLaPhotoAvenantAccepte() {
		return garderLaPhotoAvenantAccepte().negate();
	}

	private Predicate<? super DemandeAvenantEntity> garderLaPhotoAvenantAccepte() {
		return e -> StringUtils.equals(EtatDemandeAvenant.DEMANDE_AVENANT_ACCEPTEE.getCode(), e.getCodeEtat());
	}

	public List<DemandeAvenantPivot> getDemandesAvenantsModifiables(String refContrat) {
		List<DemandeAvenantPivot> demandes = getDemandesAvenantsDunContrat(refContrat);
		return demandes.stream().filter(e -> {
			try {
				return e.getEtatEnum().isPresent() && e.getEtatEnum().get().estModifiable();
			} catch (EnumNotFoundException e1) {
				return false;
			}
		}).collect(Collectors.toList());
	}

	private List<DemandeAvenantPivot> getDemandesAvenantsEnCours(String refContrat) {
		List<DemandeAvenantPivot> demandes = getDemandesAvenantsDunContrat(refContrat);
		return demandes.stream().filter(neCorrespondPasAUnEtatTerminal()).collect(Collectors.toList());
	}

	public Optional<DemandeAvenantPivot> getDemandeAvenantEnCours(String refContrat) {
		List<DemandeAvenantPivot> demandesOuvertes = getDemandesAvenantsEnCours(refContrat).stream().filter(neCorrespondPasAUnEtatTerminal()).collect(Collectors.toList());
		if (demandesOuvertes.size() > 1) {
			throw new ComponentCoherenceDatasException(
					"Il existe plusieurs demandes avenants ouvertes : " + demandesOuvertes.stream().map(DemandeAvenantPivot::getId).collect(Collectors.toList()).toString());
		}
		if (demandesOuvertes.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(demandesOuvertes.get(0));

	}

	public List<DemandeAvenantPivot> getDemandesAvenantsAcceptees(String refContrat) {
		List<DemandeAvenantEntity> entities = getEntitiesDemandesAvenantsDunContrat(refContrat);

		// @formatter:off
		return entities.stream().filter(Objects::nonNull)
				.filter(garderLaPhotoAvenantAccepte())
				.map(e -> convDemandeAvenant.convertEntityToPivot(e))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sorted(Comparator.comparing(DemandeAvenantPivot::getId)).collect(Collectors.toList());
		// @formatter:on
	}

	public List<DemandeAvenantPivot> getDemandesAvenantsEnCoursNonModifiablesMaisAnnulables(String refContrat) {
		List<DemandeAvenantPivot> demandes = getDemandesAvenantsEnCours(refContrat);
		return demandes.stream().filter(e -> {
			try {
				return e.getEtatEnum().isPresent() && !e.getEtatEnum().get().estModifiable();
			} catch (EnumNotFoundException e1) {
				return false;
			}
		}).filter(e -> {
			try {
				return e.getEtatEnum().isPresent() && e.getEtatEnum().get().estAnnulableParIntermediaire();
			} catch (EnumNotFoundException e1) {
				return false;
			}
		}).collect(Collectors.toList());
	}

	public List<DemandeAvenantPivot> getDemandesAvenantsDansEtatTerminal(String refContrat) {
		List<DemandeAvenantPivot> demandes = getDemandesAvenantsDunContrat(refContrat);
		return demandes.stream().filter(correspondAUnEtatTerminal()).collect(Collectors.toList());
	}

	private Predicate<? super DemandeAvenantPivot> neCorrespondPasAUnEtatTerminal() {
		return correspondAUnEtatTerminal().negate();
	}

	private Predicate<? super DemandeAvenantPivot> correspondAUnEtatTerminal() {
		return e -> EtatDemandeAvenant.getByCode(e.getEtat().getCode()).estTerminal();
	}

	public Optional<DemandeAvenantPivot> getDemandeAvenant(BigInteger idDemandeAvenant) {
		if (idDemandeAvenant != null) {
			Optional<DemandeAvenantEntity> oEntity = repositoryDemandeAvenant.findById(idDemandeAvenant);
			if (oEntity.isPresent()) {
				return convDemandeAvenant.convertEntityToPivot(oEntity.get());
			}
		}
		return Optional.empty();
	}

	@Deprecated
	public Optional<DemandeAvenantPivot> getDemandeAvenantCompletV3(BigInteger idDemandeAvenant) {
		if (idDemandeAvenant != null) {
			Optional<DemandeAvenantPivot> oDemandeAvenant = getDemandeAvenant(idDemandeAvenant);
			if (oDemandeAvenant.isPresent()) {
				DemandeAvenantPivot demandeAvenant = oDemandeAvenant.get();
				Optional<DemandeAvenantContratEntity> oEntityContrat = repositoryDemandeAvenantContrat.findById(idDemandeAvenant);
				if (oEntityContrat.isPresent()) {
					demandeAvenant.setCodeProduit(oEntityContrat.get().getVersionProduit());

					Optional<DemandeAvenantPivot> oContrat = convContrat.entityContratToContrat(oEntityContrat, oDemandeAvenant);
					Optional<RolePersonnePivot> oAssure = cPersonne.getAssure(idDemandeAvenant);
					Optional<RolePersonnePivot> oAdherent = cPersonne.getAdherent(idDemandeAvenant);
					List<GarantiePivot> garanties = cGarantie.getGarantiesV2(idDemandeAvenant);
					ContratPivot contratAlimente = null;
					if (StringUtils.equals(demandeAvenant.getCodeEtat(), EtatDemandeAvenant.PHOTO.getCode())) {
						demandeAvenant.setPhotoDuContratALOuvertureDemande(oContrat.get().getDemandeAvenant());
						contratAlimente = demandeAvenant.getPhotoDuContratALOuvertureDemande();
					} else {
						demandeAvenant.setDemandeAvenant(oContrat.get().getDemandeAvenant());
						contratAlimente = demandeAvenant.getDemandeAvenant();
					}
					if (contratAlimente != null) {
						contratAlimente.getInformations().setRoles(Arrays.asList(oAssure, oAdherent).stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
						contratAlimente.getInformations().setGaranties(garanties);
					}
				}
				return oDemandeAvenant;
			}
		}
		return Optional.empty();
	}

	/**
	 * Cette version améliore la récupération des garanties / à la v3
	 *
	 * @param idDemandeAvenant
	 * @return
	 */
	public Optional<DemandeAvenantPivot> getDemandeAvenantCompletV4(BigInteger idDemandeAvenant) {
		if (idDemandeAvenant != null) {
			Optional<DemandeAvenantPivot> oDemandeAvenant = getDemandeAvenant(idDemandeAvenant);
			if (oDemandeAvenant.isPresent()) {
				DemandeAvenantPivot demandeAvenant = oDemandeAvenant.get();
				Optional<DemandeAvenantContratEntity> oEntityContrat = repositoryDemandeAvenantContrat.findById(idDemandeAvenant);
				if (oEntityContrat.isPresent()) {
					demandeAvenant.setCodeProduit(oEntityContrat.get().getVersionProduit());

					Optional<DemandeAvenantPivot> oContrat = convContrat.entityContratToContrat(oEntityContrat, oDemandeAvenant);
					Optional<RolePersonnePivot> oAssure = cPersonne.getAssure(idDemandeAvenant);
					Optional<RolePersonnePivot> oAdherent = cPersonne.getAdherent(idDemandeAvenant);
					List<GarantiePivot> garanties = cGarantie.getGarantiesV3(idDemandeAvenant);

					ContratPivot contratAlimente = null;
					if (StringUtils.equals(demandeAvenant.getEtat().getCode(), EtatDemandeAvenant.PHOTO.getCode())) {
						demandeAvenant.setPhotoDuContratALOuvertureDemande(oContrat.get().getDemandeAvenant());
						contratAlimente = demandeAvenant.getPhotoDuContratALOuvertureDemande();
					} else {
						demandeAvenant.setDemandeAvenant(oContrat.get().getDemandeAvenant());
						contratAlimente = demandeAvenant.getDemandeAvenant();
					}
					if (contratAlimente != null) {
						contratAlimente.getInformations().setRoles(Arrays.asList(oAssure, oAdherent).stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
						contratAlimente.getInformations().setGaranties(garanties);
					}
				}
				return oDemandeAvenant;
			}
		}
		return Optional.empty();
	}

	public void fillContratEtGaranties(BigInteger idDemandeAvenant, Optional<DemandeAvenantPivot> oDemandeAvenant) {
		DemandeAvenantPivot demandeAvenant = oDemandeAvenant.get();
		Optional<DemandeAvenantContratEntity> oEntityContrat = repositoryDemandeAvenantContrat.findById(idDemandeAvenant);
		if (oEntityContrat.isPresent()) {
			demandeAvenant.setCodeProduit(oEntityContrat.get().getVersionProduit());

			Optional<DemandeAvenantPivot> oContrat = convContrat.entityContratToContrat(oEntityContrat, oDemandeAvenant);
			List<GarantiePivot> garanties = cGarantie.getGarantiesV3(idDemandeAvenant);
			ContratPivot contratAlimente = null;
			if (StringUtils.equals(demandeAvenant.getEtat().getCode(), EtatDemandeAvenant.PHOTO.getCode())) {
				demandeAvenant.setPhotoDuContratALOuvertureDemande(oContrat.get().getDemandeAvenant());
				contratAlimente = demandeAvenant.getPhotoDuContratALOuvertureDemande();
			} else {
				demandeAvenant.setDemandeAvenant(oContrat.get().getDemandeAvenant());
				contratAlimente = demandeAvenant.getDemandeAvenant();
			}
			if (contratAlimente != null) {
				contratAlimente.getInformations().setGaranties(garanties);
			}
		}
	}

	public Optional<IntervalleMontantsPivot> getIntervalleDeModificationDuRevenuAssure(BigDecimal revenuAssure) {
		if (revenuAssure == null) {
			return Optional.empty();
		}
		IntervalleMontantsPivot intervalleMontants = new IntervalleMontantsPivot();
		intervalleMontants
				.setMontantMinimum(cBigDecimal.minimum(cBigDecimal.arrondirRevenuAssure(revenuAssure), cBigDecimal.arrondirRevenuAssure(DemandeAvenantConstantes.BORNE_MINIMUM_CHANGEMENT_REVENU)));
		intervalleMontants.setMontantMinimumInclus(true);
		intervalleMontants.setMontantMaximum(cBigDecimal.arrondirRevenuAssure(revenuAssure.multiply(DemandeAvenantConstantes.COEFFICIENT_BORNE_MAXIMUM_CHANGEMENT_REVENU)));
		intervalleMontants.setMontantMaximumInclus(true);
		return Optional.of(intervalleMontants);
	}

	@Deprecated
	public Optional<IntervalleMontantsPivot> getIntervalleDeModificationDuRevenuAssureV2() {

		IntervalleMontantsPivot intervalleMontants = new IntervalleMontantsPivot();
		intervalleMontants.setMontantMinimum(cBigDecimal.arrondirMontant(DemandeAvenantConstantes.BORNE_MINIMUM_CHANGEMENT_REVENU));
		intervalleMontants.setMontantMinimumInclus(true);
		intervalleMontants.setMontantMaximum(cBigDecimal.arrondirMontant(BigDecimal.valueOf(160_000)));
		intervalleMontants.setMontantMaximumInclus(true);
		return Optional.of(intervalleMontants);
	}

	public void enregistrerSaisieUtilisateur(Optional<DemandeAvenantPivot> demandeAvenant, UUID uuidTrace) {
		sDemandeAvenant.enregistrerSaisieUtilisateur(demandeAvenant, uuidTrace);
	}

	public void mettreAjourEtat(BigInteger idDemandeAvenant, EtatDemandeAvenant nouvelEtat) {
		sDemandeAvenant.mettreAjourEtat(idDemandeAvenant, nouvelEtat);
	}
}