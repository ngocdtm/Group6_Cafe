import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class SnackbarService {// hiển thị thông báo

  constructor(private snackBar:MatSnackBar) { }

  openSnackBar(message:string, action:string){
    if(action === 'error'){
      this.snackBar.open(message,'',{
        horizontalPosition: 'center',
        verticalPosition: 'top',
        duration: 2000,//thời gian hiện thông báo//2s
        panelClass:['black-snackbar']
      });
    }
    else{
      this.snackBar.open(message,'',{
        horizontalPosition: 'center',
        verticalPosition: 'top',
        duration: 2000,//thời gian hiện thông báo//2s
        panelClass:['green-snackbar']
      });
    }
  }
}