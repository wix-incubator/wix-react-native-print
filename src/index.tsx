import { NativeModules } from 'react-native';

type PrintOptionsType = {
  isLandscape?: boolean;
} & ({ htmlString: string } | { url: string });


type WixReactNativePrintType = {
  print(options: PrintOptionsType): Promise<any>;
};

const { WixReactNativePrint } = NativeModules;

export default WixReactNativePrint as WixReactNativePrintType;
