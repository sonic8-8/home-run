package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.PreparedRealEstateImportServiceRequest;
import io.ssafy.p.j14c103.homerun.config.ConditionalOnRealEstateImportEnabled;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractTrap;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnRealEstateImportEnabled
@RequiredArgsConstructor
public class JdbcBatchRealEstateImportPersistenceService {

    private static final int BATCH_SIZE = 1_000;

    private static final String UPSERT_HOUSING_REGION_SQL = """
        insert into housing_regions (
            region_code,
            region_name
        ) values (?, ?)
        on conflict (region_code) do update
        set region_name = excluded.region_name
        """;

    private static final String UPSERT_HOUSING_DISTRICT_SQL = """
        insert into housing_districts (
            district_code,
            region_code,
            district_name,
            district_legal_dong_code
        ) values (?, ?, ?, ?)
        on conflict (district_code) do update
        set region_code = excluded.region_code,
            district_name = excluded.district_name,
            district_legal_dong_code = excluded.district_legal_dong_code
        """;

    private static final String UPSERT_HOUSING_LEGAL_DONG_SQL = """
        insert into housing_legal_dongs (
            legal_dong_code,
            parent_legal_dong_code,
            region_code,
            district_code,
            legal_dong_name,
            full_address_name
        ) values (?, ?, ?, ?, ?, ?)
        on conflict (legal_dong_code) do update
        set parent_legal_dong_code = excluded.parent_legal_dong_code,
            region_code = excluded.region_code,
            district_code = excluded.district_code,
            legal_dong_name = excluded.legal_dong_name,
            full_address_name = excluded.full_address_name
        """;

    private static final String INSERT_APARTMENT_TRADE_RAW_SQL = """
        insert into apartment_trade_raws (
            trade_key,
            district_code,
            legal_dong_name,
            legal_dong_code,
            apartment_name,
            jibun,
            deal_date,
            deal_amount,
            exclusive_area,
            floor,
            build_year,
            land_leasehold
        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        on conflict (trade_key) do nothing
        """;

    private static final String UPSERT_REAL_ESTATE_GEOCODE_CACHE_SQL = """
        insert into real_estate_geocode_caches (
            geocoding_query,
            geocoding_status,
            latitude,
            longitude,
            resolved_road_address,
            resolved_jibun_address
        ) values (?, ?, ?, ?, ?, ?)
        on conflict (geocoding_query) do update
        set geocoding_status = excluded.geocoding_status,
            latitude = excluded.latitude,
            longitude = excluded.longitude,
            resolved_road_address = excluded.resolved_road_address,
            resolved_jibun_address = excluded.resolved_jibun_address
        """;

    private static final String UPSERT_REAL_ESTATE_PROPERTY_SQL = """
        insert into real_estate_properties (
            provider_id,
            property_name,
            address,
            region_code,
            district_code,
            legal_dong_code,
            base_price_amount,
            latitude,
            longitude,
            housing_type,
            property_type,
            transaction_type,
            contract_traps
        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
        on conflict (provider_id) do update
        set property_name = excluded.property_name,
            address = excluded.address,
            region_code = excluded.region_code,
            district_code = excluded.district_code,
            legal_dong_code = excluded.legal_dong_code,
            base_price_amount = excluded.base_price_amount,
            latitude = coalesce(excluded.latitude, real_estate_properties.latitude),
            longitude = coalesce(excluded.longitude, real_estate_properties.longitude),
            housing_type = excluded.housing_type,
            property_type = excluded.property_type,
            transaction_type = excluded.transaction_type,
            contract_traps = excluded.contract_traps
        """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public void persist(PreparedRealEstateImportServiceRequest preparedImport) {
        upsertHousingRegions(preparedImport.getHousingRegions());
        upsertHousingDistricts(preparedImport.getHousingDistricts());
        upsertHousingLegalDongs(preparedImport.getHousingLegalDongs());
        insertApartmentTradeRaws(preparedImport.getApartmentTradeRaws());
        upsertRealEstateGeocodeCaches(preparedImport.getRealEstateGeocodeCaches());
        upsertRealEstateProperties(preparedImport.getRealEstateProperties());
    }

