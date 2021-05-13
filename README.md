# wix-react-native-print

Printing library

## Installation

```sh
npm install wix-react-native-print
```

#### iOS

> TBD

#### Android
- Edit `android/settings.gradle` to included

```java
include ':wixreactnativeprint'
project(':wixreactnativeprint').projectDir = new File(rootProject.projectDir,'../node_modules/wix-react-native-print/android')
```

- Edit `android/app/build.gradle` file to include

```java
dependencies {
  ....
  implementation project(':wixreactnativeprint')

}
```

- Edit `MainApplication.java` to include

```java
// import the package
import com.wixreactnativeprint.WixReactNativePrintPackage;

// include package
packages.add(new WixReactNativePrintPackage());
```

## Usage

#### Supported file extensions

>  "pdf", "png", "jpg", "jpeg", "gif"

```js
import WixReactNativePrint from "wix-react-native-print";

// print PDF file
await WixReactNativePrint.print({
  isLandscape: true,
  url: "https://static.wix.com/doc.pdf",
});

// print image file
await WixReactNativePrint.print({
  url: "https://static.wix.com/image.png",
});

// print html file
await WixReactNativePrint.print({
  htmlString: "<html><head><title>Test</title><body>Test</body></head></html>",
});
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
