const express = require('express');
const AdmZip = require('adm-zip');
const fs = require('fs');
const request = require('request');
const md5File = require('md5-file');
const lineReader = require('line-reader');

/*
 * Simple service to read apk info with the given url. 
 */

const download = function (uri, filename, callback) {
    request.head(uri, function (err, res, body) {
        // console.log('content-type:', res.headers['content-type']);
        // console.log('content-length:', res.headers['content-length']);
        request(uri).pipe(fs.createWriteStream(filename)).on('close', callback);
    });
};

// Temp dir.
const tempDir = './temp';
try {
    fs.accessSync(tempDir);
} catch (e) {
    fs.mkdirSync(tempDir);
}

const app = express();
const port = 3000;
app.get('/suck', function (req, res) {
    var apkUrl = req.query.apkUrl;
    console.log('apk url = ' + apkUrl);

    if (apkUrl.startsWith('http')) {
        console.log('download file...');
        // Download apk.
        var apkFile = tempDir + '/temp.apk';
        download(apkUrl, apkFile, function () {
            console.log('done');

            var apkId = 'Unspecific';
            var stats = fs.statSync(apkFile)
            var fileSize = stats.size
            var md5 = md5File.sync(apkFile)

            // Extract.
            var zip = new AdmZip(apkFile);
            zip.extractEntryTo("META-INF/CERT.SF", tempDir, false, true);
            var symbol = 'SHA1-Digest-Manifest:'
            lineReader.eachLine(tempDir + '/CERT.SF', function (line, last) {
                if (line.indexOf(symbol) != -1) {
                    apkId = line.substring(line.indexOf(symbol) + symbol.length);
                    console.log('manifest id = ' + apkId)

                    // Output.
                    res.writeHead(200, {
                        "Content-Type": "application/json"
                    });
                    var json = JSON.stringify({
                        apk_id: apkId.trim(),
                        apk_md5: md5,
                        apk_size: fileSize,
                        apk_url: apkUrl
                    });
                    res.end(json);
                    return false;
                }
            });
        });
    } else {
        console.log('apk url is invalid!');
        res.end('apk url is invalid!' + '<br> APK_URL = ' + apkUrl);
    }
});

app.listen(port);