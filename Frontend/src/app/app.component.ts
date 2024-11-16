import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SwUpdate } from '@angular/service-worker';
import { OnlineStatusService } from './services/online-status.service';

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
    private onlineStatus: OnlineStatusService
  ) {}

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
}