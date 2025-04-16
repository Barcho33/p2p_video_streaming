const { app, BrowserWindow } = require('electron');
const path = require('path');

function createWindow() {
  const hostname = process.argv[2] || 'localhost';
  const videoId = process.argv[3] || 'default';

  const win = new BrowserWindow({
    width: 1280,
    height: 720,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
    }
  });

  win.loadFile('index.html', {
    query: {
      hostname: hostname,
      videoId: videoId
    }
  });
}

app.whenReady().then(() => {
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});
