import React, { useCallback } from 'react';
import { Alert, Pressable, Text, StyleSheet } from 'react-native';
import { PermissionsAndroid } from 'react-native';

const CameraButton = ({ onPress }: { onPress: () => void }) => {
  const requestPermissions = useCallback(async () => {
    try {
      const result = await PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.CAMERA,
        PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
      ]);

      const granted =
        result[PermissionsAndroid.PERMISSIONS.CAMERA] === PermissionsAndroid.RESULTS.GRANTED &&
        result[PermissionsAndroid.PERMISSIONS.RECORD_AUDIO] === PermissionsAndroid.RESULTS.GRANTED;

      if (!granted) {
        Alert.alert('Permissions required', 'Camera and microphone permissions are needed.');
      } else {
        onPress();
      }
    } catch (e) {
      console.warn('Permission request failed:', e);
    }
  }, [onPress]);

  return (
    <Pressable
      style={[styles.button, styles.primary]}
      onPress={requestPermissions}>
      <Text style={[styles.buttonText, styles.primaryText]}>Open Native CameraActivity</Text>
    </Pressable>
  );
};

const styles = StyleSheet.create({
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

export default CameraButton;
