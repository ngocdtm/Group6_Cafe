import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { ForgotPasswordComponent } from 'src/app/forgot-password/forgot-password.component';
import { LoginComponent } from 'src/app/login/login.component';
import { SignupComponent } from 'src/app/signup/signup.component';
import { CustomerMenu, CustomerMenuItems } from 'src/app/shared/customer-menu-items';
import { CartService } from 'src/app/services/cart.service';


@Component({
  selector: 'app-customer-layout',
  templateUrl: 'customer-layout.component.html',
  styleUrls: ['./customer-layout.component.scss']
})


export class CustomerLayoutComponent {
  cartItemCount: number = 0;
  menuItems: CustomerMenu[];


  constructor(
    private dialog: MatDialog,
    private router: Router,
    private cartService: CartService,
    private customerMenuItems: CustomerMenuItems
  ) {
    this.menuItems = this.customerMenuItems.getMenuItems();
  }


  ngOnInit(): void {
    // this.userService.checkToken().subscribe((response:any)=>{
    //   this.router.navigate(['/cafe/dashboard']);
    // },(error:any)=>{
    //   console.log(error);
    // })
    this.updateCartItemCount();
    this.cartService.cartUpdated.subscribe(() => {
      this.updateCartItemCount();
    });
  }


  navigateTo(state: string) {
    this.router.navigate([state]);
  }


  updateCartItemCount() {
    this.cartItemCount = this.cartService.getCartItemCount();
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


  openCart() {
    // Implement cart opening logic here
    console.log('Opening cart');
  }
}

