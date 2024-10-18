import { SnackbarService } from './../services/snackbar.service';
import { UserService } from './../services/user.service';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { GlobalConstants } from '../shared/global-constants';
import { MatSnackBar } from '@angular/material/snack-bar';


@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.scss']
})
export class SignupComponent implements OnInit {
  password = true;
  confirmPassword = true;
  signupForm:any = FormGroup;
  responseMessage:any;


  constructor(private formBuilder: FormBuilder,
    private snackBar: MatSnackBar,
    private router: Router,
    private userService: UserService,
    private snackbarService: SnackbarService,
    public dialogRef: MatDialogRef<SignupComponent>,
    private ngxService: NgxUiLoaderService
  ) { }


  ngOnInit(): void {
    this.signupForm = this.formBuilder.group({
      name:[null,[Validators.required, Validators.pattern(GlobalConstants.nameRegex)]],
      email:[null,[Validators.required, Validators.pattern(GlobalConstants.emailRegex)]],
      phoneNumber:[null,[Validators.required, Validators.pattern(GlobalConstants.phoneNumberRegex)]],
      password:[null,[Validators.required]],
      confirmPassword:[null,[Validators.required]],
      address:[null,[Validators.required]],
    });
  }


  validateSubmit(){
    if(this.signupForm.controls['password'].value != this.signupForm.controls['confirmPassword'].value){
       return true;
    }else{
      return false;
    }
  }


  handleSubmit(){
    this.ngxService.start();
    var formData = this.signupForm.value;
    var data = {
      name: formData.name,
      email: formData.email,
      phoneNumber: formData.phoneNumber,
      password: formData.password,
      address: formData.address
    }
    this.userService.singup(data).subscribe((response:any)=>{
    this.snackBar.open('Đăng ký tài khoản thành công!', 'Close', {duration: 5000});
      this.ngxService.stop();
      this.dialogRef.close();
      this.responseMessage = response ?.message;
      this.snackbarService.openSnackBar(this.responseMessage,"");
      this.router.navigate(['/']);
    },(error)=>{
      this.ngxService.stop();
      if(error.errror?.message){
        this.snackBar.open('Đăng ký tài khoản không thành công :(', 'Close', {duration: 5000, panelClass: 'error-snackbar'});
        this.responseMessage = error.errror?.message;
      }else{
        this.responseMessage = GlobalConstants.genericError;
      }
      this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
    })
  }
}



