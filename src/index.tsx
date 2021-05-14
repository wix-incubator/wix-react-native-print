import { NativeModules } from 'react-native';

interface PrintOptions {
  isLandscape?: boolean;
}

interface PrintHtml extends PrintOptions {
  htmlString: string
}

interface PrintUrl extends PrintOptions {
  url: string
}

interface WixReactNativePrintType {
  printHtml(options: PrintHtml): Promise<any>;
  printUrl(options: PrintUrl): Promise<any>;
}

const { WixReactNativePrint } = NativeModules;

export default WixReactNativePrint as WixReactNativePrintType;

