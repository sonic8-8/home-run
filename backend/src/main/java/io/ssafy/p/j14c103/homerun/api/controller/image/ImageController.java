package io.ssafy.p.j14c103.homerun.api.controller.image;

import io.ssafy.p.j14c103.homerun.api.service.image.ImageStorageService;
import io.ssafy.p.j14c103.homerun.api.service.image.response.ImageDownloadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
@ConditionalOnProperty(name = "oci.enabled", havingValue = "true")
public class ImageController {

    private final ImageStorageService imageStorageService;

    @GetMapping
    public ResponseEntity<byte[]> download(
            @RequestParam final String objectName
    ) {
        final ImageDownloadResponse response = imageStorageService.download(objectName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .contentLength(response.contentLength())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(response.fileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(response.content());
    }
}
