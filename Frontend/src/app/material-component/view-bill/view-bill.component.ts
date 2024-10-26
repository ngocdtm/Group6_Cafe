import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { BillService } from 'src/app/services/bill.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';
import { ViewBillProductsComponent } from '../dialog/view-bill-products/view-bill-products.component';
import { ConfirmationComponent } from '../dialog/confirmation/confirmation.component';
import { saveAs } from 'file-saver';

interface Bill {
  id: number;
  customerName: string;
  uuid: string;
  customerPhone: string;
  orderType: string;
  orderStatus: string;
  orderDate: Date;
  paymentMethod: string;
  total: number;
  discount: number;
  totalAfterDiscount: number;
  productDetails: string;
  shippingAddress?: string;
}

@Component({
  selector: 'app-view-bill',
  templateUrl: './view-bill.component.html',
  styleUrls: ['./view-bill.component.scss']
})
export class ViewBillComponent implements OnInit {
  displayedColumns: string[] = [
    'name',
    'phoneNumber',
    'orderType',
    'orderStatus',
    'orderDate',
    'paymentMethod',
    'total',
    'discount',
    'totalAfterDiscount',
    'actions'
  ];
  dataSource: MatTableDataSource<Bill> = new MatTableDataSource<Bill>([]);
  responseMessage: string = '';

  constructor(
    private billService: BillService,
    private ngxService: NgxUiLoaderService,
    private dialog: MatDialog,
    private snackbarService: SnackbarService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.ngxService.start();
    this.tableData();
  }

  tableData(): void {
    this.billService.getBills().subscribe(
      (response: any) => {
        this.ngxService.stop();
        // Transform the date strings to Date objects
        const processedData = response.map((bill: Bill) => ({
          ...bill,
          orderDate: bill.orderDate
        }));
        this.dataSource = new MatTableDataSource(processedData);
      },
      (error: any) => {
        this.ngxService.stop();
        console.log(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      }
    );
  }

   // Helper method to parse different date formats
  //  parseDate(dateString: string | Date): Date {
  //   if (!dateString) return new Date(); // Return current date instead of null
    
  //   // If it's already a Date object, return it
  //   if (dateString instanceof Date) {
  //     return dateString;
  //   }
    
  //   // Try parsing the date string
  //   const date = new Date(dateString);
    
  //   // Check if the date is valid
  //   if (isNaN(date.getTime())) {
  //     // If the date string contains commas, it might be in the format "2024,10,23,20,31,2,598957000"
  //     if (typeof dateString === 'string' && dateString.includes(',')) {
  //       const [year, month, day, hours, minutes, seconds, milliseconds] = dateString.split(',').map(Number);
  //       // Note: Month is 0-based in JavaScript Date
  //       return new Date(year, month - 1, day, hours, minutes, seconds, milliseconds);
  //     }
  //     // Return current date as fallback if parsing fails
  //     console.warn(`Invalid date format: ${dateString}`);
  //     return new Date();
  //   }
    
  //   return date;
  // }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  handleViewAction(values: any) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      data: values
    };
    dialogConfig.width = "100%";
    const dialogRef = this.dialog.open(ViewBillProductsComponent, dialogConfig);
    this.router.events.subscribe(() => {
      dialogRef.close();
    });
  }

  handleDeleteAction(values: any) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      message: 'delete ' + values.customerName + ' bill.',
      confirmation: true
    };
    const dialogRef = this.dialog.open(ConfirmationComponent, dialogConfig);
    const sub = dialogRef.componentInstance.onEmitStatusChange.subscribe((response) => {
      this.ngxService.start();
      this.deleteBill(values.id);
      dialogRef.close();
    });
  }

  handleStatusUpdate(values: any, newStatus: string) {
    this.ngxService.start();
    this.billService.updateOrderStatus(values.id, newStatus).subscribe(
      (response: any) => {
        this.ngxService.stop();
        this.responseMessage = response.message;
        this.snackbarService.openSnackBar(this.responseMessage, "success");
        this.tableData();
      },
      (error: any) => {
        this.ngxService.stop();
        console.log(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      }
    );
  }

  deleteBill(id: any) {
    this.billService.deleteBill(id).subscribe(
      (response: any) => {
        this.ngxService.stop();
        this.tableData();
        this.responseMessage = response?.message;
        this.snackbarService.openSnackBar(this.responseMessage, "success");
      },
      (error: any) => {
        this.ngxService.stop();
        console.log(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      }
    );
  }

  downloadBillAction(values: any) {
    this.ngxService.start();
    var data = {
      customerName: values.customerName,
      uuid: values.uuid,
      customerPhone: values.customerPhone,
      orderType: values.orderType,
      orderStatus: values.orderStatus,
      orderDate: values.orderDate,
      paymentMethod: values.paymentMethod,
      total: values.total,
      discount: values.discount,
      totalAfterDiscount: values.totalAfterDiscount,
      productDetails: values.productDetails,
      shippingAddress: values.shippingAddress
    };

    this.downloadFile(values.uuid, data);
  }

  downloadFile(fileName: string, data: any) {
    this.billService.getPdf(data).subscribe(
      (response: any) => {
        saveAs(new Blob([response], { type: 'application/pdf' }), fileName + '.pdf');
        this.ngxService.stop();
      },
      (error: any) => {
        this.ngxService.stop();
        console.log(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = "Error downloading bill";
        }
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      }
    );
  }
}