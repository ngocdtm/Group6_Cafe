import { Injectable } from '@angular/core';
import { environment as webEnvironment } from 'src/environments/environment.web';
import { environment as mobileEnvironment } from 'src/environments/environment.mobile';

@Injectable({
  providedIn: 'root'
})
export class PlatformService {
  private environment: any;

  constructor() {
    // Kiểm tra nếu đang chạy trên mobile dựa vào URL hoặc User Agent
    this.environment = this.isMobileDevice() ? mobileEnvironment : webEnvironment;
    console.log('Selected Environment:', this.environment);
  }

  private isMobileDevice(): boolean {
    console.log('Hostname:', window.location.hostname); // Log hostname
    console.log('Protocol:', window.location.protocol); // Log protocol
    const isAndroidStudio = window.location.hostname === '10.0.2.2' || window.location.protocol === 'capacitor:';
    if (isAndroidStudio) {
        return true;
    }
    const userAgent = window.navigator.userAgent.toLowerCase();
    const mobileKeywords = ['android', 'webos', 'iphone', 'ipad', 'ipod', 'blackberry', 'windows phone'];
    const result = mobileKeywords.some(keyword => userAgent.includes(keyword));
    console.log('User Agent:', userAgent, 'Detected Mobile:', result);
    return result;
  }
  
  
  getApiUrl() {
    console.log('Detected API URL:', this.environment.apiUrl);
    return this.environment.apiUrl;
  }

  getImageUrl() {
    return this.environment.imageUrl;
  }

}