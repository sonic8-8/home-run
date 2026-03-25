package io.ssafy.p.j14c103.homerun.domain.world.housing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "real_estate_documents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealEstateDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "real_estate_document_id")
    private Long realEstateDocumentId;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private RealEstateDocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "registry_section")
    private RealEstateRegistrySection registrySection;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quiz_sample_payload")
    private RealEstateRegistryQuizSample quizSamplePayload;

    private RealEstateDocument(
        Long propertyId,
        RealEstateDocumentType documentType,
        RealEstateRegistrySection registrySection,
        RealEstateRegistryQuizSample quizSamplePayload
    ) {
        this.propertyId = propertyId;
        this.documentType = documentType;
        this.registrySection = registrySection;
        this.quizSamplePayload = quizSamplePayload;
    }

    public static RealEstateDocument create(
        Long propertyId,
        RealEstateDocumentType documentType,
        RealEstateRegistrySection registrySection,
        RealEstateRegistryQuizSample quizSamplePayload
    ) {
        return new RealEstateDocument(
            propertyId,
            documentType,
            registrySection,
            quizSamplePayload
        );
    }
}
