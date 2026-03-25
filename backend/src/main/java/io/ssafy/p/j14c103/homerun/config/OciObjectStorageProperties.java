package io.ssafy.p.j14c103.homerun.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oci")
public record OciObjectStorageProperties(
        boolean enabled,
        String configPath,
        String profile,
        String region,
        String namespace,
        String bucketName,
        String userOcid,
        String tenancyOcid,
        String fingerprint,
        String privateKeyBase64,
        String passphrase
) {
}
