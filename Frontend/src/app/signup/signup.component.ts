import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Route, Router } from '@angular/router';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { UserService } from '../services/user.service';
import { SnackbarService } from '../services/snackbar.service';
import { GlobalConstants } from '../shared/global_constants';


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

  constructor(private formBuilder:FormBuilder,//Để tạo form
    private router:Router,//Để điều hướng
    private userService:UserService,//Để gọi API đăng ký(giao tiếp vs api)
    private snackBarService:SnackbarService,//Để hiển thị thông báo
    public dialogRef:MatDialogRef<SignupComponent>,// Để đóng hộp thoại (dialog) khi cần
    private ngxService:NgxUiLoaderService//Để hiển thị loader khi thực hiện các thao tác bất đồng bộ
  ) { }

  ngOnInit(): void {
  this.signupForm = this.formBuilder.group({//Được khởi tạo dưới dạng một group (FormGroup)
    name:[null,[Validators.required, Validators.pattern(GlobalConstants.nameRegex)]],
    email:[null,[Validators.required, Validators.pattern(GlobalConstants.emailRegex)]],
    contactNumber:[null,[Validators.required, Validators.pattern(GlobalConstants.contactNumberRegex)]],
    password: [null,[Validators.required]],
    confirmPassword:[null,[Validators.required]]
  })
  }
  validateSubmit(){// kiểm tra xem mật khẩu và mật khẩu xác nhận có trùng khớp hay không
    if(this.signupForm.controls['password'].value != this.signupForm.controls['confirmPassword'].value){
      return true;
    }
    else{
      return false;
    }
  }

  handleSubmit(){//Phương thức này được gọi khi người dùng bấm nút đăng ký
    this.ngxService.start();///Hiển thị loader khi bắt đầu gửi dữ liệu
    var formData = this.signupForm.value;//Lấy dữ liệu từ form
    var data = {//tạo đối tượng 
      name: formData.name,
      email: formData.email,
      contactNumber: formData.contactNumber,
      password: formData.password
    }

    this.userService.signup(data).subscribe((respone:any)=>{//Gọi phương thức signup() từ UserService để gửi dữ liệu lên API
      this.ngxService.stop();
      this.dialogRef.close();
      this.responseMessage = respone?.message;
      this.snackBarService.openSnackBar(this.responseMessage,"");// Hiển thị thông báo thành công
      this.router.navigate(['/']);//Điều hướng người dùng đến trang chủ
    },(error)=>{
      this.ngxService.stop();
      if(error.error?.message){
        this.responseMessage = error.error?.message;
      }
      else{
        this.responseMessage = GlobalConstants.genericError;//Hiển thị thông báo lỗi nhận từ API hoặc thông báo lỗi chung 
      }
      this.snackBarService.openSnackBar(this.responseMessage, GlobalConstants.error);
    })
  }
}