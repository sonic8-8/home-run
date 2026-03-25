package io.ssafy.p.j14c103.homerun.client.image;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import io.ssafy.p.j14c103.homerun.config.OciObjectStorageProperties;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class OciImageObjectClient implements ImageObjectClient {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final ObjectStorageClient objectStorageClient;
    private final OciObjectStorageProperties properties;

    @Override
    public ImageObjectData download(final String objectName) {
        try {
            final GetObjectResponse response = objectStorageClient.getObject(buildRequest(objectName));
            final byte[] content;

            try (InputStream inputStream = response.getInputStream()) {
                content = inputStream.readAllBytes();
            }

            return new ImageObjectData(
                    objectName,
                    resolveContentType(response.getContentType()),
                    response.getContentLength() == null ? content.length : response.getContentLength(),
                    content
            );
        } catch (IOException | RuntimeException exception) {
            log.error(
                    "Failed to download image from OCI. namespace={}, bucket={}, objectName={}",
                    properties.namespace(),
                    properties.bucketName(),
                    objectName,
                    exception
            );
            throw new HomerunException(ErrorCode.IMAGE_DOWNLOAD_FAILED);
        }
    }

    private GetObjectRequest buildRequest(final String objectName) {
        return GetObjectRequest.builder()
                .namespaceName(properties.namespace())
                .bucketName(properties.bucketName())
                .objectName(objectName)
                .build();
    }

    private String resolveContentType(final String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return DEFAULT_CONTENT_TYPE;
        }

        return contentType;
    }
}
