import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { ForgotPasswordComponent } from 'src/app/forgot-password/forgot-password.component';
import { LoginComponent } from 'src/app/login/login.component';
import { SignupComponent } from 'src/app/signup/signup.component';
import { CustomerMenu, CustomerMenuItems } from 'src/app/shared/customer-menu-items';
import { CartService } from 'src/app/services/cart.service';
import { UserService } from 'src/app/services/user.service';


@Component({
  selector: 'app-customer-layout',
  templateUrl: 'customer-layout.component.html',
  styleUrls: ['./customer-layout.component.scss']
})


export class CustomerLayoutComponent {
  cartItemCount: number = 0;
  menuItems: CustomerMenu[];
  isLoggedIn: boolean = false;
  userName: string = '';


  constructor(
    private dialog: MatDialog,
    private router: Router,
    private cartService: CartService,
    private customerMenuItems: CustomerMenuItems,
    private userService: UserService
  ) {
    this.menuItems = this.customerMenuItems.getMenuItems();
  }


  ngOnInit(): void {
    // this.userService.checkToken().subscribe((response:any)=>{
    //   this.router.navigate(['/cafe/dashboard']);
    // },(error:any)=>{
    //   console.log(error);
    // })
    this.userService.isLoggedIn().subscribe(loggedIn => {
      this.isLoggedIn = loggedIn;
      if (loggedIn) {
         this.userName = this.userService.getUserName();
      }
    });
    this.userService.getUserId().subscribe(userId => {
      // Ensure userId is also being managed correctly, if needed
      console.log('User ID:', userId);
    });

    // this.cartService.getCartItemCount().subscribe(count => {
    //   this.cartItemCount = count;
    // });
  }




  navigateTo(state: string) {
    this.router.navigate([state]);
  }


  handleSignupAction(){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = "550px";
    this.dialog.open(SignupComponent, dialogConfig);
  }


  handleforgotPasswordAction(){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = "550px";
    this.dialog.open(ForgotPasswordComponent, dialogConfig);
  }


  handleLoginAction(){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = "550px";
    this.dialog.open(LoginComponent, dialogConfig);




  }


  handleLogout() {
    this.userService.logout();
    this.router.navigate(['/']);
  }


  viewProfile() {
    // Implement view profile logic
  }


  viewBillHistory() {
    // Implement bill history logic
  }
 
  openSearchBar() {
    this.router.navigate(['/search']);
  }
 
  openCart() {
    this.router.navigate(['/cart']);
  }
}


