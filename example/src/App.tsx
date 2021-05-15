import * as React from 'react';

import { StyleSheet, View, Button, Text } from 'react-native';
import { printHtml, printUrl } from 'wix-react-native-print';

export default function App() {
  const [error, setError] = React.useState<any>(null);
  const printHtmlHandler = React.useCallback(async () => {
    try {
      await printHtml({
        htmlString:
          '<html><head><title>Test</title><body><h1>Hello, Andrii</h1></body></head></html>',
      });
      setError(null);
    } catch (e) {
      setError(e);
    }
  }, []);
  const printPdfHandler = React.useCallback(async () => {
    try {
      await printUrl({
        url: 'http://www.orimi.com/pdf-test.pdf',
      });
      setError(null);
    } catch (e) {
      setError(e);
    }
  }, []);
  const printImageHandler = React.useCallback(async (url) => {
    try {
      await printUrl({ url });
      setError(null);
    } catch (e) {
      setError(e);
    }
  }, []);

  return (
    <View style={styles.container}>
      <View style={styles.btnContainer}>
        <Button title={'Print HTML'} onPress={printHtmlHandler} />
      </View>
      <View style={styles.btnContainer}>
        <Button title={'Print PDF from URL'} onPress={printPdfHandler} />
      </View>
      <View style={styles.btnContainer}>
        <Button
          title={'Print PNG from URL'}
          onPress={() =>
            printImageHandler(
              'https://wiesmann.codiferes.net/share/bitmaps/test_pattern.png'
            )
          }
        />
      </View>
      <View style={styles.btnContainer}>
        <Button
          title={'Print JPG from URL'}
          onPress={() =>
            printImageHandler(
              'https://wiesmann.codiferes.net/share/bitmaps/test_pattern.jpg'
            )
          }
        />
      </View>
      <View style={styles.btnContainer}>
        <Button
          title={'Print GIF from URL'}
          onPress={() =>
            printImageHandler(
              'https://wiesmann.codiferes.net/share/bitmaps/test_pattern.gif'
            )
          }
        />
      </View>
      <View style={styles.btnContainer}>
        <Button
          title={'Print unsupported file extension from URL'}
          onPress={() =>
            printImageHandler(
              'https://wiesmann.codiferes.net/share/bitmaps/test_pattern.svg'
            )
          }
        />
      </View>
      <View style={styles.btnContainer}>
        <Button
          title={'Print corrupted file from URL'}
          onPress={() =>
            printImageHandler('https://wiesmann.codiferes.net/share')
          }
        />
      </View>
      <View style={styles.errorContainer}>
        <Text>{error?.toString()}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'stretch',
    justifyContent: 'center',
  },
  btnContainer: {
    margin: 20,
  },
  errorContainer: {
    margin: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  item: {
    marginTop: 20,
  },
});
