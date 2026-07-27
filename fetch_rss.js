const https = require('https');
https.get('https://synd.cricbuzz.com/j2me/1.0/livematches.xml', (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => console.log(data));
});
