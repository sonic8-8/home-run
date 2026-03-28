package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.PreparedRealEstateImportServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterImportRequest;
import io.ssafy.p.j14c103.homerun.config.ConditionalOnRealEstateImportEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@ConditionalOnRealEstateImportEnabled
@RequiredArgsConstructor
public class RealEstateMasterImportService {

    private final RealEstateImportPreparationService realEstateImportPreparationService;
    private final RealEstateImportPersistenceService realEstateImportPersistenceService;
    private final TransactionTemplate transactionTemplate;

    public void importMaster(RealEstateMasterImportRequest request) {
        PreparedRealEstateImportServiceRequest preparedImport =
            realEstateImportPreparationService.prepareImport(request);
        persistPreparedImport(preparedImport);
    }

    private void persistPreparedImport(PreparedRealEstateImportServiceRequest preparedImport) {
        transactionTemplate.executeWithoutResult(
            status -> realEstateImportPersistenceService.persist(preparedImport)
        );
    }
}
