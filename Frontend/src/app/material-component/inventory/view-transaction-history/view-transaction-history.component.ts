import { DatePipe } from '@angular/common';
import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { InventoryTransactionWrapper, InventoryService, TransactionType } from 'src/app/services/inventory.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';

@Component({
  selector: 'app-view-transaction-history',
  templateUrl: './view-transaction-history.component.html',
  styleUrls: ['./view-transaction-history.component.scss'],
  providers: [DatePipe]
})
export class ViewTransactionHistoryComponent implements OnInit {

  displayedColumns: string[] = ['transactionDate', 'transactionType', 'quantity', 'note', 'createdBy'];
  dataSource: MatTableDataSource<InventoryTransactionWrapper>;
  searchText: string = '';
  responseMessage: string = '';

  // Add sorting and pagination
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<ViewTransactionHistoryComponent>,
    private inventoryService: InventoryService,
    private ngxService: NgxUiLoaderService,
    private snackbarService: SnackbarService
  ) {
    this.dataSource = new MatTableDataSource();
  }

  ngOnInit(): void {
    setTimeout(() => {
      this.ngxService.start();
    this.loadTransactionHistory();
    });
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }
  
  loadTransactionHistory() {
    this.inventoryService.getTransactionHistory(this.data.productId).subscribe(
      (response: InventoryTransactionWrapper[]) => {
        this.ngxService.stop();
        this.dataSource = new MatTableDataSource(response);
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.applyFilter();
      },
      (error: any) => {
        this.ngxService.stop();
        console.error(error);
        this.responseMessage = GlobalConstants.genericError;
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      }
    );
  }

  applyFilter() {
    this.dataSource.filterPredicate = (data: InventoryTransactionWrapper, filter: string) => {
      return (
        data.note.toLowerCase().includes(filter.toLowerCase()) ||
        data.transactionType.toLowerCase().includes(filter.toLowerCase()) ||
        data.createdBy.toLowerCase().includes(filter.toLowerCase())
      );
    };
    this.dataSource.filter = this.searchText.trim().toLowerCase();
  }

  clearSearch() {
    this.searchText = '';
    this.applyFilter();
  }

  getTypeClass(type: TransactionType): string {
    switch (type) {
      case TransactionType.OUT_OF_STOCK:
        return 'type-out-of-stock';
      case TransactionType.IMPORT:
        return 'type-stock-in';
      case TransactionType.EXPORT:
        return 'type-stock-out';
      default:
        return '';
    }
  }

  getQuantityClass(type: TransactionType): string {
    switch (type) {
      case TransactionType.OUT_OF_STOCK:
        return 'quantity-danger';
      case TransactionType.IMPORT:
        return 'quantity-positive';
      case TransactionType.EXPORT:
        return 'quantity-negative';
      default:
        return '';
    }
  }

  getQuantityDisplay(transaction: InventoryTransactionWrapper): string {
    switch (transaction.transactionType) {
      case TransactionType.OUT_OF_STOCK:
      case TransactionType.EXPORT:
        return `-${transaction.quantity}`;
      default:
        return `+${transaction.quantity}`;
    }
  }
}