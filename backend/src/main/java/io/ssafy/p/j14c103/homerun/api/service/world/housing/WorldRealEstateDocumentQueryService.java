package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstateDocumentsQueryResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocument;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentChecklistItem;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldRealEstateDocumentQueryService {

    private static final String REGISTRY_DOCUMENT_LABEL = "등기사항전부증명서";
    private static final String CONTRACT_DOCUMENT_LABEL = "계약서";

    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final RealEstateDocumentRepository realEstateDocumentRepository;

    public RealEstateDocumentsQueryResponse getDocuments(final Long propertyId) {
        validatePropertyId(propertyId);
        validatePropertyExists(propertyId);

        final List<RealEstateDocumentsQueryResponse.Document> documents = realEstateDocumentRepository
            .findAllByPropertyIdOrderByRealEstateDocumentIdAsc(propertyId)
            .stream()
            .map(this::toDocument)
            .toList();

        return RealEstateDocumentsQueryResponse.from(documents);
    }

    private void validatePropertyId(final Long propertyId) {
        if (propertyId == null || propertyId <= 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validatePropertyExists(final Long propertyId) {
        if (realEstatePropertyRepository.existsById(propertyId)) {
            return;
        }

        throw new HomerunException(ErrorCode.HOUSING_PROPERTY_NOT_FOUND);
    }

    private RealEstateDocumentsQueryResponse.Document toDocument(final RealEstateDocument document) {
        return RealEstateDocumentsQueryResponse.Document.of(
            document.getRealEstateDocumentId(),
            mapDocumentTypeLabel(document.getDocumentType()),
            document.getImageUrl(),
            toChecklist(document.getChecklist())
        );
    }

    private String mapDocumentTypeLabel(final RealEstateDocumentType documentType) {
        if (documentType == RealEstateDocumentType.REGISTRY) {
            return REGISTRY_DOCUMENT_LABEL;
        }
        if (documentType == RealEstateDocumentType.CONTRACT) {
            return CONTRACT_DOCUMENT_LABEL;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private List<RealEstateDocumentsQueryResponse.ChecklistItem> toChecklist(
        final List<RealEstateDocumentChecklistItem> checklist
    ) {
        if (checklist == null) {
            return List.of();
        }

        return checklist.stream()
            .map(item -> RealEstateDocumentsQueryResponse.ChecklistItem.of(
                item.getTrapId(),
                item.getLabel(),
                item.getIsTrapped()
            ))
            .toList();
    }
}
