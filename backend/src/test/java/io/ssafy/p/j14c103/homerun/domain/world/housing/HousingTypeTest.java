package io.ssafy.p.j14c103.homerun.domain.world.housing;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class HousingTypeTest {
    @DisplayName("HousingType enum 값은 최신 spec과 일치한다.")
    @Test
    void values(){
        assertThat(HousingType.values()).containsExactly(
                HousingType.NONE,
                HousingType.STUDIO,
                HousingType.VILLA,
                HousingType.JEONSE_APT,
                HousingType.OWNED_APT
        );
    }

}
