package io.ssafy.p.j14c103.homerun.api.service.game.realestate;

import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.WorldRealEstatePropertyProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstatePropertyDetailProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstateDocumentResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocument;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateMoneyRenderingRule;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateRegistryQuizSample;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateRegistryRow;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateRegistrySection;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RealEstateDocumentService {

    private static final RealEstateDocumentType REGISTRY_DOCUMENT_TYPE = RealEstateDocumentType.REGISTRY;
    private static final String REGISTRY_DOCUMENT_LABEL = "등기사항전부증명서";
    private static final String SALE_PRICE_RATIO_SOURCE = "sale_price_ratio";
    private static final String TEN_THOUSAND_WON_UNIT = "만원";
    private static final String RISK_VERDICT = "위험";
    private static final String NORMAL_VERDICT = "정상";
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);

    private final GameSessionRepository gameSessionRepository;
    private final UserAuthContextService userAuthContextService;
    private final WorldRealEstatePropertyProviderService worldRealEstatePropertyProviderService;
    private final RealEstateDocumentRepository realEstateDocumentRepository;
    private final RealEstateRegistryRandomService realEstateRegistryRandomService;

    public RealEstateDocumentResponse getDocument(
        final Long userId,
        final Long sessionId,
        final Long propertyId
    ) {
        userAuthContextService.getContext(userId);
        getOwnedGameSession(userId, sessionId);

        final RealEstatePropertyDetailProviderResponse property =
            worldRealEstatePropertyProviderService.getPropertyDetail(propertyId);

        final RealEstateRegistryQuizSample gapguSample = extractQuizSample(selectGapguDocument(propertyId));
        final RealEstateRegistryQuizSample eulguSample = extractQuizSample(selectEulguDocument(propertyId));

        final RealEstateDocumentResponse.SectionSolutionResponse gapguSolution = toSectionSolution(gapguSample);
        final RealEstateDocumentResponse.SectionSolutionResponse eulguSolution = toSectionSolution(eulguSample);

        return RealEstateDocumentResponse.of(
            property.getPropertyId(),
            property.getName(),
            property.getAddress(),
            property.getLatitude(),
            property.getLongitude(),
            property.getRecentPrice(),
            REGISTRY_DOCUMENT_LABEL,
            renderRows(gapguSample.getRows(), property.getRecentPrice()),
            renderRows(eulguSample.getRows(), property.getRecentPrice()),
            RealEstateDocumentResponse.SolutionResponse.of(
                toOverallVerdict(gapguSolution.verdict(), eulguSolution.verdict()),
                gapguSolution,
                eulguSolution
            )
        );
    }

    private GameSession getOwnedGameSession(final Long userId, final Long sessionId) {
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);
        return gameSession;
    }

    private RealEstateDocument selectGapguDocument(final Long propertyId) {
        final List<RealEstateDocument> gapguDocuments = realEstateDocumentRepository
            .findAllByPropertyIdAndDocumentTypeAndRegistrySectionOrderByRealEstateDocumentIdAsc(
                propertyId,
                REGISTRY_DOCUMENT_TYPE,
                RealEstateRegistrySection.GAPGU
            );
        validateSamplePool(gapguDocuments);
        return gapguDocuments.get(realEstateRegistryRandomService.nextGapguIndex(gapguDocuments.size()));
    }

    private RealEstateDocument selectEulguDocument(final Long propertyId) {
        final List<RealEstateDocument> eulguDocuments = realEstateDocumentRepository
            .findAllByPropertyIdAndDocumentTypeAndRegistrySectionOrderByRealEstateDocumentIdAsc(
                propertyId,
                REGISTRY_DOCUMENT_TYPE,
                RealEstateRegistrySection.EULGU
            );
        validateSamplePool(eulguDocuments);
        return eulguDocuments.get(realEstateRegistryRandomService.nextEulguIndex(eulguDocuments.size()));
    }

    private void validateSamplePool(final List<RealEstateDocument> documents) {
        if (!documents.isEmpty()) {
            return;
        }

        throw new HomerunException(ErrorCode.HOUSING_REGISTRY_SAMPLE_INVALID);
    }

    private RealEstateRegistryQuizSample extractQuizSample(final RealEstateDocument document) {
        final RealEstateRegistryQuizSample quizSamplePayload = document.getQuizSamplePayload();
        if (quizSamplePayload == null) {
            throw new HomerunException(ErrorCode.HOUSING_REGISTRY_SAMPLE_INVALID);
        }
        if (quizSamplePayload.getRows() == null) {
            throw new HomerunException(ErrorCode.HOUSING_REGISTRY_SAMPLE_INVALID);
        }
        if (quizSamplePayload.getKeyPoints() == null) {
            throw new HomerunException(ErrorCode.HOUSING_REGISTRY_SAMPLE_INVALID);
        }
        if (quizSamplePayload.getIssueSummary() == null) {
            throw new HomerunException(ErrorCode.HOUSING_REGISTRY_SAMPLE_INVALID);
        }
        if (quizSamplePayload.getFeedbackCorrect() == null) {
            throw new HomerunException(ErrorCode.HOUSING_REGISTRY_SAMPLE_INVALID);
        }
        if (quizSamplePayload.getFeedbackWrong() == null) {
            throw new HomerunException(ErrorCode.HOUSING_REGISTRY_SAMPLE_INVALID);
        }
        return quizSamplePayload;
    }

    private RealEstateDocumentResponse.SectionSolutionResponse toSectionSolution(
        final RealEstateRegistryQuizSample quizSample
    ) {
        return RealEstateDocumentResponse.SectionSolutionResponse.of(
            mapVerdict(quizSample.getQuizVerdict()),
            quizSample.getIssueSummary(),
            quizSample.getKeyPoints(),
            quizSample.getFeedbackCorrect(),
            quizSample.getFeedbackWrong()
        );
    }

    private List<RealEstateDocumentResponse.RegistryRowResponse> renderRows(
        final List<RealEstateRegistryRow> rows,
        final long salePrice
    ) {
        return rows.stream()
            .map(row -> RealEstateDocumentResponse.RegistryRowResponse.of(
                row.getRankNo(),
                row.getPurpose(),
                row.getReceipt(),
                row.getReason(),
                renderMoneyText(row.getDetails(), row.getRendering(), salePrice)
            ))
            .toList();
    }

    private String renderMoneyText(
        final String text,
        final Map<String, RealEstateMoneyRenderingRule> rendering,
        final long salePrice
    ) {
        if (rendering == null || rendering.isEmpty()) {
            return text;
        }

        String renderedText = text;
        for (final Map.Entry<String, RealEstateMoneyRenderingRule> entry : rendering.entrySet()) {
            final RealEstateMoneyRenderingRule rule = entry.getValue();
            if (!SALE_PRICE_RATIO_SOURCE.equals(rule.getSource())) {
                continue;
            }

            final long amount = calculateRenderedAmount(salePrice, rule);
            renderedText = renderedText.replace("{" + entry.getKey() + "}", formatWon(amount));
        }
        return renderedText;
    }

    private long calculateRenderedAmount(final long salePrice, final RealEstateMoneyRenderingRule rule) {
        if (rule.getRatioPercent() == null) {
            throw new HomerunException(ErrorCode.HOUSING_REGISTRY_SAMPLE_INVALID);
        }

        final BigDecimal ratioPercent = BigDecimal.valueOf(rule.getRatioPercent());
        final BigDecimal rawAmount = BigDecimal.valueOf(salePrice)
            .multiply(ratioPercent)
            .divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP);

        if (!TEN_THOUSAND_WON_UNIT.equals(rule.getRoundingUnit())) {
            return rawAmount.longValue();
        }

        return rawAmount
            .divide(TEN_THOUSAND, 0, RoundingMode.HALF_UP)
            .multiply(TEN_THOUSAND)
            .longValue();
    }

    private String formatWon(final long amount) {
        final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
        return "금" + numberFormat.format(amount) + "원";
    }

    private String toOverallVerdict(final String gapguVerdict, final String eulguVerdict) {
        if (RISK_VERDICT.equals(gapguVerdict) || RISK_VERDICT.equals(eulguVerdict)) {
            return RISK_VERDICT;
        }

        return NORMAL_VERDICT;
    }

    private String mapVerdict(final String quizVerdict) {
        if (RISK_VERDICT.equals(quizVerdict)) {
            return RISK_VERDICT;
        }
        if (NORMAL_VERDICT.equals(quizVerdict)) {
            return NORMAL_VERDICT;
        }

        throw new HomerunException(ErrorCode.HOUSING_REGISTRY_SAMPLE_INVALID);
    }
}
