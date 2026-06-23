# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Purpose

Learning project for contract-first Spring Boot API development with a full CI/CD pipeline, Kubernetes deployment via ArgoCD GitOps, and observability via Prometheus/Grafana/Loki. Used to practice DevOps automation patterns — not a production service.

## Tech Stack

Spring Boot 3.3 · Java 21 · Maven · Contract-first OpenAPI (openapi-generator-maven-plugin) · Docker · Kubernetes (Docker Desktop) · ArgoCD · Prometheus · Grafana · Loki

## Build and Test Commands

```bash
# Run full build, tests, and JaCoCo coverage gate
mvn verify -ntp

# Run tests only (no coverage gate)
mvn test -ntp

# Run a single test class
mvn test -Dtest=HelloServiceTest -ntp
mvn test -Dtest=HelloApiControllerTest -ntp

# Build executable JAR (skip tests)
mvn package -DskipTests -ntp

# Build Docker image locally
docker build -t hello-api .

# Run locally
java -jar target/hello-openapi-1.0.0.jar
```

## Coverage Gate

JaCoCo enforces minimums on `mvn verify`:
- Instruction coverage: **70%**
- Branch coverage: **60%**

Generated classes are excluded: `HelloApplication`, `HelloApi`, `ApiUtil`, `model/**`. If the gate fails, add tests before pushing — CI runs `mvn verify` and will block the PR.

## Adding a New Endpoint

1. Add the endpoint to `src/main/resources/openapi.yaml`
2. Run `mvn generate-sources` to regenerate the interface
3. Implement the new method in `HelloApiController` — the generated `HelloApi` interface will show a compile error until implemented
4. Add a corresponding method to `HelloService` and `HelloServiceImpl`
5. Add log statements: INFO at entry/exit, DEBUG for values, ERROR in catch blocks
6. Write a test in `HelloApiControllerTest` using MockMvc
7. Run `mvn verify -ntp` — confirms tests pass and coverage gate holds

## Contract-First Architecture

The OpenAPI spec at `src/main/resources/openapi.yaml` is the **single source of truth**. Never edit generated files under `target/generated-sources/openapi/` directly.

The code generation flow on every build:
```
openapi.yaml
    │  openapi-generator-maven-plugin
    ▼
target/generated-sources/openapi/
    ├── com/example/hello/api/HelloApi.java        ← interface (implement this)
    ├── com/example/hello/api/ApiUtil.java          ← generated utility
    └── com/example/hello/model/GetHello200Response.java  ← response model
```

To add a new endpoint: add it to `openapi.yaml` → regenerate → implement the new method in `HelloApiController`.

## Application Structure

```
HelloApiController  implements HelloApi (generated interface)
    └── HelloService (interface)
        └── HelloServiceImpl (business logic)
```

Logging uses SLF4J (`LoggerFactory.getLogger(ClassName.class)`). Log levels in `application.yml`:
- `root: INFO` — framework code
- `com.example.hello: DEBUG` — application code

Actuator endpoints exposed: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.

## CI/CD Pipeline

**Branches:**
- `main` — application code only. Protected: requires `test` (ci.yml) and `Analyze Java` (codeql.yml) status checks to pass.
- `gitops` — Kubernetes manifest updates only, written by the CD bot. ArgoCD watches this branch.

**Workflow chain:**
```
push to main
  → ci.yml: mvn verify → docker build → push image:sha-xxxxxxx to ghcr.io
  → cd.yml: updates k8s/deployment.yml image tag on gitops branch
  → ArgoCD: detects gitops change → rolling deploy to hello-api namespace
```

**Image registry:** `ghcr.io/ankittcsbackup/openapiproject` — last 3 images retained, older ones deleted by cd.yml cleanup job.

## Kubernetes

**Namespaces:**
- `hello-api` — application pods (managed by ArgoCD from `gitops` branch `k8s/` folder)
- `monitoring` — Prometheus, Grafana, Loki, Promtail (applied manually)
- `argocd` — ArgoCD itself

**ArgoCD app manifest** lives at `argocd/argocd-app.yml` — **outside** `k8s/` intentionally. Apply manually only:
```bash
kubectl apply -f argocd/argocd-app.yml
```
Never move it back into `k8s/` — ArgoCD would self-prune its own Application resource via `prune: true`.

**Access URLs (Docker Desktop):**
| Service | URL |
|---|---|
| App | http://localhost:30080/hello |
| Swagger UI | http://localhost:30080/swagger-ui/index.html |
| Grafana | http://localhost:30030 (admin/admin) |
| Prometheus | http://localhost:30090 |

**Monitoring stack deploy (one-time):**
```bash
kubectl apply -f k8s/monitoring/namespace.yml
kubectl apply -f k8s/monitoring/
```

**App namespace setup (one-time):**
```bash
kubectl create namespace hello-api
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=ankittcsbackup \
  --docker-password=<PAT> \
  -n hello-api
kubectl apply -f argocd/argocd-app.yml
```

## Monitoring

Grafana datasource UIDs (generated at provisioning time — do not change):
- Prometheus: `PBFA97CFB590B2093`
- Loki: `P8E80F9AEF21F6940`

Always use these UIDs in dashboard JSON panels. If Grafana is redeployed fresh, UIDs may change — re-fetch with:
```bash
kubectl exec -n monitoring deploy/grafana -- curl -s -u admin:admin http://localhost:3000/api/datasources | python3 -m json.tool
```

## Troubleshooting

**Pods in CrashLoopBackOff:**
```bash
kubectl logs -n hello-api -l app=hello-api --tail=50
```
Most common cause: image tag in `gitops` `k8s/deployment.yml` does not exist in ghcr.io. Verify:
```bash
docker pull ghcr.io/ankittcsbackup/openapiproject:<tag>
```

**ArgoCD OutOfSync stuck:**
```bash
kubectl apply -f argocd/argocd-app.yml
# If still stuck:
kubectl -n argocd patch app hello-api \
  -p '{"operation":{"initiatedBy":{"username":"admin"},"sync":{"revision":"HEAD"}}}' \
  --type merge
```

**ImagePullBackOff:**
The `cleanup-images` cd.yml job deleted old images and the new build did not complete. Trigger a fresh CI build:
```bash
git commit --allow-empty -m "ci: trigger rebuild" && git push origin main
```

**Coverage gate fails:**
Run `mvn verify -ntp` locally. Open `target/site/jacoco/index.html` to see exact uncovered lines. Add tests for the highlighted lines then re-run.

**ArgoCD self-pruned its Application resource:**
Happens if `argocd-app.yml` is accidentally placed inside `k8s/` — ArgoCD deletes it via `prune: true`. Fix:
```bash
kubectl apply -f argocd/argocd-app.yml
```

## Available Skills

| Skill | What it does |
|---|---|
| `/check-cluster` | Checks pods, ArgoCD sync, app response |
| `/deploy-monitoring` | Applies k8s/monitoring/ manifests |
| `/verify-build` | Runs mvn verify, reports coverage result |
| `/bootstrap` | Full one-time cluster setup |

## Key Constraints

- `spring-boot-maven-plugin` must be declared in `pom.xml` — without it `java -jar` fails with "no main manifest attribute".
- Maven dependencies: verify versions exist on Maven Central before adding. Do not retry versions that 404.
- Multi-platform Docker builds (`linux/amd64,linux/arm64`) are disabled — QEMU setup fails on GitHub Actions runners. AMD64 only.
- The `gitops` branch must never be checked out for application code changes — it is written only by `cd.yml`.
