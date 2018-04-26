const express = require('express');
const request = require('request');
const fs = require('fs');
const apkParser = require('apk-parser');
const APK_LIST_URL = 'http://get.latest.apk/api';

// Temp dir
const tempDir = './temp';
try {
    fs.accessSync(tempDir);
} catch (e) {
    fs.mkdirSync(tempDir);
}

// Downloader
const download = function (uri, filename, callback) {
    request.head(uri, function (err, res, body) {
        request(uri).pipe(fs.createWriteStream(filename)).on('close', callback);
    });
};

// Epxiress service
const app = express();
const port = 3002;
app.get('/apk-intent-filters', function (req, res) {
    console.log('Getting reuqest client request...');

    if (req.query.apkUrl) {
        var apkUrl = req.query.apkUrl;
        console.log('Geting apk, url = ' + apkUrl);
        parseApk(apkUrl, res)

    } else {
        // Request latest apk
        console.log('Geting latest apk from macross...');
        request(APK_LIST_URL, function (error, response, body) {
            if (error != null || response == null || response.statusCode != 200) {
                // Error
                console.log('error:', error); // Print the error if one occurred
                console.log('statusCode:', response && response.statusCode); // Print the response status code if a response was received
                return
            }
            var response = JSON.parse(body)
            if (response == null || response['code'] != 0 || response['data'] == null || response['data'][0] == null || response['data'][0]['inet_addr'] == null) {
                // Response data error
                console.log('body:', body); // Print the HTML for the Google homepage.
                return
            }

            // Surrcess
            var apkUrl = response['data'][0]['inet_addr']
            parseApk(apkUrl, res)
        });
    }


});
app.listen(port);

// Parese apk
parseApk = (apkUrl, res) => {
    if (apkUrl.startsWith('http')) {
        console.log('Download file...');
        // Download apk.
        var apkFile = tempDir + '/temp.apk';
        download(apkUrl, apkFile, function () {
            console.log('Done');
            console.log('Parse apk manifest...');

            // Parse apk manifest.
            apkParser(apkFile, 8 * 1024 * 1024, function (err, data) {
                if (err) {
                    console.log('parse apk error = ' + err)
                    res.end('parse apk error = ' + err);
                    return
                }
                var outterActivities = []
                var activities = []
                for (var i = 0; i < data.manifest[0]['application'][0]['activity'].length; i++) {
                    var activity = data.manifest[0]['application'][0]['activity'][i]
                    if (activity['intent-filter'] && activity['intent-filter'].length > 0) {
                        // tag <activity>
                        var datas = []
                        for (var j = 0; j < activity['intent-filter'].length; j++) {
                            var filter = activity['intent-filter'][j]
                            if (filter['data']) {
                                datas.push(filter)
                            }
                        }
                        if (datas.length > 0) {
                            var activityItem = {}
                            activityItem['activity-name'] = activity['@android:name']
                            activityItem['itent-filter'] = []
                            console.log("<activity android:name=\"" + activity['@android:name'] + "\">")
                            datas.forEach(element => {
                                // tag <intent-filter>
                                var intentFilter = {}
                                var intentFilterDatas = []
                                console.log("    <intent-filter>")
                                element['data'].forEach(data => {
                                    // tag <data>
                                    var intentFilterDatasItem = {}
                                    Object.keys(data).forEach(key => {
                                        intentFilterDatasItem[key] = data[key]
                                        console.log("        <data " + key + "=\"" + data[key] + "\" />")
                                    })
                                    intentFilterDatas.push(intentFilterDatasItem)
                                });
                                intentFilter['data'] = intentFilterDatas
                                activityItem['itent-filter'].push(intentFilter)
                                console.log("    </intent-filter>")
                            });
                            outterActivities.push(activityItem)
                            console.log("</activity>")
                            console.log("")
                            console.log("")
                        }
                    }
                }
                // Output.
                res.writeHead(200, {
                    "Content-Type": "application/json"
                });
                var json = JSON.stringify(outterActivities);
                res.end(json);
            })
        });
    } else {
        console.log('apk url is invalid! url = ' + apkUrl);
        res.end('apk url is invalid!' + '<br> APK_URL = ' + apkUrl);
    }
}
