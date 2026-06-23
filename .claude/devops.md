# DevOps

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

**Key constraint:** Multi-platform Docker builds (`linux/amd64,linux/arm64`) are disabled — QEMU setup fails on GitHub Actions runners. AMD64 only.

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

**The `gitops` branch must never be checked out for application code changes** — it is written only by `cd.yml`.

## One-Time Setup

**App namespace:**
```bash
kubectl create namespace hello-api
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=ankittcsbackup \
  --docker-password=<PAT> \
  -n hello-api
kubectl apply -f argocd/argocd-app.yml
```

**Monitoring stack:**
```bash
kubectl apply -f k8s/monitoring/namespace.yml
kubectl apply -f k8s/monitoring/
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
