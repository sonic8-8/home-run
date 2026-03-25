package io.ssafy.p.j14c103.homerun.api.service.image;

import io.ssafy.p.j14c103.homerun.config.OciObjectStorageProperties;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oci.enabled", havingValue = "true")
@Transactional(readOnly = true)
public class ImageStorageService {

    private final OciObjectStorageProperties properties;

    public String getPublicUrl(final String objectName) {
        if (!StringUtils.hasText(objectName)) {
            throw new HomerunException(ErrorCode.IMAGE_OBJECT_NAME_REQUIRED);
        }

        return UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("objectstorage.%s.oraclecloud.com".formatted(properties.region()))
                .pathSegment("n", properties.namespace(), "b", properties.bucketName(), "o")
                .pathSegment(resolveObjectPathSegments(objectName))
                .build()
                .encode()
                .toUriString();
    }

    private String[] resolveObjectPathSegments(final String objectName) {
        final String[] pathSegments = StringUtils.tokenizeToStringArray(objectName, "/");
        if (pathSegments == null || pathSegments.length == 0) {
            throw new HomerunException(ErrorCode.IMAGE_OBJECT_NAME_REQUIRED);
        }

        return pathSegments;
    }
}
