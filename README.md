cordova-screenshot-save-to-album
==================


The Screenshot plugin allows your application to take screenshots of the current screen and save them into the phone album. 

## how to install

install it via cordova cli

```
cordova plugin add https://github.com/zhujin001032/cordova-screenshot-to-album.git
```

notice:
in iOS, only jpg format is supported
in Android, the default WebView and [Crosswalk](https://crosswalk-project.org/documentation/cordova.html) are both supported

## usage
import { Screenshot } from '@ionic-native/screenshot/ngx';
constructor(
private screenshot: Screenshot)
{

```js
this.screenshot.saveToAlbum().then(onSuccess => {
console.log("success", onSuccess.filePath);
}, onError => {
console.log("fail");
});
```
take screenshot with jpg and custom quality
```js
this.screenshot.save(function(error,res){
if(error){
console.error(error);
}else{
console.log('ok',res.filePath);
}
},'jpg',50);
```

define a filename
```js
this.screenshot.screenshot.save(function(error,res){
if(error){
console.error(error);
}else{
console.log('ok',res.filePath); //should be path/to/myScreenshot.jpg
}
},'jpg',50,'myScreenShot');
```

screenshot files are stored in /sdcard/Pictures for android.

take screenshot and get it as Data URI
```js
this.screenshot.screenshot.URI(function(error,res){
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
