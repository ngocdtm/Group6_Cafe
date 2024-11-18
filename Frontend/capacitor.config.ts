import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.coffeeshop.app',
  appName: 'Coffee Shop Manager',
  webDir: 'www',
  server: { 
    cleartext: true,
    androidScheme: 'http',
    allowNavigation: [
      'localhost',
      '10.0.2.2:8080',
      'http://10.0.2.2:8080/*',
      'http://localhost:8080/*',
       'capacitor://*'
    ]
  },
  android: {
    buildOptions: {
      keystorePath: 'android/app/keystore.jks',
      keystorePassword: 'yourpassword',
      keystoreAlias: 'key0',
      keystoreAliasPassword: 'yourpassword'
    }
  }
};

export default config;