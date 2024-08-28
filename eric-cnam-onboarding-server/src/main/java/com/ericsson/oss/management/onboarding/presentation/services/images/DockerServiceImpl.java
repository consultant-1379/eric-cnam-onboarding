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

import static com.ericsson.oss.management.onboarding.presentation.constants.Commands.TIMEOUT;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.DEFAULT_DOCKER_REGISTRY;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.DOCKER_LAYER_CONTENT_TYPE;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.DOCKER_LAYER_MEDIA_TYPE_JSON;
import static com.ericsson.oss.management.onboarding.presentation.constants.FileDetails.MANIFEST_FILE;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.input.MessageDigestCalculatingInputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import com.ericsson.oss.management.onboarding.models.DockerImage;
import com.ericsson.oss.management.onboarding.models.DockerLayer;
import com.ericsson.oss.management.onboarding.models.Manifest;
import com.ericsson.oss.management.onboarding.presentation.exceptions.DockerServiceException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InternalRuntimeException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidFileException;
import com.ericsson.oss.management.onboarding.presentation.exceptions.InvalidURLException;
import com.ericsson.oss.management.onboarding.presentation.services.FileService;
import com.ericsson.oss.management.onboarding.presentation.services.layers.LayerService;
import com.ericsson.oss.management.onboarding.utils.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerServiceImpl implements DockerService {

    @Value("${docker.registry}")
    private String dockerRegistry;
    @Value("${docker.username}")
    private String registryUser;
    @Value("${docker.password}")
    private String registryPassword;
    @SuppressWarnings("java:S5852")
    private static final Pattern REGISTRY_PATTERN = Pattern.compile("(.+?(?=/))/([^/]+(?=/))?");
    private static final Pattern REGISTRY_NO_SPACE_LEFT_ERROR_PATTERN = Pattern.compile("\"Err\":28");
    private static final String MANIFEST_IN_PRIVATE_DOCKER_REGISTRY_URL = "%s/v2/%s/manifests/%s";
    private static final String UPLOAD_URL = "%s/v2/%s/blobs/uploads/";
    private static final String LAYERS_IN_PRIVATE_DOCKER_REGISTRY_URL = "%s/v2/%s/blobs/sha256:%s";
    private static final String HTTPS_PROTOCOL = "https://";

    private final FileService fileService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final LayerService layerService;

    @Override
    public List<DockerImage> unpackDockerTar(Path pathToTar) {
        log.info("Unpack docker tar from path: {}", pathToTar);
        var manifestPath = extractFile(pathToTar, pathToTar.getParent());
        return getImagesFromFile(manifestPath);
    }

    @Override
    public void uploadImage(DockerImage image, Path directoryWithImages) {
        log.info("RepoTags are {} ", image.getRepoTags());
        for (String originalRepoTag : image.getRepoTags()) {
            String[] repoTagSplit = prepareTagName(originalRepoTag);
            String repo = repoTagSplit[0];
            if (image.getLayers() == null) {
                throw new InternalRuntimeException("Docker image layers were not found");
            }
            List<DockerLayer> layers = image.getLayers()
                    .parallelStream()
                    .map(layer -> processLayer(directoryWithImages, repo, layer))
                    .collect(Collectors.toList());

            log.info("Uploading config {} to registry {}", image.getConfig(), dockerRegistry);
            DockerLayer config = processLayerConfig(directoryWithImages, repo, image.getConfig());
            config.setMediaType(DOCKER_LAYER_MEDIA_TYPE_JSON);

            String tag = repoTagSplit[1];
            processManifest(layers, repo, tag, config);
        }
    }

    private DockerLayer processLayer(Path directoryWithImages, String repo, String layer) {
        log.info("Processing layer {}", layer);
        var layerTarPath = directoryWithImages.resolve(Path.of(layer));

        String layerDigest = generateHash(layerTarPath);
        long size = layerTarPath.toFile().length();
        log.debug("File size in bytes is {}", size);

        if (layerExists(repo, layerDigest)) {
            log.info("Layer {} already exists", layer);
        } else {
            uploadLayer(repo, layer, layerTarPath, layerDigest);
        }

        return new DockerLayer(size, layerDigest);
    }

    private DockerLayer processLayerConfig(Path directoryWithImages, String repo, String layerConfig) {
        log.info("Processing config {}", layerConfig);
        var layerConfigPath = directoryWithImages.resolve(layerConfig);

        String configDigest = generateHash(layerConfigPath);
        long size = layerConfigPath.toFile().length();
        log.debug("File size in bytes is {}", size);

        if (layerExists(repo, configDigest)) {
            log.info("Config {} already exists", layerConfig);
        } else {
            uploadLayer(repo, layerConfig, layerConfigPath, configDigest);
        }

        return new DockerLayer(size, configDigest);
    }

    private void processManifest(List<DockerLayer> layers, String repo, String tag, DockerLayer config) {
        log.info("Generating and pushing manifest for {}:{}", repo, tag);
        String manifestJson;
        try {
            manifestJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new Manifest(config, layers));
        } catch (IOException e) {
            throw new InternalRuntimeException(String.format("Failed to generate manifest for %s:%s", repo, tag));
        }
        String dockerRegistryWithProtocol = prepareRegistryUrl(dockerRegistry);
        var url = String.format(MANIFEST_IN_PRIVATE_DOCKER_REGISTRY_URL, dockerRegistryWithProtocol, repo, tag);
        var httpStatus = restClient.put(url, manifestJson, registryUser, registryPassword, DOCKER_LAYER_CONTENT_TYPE).getStatusCode();
        if (!httpStatus.equals(HttpStatus.CREATED)) {
            throw new InternalRuntimeException("Failed to upload manifest with Http Status " + httpStatus.value());
        }
    }

    private void uploadLayer(String repo, String layer, Path layerPath, final String layerDigest) {
        log.info("Uploading {} to docker registry {}", layer, dockerRegistry);
        var dockerRegistryWithProtocol = prepareRegistryUrl(dockerRegistry);
        var uploadUrl = String.format(UPLOAD_URL, dockerRegistryWithProtocol, repo);
        log.info("Start upload url: {}", uploadUrl);

        try {
            ResponseEntity<String> responseEntity = restClient.post(uploadUrl, registryUser, registryPassword);
            uploadUrl = getUriPath(responseEntity);
            log.info("Upload url: {}", uploadUrl);
            layerService.pushLayer(layerPath, uploadUrl, layerDigest);
        } catch (Exception e) {
            String details = isNoSpaceLeftOnDeviceException(e) ? "no space left on docker registry" : e.getMessage();
            throw new DockerServiceException("Error happened during layer upload. Details: " + details);
        }
    }

    private boolean layerExists(String repo, String digest) {
        var dockerRegistryWithProtocol = prepareRegistryUrl(dockerRegistry);
        var url = String.format(LAYERS_IN_PRIVATE_DOCKER_REGISTRY_URL, dockerRegistryWithProtocol, repo, digest);
        log.info("Checking if URL exists: {}", url);
        return restClient.head(url, registryUser, registryPassword).equals(HttpStatus.OK);
    }

    private Path extractFile(Path pathToTar, Path directory) {
        log.info("Extracting file {} from tar archive {}", MANIFEST_FILE, pathToTar);
        Path untarDirectory = fileService.untar(pathToTar, directory, TIMEOUT);
        return Paths.get(untarDirectory.toString(), MANIFEST_FILE);
    }

    private List<DockerImage> getImagesFromFile(Path file) {
        JSONArray manifest = fileService.readFile(file);
        List<DockerImage> images = new ArrayList<>();

        log.info("Starting to map images");
        for (var i = 0; i < manifest.length(); i++) {
            var jsonObject = (JSONObject) manifest.get(i);
            DockerImage image = getImage(jsonObject);
            images.add(image);
        }
        log.info("{} images were mapped successfully.", images.size());

        return images;
    }

    private DockerImage getImage(JSONObject jsonObject) {
        try {
            String dockerRegistryWithProtocol = prepareRegistryUrl(dockerRegistry);
            var dockerImage = objectMapper.readValue(jsonObject.toString(), DockerImage.class);
            List<String> tags = Optional.ofNullable(dockerImage.getRepoTags())
                    .orElseThrow(() -> new InvalidFileException("Cannot process image. RepoTags are null."));
            tags = tags.stream()
                    .map(item -> item.replace(DEFAULT_DOCKER_REGISTRY, prepareRegistry(dockerRegistryWithProtocol)))
                    .collect(Collectors.toList());
            dockerImage.setRepoTags(tags);
            return dockerImage;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse image info from the file. Details: " + e.getMessage());
        }
    }

    private <E extends Exception> boolean isNoSpaceLeftOnDeviceException(E e) {
        if (e instanceof HttpServerErrorException) {
            HttpServerErrorException exception = (HttpServerErrorException) e;
            var body = exception.getResponseBodyAsString();
            var matcher = REGISTRY_NO_SPACE_LEFT_ERROR_PATTERN.matcher(body);
            return exception.getStatusCode().is5xxServerError() && matcher.find();
        }
        if (e instanceof InternalRuntimeException) {
            return e.getMessage().contains("no space left on device");
        }
        return false;
    }

    private String[] prepareTagName(String originalRepoTag) {
        var registryMatcher = REGISTRY_PATTERN.matcher(originalRepoTag);
        String repoTag = registryMatcher.find() ? originalRepoTag.replace(registryMatcher.group(1) + "/", "") : originalRepoTag;
        String[] repoTagUnits = repoTag.split(":");
        if (repoTagUnits.length < 2) {
            throw new InvalidFileException("RepoTag unit must contain at least two part: repo and tag, but it was only " + repoTagUnits.length);
        }
        return repoTagUnits;
    }

    private String generateHash(final Path artifact) {
        try (var stream =
                new MessageDigestCalculatingInputStream(new BufferedInputStream(Files.newInputStream(artifact)),
                                                        MessageDigest.getInstance("SHA-256"))) {
            stream.consume();
            byte[] hash = stream.getMessageDigest().digest();
            return DatatypeConverter.printHexBinary(hash).toLowerCase();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new InternalRuntimeException(
                    String.format("Failed to generate digest for %s due to %s", artifact.getFileName().toString(), e.getMessage()));
        }
    }

    private String getUriPath(ResponseEntity<String> responseEntity) {
        var uriPath = Optional.ofNullable(responseEntity.getHeaders().getLocation());

        return String.valueOf(uriPath
                .filter(uri -> Optional.ofNullable(uri.getPath()).isPresent())
                .orElseThrow(() -> new InvalidURLException(String.format("Not correct url %s in the headers location", uriPath.orElse(null)))));
    }

    private String prepareRegistry(String dockerRegistry) {
        final String[] registrySplit = dockerRegistry.split("//");
        if (registrySplit.length < 2) {
            throw new InvalidURLException(
                    String.format("Not correct docker registry url, it must be prefixed with http(s) but actually: %s", registrySplit[0]));
        }
        return registrySplit[1];
    }

    private String prepareRegistryUrl(String dockerRegistry) {
        return HTTPS_PROTOCOL + dockerRegistry;
    }
}
