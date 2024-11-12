import { Injectable } from '@angular/core';
import { BehaviorSubject, fromEvent, Observable } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class OnlineStatusService {
  private online$ = new BehaviorSubject<boolean>(navigator.onLine);

  constructor(private snackBar: MatSnackBar) {
    fromEvent(window, 'online').subscribe(() => {
      this.online$.next(true);
      this.showNotification('You are back online');
    });

    fromEvent(window, 'offline').subscribe(() => {
      this.online$.next(false);
      this.showNotification('You are offline. Some features may be limited');
    });
  }

  get isOnline$(): Observable<boolean> {
    return this.online$.asObservable();
  }

  private showNotification(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom'
    });
  }
}