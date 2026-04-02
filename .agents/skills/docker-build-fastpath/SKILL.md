---
name: docker-build-fastpath
description: Speed up Homerun local backend Docker build and test loops in WSL using persistent buildx local caches, smaller build contexts, and safe Testcontainers patterns. Use when repeated backend docker builds feel slow, Dockerized Gradle leaves root-owned build artifacts, or you need benchmark numbers for local container startup/build performance.
---

# Docker Build Fastpath

Use this skill for local WSL development when backend Docker build or test loops are slow.

What this skill gives you:

- a persistent local `buildx` cache under `$HOME/.cache/homerun/buildx/backend`
- a persistent dedicated builder named `homerun-backend-cache` by default, so future sessions don't depend on whatever builder is currently selected
- a cached backend build wrapper that keeps working across new Codex sessions
- a cleanup helper for root-owned `backend/build` artifacts left by Docker bind mounts
- a benchmark script that compares fresh-builder builds with and without the persistent cache

Use these scripts:

- `scripts/backend-buildx-cached.sh`
  Use for local backend Docker builds with a persistent local cache.
  Examples:
  - `bash .agents/skills/docker-build-fastpath/scripts/backend-buildx-cached.sh base`
  - `bash .agents/skills/docker-build-fastpath/scripts/backend-buildx-cached.sh tester homerun-backend-tester`
  - `bash .agents/skills/docker-build-fastpath/scripts/backend-buildx-cached.sh docs homerun-backend-docs:local`

- `scripts/benchmark-backend-buildx.sh`
  Use when you need a fresh-builder benchmark that shows the effect of the persistent cache.
  Example:
  - `bash .agents/skills/docker-build-fastpath/scripts/benchmark-backend-buildx.sh base`
  Output:
  - `fresh_no_external_cache`: a new builder with no external cache
  - `fresh_with_external_cache_warmup`: a new builder that writes the external cache for the first time
  - `fresh_with_external_cache_reused`: another new builder that reuses the saved cache
  - `*_saved_seconds` and `*_saved_percent`: how much time the external cache actually saved versus the no-cache baseline

- `scripts/clean-root-owned-build.sh`
  Use when Gradle fails with `AccessDeniedException` under `backend/build` after Dockerized builds.

Rules:

- keep this optimization local-first; do not enable Testcontainers reusable containers in CI
- prefer Spring Boot `@ServiceConnection` or typed Testcontainers over manual container `start()` inside `@DynamicPropertySource` for Spring Boot integration tests
- use the persistent local cache for repeated local builds; CI cache strategy can be different
- benchmark with fresh builders when you want to show cross-session benefit

Read next only when needed:

- if local build speed is still poor after using the cache wrapper, inspect `backend/.dockerignore` and `backend/Dockerfile.buildkit-cache`
- if a Spring Boot container-backed test flakes only in full-suite runs, inspect the test for manual `DynamicPropertySource` startup and switch to `@ServiceConnection` where applicable
