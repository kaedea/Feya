var express = require('express');
var AdmZip = require('adm-zip');
var fs = require('fs'),  request = require('request');
var md5File = require('md5-file')

var download = function(uri, filename, callback){
  request.head(uri, function(err, res, body){
    // console.log('content-type:', res.headers['content-type']);
    // console.log('content-length:', res.headers['content-length']);
    request(uri).pipe(fs.createWriteStream(filename)).on('close', callback);
  });
};


var app = express();
app.get('/suck', function(req, res){
  var apkUrl = req.query.apkUrl;
  console.log('apk url = ' + apkUrl);

  if (apkUrl.endsWith('.apk')) {
    console.log('download apk...');
    // Download apk.
    download(apkUrl, './temp/apks/temp.apk', function(){
        console.log('done');

        var apkId = 'Unspecific';
        var stats = fs.statSync('./temp/apks/temp.apk')
        var fileSize = stats.size
        var md5 = md5File.sync('./temp/apks/temp.apk')

        // Extract.
        var zip = new AdmZip("./temp/apks/temp.apk");
        zip.extractEntryTo("META-INF/CERT.SF", "./temp", false, true);

        var lineReader = require('line-reader');
        var symbol = 'SHA1-Digest-Manifest:'
        lineReader.eachLine('./temp/CERT.SF', function(line, last) {
            if (line.indexOf(symbol) !=-1) {
              apkId = line.substring(line.indexOf(symbol) + symbol.length);
              console.log('manifest id = ' + apkId)

              // Output.
              res.send('APK_ID = ' + apkId.trim() +
                      '<br> APK_MD5 = ' + md5 +
                      '<br> APK_SIZE = ' + fileSize +
                      '<br> APK_URL = ' + apkUrl)
              return false;
            }
        });
    });
  }
});

app.listen(3000);
