package com.example.bfhqualifier;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AppRunner {

    private static final String GENERATE_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    private static final String DEFAULT_SUBMIT_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    private final RestTemplate restTemplate = new RestTemplate();

    public void execute() {
        try {
            // 1) Build request body with YOUR details
            GenerateWebhookRequest requestBody =
                    new GenerateWebhookRequest(
                            "Apoorv Mahajan",    // TODO: Replace
                            "22BCE2989",       // TODO: Replace
                            "apoorvmahajan0910@gmail.com"         // TODO: Replace
                    );

            // 2) Call generateWebhook API
            ResponseEntity<GenerateWebhookResponse> response =
                    restTemplate.postForEntity(
                            GENERATE_URL,
                            requestBody,
                            GenerateWebhookResponse.class
                    );

            GenerateWebhookResponse body = response.getBody();
            if (body == null) {
                System.out.println("generateWebhook returned EMPTY response.");
                return;
            }

            String webhookUrl = body.getWebhook();
            String accessToken = body.getAccessToken();

            if (webhookUrl == null || webhookUrl.isBlank()) {
                webhookUrl = DEFAULT_SUBMIT_URL;
            }

            System.out.println("Webhook URL: " + webhookUrl);
            System.out.println("Access Token: " + accessToken);

            // 3) SQL answer (Question 1)
            String finalQuery = """
                    WITH emp_totals AS (
                        SELECT
                            d.department_name,
                            e.emp_id,
                            e.first_name,
                            e.last_name,
                            SUM(p.amount) AS salary,
                            EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.dob)) AS age
                        FROM employee e
                        JOIN department d
                            ON e.department = d.department_id
                        JOIN payments p
                            ON p.emp_id = e.emp_id
                        WHERE EXTRACT(DAY FROM p.payment_time) <> 1
                        GROUP BY
                            d.department_name,
                            e.emp_id,
                            e.first_name,
                            e.last_name,
                            e.dob
                    ),
                    ranked AS (
                        SELECT
                            department_name,
                            salary,
                            first_name || ' ' || last_name AS employee_name,
                            age,
                            ROW_NUMBER() OVER (PARTITION BY department_name ORDER BY salary DESC) AS rn
                        FROM emp_totals
                    )
                    SELECT
                        department_name AS DEPARTMENT_NAME,
                        salary AS SALARY,
                        employee_name AS EMPLOYEE_NAME,
                        age AS AGE
                    FROM ranked
                    WHERE rn = 1
                    ORDER BY department_name;
                    """;

            SubmitSolutionRequest submitBody = new SubmitSolutionRequest(finalQuery);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", accessToken);

            HttpEntity<SubmitSolutionRequest> entity =
                    new HttpEntity<>(submitBody, headers);

            // 4) Submit final SQL
            ResponseEntity<String> result =
                    restTemplate.postForEntity(
                            webhookUrl,
                            entity,
                            String.class
                    );

            System.out.println("Submission status: " + result.getStatusCode());
            System.out.println("Server reply: " + result.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
