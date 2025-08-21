// src/components/CameraComponent.tsx
import React from 'react';
import { View, StyleSheet, Pressable, Text, NativeModules, Alert, Platform } from 'react-native';
import { check, request, PERMISSIONS, RESULTS } from 'react-native-permissions';

const { ARGearBridge } = NativeModules as {
  ARGearBridge?: { startCameraActivity: () => void };
};

const CameraComponent = () => {
  const openARGearCamera = async () => {
    try {
      const permission =
        Platform.OS === 'android'
          ? PERMISSIONS.ANDROID.CAMERA
          : PERMISSIONS.IOS.CAMERA;

      const status = await check(permission);

      if (status === RESULTS.GRANTED) {
        ARGearBridge?.startCameraActivity();
      } else {
        const result = await request(permission);
        if (result === RESULTS.GRANTED) {
          ARGearBridge?.startCameraActivity();
        } else {
          Alert.alert(
            'Camera Permission',
            'Camera permission is required to use AR filters.'
          );
        }
      }
    } catch (e) {
      console.warn('Permission check failed', e);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Open ARGear Camera</Text>
      <Pressable style={styles.button} onPress={openARGearCamera}>
        <Text style={styles.buttonText}>Start AR Filters</Text>
      </Pressable>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#000',
  },
  title: {
    fontSize: 20,
    fontWeight: '600',
    color: '#fff',
    marginBottom: 20,
  },
  button: {
    paddingVertical: 14,
    paddingHorizontal: 20,
    backgroundColor: '#0A84FF',
    borderRadius: 12,
  },
  buttonText: {
    color: '#fff',
    fontWeight: '600',
  },
});

export default CameraComponent;
