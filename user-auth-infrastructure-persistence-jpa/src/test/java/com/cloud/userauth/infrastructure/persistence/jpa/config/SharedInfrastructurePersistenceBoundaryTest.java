package com.cloud.userauth.infrastructure.persistence.jpa.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.framework.starter.domain.eventstore.persistence.StoredDomainEventPersistenceRepository;
import com.cloud.userauth.infrastructure.persistence.jpa.repository.DomainEventJpaPersistenceRepository;
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

    @Test
    void shouldKeepConcreteDomainEventStoreOutsideDomainAndApplication() throws IOException {
        assertFalse(containsSourceText(
                Path.of("..", "user-auth-domain", "src", "main", "java"),
                "com.cloud.framework.starter.domain.eventstore"));
        assertFalse(containsSourceText(
                Path.of("..", "user-auth-application", "src", "main", "java"),
                "com.cloud.framework.starter.domain.eventstore"));
        assertTrue(StoredDomainEventPersistenceRepository.class
                .isAssignableFrom(DomainEventJpaPersistenceRepository.class));
    }

    private static boolean containsSourceText(Path root, String expected) throws IOException {
        try (Stream<Path> sources = Files.walk(root)) {
            return sources.filter(Files::isRegularFile).anyMatch(path -> contains(path, expected));
        }
    }

    private static boolean contains(Path path, String expected) {
        try {
            return Files.readString(path).contains(expected);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to inspect source boundary", exception);
        }
    }
}
