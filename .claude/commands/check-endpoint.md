The user has provided a URL as an argument: $ARGUMENTS

Run `curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$ARGUMENTS"` to check the HTTP status code.

Then report in this format:

**URL:** $ARGUMENTS
**Status:** HTTP <code> — OK or FAILED
**Verdict:** one sentence — e.g. "Endpoint is healthy" or "Endpoint is not reachable (connection refused / timeout / non-200)"

If $ARGUMENTS is empty, tell the user to provide a URL, for example:
  `/check-endpoint http://localhost:30080/hello`

Known endpoints in this project for reference:
- http://localhost:30080/hello — main API
- http://localhost:30080/swagger-ui/index.html — Swagger UI
- http://localhost:30080/actuator/health — health check
- http://localhost:30090 — Prometheus
- http://localhost:30030 — Grafana
