Run `mvn verify -ntp` from the project root `/Users/ankittiwari/IntelliJ-workspace/hello-openai`.

When the command finishes, report the following in this exact format:

**Build:** PASSED or FAILED
**Tests:** X run, X failed, X skipped (extract from the Surefire summary line)
**Coverage gate:** PASSED or FAILED (look for "BUILD SUCCESS" vs "BUILD FAILURE" — failure here usually means JaCoCo minimums were not met)

If the build failed, quote the first ERROR line from the output so the user knows where to look.

Keep the entire response to 5 lines or fewer.
