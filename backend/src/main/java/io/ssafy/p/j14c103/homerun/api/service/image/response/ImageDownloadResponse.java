package io.ssafy.p.j14c103.homerun.api.service.image.response;

public record ImageDownloadResponse(
        String fileName,
        String contentType,
        long contentLength,
        byte[] content
) {
}
