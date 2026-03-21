package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.RealEstateMasterSyncService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterSyncServiceRequest;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class RealEstateImportConfigTest {

    private final RealEstateMasterSyncService realEstateMasterSyncService = org.mockito.Mockito.mock(
        RealEstateMasterSyncService.class
    );

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(RealEstateImportConfig.class)
        .withBean(RealEstateMasterSyncService.class, () -> realEstateMasterSyncService);

    @DisplayName("적재 실행이 활성화되면 설정값을 sync 요청으로 전달한다")
    @Test
    void realEstateImportRunner() throws Exception {
        contextRunner
            .withPropertyValues(
                "app.real-estate-import.enabled=true",
                "app.real-estate-import.regions[0]=SEOUL",
                "app.real-estate-import.regions[1]=GWANGJU",
                "app.real-estate-import.from-year-month=2024-01",
                "app.real-estate-import.to-year-month=2024-12",
                "app.real-estate-import.dataset-types[0]=APT_SALE"
            )
            .run(context -> {
                // given
                assertThat(context).hasSingleBean(ApplicationRunner.class);
                ApplicationRunner runner = context.getBean(ApplicationRunner.class);

                // when
                runner.run(new DefaultApplicationArguments(new String[0]));

                // then
                ArgumentCaptor<RealEstateMasterSyncServiceRequest> captor =
                    ArgumentCaptor.forClass(RealEstateMasterSyncServiceRequest.class);

                then(realEstateMasterSyncService).should().sync(captor.capture());
                assertThat(captor.getValue().getRegions()).containsExactly("SEOUL", "GWANGJU");
                assertThat(captor.getValue().getFromYearMonth()).isEqualTo(YearMonth.of(2024, 1));
                assertThat(captor.getValue().getToYearMonth()).isEqualTo(YearMonth.of(2024, 12));
            });
    }

    @DisplayName("적재 실행이 비활성화되면 sync 서비스를 호출하지 않는다")
    @Test
    void realEstateImportRunnerWhenDisabled() throws Exception {
        contextRunner
            .withPropertyValues(
                "app.real-estate-import.enabled=false",
                "app.real-estate-import.regions[0]=SEOUL",
                "app.real-estate-import.from-year-month=2024-01",
                "app.real-estate-import.to-year-month=2024-01",
                "app.real-estate-import.dataset-types[0]=APT_SALE"
            )
            .run(context -> {
                assertThat(context).doesNotHaveBean(ApplicationRunner.class);
                verifyNoInteractions(realEstateMasterSyncService);
            });
    }
}
