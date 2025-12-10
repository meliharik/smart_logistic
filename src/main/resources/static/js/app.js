const API_BASE_URL = 'http://localhost:8080/api';

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
    loadVehicles();
    loadPackages();
    loadRoutes();
    setupAssignForm();

    // Refresh data every 10 seconds
    setInterval(() => {
        loadVehicles();
        loadPackages();
        loadRoutes();
    }, 10000);
});

// Load vehicles
async function loadVehicles() {
    try {
        const response = await fetch(`${API_BASE_URL}/vehicles`);
        const vehicles = await response.json();
        displayVehicles(vehicles);
        populateVehicleSelect(vehicles);
    } catch (error) {
        console.error('Error loading vehicles:', error);
        showStatus('Failed to load vehicles', 'error');
    }
}

// Display vehicles
function displayVehicles(vehicles) {
    const container = document.getElementById('vehicles-list');

    if (vehicles.length === 0) {
        container.innerHTML = '<div class="empty-state">No vehicles available</div>';
        return;
    }

    container.innerHTML = vehicles.map(vehicle => `
        <div class="vehicle-card">
            <h3>🚚 ${vehicle.licensePlate}</h3>
            <div class="info">
                <div class="info-row">
                    <span class="info-label">Status:</span>
                    <span class="badge badge-${vehicle.status.toLowerCase().replace('_', '-')}">${vehicle.status}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Capacity:</span>
                    <span class="info-value">${vehicle.capacityKg} kg</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Current Load:</span>
                    <span class="info-value">${vehicle.currentLoadKg} kg</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Remaining:</span>
                    <span class="info-value">${vehicle.remainingCapacityKg} kg</span>
                </div>
            </div>
            <div class="progress-bar">
                <div class="progress-fill" style="width: ${(vehicle.currentLoadKg / vehicle.capacityKg * 100)}%"></div>
            </div>
        </div>
    `).join('');
}

// Populate vehicle select dropdown
function populateVehicleSelect(vehicles) {
    const select = document.getElementById('vehicle-select');
    const availableVehicles = vehicles.filter(v => v.status === 'AVAILABLE');

    select.innerHTML = '<option value="">-- Choose a vehicle --</option>' +
        availableVehicles.map(v =>
            `<option value="${v.id}">${v.licensePlate} (${v.remainingCapacityKg}kg available)</option>`
        ).join('');
}

// Load packages
async function loadPackages() {
    try {
        const response = await fetch(`${API_BASE_URL}/packages`);
        const packages = await response.json();
        displayPackages(packages);
        populatePackageCheckboxes(packages);
    } catch (error) {
        console.error('Error loading packages:', error);
        showStatus('Failed to load packages', 'error');
    }
}

// Display packages
function displayPackages(packages) {
    const container = document.getElementById('packages-list');

    if (packages.length === 0) {
        container.innerHTML = '<div class="empty-state">No packages available</div>';
        return;
    }

    container.innerHTML = packages.map(pkg => `
        <div class="package-card">
            <h3>📦 Package #${pkg.id}</h3>
            <div class="info">
                <div class="info-row">
                    <span class="info-label">Status:</span>
                    <span class="badge badge-${pkg.status.toLowerCase()}">${pkg.status}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Weight:</span>
                    <span class="info-value">${pkg.weightKg} kg</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Address:</span>
                    <span class="info-value" style="font-size: 0.85rem">${pkg.deliveryAddress}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Deadline:</span>
                    <span class="info-value">${formatDate(pkg.deliveryDeadline)}</span>
                </div>
            </div>
        </div>
    `).join('');
}

// Populate package checkboxes
function populatePackageCheckboxes(packages) {
    const container = document.getElementById('package-checkboxes');
    const unassignedPackages = packages.filter(p => p.status === 'CREATED');

    if (unassignedPackages.length === 0) {
        container.innerHTML = '<div class="empty-state">No unassigned packages available</div>';
        return;
    }

    container.innerHTML = unassignedPackages.map(pkg => `
        <div class="checkbox-item">
            <input type="checkbox" id="pkg-${pkg.id}" value="${pkg.id}">
            <label for="pkg-${pkg.id}">
                Package #${pkg.id} - ${pkg.weightKg}kg - ${pkg.deliveryAddress.substring(0, 30)}...
            </label>
        </div>
    `).join('');
}

// Setup assign form
function setupAssignForm() {
    const form = document.getElementById('assign-form');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const vehicleId = document.getElementById('vehicle-select').value;
        const selectedPackages = Array.from(document.querySelectorAll('#package-checkboxes input:checked'))
            .map(cb => parseInt(cb.value));

        if (!vehicleId) {
            showStatus('Please select a vehicle', 'error');
            return;
        }

        if (selectedPackages.length === 0) {
            showStatus('Please select at least one package', 'error');
            return;
        }

        await assignPackages(vehicleId, selectedPackages);
    });
}

// Assign packages to vehicle
async function assignPackages(vehicleId, packageIds) {
    try {
        const response = await fetch(`${API_BASE_URL}/delivery/assign`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                vehicleId: parseInt(vehicleId),
                packageIds: packageIds
            })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Assignment failed');
        }

        const result = await response.json();
        showStatus(`Successfully assigned ${packageIds.length} package(s) to vehicle!`, 'success');

        // Refresh all data
        setTimeout(() => {
            loadVehicles();
            loadPackages();
            loadRoutes();
            document.getElementById('assign-form').reset();
        }, 1000);

    } catch (error) {
        console.error('Error assigning packages:', error);
        showStatus(error.message, 'error');
    }
}

// Load delivery routes
async function loadRoutes() {
    try {
        const response = await fetch(`${API_BASE_URL}/delivery/routes`);
        const routes = await response.json();
        displayRoutes(routes);
    } catch (error) {
        console.error('Error loading routes:', error);
        showStatus('Failed to load routes', 'error');
    }
}

// Display routes
function displayRoutes(routes) {
    const container = document.getElementById('routes-list');

    if (routes.length === 0) {
        container.innerHTML = '<div class="empty-state">No active delivery routes</div>';
        return;
    }

    container.innerHTML = routes.map(route => `
        <div class="route-item">
            <div class="route-header">
                <div>
                    <h3>Route #${route.id} - ${route.vehicleLicensePlate}</h3>
                    <p style="color: #666; font-size: 0.9rem;">Created: ${formatDate(route.createdAt)}</p>
                </div>
                <div>
                    <strong>Total Weight: ${route.totalWeight} kg</strong>
                </div>
            </div>
            <div class="route-packages">
                ${route.packages.map(pkg => `
                    <div class="route-package">
                        <strong>Package #${pkg.id}</strong> - ${pkg.weightKg}kg -
                        <span class="badge badge-${pkg.status.toLowerCase()}">${pkg.status}</span>
                        <br>
                        <small>${pkg.deliveryAddress}</small>
                    </div>
                `).join('')}
            </div>
        </div>
    `).join('');
}

// Show status message
function showStatus(message, type) {
    const statusEl = document.getElementById('status-message');
    statusEl.textContent = message;
    statusEl.className = `status-message ${type} show`;

    setTimeout(() => {
        statusEl.classList.remove('show');
    }, 5000);
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diff = date - now;
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

    if (hours < 0) {
        return 'OVERDUE';
    } else if (hours === 0) {
        return `${minutes}m`;
    } else if (hours < 24) {
        return `${hours}h ${minutes}m`;
    } else {
        return date.toLocaleString();
    }
}
