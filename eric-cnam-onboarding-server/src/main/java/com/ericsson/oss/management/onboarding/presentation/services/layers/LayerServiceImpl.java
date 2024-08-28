/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.management.onboarding.presentation.services.layers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ericsson.oss.management.onboarding.presentation.exceptions.DockerServiceException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.LayerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LayerServiceImpl implements LayerService {

    public static final int CAPACITY = 20971520;

    private final RestTemplate restTemplate;

    @Override
    public void pushLayer(final Path layerPath, final String uploadUrl, final String layerDigest) throws LayerException {
        var targetFile = layerPath.toFile();
        try (var reader = new RandomAccessFile(targetFile, "r");
                FileChannel fis = reader.getChannel()) {
            var uploadUri = new URI(uploadUrl);
            var buf = ByteBuffer.allocate(CAPACITY);
            int i;
            var start = 0L;
            long offset;

            do {
                i = fis.read(buf);
                offset = start + i;
                long fileSize = Files.size(layerPath);
                log.info("File size {} pushed payload {}", fileSize, offset);

                if (offset == fileSize) {
                    byte[] lastChunk = Arrays.copyOf(buf.array(), i);
                    final var buildUrl = new URI(uploadUri + "&digest=sha256:" + layerDigest);
                    RequestEntity<byte[]> request = buildHeaders(RequestEntity.put(buildUrl), lastChunk, start, offset);
                    pushLayerToContainerRegistry(request);
                    break;
                } else {
                    if (uploadUri != null) {
                        RequestEntity<byte[]> request = buildHeaders(RequestEntity.patch(uploadUri), buf.array(), start, offset);
                        ResponseEntity<Void> response = pushLayerToContainerRegistry(request);
                        uploadUri = response.getHeaders().getLocation();
                    }
                }

                start = offset;
                buf.clear();
            } while (i != -1);
        } catch (IOException e) {
            throw new DockerServiceException(String.format("There is no any file for path %s", layerPath));
        } catch (URISyntaxException e) {
            throw new DockerServiceException(String.format("Exception. uploadUrl %s is wrong", uploadUrl));
        }
    }

    private RequestEntity<byte[]> buildHeaders(final RequestEntity.BodyBuilder bodyBuilder, byte[] content, final long startAt, final long offset) {
        return bodyBuilder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(content.length))
                .header(HttpHeaders.CONTENT_RANGE, HttpRange.toString(Collections.singletonList(HttpRange.createByteRange(startAt, offset))))
                .body(content);
    }

    private ResponseEntity<Void> pushLayerToContainerRegistry(final RequestEntity<byte[]> request) throws LayerException {
        ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
        if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            throw new LayerException("Internal server error on docker registry");
        }
        return response;
    }
}
