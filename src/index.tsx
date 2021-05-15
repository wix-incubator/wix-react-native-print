import { NativeModules } from 'react-native';

interface PrintOptions {
  isLandscape?: boolean;
}

interface PrintHtml extends PrintOptions {
  htmlString: string;
}

interface PrintUrl extends PrintOptions {
  url: string;
}

interface WixReactNativePrintType {
  printHtml(options: PrintHtml): Promise<any>;
  printUrl(options: PrintUrl): Promise<any>;
}

const { WixReactNativePrint } = NativeModules;
const { printHtml, printUrl } = WixReactNativePrint as WixReactNativePrintType;

export { printHtml, printUrl };
