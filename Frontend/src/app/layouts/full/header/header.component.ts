import { ConfirmationComponent } from './../../../material-component/dialog/confirmation/confirmation.component';
import { Component } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ChangePasswordComponent } from 'src/app/material-component/dialog/change-password/change-password.component';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class AppHeaderComponent {

  role:any;
  isLoggedIn: boolean = false;
  userName: string = '';
  
  constructor(private router:Router,
    private userService: UserService,
    private dialog:MatDialog) {
  }

  ngOnInit(): void {

   this.userService.isLoggedIn().subscribe(loggedIn => {
     this.isLoggedIn = loggedIn;
     if (loggedIn) {
        this.userName = this.userService.getUserName();
     }
   });
   this.userService.getUserId().subscribe(userId => {
     console.log('User ID:', userId);
       this.userName = this.userService.getUserName();
     }
   );
 }

  logout() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      message: 'Logout',
      confirmation: true
    };
    
    const dialogRef = this.dialog.open(ConfirmationComponent, dialogConfig);
    const sub = dialogRef.componentInstance.onEmitStatusChange.subscribe((response) => {
      dialogRef.close();
      this.userService.logout();
      
      // Thêm setTimeout để đảm bảo dialog đã đóng hoàn toàn
      setTimeout(() => {
        this.router.navigate(['/']);
      }, 100);
    });
  }

  changePassword(){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width="550px";
    this.dialog.open(ChangePasswordComponent,dialogConfig);
  }
}
