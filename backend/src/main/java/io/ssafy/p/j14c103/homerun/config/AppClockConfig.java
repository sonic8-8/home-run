package io.ssafy.p.j14c103.homerun.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppClockConfig {

    @Bean
    public Clock appClock() {
        return Clock.systemDefaultZone();
    }
}
