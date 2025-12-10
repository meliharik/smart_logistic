# GitHub Actions Workflows

This directory contains automated CI/CD workflows for the Smart Logistic project.

## Available Workflows

### 1. CI/CD Pipeline (`ci.yml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches

**Jobs:**

#### Build Job
- Sets up PostgreSQL test database
- Builds the project with Maven
- Runs all unit and integration tests
- Generates test coverage reports
- Verifies code style with Checkstyle
- Packages the application
- Uploads build artifacts

#### Code Quality Job
- Runs PMD static code analysis
- Runs SpotBugs for bug detection

#### Security Scan Job
- Performs OWASP dependency check
- Runs Trivy vulnerability scanner
- Uploads security findings to GitHub Security tab

### 2. Docker Build and Push (`docker-build.yml`)

**Triggers:**
- Push to `main` branch
- Version tags (e.g., `v1.0.0`)
- Release publications

**Features:**
- Builds Docker image with multi-stage build
- Pushes to GitHub Container Registry (ghcr.io)
- Automatic semantic versioning
- Docker layer caching for faster builds
- Vulnerability scanning with Trivy

**Image Tags Generated:**
- `latest` - Most recent build from main branch
- `{branch}` - Branch name
- `{version}` - Semantic version (from git tags)
- `{major}.{minor}` - Major and minor version
- `{branch}-{sha}` - Branch with commit SHA

### 3. GitHub Pages Deployment (`deploy.yml`)

**Triggers:**
- Push to `main` branch (only when `docs/**` changes)
- Manual workflow dispatch

**Deployment:**
- Deploys documentation from `docs/` directory
- Accessible at: https://meliharik.github.io/smart_logistic/

## Secrets Required

No secrets are required for basic functionality. The workflows use GitHub's built-in `GITHUB_TOKEN` for authentication.

### Optional Secrets (for advanced features):
- `DOCKER_USERNAME` - Docker Hub username (if pushing to Docker Hub)
- `DOCKER_PASSWORD` - Docker Hub password/token
- `SONAR_TOKEN` - SonarCloud token (for code quality analysis)

## Local Testing

You can test the workflows locally using [act](https://github.com/nektos/act):

```bash
# Test CI workflow
act -j build

# Test Docker build
act -j build-and-push --secret GITHUB_TOKEN=your_token
```

## Workflow Status

Check the status of all workflows:
- [CI/CD Pipeline](https://github.com/meliharik/smart_logistic/actions/workflows/ci.yml)
- [Docker Build](https://github.com/meliharik/smart_logistic/actions/workflows/docker-build.yml)
- [GitHub Pages](https://github.com/meliharik/smart_logistic/actions/workflows/deploy.yml)

## Customization

To customize the workflows:

1. **Add more tests**: Modify the `ci.yml` to include additional test steps
2. **Change deployment target**: Update `docker-build.yml` to push to different registries
3. **Add notifications**: Integrate Slack, Discord, or email notifications on workflow completion
4. **Schedule periodic scans**: Add `schedule` triggers for automated security scans

## Best Practices

- All workflows use pinned action versions for security
- Caching is enabled for Maven dependencies and Docker layers
- Security scanning is integrated into the pipeline
- Artifacts are retained for 30 days
- Failed builds block merging (branch protection rules)

## Troubleshooting

### Build Failures
1. Check the workflow logs in the Actions tab
2. Verify all tests pass locally: `mvn clean test`
3. Ensure PostgreSQL is configured correctly

### Docker Build Issues
1. Verify the Dockerfile syntax
2. Check that the JAR file is being created: `mvn package`
3. Ensure GITHUB_TOKEN has package write permissions

### Pages Deployment Issues
1. Enable GitHub Pages in repository settings
2. Select "GitHub Actions" as the source
3. Verify the `docs/` directory exists and contains `index.html`
