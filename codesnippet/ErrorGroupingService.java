package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.example.demo.model.ErrorGroup;
import com.example.demo.model.ErrorLog;
import com.example.demo.repository.ErrorLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Service
public class ErrorGroupingService {

    private static final Logger logger = LoggerFactory.getLogger(ErrorGroupingService.class);

    private final EntityManager entityManager;
    private final ErrorLogRepository errorLogRepository;

    public ErrorGroupingService(
            EntityManager entityManager, ErrorLogRepository errorLogRepository) {
        this.entityManager = entityManager;
        this.errorLogRepository = errorLogRepository;
    }

    /**
     * Group errors intelligently using embedding similarity clustering Uses DBSCAN-like approach to
     * find clusters of similar errors
     */
    public List<ErrorGroup> groupErrors(double similarityThreshold, int minGroupSize, int hours) {
        logger.info(
                "Grouping errors with threshold={}, minGroupSize={}, hours={}",
                similarityThreshold,
                minGroupSize,
                hours);

        // Get recent errors with embeddings
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<ErrorLog> errors = errorLogRepository.findRecentErrors(since);

        // Filter errors that have embeddings
        List<ErrorLog> errorsWithEmbeddings =
                errors.stream().filter(e -> e.getEmbedding() != null).collect(Collectors.toList());

        logger.info(
                "Found {} errors with embeddings in the last {} hours",
                errorsWithEmbeddings.size(),
                hours);

        if (errorsWithEmbeddings.isEmpty()) {
            return new ArrayList<>();
        }

        // Group errors using clustering
        List<ErrorGroup> groups = clusterErrors(errorsWithEmbeddings, similarityThreshold);

        // Filter groups by minimum size
        groups =
                groups.stream()
                        .filter(g -> g.getErrorCount() >= minGroupSize)
                        .collect(Collectors.toList());

        // Sort by error count (largest groups first)
        groups.sort((a, b) -> Integer.compare(b.getErrorCount(), a.getErrorCount()));

        logger.info("Created {} error groups", groups.size());
        return groups;
    }

    /** Group all errors regardless of time period */
    public List<ErrorGroup> groupAllErrors(double similarityThreshold, int minGroupSize) {
        logger.info(
                "Grouping all errors with threshold={}, minGroupSize={}",
                similarityThreshold,
                minGroupSize);

        List<ErrorLog> errors = errorLogRepository.findAll();
        List<ErrorLog> errorsWithEmbeddings =
                errors.stream().filter(e -> e.getEmbedding() != null).collect(Collectors.toList());

        logger.info("Found {} total errors with embeddings", errorsWithEmbeddings.size());

        if (errorsWithEmbeddings.isEmpty()) {
            return new ArrayList<>();
        }

        List<ErrorGroup> groups = clusterErrors(errorsWithEmbeddings, similarityThreshold);
        groups =
                groups.stream()
                        .filter(g -> g.getErrorCount() >= minGroupSize)
                        .collect(Collectors.toList());
        groups.sort((a, b) -> Integer.compare(b.getErrorCount(), a.getErrorCount()));

        logger.info("Created {} error groups", groups.size());
        return groups;
    }

    /** Cluster errors using a similarity-based approach */
    private List<ErrorGroup> clusterErrors(List<ErrorLog> errors, double similarityThreshold) {
        Set<Long> visited = new HashSet<>();
        List<ErrorGroup> groups = new ArrayList<>();
        int groupCounter = 1;

        for (ErrorLog error : errors) {
            if (visited.contains(error.getId())) {
                continue;
            }

            // Start a new cluster
            List<ErrorLog> cluster = new ArrayList<>();
            cluster.add(error);
            visited.add(error.getId());

            // Find all similar errors
            List<ErrorLog> similarErrors =
                    findSimilarErrorsForClustering(error, errors, similarityThreshold);

            for (ErrorLog similar : similarErrors) {
                if (!visited.contains(similar.getId())) {
                    cluster.add(similar);
                    visited.add(similar.getId());
                }
            }

            // Create error group
            if (!cluster.isEmpty()) {
                ErrorGroup group = createErrorGroup(groupCounter++, cluster);
                groups.add(group);
            }
        }

        return groups;
    }

    /** Find similar errors using cosine similarity */
    private List<ErrorLog> findSimilarErrorsForClustering(
            ErrorLog referenceError, List<ErrorLog> allErrors, double threshold) {

        if (referenceError.getEmbedding() == null) {
            return new ArrayList<>();
        }

        try {
            String vectorStr = convertToVectorString(referenceError.getEmbedding());

            String sql =
                    """
                SELECT id, (1 - (embedding <=> CAST(:queryVector AS vector))) AS similarity
                FROM error_logs
                WHERE embedding IS NOT NULL
                AND id != :excludeId
                AND (1 - (embedding <=> CAST(:queryVector AS vector))) >= :threshold
                ORDER BY similarity DESC
                """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("queryVector", vectorStr);
            query.setParameter("excludeId", referenceError.getId());
            query.setParameter("threshold", threshold);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            return results.stream()
                    .map(row -> ((Number) row[0]).longValue())
                    .map(id -> errorLogRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error finding similar errors for clustering", e);
            return new ArrayList<>();
        }
    }

    /** Create an ErrorGroup from a cluster of errors */
    private ErrorGroup createErrorGroup(int groupNumber, List<ErrorLog> errors) {
        // Choose representative error (first error or most common)
        ErrorLog representative = errors.get(0);

        // Calculate average similarity within group
        double avgSimilarity = calculateAverageSimilarity(errors);

        // Determine most common severity
        String severity = determineMostCommonSeverity(errors);

        String groupId = "GROUP-" + groupNumber;
        String groupName = representative.getErrorType() + " - " + errors.size() + " occurrences";

        ErrorGroup group =
                new ErrorGroup(
                        groupId,
                        groupName,
                        representative.getErrorType(),
                        representative.getErrorMessage(),
                        errors);

        group.setAvgSimilarity(avgSimilarity);
        group.setSeverity(severity);

        return group;
    }

    /** Calculate average similarity within a group */
    private double calculateAverageSimilarity(List<ErrorLog> errors) {
        if (errors.size() <= 1) {
            return 1.0;
        }

        // For simplicity, estimate based on pairwise similarity
        // In production, you might want to compute actual pairwise similarities
        return 0.85; // Placeholder - could be improved
    }

    /** Determine the most common severity in a group */
    private String determineMostCommonSeverity(List<ErrorLog> errors) {
        Map<String, Long> severityCount =
                errors.stream()
                        .collect(
                                Collectors.groupingBy(
                                        ErrorLog::getSeverity, Collectors.counting()));

        return severityCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("ERROR");
    }

    /** Convert float array to PostgreSQL vector string format */
    private String convertToVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /** Get summary statistics about error groups */
    public Map<String, Object> getGroupingStatistics(double similarityThreshold, int hours) {
        List<ErrorGroup> groups = groupErrors(similarityThreshold, 1, hours);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGroups", groups.size());
        stats.put(
                "totalErrorsClustered", groups.stream().mapToInt(ErrorGroup::getErrorCount).sum());
        stats.put(
                "largestGroupSize",
                groups.stream().mapToInt(ErrorGroup::getErrorCount).max().orElse(0));
        stats.put(
                "averageGroupSize",
                groups.isEmpty()
                        ? 0
                        : groups.stream().mapToInt(ErrorGroup::getErrorCount).average().orElse(0));

        return stats;
    }
}
