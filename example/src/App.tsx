import * as React from 'react';

import { StyleSheet, View, Text, TouchableOpacity } from 'react-native';
import WixReactNativePrint from 'wix-react-native-print';

export default function App() {
  const onPressHandler = React.useCallback(() => {
    WixReactNativePrint.print({html: '<html><head><title>Test</title><body>Test</body></head></html>'});
  }, []);

  return (
    <View style={styles.container}>
      <TouchableOpacity onPress={onPressHandler}><Text>Press</Text></TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
