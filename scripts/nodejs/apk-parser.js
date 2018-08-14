const parseApk = require('apk-parser');

var apkFile = "/my-latest.apk";

// Parse apk manifest.
parseApk(apkFile, 8 * 1024 * 1024, function (err, data) {
    // Handle error or do something with data.
    // console.log('parse apk = ', util.inspect(data.manifest[0], true, null))
    console.log('parse apk error = ' + err)

    for (var i = 0; i < data.manifest[0]['application'][0]['activity'].length; i++) {
        var activity = data.manifest[0]['application'][0]['activity'][i]
        if (activity['intent-filter'] && activity['intent-filter'].length > 0) {
            var datas = []
            for (var j = 0; j < activity['intent-filter'].length; j++) {
                var filter = activity['intent-filter'][j]
                if (filter['data']) {
                    datas.push(filter)
                }
            }
            if (datas.length > 0) {
                console.log("<activity android:name=\"" + activity['@android:name'] + "\">")
                datas.forEach(element => {
                    console.log("    <intent-filter>")
                    element['data'].forEach(data => {
                        Object.keys(data).forEach(key => {
                            console.log("        <data " + key + "=\"" + data[key] + "\" />")
                        })
                    });
                    console.log("    </intent-filter>")
                });
                console.log("</activity>")
                console.log("")
                console.log("")
            }
        }
    }
});