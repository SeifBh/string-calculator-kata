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


}