const API_BASE = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', () => {
    loadAll();
    setupForm();
    setInterval(loadAll, 10000);
});

async function loadAll() {
    await loadVehicles();
    await loadPackages();
    await loadRoutes();
}

async function loadVehicles() {
    try {
        const res = await fetch(`${API_BASE}/vehicles`);
        const vehicles = await res.json();

        const html = vehicles.map(v => `
            <div class="item">
                <div class="item-header">${v.licensePlate}</div>
                <div class="item-row">
                    <span>Status</span>
                    <span class="badge badge-${v.status.toLowerCase().replace('_', '-')}">${v.status}</span>
                </div>
                <div class="item-row">
                    <span>Capacity</span>
                    <span>${v.capacityKg} kg</span>
                </div>
                <div class="item-row">
                    <span>Load</span>
                    <span>${v.currentLoadKg} / ${v.capacityKg} kg</span>
                </div>
                <div class="progress">
                    <div class="progress-bar" style="width: ${(v.currentLoadKg/v.capacityKg*100)}%"></div>
                </div>
            </div>
        `).join('');

        document.getElementById('vehicles-list').innerHTML = html || '<div class="empty">No vehicles</div>';

        const select = document.getElementById('vehicle-select');
        select.innerHTML = '<option value="">Select Vehicle</option>' +
            vehicles.filter(v => v.status === 'AVAILABLE')
                .map(v => `<option value="${v.id}">${v.licensePlate} (${v.remainingCapacityKg}kg)</option>`)
                .join('');
    } catch (e) {
        console.error(e);
    }
}

async function loadPackages() {
    try {
        const res = await fetch(`${API_BASE}/packages`);
        const packages = await res.json();

        const html = packages.map(p => `
            <div class="item">
                <div class="item-header">Package #${p.id}</div>
                <div class="item-row">
                    <span>Status</span>
                    <span class="badge badge-${p.status.toLowerCase()}">${p.status}</span>
                </div>
                <div class="item-row">
                    <span>Weight</span>
                    <span>${p.weightKg} kg</span>
                </div>
                <div class="item-row">
                    <span>Address</span>
                    <span>${p.deliveryAddress.substring(0, 30)}...</span>
                </div>
            </div>
        `).join('');

        document.getElementById('packages-list').innerHTML = html || '<div class="empty">No packages</div>';

        const checkboxes = packages.filter(p => p.status === 'CREATED')
            .map(p => `
                <div class="checkbox-item">
                    <input type="checkbox" id="pkg-${p.id}" value="${p.id}">
                    <label for="pkg-${p.id}">Package #${p.id} - ${p.weightKg}kg</label>
                </div>
            `).join('');

        document.getElementById('package-checkboxes').innerHTML = checkboxes || '<div class="empty">No unassigned packages</div>';
    } catch (e) {
        console.error(e);
    }
}

async function loadRoutes() {
    try {
        const res = await fetch(`${API_BASE}/delivery/routes`);
        const routes = await res.json();

        const html = routes.map(r => `
            <div class="route">
                <div class="route-header">Route #${r.id} - ${r.vehicleLicensePlate} (${r.totalWeight}kg)</div>
                ${r.packages.map(p => `
                    <div class="route-package">
                        #${p.id} - ${p.weightKg}kg - ${p.deliveryAddress.substring(0, 40)}...
                    </div>
                `).join('')}
            </div>
        `).join('');

        document.getElementById('routes-list').innerHTML = html || '<div class="empty">No active routes</div>';
    } catch (e) {
        console.error(e);
    }
}

function setupForm() {
    document.getElementById('assign-form').addEventListener('submit', async (e) => {
        e.preventDefault();

        const vehicleId = document.getElementById('vehicle-select').value;
        const packageIds = Array.from(document.querySelectorAll('#package-checkboxes input:checked'))
            .map(cb => parseInt(cb.value));

        if (!vehicleId || packageIds.length === 0) {
            showToast('Please select vehicle and packages', 'error');
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/delivery/assign`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ vehicleId: parseInt(vehicleId), packageIds })
            });

            if (!res.ok) {
                const error = await res.json();
                throw new Error(error.message);
            }

            showToast('Packages assigned successfully!', 'success');
            setTimeout(() => {
                loadAll();
                e.target.reset();
            }, 1000);
        } catch (e) {
            showToast(e.message, 'error');
        }
    });
}

function showToast(message, type) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    setTimeout(() => toast.classList.remove('show'), 5000);
}
