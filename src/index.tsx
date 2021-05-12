import { NativeModules } from 'react-native';

type WixReactNativePrintType = {
  multiply(a: number, b: number): Promise<number>;
};

const { WixReactNativePrint } = NativeModules;

export default WixReactNativePrint as WixReactNativePrintType;
