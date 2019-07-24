cordova-screenshot-save-to-album
==================


The Screenshot plugin allows your application to take screenshots of the current screen and save them into the phone album. 

## how to install

install it via cordova cli

```
cordova plugin add https://github.com/zhujin001032/cordova-screenshot-to-album.git
or
ionic cordova plugin add com.jasonhe.cordova.screenshot
```

notice:
in iOS, only jpg format is supported
in Android, the default WebView and [Crosswalk](https://crosswalk-project.org/documentation/cordova.html) are both supported

## usage
after import
declare let cordova;

```js
cordova.plugins.Screenshot.saveToAlbum( (error, onSuccess) => {
if (error) {
console.error(error);
} else {
console.log('success', onSuccess.filePath);
}
});
```
take screenshot with jpg and custom quality
```js
cordova.plugins.Screenshot.save((error, onSuccess) => {
if (error) {
console.error(error);
} else {
console.log('success', onSuccess.filePath);
}
},'jpg',50);
```

define a filename
```js
cordova.plugins.Screenshot.screenshot.save((error, onSuccess) => {
if (error) {
console.error(error);
} else {
console.log('success', onSuccess.filePath);
}
},'jpg',50,'myScreenShot');
```

screenshot files are stored in /sdcard/Pictures for android.

take screenshot and get it as Data URI
```js
cordova.plugins.Screenshot.URI(function(error,res){
if(error){
console.error(error);
}else{
html = '<img style="width:50%;" src="'+res.URI+'">';
document.body.innerHTML = html;
}
},50);
```

## Known Issue
### in Android platform I receive the black image with crosswalk 
#### solution: 

add this line ``<preference name="CrosswalkAnimatable" value="true" />`` in config.xml, 
