const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 9229;
// ��������� �����, ��� ����� index.html � ���������. '.' � ��� ������� �����.
const ROOT_DIR = path.join(__dirname, '.');

const MIME = {
    '.html': 'text/html',
    '.css': 'text/css',
    '.js': 'application/javascript',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon'
};

const server = http.createServer((req, res) => {
    let filePath = path.join(ROOT_DIR, req.url === '/' ? 'index.html' : req.url);

    // ���������� API-��������, ����� fetch �� �������
    if (req.url.startsWith('/api/')) {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        if (req.url === '/api/navigation') {
            res.end(JSON.stringify([]));
        } else if (req.url.startsWith('/api/levels/')) {
            res.end(JSON.stringify({ tutorial: null, subtasks: [] }));
        } else {
            res.writeHead(404);
            res.end();
        }
        return;
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME[ext] || 'application/octet-stream';

    fs.readFile(filePath, (err, content) => {
        if (err) {
            if (err.code === 'ENOENT') {
                res.writeHead(404, { 'Content-Type': 'text/plain' });
                res.end('���� �� ������');
            } else {
                res.writeHead(500);
                res.end('������ �������');
            }
        } else {
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(content);
        }
    });
});

server.listen(PORT, () => {
    console.log(`������ �������: http://localhost:${PORT}`);
});