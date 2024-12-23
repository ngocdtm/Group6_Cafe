import { SnackbarService } from './../services/snackbar.service';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../services/user.service';
import { MatDialogRef } from '@angular/material/dialog';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { Router } from '@angular/router';
import { GlobalConstants } from '../shared/global-constants';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  hide = true;
  loginForm:any = FormGroup;
  responseMessage:any;

  constructor(private formBuilder: FormBuilder,
    private router: Router,
    private userService: UserService,
    public dialogRef: MatDialogRef<LoginComponent>,
    private ngxService: NgxUiLoaderService,
    private snackbarService: SnackbarService
  ) { }

  ngOnInit(): void {
    const returnUrl = localStorage.getItem('returnUrl');
  if (returnUrl) {
    localStorage.removeItem('returnUrl');
    // Redirect sau khi đăng nhập thành công
    this.router.navigateByUrl(returnUrl);
  }
    this.loginForm = this.formBuilder.group({
      email:[null,[Validators.required, Validators.pattern(GlobalConstants.emailRegex)]],
      password:[null,[Validators.required]]
    });
  }

  handleSubmit() {
    this.ngxService.start();
    var formData = this.loginForm.value;
    var data = {
      email: formData.email,
      password: formData.password
    }
    
    this.userService.login(data).subscribe((response: any) => {
      this.ngxService.stop();
      this.dialogRef.close();
      localStorage.setItem('token', response.token);
      
      // Store complete user info
      this.userService.setUserInfo(
        response.name,
        response.role,
        response.id
      );
      
      // Set login status
      this.userService.setLoggedIn(true);
      
      // Navigate based on role
      if (response.role === 'ADMIN' || response.role === 'EMPLOYEE') {
        this.router.navigate(['/cafe/dashboard']);
      } else {
        this.router.navigate(['/']);
      }
    }, (error) => {
      this.ngxService.stop();
      if (error.error?.message) {
        this.responseMessage = error.error?.message;
      } else {
        this.responseMessage = GlobalConstants.genericError;
      }
      this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
    });
  }
}