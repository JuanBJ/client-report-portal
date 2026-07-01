# Client Report Portal - Java / Spring Boot MVP

Java/Spring Boot implementation of a client financial report workflow. The app
focuses on the core time-boxed path: create a client, add accounts and
liabilities, enter quarterly balances, calculate the report values, and generate
SACS/TCC PDFs.

## Running it

This repository includes Maven bootstrap scripts, so Maven does not need to be
installed globally. You only need a JDK on PATH.

```powershell
.\run.cmd
```

Or run the build and jar manually:

```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target\client-report-portal-0.1.0.jar
```

On macOS/Linux:

```bash
./mvnw clean package -DskipTests
java -jar target/client-report-portal-0.1.0.jar
```

Then open:

```text
http://localhost:8080
```

Build a runnable jar without starting it:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Note: on Windows paths with spaces or accented characters, `spring-boot:run` can
fail because the plugin launches Java with a temporary classpath file. Running
the packaged jar avoids that classpath issue.

## What is implemented

- Client creation with salary, expense budget, and insurance deductible inputs.
- Account and liability setup.
- Quarterly report creation with per-account and per-liability balances.
- Centralized financial calculations:
  - Excess = Inflow - Outflow
  - Reserve target = 6 months of expenses + insurance deductibles
  - Liabilities are displayed separately and not subtracted from net worth
  - Trust value is kept separate from non-retirement totals
- SACS PDF generation.
- TCC PDF generation.
- Static HTML frontend served by Spring Boot from the same origin as the API.

## Project layout

```text
src/main/java/com/clientreportportal/
  ClientReportPortalApplication.java
  model/          Client, Account, Liability, QuarterlyReport, balances
  dto/            request/response records
  repository/     Spring Data JPA repositories
  service/        CalculationService, PdfService
  controller/     ClientController, ReportController
src/main/resources/
  application.properties
  static/index.html
```

## API overview

- `GET /api/health`
- `GET /api/clients`
- `POST /api/clients`
- `GET /api/clients/{id}`
- `PUT /api/clients/{id}`
- `POST /api/clients/{clientId}/accounts`
- `POST /api/clients/{clientId}/liabilities`
- `POST /api/clients/{clientId}/reports`
- `GET /api/clients/{clientId}/reports`
- `GET /api/reports/{reportId}/calculate`
- `GET /api/reports/{reportId}/pdf/sacs`
- `GET /api/reports/{reportId}/pdf/tcc`

## Time-boxed tradeoffs

This is intentionally scoped as an MVP rather than a full production system.
Authentication, Canva/template export, historical report comparison, polished PDF
layout matching, validation hardening, and Postgres/Flyway migration setup are
the next logical steps.
