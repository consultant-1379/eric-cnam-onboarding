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

package com.ericsson.oss.management.onboarding.presentation.services;

import java.nio.file.Path;

import org.json.JSONArray;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for work with files
 */
public interface FileService {

    /**
     * Unzip archive, for example CSAR
     *
     * @param pathToUnpack
     * @param timeout
     */
    void unzip(Path pathToUnpack, int timeout);

    /**
     * Unpack archive of tgz/tar.gz/tar formats
     *
     * @param archive to unpack
     * @param directory to store the unpacked files
     * @param timeout
     * @return path to the directory with unpacked content of archive
     */
    Path untar(Path archive, Path directory, int timeout);

    /**
     * Create a directory
     *
     * @return the path to the directory
     */
    Path createDirectory();

    /**
     * Create a directory with the given name
     *
     * @param directoryName
     * @return path to the created directory
     */
    Path createDirectory(String directoryName);

    /**
     * Create a directory with the given path
     *
     * @param path
     * @return path to the created directory
     */
    Path createDirectory(Path path);

    /**
     * Delete a directory, and all it's contents
     *
     * @param directory to delete
     */
    void deleteDirectory(Path directory);

    /**
     * Get file by name from directory
     *
     * @param directory to search in
     * @param filename of the file
     * @return path to the searched file
     */
    Path getFileFromDirectory(Path directory, String filename);

    /**
     * Get file from the directory excluding search in indicated location
     *
     * @param directory
     * @param fileName to find
     * @param directoryToExclude
     * @return file
     */
    Path getFileFromTheDirectoryExcludingLocation(Path directory, String fileName,
                                                  String directoryToExclude);

    /**
     * Get file from the directory by name part excluding search in indicated location
     *
     * @param directory
     * @param fileNamePart to find
     * @param directoryToExclude
     * @return file
     */
    Path getFileFromTheDirectoryByNamePartExcludingLocation(Path directory, String fileNamePart,
                                                            String directoryToExclude);

    /**
     * Get value from file
     *
     * @param file to work with
     * @param propertyName to extract value by
     * @return value
     */
    String getValueByPropertyFromFile(Path file, String propertyName);

    /**
     * Convert multipart file to type Path
     *
     * @param file to convert
     * @return the path to the file
     */
    Path convertToPath(MultipartFile file);

    /**
     * Store the contents of a file
     *
     * @param directory to store the file in
     * @param file which came in the request
     * @param filename the name to give the file
     * @return the path to the file
     */
    Path storeFileIn(Path directory, MultipartFile file, String filename);

    /**
     * Read file and return as JSONArray
     *
     * @param filePath to file to read
     * @return content of file
     */
    JSONArray readFile(Path filePath);

    /**
     * Check if directory exists
     *
     * @param directory path to checked directory
     * @return boolean result of checking
     */
    boolean verifyDirectoryExist(Path directory);

    /**
     * Read data from file
     *
     * @param file to read
     * @return string content of a file
     */
    String readDataFromFile(Path file);

    /**
     * Read data from MultipartFile
     *
     * @param file to read
     * @return byte array content of a file
     */
    byte[] readAsBytes(final MultipartFile file);
}
