const https = require('https');
https.get('https://www.cricbuzz.com/cricket-match/live-scores', (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
        let match = data.match(/\\"typeMatches\\":\[(.*?)]\}\}\]/);
        if(match) {
            let json = JSON.parse('{"t":[' + match[1].replace(/\\"/g, '"').replace(/\\\\/g, '\\') + ']}');
            let firstMatch = json.t[0].seriesMatches[0].matches[0];
            console.log(JSON.stringify(firstMatch.matchScore, null, 2));
        }
    });
});
