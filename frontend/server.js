// Zero-dependency static file server for local frontend development.
// Deliberately not using a devDependency (http-server, live-server, ...) so
// `npm install` isn't a prerequisite for a lab partner to run this.
const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');

const PORT = process.env.PORT ? Number(process.env.PORT) : 3000;
const ROOT = __dirname;

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
};

const server = http.createServer((req, res) => {
  let requestedPath;
  try {
    requestedPath = decodeURIComponent(req.url.split('?')[0]);
  } catch {
    // Malformed percent-encoding (e.g. /%E0) would otherwise throw and kill the server.
    res.writeHead(400, { 'Content-Type': 'text/plain' });
    res.end('Bad request');
    return;
  }
  const relativePath = requestedPath === '/' ? '/index.html' : requestedPath;
  const filePath = path.normalize(path.join(ROOT, relativePath));

  // Guard against path traversal outside the frontend root (including sibling
  // folders whose names merely start with the root's name, e.g. ../frontend2).
  if (!filePath.startsWith(ROOT + path.sep)) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    res.end('Forbidden');
    return;
  }

  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('Not found');
      return;
    }
    const ext = path.extname(filePath);
    res.writeHead(200, { 'Content-Type': MIME_TYPES[ext] || 'application/octet-stream' });
    res.end(data);
  });
});

server.listen(PORT, () => {
  console.log(`FD frontend dev server running at http://localhost:${PORT}`);
});