    private void upsertHousingRegions(List<PreparedRealEstateImportServiceRequest.HousingRegionRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
            UPSERT_HOUSING_REGION_SQL,
            rows,
            BATCH_SIZE,
            (PreparedStatement statement, PreparedRealEstateImportServiceRequest.HousingRegionRow row) -> {
                statement.setString(1, row.getRegionCode());
                statement.setString(2, row.getRegionName());
            }
        );
    }

    private void upsertHousingDistricts(List<PreparedRealEstateImportServiceRequest.HousingDistrictRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
            UPSERT_HOUSING_DISTRICT_SQL,
            rows,
            BATCH_SIZE,
            (PreparedStatement statement, PreparedRealEstateImportServiceRequest.HousingDistrictRow row) -> {
                statement.setString(1, row.getDistrictCode());
                statement.setString(2, row.getRegionCode());
                statement.setString(3, row.getDistrictName());
                statement.setString(4, row.getDistrictLegalDongCode());
            }
        );
    }

    private void upsertHousingLegalDongs(List<PreparedRealEstateImportServiceRequest.HousingLegalDongRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
            UPSERT_HOUSING_LEGAL_DONG_SQL,
            rows,
            BATCH_SIZE,
            (PreparedStatement statement, PreparedRealEstateImportServiceRequest.HousingLegalDongRow row) -> {
                statement.setString(1, row.getLegalDongCode());
                statement.setString(2, row.getParentLegalDongCode());
                statement.setString(3, row.getRegionCode());
                statement.setString(4, row.getDistrictCode());
                statement.setString(5, row.getLegalDongName());
                statement.setString(6, row.getFullAddressName());
            }
        );
    }

    private void insertApartmentTradeRaws(List<PreparedRealEstateImportServiceRequest.ApartmentTradeRawRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
            INSERT_APARTMENT_TRADE_RAW_SQL,
            rows,
            BATCH_SIZE,
            (PreparedStatement statement, PreparedRealEstateImportServiceRequest.ApartmentTradeRawRow row) -> {
                statement.setString(1, row.getTradeKey());
                statement.setString(2, row.getDistrictCode());
                statement.setString(3, row.getLegalDongName());
                statement.setString(4, row.getLegalDongCode());
                statement.setString(5, row.getApartmentName());
                statement.setString(6, row.getJibun());
                statement.setObject(7, row.getDealDate());
                statement.setBigDecimal(8, row.getDealAmount().getAmount());
                statement.setBigDecimal(9, row.getExclusiveArea());
                statement.setInt(10, row.getFloor());
                statement.setInt(11, row.getBuildYear());
                statement.setBoolean(12, row.isLandLeasehold());
            }
        );
    }

    private void upsertRealEstateGeocodeCaches(
        List<PreparedRealEstateImportServiceRequest.RealEstateGeocodeCacheRow> rows
    ) {
        if (rows.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
            UPSERT_REAL_ESTATE_GEOCODE_CACHE_SQL,
            rows,
            BATCH_SIZE,
            (PreparedStatement statement, PreparedRealEstateImportServiceRequest.RealEstateGeocodeCacheRow row) -> {
                statement.setString(1, row.getGeocodingQuery());
                statement.setString(2, row.getGeocodingStatus().name());
                setNullableBigDecimal(statement, 3, row.getLatitude());
                setNullableBigDecimal(statement, 4, row.getLongitude());
                statement.setString(5, row.getResolvedRoadAddress());
                statement.setString(6, row.getResolvedJibunAddress());
            }
        );
    }

    private void upsertRealEstateProperties(
        List<PreparedRealEstateImportServiceRequest.RealEstatePropertyRow> rows
    ) {
        if (rows.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
            UPSERT_REAL_ESTATE_PROPERTY_SQL,
            rows,
            BATCH_SIZE,
            (PreparedStatement statement, PreparedRealEstateImportServiceRequest.RealEstatePropertyRow row) -> {
                statement.setString(1, row.getProviderId());
                statement.setString(2, row.getPropertyName());
                statement.setString(3, row.getAddress());
                statement.setString(4, row.getRegionCode());
                statement.setString(5, row.getDistrictCode());
                statement.setString(6, row.getLegalDongCode());
                statement.setBigDecimal(7, row.getBasePrice().getAmount());
                setNullableBigDecimal(statement, 8, row.getLatitude());
                setNullableBigDecimal(statement, 9, row.getLongitude());
                statement.setString(10, row.getHousingType().name());
                statement.setString(11, row.getPropertyType().name());
                statement.setString(12, row.getTransactionType().name());
                statement.setString(13, serializeContractTraps(row));
            }
        );
    }

    private void setNullableBigDecimal(
        PreparedStatement statement,
        int parameterIndex,
        BigDecimal value
    ) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, Types.NUMERIC);
            return;
        }
        statement.setBigDecimal(parameterIndex, value);
    }

    private String serializeContractTraps(PreparedRealEstateImportServiceRequest.RealEstatePropertyRow row) {
        try {
            return objectMapper.writeValueAsString(resolveContractTraps(row));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize contract traps", exception);
        }
    }

    private List<ContractTrap> resolveContractTraps(
        PreparedRealEstateImportServiceRequest.RealEstatePropertyRow row
    ) {
        if (row.getContractTraps().isEmpty()) {
            return List.of();
        }
        return row.getContractTraps();
    }
}
