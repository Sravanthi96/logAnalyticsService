package com.sravanthi.logAnalytics.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AthenaQueryService {

    private final AthenaClient athenaClient;
    private final String database;
    private final String outputLocation;

    public AthenaQueryService(
            AthenaClient athenaClient,
            @Value("${aws.athena.database}") String database,
            @Value("${aws.athena.output-location}") String outputLocation) {

        this.athenaClient = athenaClient;
        this.database = database;
        this.outputLocation = outputLocation;
    }

    public List<Map<String, String>> findLogs(
            String year,
            String month,
            String day,
            String orderId) {

        validateDatePartition(year, month, day);
        validateOrderId(orderId);

        String query = """
                SELECT
                    event_timestamp,
                    level,
                    message,
                    order_id,
                    correlation_id
                FROM application_logs
                WHERE year = '%s'
                  AND month = '%s'
                  AND day = '%s'
                  AND order_id = '%s'
                ORDER BY event_timestamp ASC
                LIMIT 100
                """.formatted(year, month, day, orderId);

        String executionId = startQuery(query);
        waitForCompletion(executionId);

        return readResults(executionId);
    }

    private String startQuery(String query) {
        StartQueryExecutionRequest request =
                StartQueryExecutionRequest.builder()
                        .queryString(query)
                        .queryExecutionContext(
                                QueryExecutionContext.builder()
                                        .database(database)
                                        .build())
                        .resultConfiguration(
                                ResultConfiguration.builder()
                                        .outputLocation(outputLocation)
                                        .build())
                        .build();

        return athenaClient.startQueryExecution(request)
                .queryExecutionId();
    }

    private void waitForCompletion(String executionId) {
        int attempts = 0;

        while (attempts++ < 60) {
            GetQueryExecutionResponse response =
                    athenaClient.getQueryExecution(
                            GetQueryExecutionRequest.builder()
                                    .queryExecutionId(executionId)
                                    .build());

            QueryExecutionStatus status =
                    response.queryExecution().status();

            QueryExecutionState state = status.state();

            if (state == QueryExecutionState.SUCCEEDED) {
                return;
            }

            if (state == QueryExecutionState.FAILED ||
                    state == QueryExecutionState.CANCELLED) {

                throw new IllegalStateException(
                        "Athena query " + state +
                                ": " + status.stateChangeReason());
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(
                        "Interrupted while waiting for Athena", exception);
            }
        }

        throw new IllegalStateException("Athena query timed out");
    }

    private List<Map<String, String>> readResults(String executionId) {
        GetQueryResultsResponse response =
                athenaClient.getQueryResults(
                        GetQueryResultsRequest.builder()
                                .queryExecutionId(executionId)
                                .maxResults(100)
                                .build());

        List<Row> rows = response.resultSet().rows();
        List<Map<String, String>> results = new ArrayList<>();

        if (rows.size() <= 1) {
            return results;
        }

        List<String> headers = rows.getFirst()
                .data()
                .stream()
                .map(Datum::varCharValue)
                .toList();

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            List<Datum> values = rows.get(rowIndex).data();
            Map<String, String> row = new LinkedHashMap<>();

            for (int columnIndex = 0;
                 columnIndex < headers.size();
                 columnIndex++) {

                String value = columnIndex < values.size()
                        ? values.get(columnIndex).varCharValue()
                        : null;

                row.put(headers.get(columnIndex), value);
            }

            results.add(row);
        }

        return results;
    }

    private void validateDatePartition(
            String year,
            String month,
            String day) {

        if (!year.matches("\\d{4}") ||
                !month.matches("\\d{2}") ||
                !day.matches("\\d{2}")) {

            throw new IllegalArgumentException(
                    "Date must use year=YYYY, month=MM and day=DD");
        }
    }

    private void validateOrderId(String orderId) {
        if (orderId == null ||
                !orderId.matches("[A-Za-z0-9_-]{1,100}")) {

            throw new IllegalArgumentException("Invalid orderId");
        }
    }
}