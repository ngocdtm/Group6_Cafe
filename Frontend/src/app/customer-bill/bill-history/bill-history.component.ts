import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BillItem, BillService } from '../../services/bill.service';
import { BillDetailsDialogComponent } from '../bill-details-dialog/bill-details-dialog.component';
import { ThemePalette } from '@angular/material/core';
import { ConfirmationComponent } from '../../material-component/dialog/confirmation/confirmation.component';
import { ReviewDialogComponent } from '../review-dialog/review-dialog.component';
import { ReviewListDialogComponent } from '../review-list-dialog/review-list-dialog.component';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-bill-history',
  templateUrl: './bill-history.component.html',
  styleUrls: ['./bill-history.component.scss']
})

export class BillHistoryComponent implements OnInit {
  dataSource: MatTableDataSource<any>;
  displayedColumns: string[] = ['orderDate', 'total', 'orderStatus', 'paymentMethod', 'actions', 'review'];
  isLoading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private billService: BillService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.dataSource = new MatTableDataSource<any>([]);
  }

  ngOnInit(): void {
    this.loadBills();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }
  
  loadBills(): void {
    this.isLoading = true;
    this.billService.getBills().subscribe({
      next: (response) => {
        this.dataSource.data = response;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading dataSource:', error);
        this.snackBar.open('Failed to load dataSource', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  openReviewsDialog(bill: any): void {
    this.dialog.open(ReviewListDialogComponent, {
      width: '800px',
      data: { bill }
    });
  }
  
  openReviewDialog(item: BillItem, billId: number): void {
    const dialogRef = this.dialog.open(ReviewDialogComponent, {
      width: '500px',
      data: {
        billId: billId,
        productId: item.originalProductId,
        productName: item.productName
      }
    });
  
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadBills(); // Refresh list after successful review
      }
    });
  }
  
  openBillDetails(bill: any): void {
    const dialogRef = this.dialog.open(BillDetailsDialogComponent, {
      width: '800px',
      data: bill,
      autoFocus: false
    });

    // Refresh bills list after dialog closes in case any changes were made
    dialogRef.afterClosed().subscribe(() => {
      this.loadBills();
    });
  }

  cancelOrder(bill: any): void {
    if (bill.orderStatus !== 'PENDING') {
      this.snackBar.open('Only pending orders can be cancelled', 'Close', { 
        duration: 3000,
        panelClass: ['warning-snackbar']
      });
      return;
    }

    const dialogRef = this.dialog.open(ConfirmationComponent, {
      width: '400px',
      data: {
        message: 'cancel this order?',
        confirmation: true
      }
    });

    dialogRef.componentInstance.onEmitStatusChange.subscribe(() => {
      this.isLoading = true;
      this.billService.updateOrderStatus(bill.id, 'CANCELLED').subscribe({
        next: () => {
          this.snackBar.open('Order cancelled successfully', 'Close', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.loadBills();
          dialogRef.close();
        },
        error: (error) => {
          console.error('Error cancelling order:', error);
          this.snackBar.open(
            error.error?.message || 'Failed to cancel order', 
            'Close', 
            { 
              duration: 3000,
              panelClass: ['error-snackbar']
            }
          );
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    });
  }

  getOrderStatusColor(status: string): ThemePalette {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'warn';
      case 'CONFIRMED':
        return 'primary';
      case 'PROCESSING':
        return 'accent';
      case 'COMPLETED':
        return 'primary';
      case 'CANCELLED':
        return 'warn';
      default:
        return undefined;
    }
  }

  getOrderStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'status-pending';
      case 'CONFIRMED':
        return 'status-confirmed';
      case 'PROCESSING':
        return 'status-processing';
      case 'COMPLETED':
        return 'status-completed';
      case 'CANCELLED':
        return 'status-cancelled';
      default:
        return '';
    }
  }
}