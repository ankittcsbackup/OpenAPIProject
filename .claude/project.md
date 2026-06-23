# Project

## Purpose

Learning project for contract-first Spring Boot API development with a full CI/CD pipeline, Kubernetes deployment via ArgoCD GitOps, and observability via Prometheus/Grafana/Loki. Used to practice DevOps automation patterns — not a production service.

## Tech Stack

Spring Boot 3.3 · Java 21 · Maven · Contract-first OpenAPI (openapi-generator-maven-plugin) · Docker · Kubernetes (Docker Desktop) · ArgoCD · Prometheus · Grafana · Loki

## Access URLs (Docker Desktop)

| Service | URL |
|---|---|
| App | http://localhost:30080/hello |
| Swagger UI | http://localhost:30080/swagger-ui/index.html |
| Grafana | http://localhost:30030 (admin/admin) |
| Prometheus | http://localhost:30090 |

## Available Skills

| Skill | What it does |
|---|---|
| `/verify-build` | Runs `mvn verify`, reports test count and coverage gate result |
| `/check-endpoint <url>` | Checks if a URL returns HTTP 200 |
| `/check-cluster` | Checks pods, ArgoCD sync, and app response in one table |
| `/deploy-monitoring` | Applies k8s/monitoring/ manifests |
| `/bootstrap` | Full one-time cluster setup |
