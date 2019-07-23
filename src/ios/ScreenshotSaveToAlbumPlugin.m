/********* ScreenshotSaveToAlbumPlugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <Photos/Photos.h>

@interface ScreenshotSaveToAlbumPlugin : CDVPlugin {
  // Member variables go here.
}

- (void)coolMethod:(CDVInvokedUrlCommand*)command;
@end

@implementation ScreenshotSaveToAlbumPlugin

CGFloat statusBarHeight()
{
    CGSize statusBarSize = [[UIApplication sharedApplication] statusBarFrame].size;
    return MIN(statusBarSize.width, statusBarSize.height);
}

- (UIImage *)getScreenshot
{
    UIWindow *keyWindow = [[UIApplication sharedApplication] keyWindow];
    CGRect rect = [keyWindow bounds];
    UIGraphicsBeginImageContextWithOptions(rect.size, YES, 0);
    [keyWindow drawViewHierarchyInRect:keyWindow.bounds afterScreenUpdates:NO];
    UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    // cut the status bar from the screenshot
    CGRect smallRect = CGRectMake (0,statusBarHeight()*img.scale,rect.size.width*img.scale,rect.size.height*img.scale);
    
    CGImageRef subImageRef = CGImageCreateWithImageInRect(img.CGImage, smallRect);
    CGRect smallBounds = CGRectMake(0,0,CGImageGetWidth(subImageRef), CGImageGetHeight(subImageRef));
    
    UIGraphicsBeginImageContext(smallBounds.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextDrawImage(context,smallBounds,subImageRef);
    UIImage* cropped = [UIImage imageWithCGImage:subImageRef];
    UIGraphicsEndImageContext();
    
    CGImageRelease(subImageRef);
    
    return cropped;
}

- (void)saveScreenshot:(CDVInvokedUrlCommand*)command
{
    NSString *filename = [command.arguments objectAtIndex:2];
    NSNumber *quality = [command.arguments objectAtIndex:1];
    
    NSString *path = [NSString stringWithFormat:@"%@.jpg",filename];
    NSString *jpgPath = [NSTemporaryDirectory() stringByAppendingPathComponent:path];
    
    UIImage *image = [self getScreenshot];
    NSData *imageData = UIImageJPEGRepresentation(image,[quality floatValue]);
    [imageData writeToFile:jpgPath atomically:NO];
    
    CDVPluginResult* pluginResult = nil;
    NSDictionary *jsonObj = [ [NSDictionary alloc]
                             initWithObjectsAndKeys :
                             jpgPath, @"filePath",
                             @"true", @"success",
                             nil
                             ];
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:jsonObj];
    NSString* callbackId = command.callbackId;
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}
- (void)saveScreenshotToAlbum:(CDVInvokedUrlCommand*)command
{
    UIImage *image = [self getScreenshot];
    NSMutableArray *imageIds = [NSMutableArray array];
    [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
        
        //写入图片到相册
        PHAssetChangeRequest *req = [PHAssetChangeRequest creationRequestForAssetFromImage:image];
        //记录本地标识，等待完成后取到相册中的图片对象
        [imageIds addObject:req.placeholderForCreatedAsset.localIdentifier];
        
        
    } completionHandler:^(BOOL success, NSError * _Nullable error) {
        
        NSLog(@"success = %d, error = %@", success, error);
        
        if (success)
        {
            //成功后取相册中的图片对象
            __block PHAsset *imageAsset = nil;
            PHFetchResult *result = [PHAsset fetchAssetsWithLocalIdentifiers:imageIds options:nil];
            [result enumerateObjectsUsingBlock:^(PHAsset * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
                
                imageAsset = obj;
                *stop = YES;
                
            }];
            
            if (imageAsset)
            {
                //加载图片数据
                [[PHImageManager defaultManager] requestImageDataForAsset:imageAsset
                                                                  options:nil
                                                            resultHandler:^(NSData * _Nullable imageData, NSString * _Nullable dataUTI, UIImageOrientation orientation, NSDictionary * _Nullable info) {
                                                                NSURL *fileUrl = [info objectForKey:@"PHImageFileURLKey"];
                                                                CDVPluginResult* pluginResult = nil;
                                                                NSDictionary *jsonObj = [ [NSDictionary alloc]
                                                                                         initWithObjectsAndKeys :
                                                                                         @"true", @"success",
                                                                                         fileUrl.absoluteString, @"filePath",
                                                                                         nil
                                                                                         ];
                                                                
                                                                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:jsonObj];
                                                                NSString* callbackId = command.callbackId;
                                                                [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
                                                                
                                                            }];
            }
        }
        
    }];
}
- (void) getScreenshotAsURI:(CDVInvokedUrlCommand*)command
{
    NSNumber *quality = command.arguments[0];
    UIImage *image = [self getScreenshot];
    NSData *imageData = UIImageJPEGRepresentation(image,[quality floatValue]);
    NSString *base64Encoded = [imageData base64EncodedStringWithOptions:0];
    NSDictionary *jsonObj = @{
                              @"URI" : [NSString stringWithFormat:@"data:image/jpeg;base64,%@", base64Encoded]
                              };
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:jsonObj];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:[command callbackId]];
}
@end
