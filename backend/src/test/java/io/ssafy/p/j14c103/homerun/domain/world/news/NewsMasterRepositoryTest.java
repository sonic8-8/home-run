package io.ssafy.p.j14c103.homerun.domain.world.news;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class NewsMasterRepositoryTest {

    @Autowired
    private NewsMasterRepository newsMasterRepository;

    @Autowired
    private TestEntityManager entityManager;

    @DisplayName("NewsMaster를 저장하면 sectorImpact와 jobImpact JSON을 다시 조회할 수 있다")
    @Test
    void saveNewsMaster() {
        // given
        NewsMaster newsMaster = NewsMaster.create(
            "NEWS-001",
            "금리 인하 기조 지속",
            "ECONOMY",
            "POSITIVE",
            Map.of("FINANCE", 2, "REAL_ESTATE", 1),
            0,
            3,
            Map.of("STARTUP", Map.of("salaryMult", 95), "FREELANCER", Map.of("incomeMult", 110))
        );

        // when
        NewsMaster saved = newsMasterRepository.saveAndFlush(newsMaster);
        entityManager.clear();

        NewsMaster found = newsMasterRepository.findById(saved.getNewsId())
            .orElseThrow();

        // then
        assertThat(found.getTitle()).isEqualTo("금리 인하 기조 지속");
        assertThat(found.getSectorImpact()).containsEntry("FINANCE", 2);
        assertThat(found.getRealEstateImpact()).isEqualTo(3);
        assertThat(found.getJobImpact()).containsKey("STARTUP");
        @SuppressWarnings("unchecked")
        Map<String, Object> freelancerImpact = (Map<String, Object>) found.getJobImpact().get("FREELANCER");
        assertThat(freelancerImpact).containsEntry("incomeMult", 110);
    }
}
