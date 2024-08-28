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
package com.ericsson.oss.management.onboarding.presentation.services.images;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpServerErrorException;

import com.ericsson.oss.management.onboarding.models.DockerImage;
import com.ericsson.oss.management.onboarding.presentation.exceptions.DockerServiceException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InternalRuntimeException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidInputException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.LayerException;
import com.ericsson.oss.management.onboarding.presentation.services.FileService;
import com.ericsson.oss.management.onboarding.presentation.services.FileServiceImpl;
import com.ericsson.oss.management.onboarding.presentation.services.layers.LayerService;
import com.ericsson.oss.management.onboarding.utils.CommandExecutor;
import com.ericsson.oss.management.onboarding.utils.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = { DockerServiceImpl.class, FileServiceImpl.class,
        CommandExecutor.class, ObjectMapper.class })
@TestPropertySource(properties = { "DOCKER.REGISTRY=localhost" })
class DockerServiceImplTest {

    private static final String UPLOAD_LAYER_URI = "/data.html";
    private static final String UPLOAD_LAYER_URL = "https://localhost:8080/data.html";
    private static final String INCORRECT_UPLOAD_LAYER_URL = "localhost:8080/data.html";
    private static final String BASIC_DOCKER_PATH = "src/test/resources/images/docker.tar";
    private static final String DOCKER_TAR_FOLDER = "src/test/resources/docker-tar";
    private static final String CONFIG = "219ee5171f8006d1462fa76c12b9b01ab672dbc8b283f186841bf2c3ca8e3c93.json";
    private static final String LAYER = "5b4ef804af565ff504bf7693239e5f26b4782484f068226b7244117760603d0d/layer.tar";
    private static final String REPO_TAG = "localhost/dockerhub-ericsson-remote/busybox:1.32.0";

    @Autowired
    private DockerServiceImpl dockerService;
    @Autowired
    private FileService fileService;
    @MockBean
    private RestClient restClient;
    @MockBean
    private LayerService layerService;

    @TempDir
    private Path tempDir;

    @Test
    void shouldUnpackImagesSuccessfully() throws IOException {
        Path dockerTar = Path.of(BASIC_DOCKER_PATH);
        dockerTar = Files.copy(dockerTar, tempDir.resolve(dockerTar.getFileName()));

        List<DockerImage> result = dockerService.unpackDockerTar(dockerTar);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        DockerImage image = result.get(0);
        assertThat(image).isNotNull();
        assertThat(image.getConfig()).isEqualTo(CONFIG);
        assertThat(image.getLayers().size()).isEqualTo(1);
        assertThat(image.getLayers().get(0)).isEqualTo(LAYER);
        assertThat(image.getRepoTags().size()).isEqualTo(1);
        assertThat(image.getRepoTags().get(0)).isEqualTo(REPO_TAG);

        fileService.deleteDirectory(dockerTar);
    }

    @Test
    void shouldFailWithoutManifestFile() throws IOException {
        Path path = Path.of("src/test/resources/images/docker_no_manifest.tar");
        Path dockerTar = Files.copy(path, tempDir.resolve(path.getFileName()));

        assertThatThrownBy(() -> dockerService.unpackDockerTar(dockerTar))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("file does not exist");

        fileService.deleteDirectory(dockerTar);
    }

