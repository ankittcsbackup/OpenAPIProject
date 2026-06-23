Perform a full cluster health check for this project. Run each of the following commands and collect the output:

1. `kubectl get pods -n hello-api`
2. `kubectl get pods -n monitoring`
3. `kubectl -n argocd get app hello-api -o jsonpath='{.status.sync.status} {.status.health.status}'`
4. `curl -s -o /dev/null -w "%{http_code}" --max-time 5 http://localhost:30080/hello`

Then report a summary table in this format:

| Component | Status |
|---|---|
| App pods (hello-api) | X/X Running |
| Monitoring pods | X/X Running |
| ArgoCD sync | Synced / OutOfSync |
| ArgoCD health | Healthy / Degraded |
| App response | HTTP 200 OK / FAILED |

After the table, add one line:
- "All systems healthy." if everything is green
- Or a brief note on what needs attention (e.g. "1 pod in CrashLoopBackOff — check logs with: kubectl logs -n hello-api -l app=hello-api --tail=50")

Do not show raw kubectl output — only the summary table and the one-line verdict.
