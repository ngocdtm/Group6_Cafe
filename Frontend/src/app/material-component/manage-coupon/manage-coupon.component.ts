import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { Route, Router } from '@angular/router';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { CouponService } from 'src/app/services/coupon.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';
import { CouponComponent } from '../dialog/coupon/coupon.component';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-coupon',
  templateUrl: './manage-coupon.component.html',
  styleUrls: ['./manage-coupon.component.scss'],
  providers: [DatePipe]
})
export class ManageCouponComponent implements OnInit {
  displayedColumns: string[] = ['name', 'code', 'discount', 'expirationDate', 'edit'];
  dataSource:any;
  responseMessage:any

  constructor(private couponService: CouponService,
    private ngxService:NgxUiLoaderService,
    private dialog:MatDialog,
    private snackbarService:SnackbarService,
    private router:Router,
    private datePipe: DatePipe
) { }
ngOnInit(): void {
  this.ngxService.start();
  this.tableData();
}

tableData(){
  this.couponService.getCoupon().subscribe((response:any)=>{
    this.ngxService.stop();
    this.dataSource = new MatTableDataSource(response);
  },(error:any)=>{
    this.ngxService.stop();
    console.log(error.error?.message);
    if(error.error?.message){
      this.responseMessage = error.error?.message;
    }else{
      this.responseMessage = GlobalConstants.genericError;
    }
    this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
  })
}

applyFilter(event:Event){
  const filterValue = (event.target as HTMLInputElement).value;
  this.dataSource.filter = filterValue.trim().toLowerCase();
}

handleAddAction(){
  const dialogConfig = new MatDialogConfig();
  dialogConfig.data = {
    action: 'Add'
  };
  dialogConfig.width = "850px";
  const dialogRef = this.dialog.open(CouponComponent,dialogConfig);
  this.router.events.subscribe(()=>{
    dialogRef.close();
  });
  const sub = dialogRef.componentInstance.onAddCoupon.subscribe((response)=>{
    this.tableData();
  });
}

handleEditAction(values:any){
  const dialogConfig = new MatDialogConfig();
  dialogConfig.data = {
    action: 'Edit',
    data:values
  };
  dialogConfig.width = "850px";
  const dialogRef = this.dialog.open(CouponComponent,dialogConfig);
  this.router.events.subscribe(()=>{
    dialogRef.close();
  });
  const sub = dialogRef.componentInstance.onEditCoupon.subscribe((response)=>{
    this.tableData();
  });
}
}


 


  

  