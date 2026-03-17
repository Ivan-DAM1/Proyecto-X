const API_URL = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
    ? 'http://localhost:8081/api'
    : 'https://proyecto-x-v1-0-0-backend.onrender.com/api';

async function checkApiStatus() {
    const statusDiv = document.getElementById('api-status');
    if (!statusDiv) return;

    statusDiv.innerHTML = '<p class="text-gold">Verificando...</p>';

    try {
        const response = await fetch(`${API_URL}/health`);
        const data = await response.json();

        statusDiv.innerHTML = `
            <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded">
                <p class="font-bold">✓ Backend conectado</p>
                <p>Estado: ${data.status}</p>
                <p>Servicio: ${data.service}</p>
            </div>
        `;
    } catch (error) {
        statusDiv.innerHTML = `
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                <p class="font-bold">✗ Error de conexión</p>
                <p>${error.message}</p>
            </div>
        `;
    }
}

async function loadUsers() {
    const usersDiv = document.getElementById('users-list');
    if (!usersDiv) return;

    usersDiv.innerHTML = '<p class="text-gold">Cargando usuarios...</p>';

    try {
        const response = await fetch(`${API_URL}/users`);
        const users = await response.json();

        if (!Array.isArray(users) || users.length === 0) {
            usersDiv.innerHTML = '<p class="text-gray-500">No hay usuarios en la base de datos.</p>';
            return;
        }

        usersDiv.innerHTML = `
            <div class="space-y-2">
                ${users.map(user => `
                    <div class="bg-gray-50 p-3 rounded border border-gray-200">
                        <p class="font-semibold text-gray-800">${user.nombreCompleto || user.name}</p>
                        <p class="text-sm text-gray-600">${user.email}</p>
                    </div>
                `).join('')}
            </div>
        `;
    } catch (error) {
        usersDiv.innerHTML = `
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                <p class="font-bold">✗ Error al cargar usuarios</p>
                <p>${error.message}</p>
            </div>
        `;
    }
}

const userForm = document.getElementById('userForm');
if (userForm) {
    userForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const name = document.getElementById('userName').value;
        const email = document.getElementById('userEmail').value;

        try {
            const response = await fetch(`${API_URL}/users`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ nombreCompleto: name, email })
            });

            if (response.ok) {
                alert('Usuario agregado correctamente');
                userForm.reset();
                loadUsers();
            } else {
                alert('Error al agregar usuario');
            }
        } catch (error) {
            alert('Error: ' + error.message);
        }
    });
}

window.addEventListener('load', () => {
    checkApiStatus();
    loadUsers();
});
