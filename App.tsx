// App.tsx

import React, { useEffect, useCallback, useState } from 'react';
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
import CameraButton from './src/components/CameraButton';
import CameraComponent from './src/components/CameraComponent';  // Import the CameraComponent

type ARGearBridgeType = {
  exampleMethod: (message: string) => void;
  startCameraActivity: () => void;
};

const { ARGearBridge } = NativeModules as { ARGearBridge?: ARGearBridgeType };

function App() {
  const isDarkMode = useColorScheme() === 'dark';
  const [isCameraOpen, setIsCameraOpen] = useState(false);  // State to manage camera view

  useEffect(() => {
    console.log('ARGearBridge methods:', Object.keys(ARGearBridge || {}));
    ARGearBridge?.exampleMethod?.('Hello from JavaScript!');
  }, []);

  const handleOpenCamera = useCallback(() => {
    if (!ARGearBridge?.startCameraActivity) {
      Alert.alert('Module not linked', 'ARGearBridge is not available.');
      return;
    }

    try {
      ARGearBridge.startCameraActivity();
    } catch (e) {
      console.error('Failed to start CameraActivity:', e);
      Alert.alert('Error', 'Could not open the native camera screen.');
    }
  }, []);

  const handleOpenCameraComponent = useCallback(() => {
    // This will open the CameraComponent inside the app
    setIsCameraOpen(true);
  }, []);

  return (
    <SafeAreaProvider>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <AppContent
        handleOpenCamera={handleOpenCamera}
        handleOpenCameraComponent={handleOpenCameraComponent}
        isCameraOpen={isCameraOpen}  // Pass state to control camera component visibility
        setIsCameraOpen={setIsCameraOpen}  // Set state to close camera component if needed
      />
    </SafeAreaProvider>
  );
}

function AppContent({
  handleOpenCamera,
  handleOpenCameraComponent,
  isCameraOpen,
  setIsCameraOpen,
}: {
  handleOpenCamera: () => void;
  handleOpenCameraComponent: () => void;
  isCameraOpen: boolean;
  setIsCameraOpen: (value: boolean) => void;
}) {
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.container, { paddingTop: insets.top, paddingBottom: insets.bottom }]}>
      <Text style={styles.title}>ARGear RN Bridge Smoke Test</Text>

      <Pressable style={styles.button} onPress={() => ARGearBridge?.exampleMethod?.('Ping from RN')}>
        <Text style={styles.buttonText}>Test Bridge (Toast)</Text>
      </Pressable>

      <CameraButton onPress={handleOpenCamera} />

      {/* New Camera Component Button */}
      <Pressable style={styles.button} onPress={handleOpenCameraComponent}>
        <Text style={styles.buttonText}>Open Camera Component</Text>
      </Pressable>

      {/* Show Camera Component if the state is true */}
      {isCameraOpen && <CameraComponent />}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    gap: 16,
    paddingHorizontal: 16,
    justifyContent: 'center',
    backgroundColor: '#7bb6eaff',
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
    color: '#6ea2ecff',
  },
});

export default App;
