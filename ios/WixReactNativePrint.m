#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(WixReactNativePrint, NSObject)

RCT_EXTERN_METHOD(printUrl:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(
                  printHtml:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

@end
