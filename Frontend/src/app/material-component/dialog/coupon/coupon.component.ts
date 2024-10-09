import { DatePipe } from '@angular/common';
import { Component, EventEmitter, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogConfig, MatDialogRef } from '@angular/material/dialog';
import { CouponService } from 'src/app/services/coupon.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';

@Component({
  selector: 'app-coupon',
  templateUrl: './coupon.component.html',
  styleUrls: ['./coupon.component.scss'],
  providers: [DatePipe]
})
export class CouponComponent implements OnInit {

  onAddCoupon = new EventEmitter();
  onEditCoupon = new EventEmitter();
  couponForm:any = FormGroup;
  dialogAction:any = "Add"
  action:any = "Add";
  responseMessage:any;

  constructor(@Inject(MAT_DIALOG_DATA) public dialogData:any,
  private formBuilder: FormBuilder,
  private couponService: CouponService,
  public dialogRef: MatDialogRef<CouponComponent>,
  private snackbarService: SnackbarService,
  private datePipe: DatePipe
  ) { }

  ngOnInit(): void {
    this.couponForm = this.formBuilder.group({
      name:[null,[Validators.required]],
      code:[null,[Validators.required]],
      discount:[null,[Validators.required]],
      expirationDate:[null,[Validators.required]]
    })
    if(this.dialogData.action === 'Edit'){
      this.dialogAction = "Edit";
      this.action = "Update";
      this.couponForm.patchValue(this.dialogData.data);
    }
  }

  handleSubmit(){
    if(this.dialogAction === 'Edit'){
      this.edit();
    }else{
      this.add();
    }
  }

  add(){
    var formData = this.couponForm.value;
    var data = {
      name: formData.name,
      code: formData.code,
      discount: formData.discount,
      expirationDate: this.formatDate(formData.expirationDate)
    }
    this.couponService.add(data).subscribe((response:any)=>{
      this.dialogRef.close();
      this.onAddCoupon.emit();
      this.responseMessage = response.message;
      this.snackbarService.openSnackBar(this.responseMessage, "success");
    },(error)=>{
      this.dialogRef.close();
      console.error(error);
      if(error.error?.message){
        this.responseMessage = error.error?.message;
      }else{
        this.responseMessage = GlobalConstants.genericError;
      }
      this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
    });
  }

  edit(){
    var formData = this.couponForm.value;
    var data = {
      id:this.dialogData.data.id,
      name: formData.name,
      code: formData.code,
      discount: formData.discount,
      expirationDate: this.formatDate(formData.expirationDate)
    }
    this.couponService.update(data).subscribe((response:any)=>{
      this.dialogRef.close();
      this.onEditCoupon.emit();
      this.responseMessage = response.message;
      this.snackbarService.openSnackBar(this.responseMessage, "success");
    },(error)=>{
      this.dialogRef.close();
      console.error(error);
      if(error.error?.message){
        this.responseMessage = error.error?.message;
      }else{
        this.responseMessage = GlobalConstants.genericError;
      }
      this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
    });
  }

  private formatDate(date: Date | null): string | null { if (date) { return this.datePipe.transform(date, 'yyyy-MM-dd'); } return null; }



}
