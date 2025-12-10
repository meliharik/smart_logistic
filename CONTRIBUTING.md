# Contributing to LogiRoute

Thank you for your interest in contributing to LogiRoute! We welcome contributions from the community.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Reporting Bugs](#reporting-bugs)
- [Requesting Features](#requesting-features)

## 🤝 Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment for everyone.

## 🚀 Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/logiroute.git
   cd logiroute
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/logiroute.git
   ```

## 💻 Development Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker Desktop
- Your favorite IDE (IntelliJ IDEA recommended)

### Setup Steps

1. **Start PostgreSQL**:
   ```bash
   docker-compose up -d
   ```

2. **Run tests** to verify setup:
   ```bash
   mvn test
   ```

3. **Start the application**:
   ```bash
   mvn spring-boot:run
   ```

## 🔄 Making Changes

### Branching Strategy

We use the following branch naming conventions:

- `feature/description` - New features
- `bugfix/description` - Bug fixes
- `hotfix/description` - Critical fixes
- `refactor/description` - Code refactoring
- `docs/description` - Documentation updates

### Creating a Branch

```bash
# Update your local main branch
git checkout main
git pull upstream main

# Create a feature branch
git checkout -b feature/your-feature-name
```

## 📝 Coding Standards

### Java Code Style

- **Follow existing patterns** in the codebase
- **Use Lombok** annotations to reduce boilerplate
- **Write clear, descriptive names** for variables, methods, and classes
- **Add JavaDoc** for public methods and classes
- **Keep methods small** (ideally under 20 lines)
- **Follow SOLID principles**

### Example:

```java
/**
 * Assigns packages to a vehicle with capacity validation.
 *
 * @param vehicleId the ID of the vehicle
 * @param packageIds the list of package IDs to assign
 * @return the created delivery route
 * @throws VehicleOverloadedException if packages exceed vehicle capacity
 * @throws ResourceNotFoundException if vehicle or packages not found
 */
@Transactional
public DeliveryRouteDto assignPackagesToVehicle(Long vehicleId, List<Long> packageIds) {
    // Implementation
}
```

### Architecture Guidelines

- **Controller Layer**: Only handle HTTP requests/responses, validation, and DTO mapping
- **Service Layer**: Business logic, transaction management
- **Repository Layer**: Data access only
- **Domain Layer**: Rich domain models with business methods

### Commit Messages

Follow the conventional commits format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Example:**
```
feat(delivery): add capacity guard validation

Implement vehicle overload prevention by validating total package
weight against vehicle remaining capacity before assignment.

Closes #123
```

## 🧪 Testing

### Writing Tests

- Write unit tests for all new features
- Maintain or improve code coverage
- Use meaningful test names following the pattern: `methodName_scenario_expectedBehavior`

### Example:

```java
@Test
@DisplayName("Capacity Guard: Should throw exception when package weight exceeds capacity")
void assignPackagesToVehicle_PackageExceedsCapacity_ThrowsException() {
    // Arrange
    Vehicle vehicle = createVehicle(100.0); // 100kg capacity
    Package pkg = createPackage(150.0);     // 150kg weight
    
    // Act & Assert
    assertThatThrownBy(() -> deliveryService.assignPackagesToVehicle(vehicleId, packageIds))
        .isInstanceOf(VehicleOverloadedException.class);
}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=DeliveryServiceTest

# Run with coverage
mvn test jacoco:report
```

## 📤 Submitting Changes

### Pull Request Process

1. **Update your branch** with the latest changes:
   ```bash
   git checkout main
   git pull upstream main
   git checkout your-feature-branch
   git rebase main
   ```

2. **Ensure all tests pass**:
   ```bash
   mvn clean test
   ```

3. **Push your changes**:
   ```bash
   git push origin your-feature-branch
   ```

4. **Create a Pull Request** on GitHub with:
   - Clear title describing the change
   - Detailed description of what changed and why
   - Reference to related issues (e.g., "Closes #123")
   - Screenshots (if UI changes)

### PR Checklist

Before submitting, ensure:

- [ ] Code follows the project's style guidelines
- [ ] All tests pass (`mvn test`)
- [ ] New tests added for new features
- [ ] Documentation updated (if needed)
- [ ] No merge conflicts with main branch
- [ ] Commits are clean and well-described
- [ ] PR description clearly explains the changes

## 🐛 Reporting Bugs

### Before Submitting

- Check if the bug has already been reported
- Verify the bug exists in the latest version
- Collect relevant information (logs, screenshots, etc.)

### Bug Report Template

Use the GitHub issue template and include:

- **Description**: Clear and concise description of the bug
- **Steps to Reproduce**: Step-by-step instructions
- **Expected Behavior**: What should happen
- **Actual Behavior**: What actually happens
- **Environment**: Java version, OS, etc.
- **Logs**: Relevant error messages or stack traces

## 💡 Requesting Features

### Feature Request Template

- **Problem**: What problem does this solve?
- **Proposed Solution**: How should it work?
- **Alternatives**: Other solutions considered?
- **Additional Context**: Screenshots, mockups, etc.

## 🎯 Good First Issues

Look for issues labeled `good first issue` - these are great for new contributors!

## 📞 Questions?

- Open a [GitHub Discussion](https://github.com/yourusername/logiroute/discussions)
- Check existing [Issues](https://github.com/yourusername/logiroute/issues)

---

Thank you for contributing to LogiRoute! 🚚
