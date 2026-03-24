package io.ssafy.p.j14c103.homerun.api.service.image.response;

import lombok.Getter;

@Getter
public class ImageDownloadResponse {

    private final String fileName;
    private final String contentType;
    private final long contentLength;
    private final byte[] content;

    public ImageDownloadResponse(
        final String fileName,
        final String contentType,
        final long contentLength,
        final byte[] content
    ) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.content = content;
    }

    public String fileName() {
        return fileName;
    }

    public String contentType() {
        return contentType;
    }

    public long contentLength() {
        return contentLength;
    }

    public byte[] content() {
        return content;
    }
}
