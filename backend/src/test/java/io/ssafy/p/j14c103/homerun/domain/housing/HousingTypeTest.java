package io.ssafy.p.j14c103.homerun.domain.housing;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;


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
