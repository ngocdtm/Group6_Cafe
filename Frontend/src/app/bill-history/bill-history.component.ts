import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BillService } from '../services/bill.service';
import { BillDetailsDialogComponent } from '../material-component/dialog/bill-details-dialog/bill-details-dialog.component';
import { ThemePalette } from '@angular/material/core';
import { ConfirmationComponent } from '../material-component/dialog/confirmation/confirmation.component';

@Component({
  selector: 'app-bill-history',
  templateUrl: './bill-history.component.html',
  styleUrls: ['./bill-history.component.scss']
})

export class BillHistoryComponent implements OnInit {
  dataSource: any[] = [];
  displayedColumns: string[] = ['orderDate', 'total', 'orderStatus', 'paymentMethod', 'actions'];
  isLoading = false;

  constructor(
    private billService: BillService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadBills();
  }

  loadBills(): void {
    this.isLoading = true;
    this.billService.getBills().subscribe({
      next: (response) => {
        this.dataSource = response;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading dataSource:', error);
        this.snackBar.open('Failed to load dataSource', 'Close', { duration: 3000 });
        this.isLoading = false;
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