package io.ssafy.p.j14c103.homerun.config;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import io.ssafy.p.j14c103.homerun.client.image.ImageObjectClient;
import io.ssafy.p.j14c103.homerun.client.image.OciImageObjectClient;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(OciObjectStorageProperties.class)
public class OciObjectStorageConfig {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "oci.enabled", havingValue = "true")
    public ObjectStorageClient objectStorageClient(
            final OciObjectStorageProperties properties
    ) throws IOException {
        final ObjectStorageClient client;

        if (usesInlineCredentials(properties)) {
            validateInlineCredentials(properties);

            final byte[] privateKey = decodePrivateKey(properties.privateKeyBase64());
            final SimpleAuthenticationDetailsProvider.SimpleAuthenticationDetailsProviderBuilder builder =
                    SimpleAuthenticationDetailsProvider.builder()
                            .userId(properties.userOcid())
                            .tenantId(properties.tenancyOcid())
                            .fingerprint(properties.fingerprint())
                            .privateKeySupplier(() -> new ByteArrayInputStream(privateKey));

            if (StringUtils.hasText(properties.passphrase())) {
                builder.passphraseCharacters(properties.passphrase().toCharArray());
            }

            client = ObjectStorageClient.builder().build(builder.build());
        } else {
            validateConfigFileCredentials(properties);

            final ConfigFileAuthenticationDetailsProvider provider =
                    new ConfigFileAuthenticationDetailsProvider(
                            properties.configPath(),
                            properties.profile()
                    );

            client = ObjectStorageClient.builder().build(provider);
        }

        client.setRegion(Region.fromRegionCodeOrId(properties.region()));
        return client;
    }

    @Bean
    @ConditionalOnProperty(name = "oci.enabled", havingValue = "true")
    public ImageObjectClient imageObjectClient(
            final ObjectStorageClient objectStorageClient,
            final OciObjectStorageProperties properties
    ) {
        return new OciImageObjectClient(objectStorageClient, properties);
    }

    private boolean usesInlineCredentials(final OciObjectStorageProperties properties) {
        return StringUtils.hasText(properties.userOcid())
                || StringUtils.hasText(properties.tenancyOcid())
                || StringUtils.hasText(properties.fingerprint())
                || StringUtils.hasText(properties.privateKeyBase64());
    }

    private void validateInlineCredentials(final OciObjectStorageProperties properties) {
        validateCommonProperties(properties);

        if (!StringUtils.hasText(properties.userOcid())) {
            throw new HomerunException(ErrorCode.OCI_CONFIGURATION_INVALID);
        }
        if (!StringUtils.hasText(properties.tenancyOcid())) {
            throw new HomerunException(ErrorCode.OCI_CONFIGURATION_INVALID);
        }
        if (!StringUtils.hasText(properties.fingerprint())) {
            throw new HomerunException(ErrorCode.OCI_CONFIGURATION_INVALID);
        }
        if (!StringUtils.hasText(properties.privateKeyBase64())) {
            throw new HomerunException(ErrorCode.OCI_CONFIGURATION_INVALID);
        }
    }

    private void validateConfigFileCredentials(final OciObjectStorageProperties properties) {
        validateCommonProperties(properties);

        if (!StringUtils.hasText(properties.configPath())) {
            throw new HomerunException(ErrorCode.OCI_CONFIGURATION_INVALID);
        }
        if (!StringUtils.hasText(properties.profile())) {
            throw new HomerunException(ErrorCode.OCI_CONFIGURATION_INVALID);
        }
    }

    private void validateCommonProperties(final OciObjectStorageProperties properties) {
        if (!StringUtils.hasText(properties.region())) {
            throw new HomerunException(ErrorCode.OCI_CONFIGURATION_INVALID);
        }
        if (!StringUtils.hasText(properties.namespace())) {
            throw new HomerunException(ErrorCode.OCI_CONFIGURATION_INVALID);
        }
        if (!StringUtils.hasText(properties.bucketName())) {
            throw new HomerunException(ErrorCode.OCI_CONFIGURATION_INVALID);
        }
    }

    private byte[] decodePrivateKey(final String privateKeyBase64) {
        try {
            return Base64.getDecoder().decode(privateKeyBase64);
        } catch (IllegalArgumentException exception) {
            throw new HomerunException(ErrorCode.OCI_CONFIGURATION_INVALID);
        }
    }
}
