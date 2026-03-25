package io.ssafy.p.j14c103.homerun.api.service.world.housing.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.Getter;

@Getter
public class RealEstateDocumentsQueryResponse {

    private final List<Document> documents;

    private RealEstateDocumentsQueryResponse(final List<Document> documents) {
        if (documents == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        this.documents = List.copyOf(documents);
    }

    public static RealEstateDocumentsQueryResponse from(final List<Document> documents) {
        return new RealEstateDocumentsQueryResponse(documents);
    }

    @Getter
    public static class Document {

        private final Long documentId;
        private final String type;
        private final String imageUrl;
        private final List<ChecklistItem> checklist;

        private Document(
            final Long documentId,
            final String type,
            final String imageUrl,
            final List<ChecklistItem> checklist
        ) {
            validateDocumentId(documentId);
            validateType(type);
            validateImageUrl(imageUrl);
            validateChecklist(checklist);

            this.documentId = documentId;
            this.type = type;
            this.imageUrl = imageUrl;
            this.checklist = List.copyOf(checklist);
        }

        public static Document of(
            final Long documentId,
            final String type,
            final String imageUrl,
            final List<ChecklistItem> checklist
        ) {
            return new Document(documentId, type, imageUrl, checklist);
        }

        private static void validateDocumentId(final Long documentId) {
            if (documentId == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }

        private static void validateType(final String type) {
            if (type == null || type.isBlank()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }

        private static void validateImageUrl(final String imageUrl) {
            if (imageUrl == null || imageUrl.isBlank()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }

        private static void validateChecklist(final List<ChecklistItem> checklist) {
            if (checklist == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    @Getter
    public static class ChecklistItem {

        private final String trapId;
        private final String label;
        private final boolean isTrapped;

        private ChecklistItem(
            final String trapId,
            final String label,
            final boolean isTrapped
        ) {
            validateTrapId(trapId);
            validateLabel(label);

            this.trapId = trapId;
            this.label = label;
            this.isTrapped = isTrapped;
        }

        public static ChecklistItem of(
            final String trapId,
            final String label,
            final boolean isTrapped
        ) {
            return new ChecklistItem(trapId, label, isTrapped);
        }

        private static void validateTrapId(final String trapId) {
            if (trapId == null || trapId.isBlank()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }

        private static void validateLabel(final String label) {
            if (label == null || label.isBlank()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }
}
