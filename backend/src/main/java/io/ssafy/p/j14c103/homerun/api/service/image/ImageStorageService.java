package io.ssafy.p.j14c103.homerun.api.service.image;

import io.ssafy.p.j14c103.homerun.api.service.image.response.ImageDownloadResponse;
import io.ssafy.p.j14c103.homerun.client.image.ImageObjectClient;
import io.ssafy.p.j14c103.homerun.client.image.ImageObjectData;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oci.enabled", havingValue = "true")
@Transactional(readOnly = true)
public class ImageStorageService {

    private final ImageObjectClient imageObjectClient;

    public ImageDownloadResponse download(final String objectName) {
        if (objectName == null || objectName.isBlank()) {
            throw new HomerunException(ErrorCode.IMAGE_OBJECT_NAME_REQUIRED);
        }

        final ImageObjectData objectData = imageObjectClient.download(objectName);

        return new ImageDownloadResponse(
                objectData.fileName(),
                objectData.contentType(),
                objectData.contentLength(),
                objectData.content()
        );
    }
}
