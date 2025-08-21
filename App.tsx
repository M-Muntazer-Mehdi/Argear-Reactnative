// App.tsx
/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, { useEffect, useCallback } from 'react';
import {
  Alert,
  PermissionsAndroid,
  Platform,
  Pressable,
  StatusBar,
  StyleSheet,
  Text,
  View,
  useColorScheme,
  NativeModules,
} from 'react-native';
import {
  SafeAreaProvider,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';

type ARGearBridgeType = {
  exampleMethod: (message: string) => void;
  startCameraActivity: () => void;
  initialize?: (apiUrl: string, apiKey: string, secretKey: string, authKey: string) => void;
};

const { ARGearBridge } = NativeModules as { ARGearBridge?: ARGearBridgeType };

function App() {
  const isDarkMode = useColorScheme() === 'dark';

  useEffect(() => {
    // See what methods the native module exposes
    console.log('ARGearBridge methods:', Object.keys(ARGearBridge || {}));

    // Quick smoke test (should show a Toast from native)
    ARGearBridge?.exampleMethod?.('Hello from JavaScript!');
  }, []);

  return (
    <SafeAreaProvider>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <AppContent />
    </SafeAreaProvider>
  );
}

function AppContent() {
  const insets = useSafeAreaInsets();

  const requestAndroidPermissions = useCallback(async () => {
    if (Platform.OS !== 'android') return true;

    try {
      const result = await PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.CAMERA,
        PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
        // If you capture/save media on API <=32 you might also need:
        // PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
      ]);

      const granted =
        result[PermissionsAndroid.PERMISSIONS.CAMERA] === PermissionsAndroid.RESULTS.GRANTED &&
        result[PermissionsAndroid.PERMISSIONS.RECORD_AUDIO] === PermissionsAndroid.RESULTS.GRANTED;

      if (!granted) {
        Alert.alert('Permissions required', 'Camera and microphone permissions are needed.');
      }
      return granted;
    } catch (e) {
      console.warn('Permission request failed:', e);
      return false;
    }
  }, []);

  const handleOpenCamera = useCallback(async () => {
    if (!ARGearBridge?.startCameraActivity) {
      Alert.alert('Module not linked', 'ARGearBridge is not available.');
      return;
    }
    const ok = await requestAndroidPermissions();
    if (!ok) return;

    try {
      ARGearBridge.startCameraActivity();
    } catch (e) {
      console.error('Failed to start CameraActivity:', e);
      Alert.alert('Error', 'Could not open the native camera screen.');
    }
  }, [requestAndroidPermissions]);

  return (
    <View style={[styles.container, { paddingTop: insets.top, paddingBottom: insets.bottom }]}>
      <Text style={styles.title}>ARGear RN Bridge Smoke Test</Text>

      <Pressable style={styles.button} onPress={() => ARGearBridge?.exampleMethod?.('Ping from RN')}>
        <Text style={styles.buttonText}>Test Bridge (Toast)</Text>
      </Pressable>

      <Pressable style={[styles.button, styles.primary]} onPress={handleOpenCamera}>
        <Text style={[styles.buttonText, styles.primaryText]}>Open Native CameraActivity</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    gap: 16,
    paddingHorizontal: 16,
    justifyContent: 'center',
    backgroundColor: '#fff',
  },
  title: {
    fontSize: 20,
    fontWeight: '600',
    textAlign: 'center',
    marginBottom: 8,
  },
  button: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 12,
    paddingVertical: 14,
    paddingHorizontal: 16,
    alignItems: 'center',
  },
  buttonText: {
    fontWeight: '600',
  },
  primary: {
    backgroundColor: '#0A84FF',
    borderColor: '#0A84FF',
  },
  primaryText: {
    color: '#fff',
  },
});

export default App;
