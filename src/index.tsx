import { NativeModules } from 'react-native';

type PrintOptionsType = {
  printerURL?: string;
  isLandscape?: boolean;
  jobName?: string;
} & ({ html: string } | { filePath: string });


type WixReactNativePrintType = {
  print(options: PrintOptionsType): Promise<any>;
};

const { WixReactNativePrint } = NativeModules;

export default WixReactNativePrint as WixReactNativePrintType;
