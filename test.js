const https = require('https');
https.get('https://www.cricbuzz.com/cricket-match/live-scores', (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
        let searchStr = '\\"typeMatches\\":[';
        let idx = data.indexOf(searchStr);
        if (idx === -1) {
            searchStr = '"typeMatches":[';
            idx = data.indexOf(searchStr);
        }
        if (idx !== -1) {
            let startIdx = idx + searchStr.length - 1;
            let brackets = 0;
            let endIdx = -1;
            let inString = false;
            let escape = false;
            for (let i = startIdx; i < data.length; i++) {
                let c = data[i];
                if (escape) {
                    escape = false;
                    continue;
                }
                if (c === '\\') {
                    escape = true;
                    continue;
                }
                if (c === '"') {
                    inString = !inString;
                    continue;
                }
                if (!inString) {
                    if (c === '[') brackets++;
                    else if (c === ']') brackets--;
                    if (brackets === 0) {
                        endIdx = i;
                        break;
                    }
                }
            }
            if (endIdx !== -1) {
                let jsonStr = data.substring(startIdx, endIdx + 1);
                jsonStr = jsonStr.replace(/\\"/g, '"').replace(/\\\\/g, '\\');
                try {
                    let arr = JSON.parse(jsonStr);
                    console.log("Parsed array of length " + arr.length);
                    console.log("First item matchType:", arr[0].matchType);
                } catch (e) {
                    console.error("Error parsing JSON", e);
                }
            } else {
                console.log("End index not found");
            }
        }
    });
});
