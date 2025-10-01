# Email Verification Service

The `xtremand-email-verification` module provides a robust, in-house service for validating email addresses without relying on external SaaS providers. It uses a multi-layered approach to score emails and provide a clear recommendation for deliverability.

## Features

The service performs the following checks in a logical sequence, starting with the cheapest and fastest:

1.  **Syntax Check**: Validates the email against RFC 5322 format standards.
2.  **Blacklist Check**: Checks the email and its domain against a configurable internal blacklist.
3.  **Disposable Domain Check**: Detects if the email is from a known temporary or disposable email provider (e.g., `mailinator.com`).
4.  **MX Record Check**: Verifies that the email's domain has valid Mail Exchange (MX) DNS records, indicating it's configured to receive email.
5.  **Role-Based Check**: Detects non-person-specific, role-based addresses (e.g., `admin@`, `support@`, `info@`).
6.  **SMTP Probe**: The most powerful check. It connects to the recipient's mail server and simulates a delivery attempt to verify if the mailbox exists. This includes:
    *   **Greylisting Detection**: Identifies temporary server errors (4xx) and marks the result as `UNKNOWN` for a later retry.
    *   **Catch-All Detection**: After confirming a mailbox is deliverable, it performs a second probe with a random, non-existent email address. If that also succeeds, the domain is flagged as a catch-all.
    *   **SMTP Ping Fallback**: If a full mailbox check isn't possible, it confirms a basic connection to the mail server can be established.

## Scoring & Classification

Each check contributes to a final score (0-100) based on configurable weights. The score determines the final status and recommendation:

*   **Status**: `VALID`, `INVALID`, `RISKY`, `UNKNOWN`, `DISPOSABLE`, `BLACKLISTED`
*   **Confidence**: `HIGH`, `MEDIUM`, `LOW`
*   **Recommendation**: `ACCEPT`, `REVIEW`, `REJECT`

## API Contract

The service exposes the following REST endpoints under the base path `/api/v1/email/verify`. All endpoints require authentication.

### Single Email Verification

*   **Endpoint**: `POST /`
*   **Request Body**:
    ```json
    {
      "email": "user@example.com",
      "userId": 123, // Optional
      "forceSmtp": false // Optional, future use
    }
    ```
*   **Success Response (200 OK)**:
    ```json
    {
      "email": "user@example.com",
      "status": "VALID",
      "score": 92,
      "confidence": "HIGH",
      "recommendation": "ACCEPT",
      "checks": {
        "syntax_check": true,
        "mx_check": true,
        "disposable_check": true,
        "role_based_check": true,
        "blacklist_check": true,
        "catch_all_check": true,
        "smtp_check": true,
        "smtp_ping": true
      },
      "smtp": {
        "smtp_check_status": "DELIVERABLE",
        "smtp_ping_status": "SUCCESS",
        "is_catch_all": false,
        "is_greylisted": false,
        "smtp_logs": "..."
      },
      "details": {
        "mx_hosts": ["mx1.example.com"]
      }
    }
    ```

### Batch Email Verification

*   **Endpoint**: `POST /batch`
*   **Description**: Accepts a list of emails for asynchronous processing. The API returns `202 Accepted` immediately, and the verification is performed in the background. Results are stored in the verification history table.
*   **Request Body**:
    ```json
    {
      "emails": ["user1@example.com", "user2@example.com"],
      "userId": 123 // Optional
    }
    ```

### Retrieve Verification History

*   **Endpoint**: `GET /results/{id}`
*   **Description**: Retrieves a previously stored verification record by its unique ID.

## Configuration

The service is configured via `application.yml`. (Note: Due to a sandbox issue, this file could not be created, so the service currently runs on the defaults shown below).

```yaml
xtremand:
  email:
    verification:
      smtp:
        enabled: true
        mail-from: "verify@xtremand.com"
        helo-host: "xtremand.com"
        connection-timeout: 5000
        read-timeout: 5000
      scoring:
        weights:
          syntax-valid: 20
          mx-valid: 20
          not-disposable: 15
          not-role-based: 10
          not-catch-all: 10
          not-blacklisted: 15
          smtp-deliverable: 10
          smtp-ping-success: 5
        thresholds:
          confidence-high: 90
          confidence-medium: 70
```

## KPI & Reporting Implications

The `xt_user_email_verification_history` table provides a rich source for analytics. Key metrics to monitor include:
*   **Verification Volume**: Total number of checks over time.
*   **Status Distribution**: Breakdown of `VALID`, `INVALID`, `RISKY`, etc., to measure the quality of incoming email lists.
*   **SMTP Outcomes**: Track the rate of `DELIVERABLE`, `INVALID`, `CATCH_ALL`, and `UNKNOWN` results to assess the effectiveness of the SMTP probe.
*   **Fallback Rate**: Monitor how often the service falls back to an `UNKNOWN` status due to greylisting or other SMTP issues. This can help identify if mail servers are throttling or blocking probes.