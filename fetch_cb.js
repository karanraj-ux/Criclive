const https = require('https');

https.get('https://www.cricbuzz.com/cricket-match/live-scores/recent-matches', (res) => {
    let data = '';
    res.on('data', chunk => { data += chunk; });
    res.on('end', () => {
        const match = data.match(/"typeMatches":\[.*?\],?"filters"/);
        if (match) {
            const jsonStr = '{' + match[0].replace(/,?"filters"$/, '') + '}';
            const obj = JSON.parse(jsonStr);
            for (const m of obj.typeMatches) {
                for (const sm of (m.seriesMatches || [])) {
                    if (sm.seriesAdWrapper) {
                        for (const mat of (sm.seriesAdWrapper.matches || [])) {
                            const info = mat.matchInfo || {};
                            const score = mat.matchScore || {};
                            console.log(info.team1?.teamName + ' vs ' + info.team2?.teamName);
                            console.log('Score:', JSON.stringify(score));
                        }
                    }
                }
            }
        }
    });
});
