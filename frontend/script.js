const API_URL = 'http://localhost:8081/api';

const btnFail = document.getElementById('btnFail');
const btnDetect = document.getElementById('btnDetect');
const btnReset = document.getElementById('btnReset');
const nodesContainer = document.getElementById('nodesContainer');
const logsContainer = document.getElementById('logs');

// Estado actual
let lastLogsCount = 0;

async function fetchState() {
    try {
        const response = await fetch(`${API_URL}/state`);
        const data = await response.json();
        renderNodes(data.procesos);
        renderLogs(data.logs);
        
        // Logica para habilitar botones
        const p5 = data.procesos.find(p => p.id === 5);
        if (!p5.activo && p5.esCoordinador === false) {
            btnFail.disabled = true;
            // Solo habilitar si no se ha elegido un nuevo coordinador (o sea, si no hay nadie mas como coord)
            const hasCoord = data.procesos.some(p => p.id !== 5 && p.esCoordinador);
            btnDetect.disabled = hasCoord;
        } else {
            btnFail.disabled = false;
            btnDetect.disabled = true;
        }

    } catch (error) {
        console.error('Error fetching state:', error);
    }
}

function renderNodes(procesos) {
    nodesContainer.innerHTML = '';
    procesos.forEach(p => {
        const node = document.createElement('div');
        let classes = ['node'];
        if (p.esCoordinador) classes.push('coordinator');
        if (p.activo) classes.push('active');
        else classes.push('inactive');

        node.className = classes.join(' ');
        
        node.innerHTML = `
            <div>P${p.id}</div>
            <div class="status">${p.esCoordinador ? 'Líder' : (p.activo ? 'Activo' : 'Caído')}</div>
        `;
        nodesContainer.appendChild(node);
    });
}

function renderLogs(logs) {
    if (logs.length > lastLogsCount) {
        logsContainer.innerHTML = '';
        logs.forEach(log => {
            const el = document.createElement('div');
            el.className = 'log-entry';
            el.textContent = log;
            logsContainer.appendChild(el);
        });
        logsContainer.scrollTop = logsContainer.scrollHeight;
        lastLogsCount = logs.length;
    } else if (logs.length === 0) {
        logsContainer.innerHTML = '';
        lastLogsCount = 0;
    }
}

async function handleAction(endpoint) {
    try {
        await fetch(`${API_URL}/${endpoint}`, { method: 'POST' });
        fetchState();
    } catch (error) {
        console.error(`Error executing ${endpoint}:`, error);
    }
}

btnFail.addEventListener('click', () => handleAction('fail'));
btnDetect.addEventListener('click', () => handleAction('detect'));
btnReset.addEventListener('click', () => handleAction('reset'));

// Initial fetch y polling simple cada 1 segundo
fetchState();
setInterval(fetchState, 1000);
