package io.ssafy.p.j14c103.homerun.client.image;

public record ImageObjectData(
        String fileName,
        String contentType,
        long contentLength,
        byte[] content
) {
}
