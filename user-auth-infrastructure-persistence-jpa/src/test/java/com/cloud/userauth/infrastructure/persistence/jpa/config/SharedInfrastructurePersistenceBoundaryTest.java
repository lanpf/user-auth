package com.cloud.userauth.infrastructure.persistence.jpa.config;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SharedInfrastructurePersistenceBoundaryTest {

    @Test
    void shouldNotKeepPersistenceTechnologyDataObjectsOrMappersInSharedInfrastructure()
            throws IOException {
        Path sharedPersistence = Path.of(
                "..", "user-auth-infrastructure", "src", "main", "java", "com", "cloud",
                "userauth", "infrastructure", "persistence");

        try (Stream<Path> sources = Files.walk(sharedPersistence)) {
            assertFalse(sources
                    .filter(Files::isRegularFile)
                    .map(path -> sharedPersistence.relativize(path).toString())
                    .anyMatch(path -> path.startsWith("model/") || path.startsWith("mapper/")));
        }
    }
}
