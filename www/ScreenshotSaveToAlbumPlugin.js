var exec = require('cordova/exec'); formats = ['png','jpg'];

module.exports = {
	save:function(callback,format,quality, filename) {
		format = (format || 'png').toLowerCase();
		filename = filename || 'screenshot_'+Math.round((+(new Date()) + Math.random()));
		if(formats.indexOf(format) === -1){
			return callback && callback(new Error('invalid format '+format));
		}
		quality = typeof(quality) !== 'number'?100:quality;
		exec(function(res){
			callback && callback(null,res);
		}, function(error){
			callback && callback(error);
		}, "ScreenshotSaveToAlbumPlugin", "saveScreenshot", [format, quality, filename]);
	},
	saveToAlbum:function(callback) {
		
		exec(function(res){
			callback && callback(null,res);
		}, function(error){
			callback && callback(error);
		}, "ScreenshotSaveToAlbumPlugin", "saveScreenshotToAlbum", []);
	},
	URI:function(callback, quality){
		quality = typeof(quality) !== 'number'?100:quality;
		exec(function(res){
			callback && callback(null, res);
		}, function(error){
			callback && callback(error);
		}, "ScreenshotSaveToAlbumPlugin", "getScreenshotAsURI", [quality]);

	},

	URISync:function(callback,quality){
		var method = navigator.userAgent.indexOf("Android") > -1 ? "getScreenshotAsURISync" : "getScreenshotAsURI";
		quality = typeof(quality) !== 'number'?100:quality;
		exec(function(res){
			callback && callback(null,res);
		}, function(error){
			callback && callback(error);
		}, "ScreenshotSaveToAlbumPlugin", method, [quality]);
	}
};