import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SwUpdate } from '@angular/service-worker';
import { OnlineStatusService } from './services/online-status.service';
import { NgModule } from '@angular/core';
import { Plugins } from '@capacitor/core';
const { Camera, Storage, Network, PushNotifications } = Plugins;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'Frontend';
  constructor(
    private swUpdate: SwUpdate,
    private snackBar: MatSnackBar,
    private onlineStatus: OnlineStatusService,
  ) {
    
  }

  ngOnInit() {
    if (this.swUpdate.isEnabled) {
      this.swUpdate.available.subscribe(() => {
        const snack = this.snackBar.open('Update Available', 'Reload', {
          duration: 6000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom'
        });

        snack.onAction().subscribe(() => {
          window.location.reload();
        });
      });
    }
  }

  async initializeApp() {
    // Request permissions
    await PushNotifications.requestPermissions();
    // Register for push notifications
    await PushNotifications.register();

    // Listen for network status
    Network.addListener('networkStatusChange', (status: any) => {
      console.log('Network status changed', status);
    });
  }
}