    @Test
    void testUploadImagesSuccessfully() throws IOException, LayerException {
        DockerImage image = createImage();

        Path dockerTarFolder = Path.of(DOCKER_TAR_FOLDER);
        FileUtils.copyDirectory(dockerTarFolder.toFile(), tempDir.toFile());

        ResponseEntity<String> uploadLayerResponse = ResponseEntity.ok().header(HttpHeaders.LOCATION, UPLOAD_LAYER_URL).build();
        ResponseEntity<String> processManifestResponse = ResponseEntity.created(URI.create("")).build();
        when(restClient.head(anyString(), anyString(), anyString())).thenReturn(HttpStatus.NOT_FOUND);
        when(restClient.post(anyString(), anyString(), anyString())).thenReturn(uploadLayerResponse);
        when(restClient.put(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(processManifestResponse);

        dockerService.uploadImage(image, tempDir);

        verify(layerService, times(2)).pushLayer(any(Path.class), anyString(), anyString());

        fileService.deleteDirectory(tempDir);
    }

    @Test
    void testUploadImagesWithLayerSymbolicLinkSuccessfully() throws IOException, LayerException {
        DockerImage image = createImage();
        image.setLayers(Collections.singletonList("5b4ef804af565ff504bf7693239e5f26b4782484f068226b7244117760603d0d/layer-symb.tar"));

        Path dockerTarFolder = Path.of("src/test/resources/docker-symb-tar");
        FileUtils.copyDirectory(dockerTarFolder.toFile(), tempDir.toFile());

        ResponseEntity<String> uploadLayerResponse = ResponseEntity.ok().header(HttpHeaders.LOCATION, UPLOAD_LAYER_URL).build();
        ResponseEntity<String> processManifestResponse = ResponseEntity.created(URI.create("")).build();
        when(restClient.head(anyString(), anyString(), anyString())).thenReturn(HttpStatus.NOT_FOUND);
        when(restClient.post(anyString(), anyString(), anyString())).thenReturn(uploadLayerResponse);
        when(restClient.put(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(processManifestResponse);

        dockerService.uploadImage(image, tempDir);

        verify(layerService, times(2)).pushLayer(any(Path.class), anyString(), anyString());

        fileService.deleteDirectory(tempDir);
    }

    @Test
    void testUploadImagesWhenLayerExistSuccessfully() throws IOException, LayerException {
        DockerImage image = createImage();

        Path dockerTarFolder = Path.of(DOCKER_TAR_FOLDER);
        FileUtils.copyDirectory(dockerTarFolder.toFile(), tempDir.toFile());

        ResponseEntity<String> uploadLayerResponse = ResponseEntity.ok().header(HttpHeaders.LOCATION, UPLOAD_LAYER_URL).build();
        ResponseEntity<String> processManifestResponse = ResponseEntity.created(URI.create("")).build();
        when(restClient.head(anyString(), anyString(), anyString())).thenReturn(HttpStatus.OK);
        when(restClient.post(anyString(), anyString(), anyString())).thenReturn(uploadLayerResponse);
        when(restClient.put(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(processManifestResponse);

        dockerService.uploadImage(image, tempDir);

        verify(layerService, never()).pushLayer(any(Path.class), anyString(), anyString());

        fileService.deleteDirectory(tempDir);
    }

    @Test
    void shouldFailWhenBadResponseDuringUploadLayer() throws IOException, LayerException {
        DockerImage image = createImage();

        Path dockerTarFolder = Path.of(DOCKER_TAR_FOLDER);
        FileUtils.copyDirectory(dockerTarFolder.toFile(), tempDir.toFile());

        ResponseEntity<String> uploadLayerResponse = ResponseEntity.ok().header(HttpHeaders.LOCATION, UPLOAD_LAYER_URL).build();
        ResponseEntity<String> processManifestResponse = ResponseEntity.created(URI.create("")).build();
        when(restClient.head(anyString(), anyString(), anyString())).thenReturn(HttpStatus.BAD_REQUEST);
        when(restClient.post(anyString(), anyString(), anyString())).thenReturn(uploadLayerResponse);
        when(restClient.put(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(processManifestResponse);
        doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "bad request"))
                .when(layerService).pushLayer(any(), anyString(), anyString());

        assertThatThrownBy(() -> dockerService.uploadImage(image, tempDir))
                .isInstanceOf(DockerServiceException.class);

        verify(layerService).pushLayer(any(Path.class), anyString(), anyString());

        fileService.deleteDirectory(tempDir);
    }

    @Test
    void shouldFailWhenNoSpaceOnDevice() throws LayerException, IOException {
        DockerImage image = createImage();

        Path dockerTarFolder = Path.of(DOCKER_TAR_FOLDER);
        FileUtils.copyDirectory(dockerTarFolder.toFile(), tempDir.toFile());

        ResponseEntity<String> uploadLayerResponse = ResponseEntity.ok().header(HttpHeaders.LOCATION, UPLOAD_LAYER_URL).build();
        ResponseEntity<String> processManifestResponse = ResponseEntity.created(URI.create("")).build();
        when(restClient.head(anyString(), anyString(), anyString())).thenReturn(HttpStatus.BAD_REQUEST);
        when(restClient.post(anyString(), anyString(), anyString())).thenReturn(uploadLayerResponse);
        when(restClient.put(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(processManifestResponse);
        doThrow(new InternalRuntimeException("no space left on device"))
                .when(layerService).pushLayer(any(), anyString(), anyString());

        assertThatThrownBy(() -> dockerService.uploadImage(image, tempDir))
                .isInstanceOf(DockerServiceException.class);

        verify(layerService).pushLayer(any(Path.class), anyString(), anyString());

        fileService.deleteDirectory(tempDir);
    }

    @Test
    void shouldFailWhenUnpredictedExceptionDuringLayerUpload() throws LayerException, IOException {
        DockerImage image = createImage();

        Path dockerTarFolder = Path.of(DOCKER_TAR_FOLDER);
        FileUtils.copyDirectory(dockerTarFolder.toFile(), tempDir.toFile());

        ResponseEntity<String> uploadLayerResponse = ResponseEntity.ok().header(HttpHeaders.LOCATION, UPLOAD_LAYER_URL).build();
        ResponseEntity<String> processManifestResponse = ResponseEntity.created(URI.create("")).build();
        when(restClient.head(anyString(), anyString(), anyString())).thenReturn(HttpStatus.BAD_REQUEST);
        when(restClient.post(anyString(), anyString(), anyString())).thenReturn(uploadLayerResponse);
        when(restClient.put(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(processManifestResponse);
        doThrow(NullPointerException.class).when(layerService).pushLayer(any(), anyString(), anyString());

        assertThatThrownBy(() -> dockerService.uploadImage(image, tempDir))
                .isInstanceOf(DockerServiceException.class);

        verify(layerService).pushLayer(any(Path.class), anyString(), anyString());

        fileService.deleteDirectory(tempDir);
    }

    @Test
    void shouldFailWhenCantProcessManifest() throws IOException {
        DockerImage image = createImage();

        Path dockerTarFolder = Path.of(DOCKER_TAR_FOLDER);
        FileUtils.copyDirectory(dockerTarFolder.toFile(), tempDir.toFile());

        ResponseEntity<String> uploadLayerResponse = ResponseEntity.ok().header(HttpHeaders.LOCATION, UPLOAD_LAYER_URL).build();
        ResponseEntity<String> processManifestResponse = ResponseEntity.badRequest().build();
        when(restClient.head(anyString(), anyString(), anyString())).thenReturn(HttpStatus.BAD_REQUEST);
        when(restClient.post(anyString(), anyString(), anyString())).thenReturn(uploadLayerResponse);
        when(restClient.put(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(processManifestResponse);

        assertThatThrownBy(() -> dockerService.uploadImage(image, tempDir))
                .isInstanceOf(InternalRuntimeException.class);

        fileService.deleteDirectory(tempDir);
    }

    @Test
    void testUploadImagesSuccessfullyWhenInHeadersLocationContainsURI() throws IOException, LayerException {
        DockerImage image = createImage();

        Path dockerTarFolder = Path.of(DOCKER_TAR_FOLDER);
        FileUtils.copyDirectory(dockerTarFolder.toFile(), tempDir.toFile());

        ResponseEntity<String> uploadLayerResponse = ResponseEntity.ok().header(HttpHeaders.LOCATION, UPLOAD_LAYER_URI).build();
        ResponseEntity<String> processManifestResponse = ResponseEntity.created(URI.create("")).build();
        when(restClient.head(anyString(), anyString(), anyString())).thenReturn(HttpStatus.NOT_FOUND);
        when(restClient.post(anyString(), anyString(), anyString())).thenReturn(uploadLayerResponse);
        when(restClient.put(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(processManifestResponse);

        dockerService.uploadImage(image, tempDir);

        verify(layerService, times(2)).pushLayer(any(Path.class), anyString(), anyString());

        fileService.deleteDirectory(tempDir);
    }

    @Test
    void testUploadImagesSuccessfullyWhenInHeadersLocationContainsURL() throws IOException, LayerException {
        DockerImage image = createImage();

        Path dockerTarFolder = Path.of(DOCKER_TAR_FOLDER);
        FileUtils.copyDirectory(dockerTarFolder.toFile(), tempDir.toFile());

        ResponseEntity<String> uploadLayerResponse = ResponseEntity.ok().header(HttpHeaders.LOCATION, UPLOAD_LAYER_URL).build();
        ResponseEntity<String> processManifestResponse = ResponseEntity.created(URI.create("")).build();
        when(restClient.head(anyString(), anyString(), anyString())).thenReturn(HttpStatus.NOT_FOUND);
        when(restClient.post(anyString(), anyString(), anyString())).thenReturn(uploadLayerResponse);
        when(restClient.put(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(processManifestResponse);

        dockerService.uploadImage(image, tempDir);

        verify(layerService, times(2)).pushLayer(any(Path.class), anyString(), anyString());

        fileService.deleteDirectory(tempDir);
    }

    @Test
    void testUploadImagesFailedWhenInHeadersLocationContainsIncorrectPath() throws IOException {
        DockerImage image = createImage();

        Path dockerTarFolder = Path.of(DOCKER_TAR_FOLDER);
        FileUtils.copyDirectory(dockerTarFolder.toFile(), tempDir.toFile());

        ResponseEntity<String> uploadLayerResponse = ResponseEntity.ok().header(HttpHeaders.LOCATION, INCORRECT_UPLOAD_LAYER_URL).build();
        ResponseEntity<String> processManifestResponse = ResponseEntity.created(URI.create("")).build();
        when(restClient.head(anyString(), anyString(), anyString())).thenReturn(HttpStatus.NOT_FOUND);
        when(restClient.post(anyString(), anyString(), anyString())).thenReturn(uploadLayerResponse);
        when(restClient.put(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(processManifestResponse);

        assertThatThrownBy(() -> dockerService.uploadImage(image, tempDir))
                .isInstanceOf(DockerServiceException.class)
                .hasMessageContaining("Not correct url " + INCORRECT_UPLOAD_LAYER_URL + " in the headers location");

        fileService.deleteDirectory(tempDir);
    }

    private DockerImage createImage() {
        DockerImage image = new DockerImage();
        image.setConfig(CONFIG);
        image.setLayers(Collections.singletonList(LAYER));
        image.setRepoTags(Collections.singletonList(REPO_TAG));
        return image;
    }

}