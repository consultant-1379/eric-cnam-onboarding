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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.ericsson.oss.management.onboarding.presentation.exceptions.DockerServiceException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.LayerException;
import com.ericsson.oss.management.onboarding.presentation.services.FileServiceImpl;
import com.ericsson.oss.management.onboarding.utils.CommandExecutor;

@SpringBootTest(classes = { LayerServiceImpl.class, FileServiceImpl.class, CommandExecutor.class })
class LayerServiceImplTest {

    private static final String UPLOAD_URL = "https://docker-registry.zname.claster.rnd.gic.ericsson.se/v2/pathToRepo/blobs/uploads/digest";
    private static final String INVALID_URL = "invalidUrl";
    private static final String DIGEST = "sha256:f5600c6330da7bb112776ba067a32a9c20842d6ecc8ee3289f1a713b644092f8";
    private static final String SOME_DATA = "some data";
    private Path layerPath;

    @Autowired
    private LayerServiceImpl layerService;

    @Autowired
    private FileServiceImpl fileService;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() throws IOException {
        layerPath = File.createTempFile("layer", "tar").toPath();
        Files.write(layerPath, SOME_DATA.getBytes(StandardCharsets.UTF_8));
    }

    @AfterEach
    void after() {
        fileService.deleteDirectory(layerPath);
    }

    @Test
    void shouldPushLayerSuccessfully() throws Exception {
        given(restTemplate.exchange(any(), ArgumentMatchers.<Class<Void>>any())).willReturn(ResponseEntity.ok().build());
        layerService.pushLayer(layerPath, UPLOAD_URL, DIGEST);

        verify(restTemplate, times(1)).exchange(any(), ArgumentMatchers.<Class<Void>>any());
    }

    @Test
    void shouldFailWhenWrongPathForLayerProvided() {
        Path layerPath = Path.of("path/to/fileNotExist");
        given(restTemplate.exchange(any(), ArgumentMatchers.<Class<Void>>any())).willReturn(ResponseEntity.ok().build());

        assertThatThrownBy(() -> layerService.pushLayer(layerPath, UPLOAD_URL, DIGEST))
                .isInstanceOf(DockerServiceException.class);
    }

    @Test
    void shouldFailWhenBadUpload() {
        given(restTemplate.exchange(any(), ArgumentMatchers.<Class<Void>>any())).willReturn(ResponseEntity.internalServerError().build());

        assertThatThrownBy(() -> layerService.pushLayer(layerPath, UPLOAD_URL, DIGEST))
                .isInstanceOf(LayerException.class);
    }

    @Test
    void shouldFailWhenWrongUploadUrl() {
        given(restTemplate.exchange(any(), ArgumentMatchers.<Class<Void>>any())).willReturn(ResponseEntity.ok().build());

        assertThatThrownBy(() -> layerService.pushLayer(layerPath, INVALID_URL, DIGEST))
                .isInstanceOf(DockerServiceException.class);
    }

}