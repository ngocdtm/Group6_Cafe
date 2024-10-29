import { Component, EventEmitter, Inject, Output } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { InventoryService } from 'src/app/services/inventory.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';

@Component({
  selector: 'app-update-min-max-stock',
  templateUrl: './update-min-max-stock.component.html',
  styleUrls: ['./update-min-max-stock.component.scss']
})

export class UpdateMinMaxStockComponent {
  @Output() onUpdate = new EventEmitter();

  productId: number;
  productName: string;
  minQuantity: number;
  maxQuantity: number;

  constructor(
    public dialogRef: MatDialogRef<UpdateMinMaxStockComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private inventoryService: InventoryService,
    private snackbarService: SnackbarService
  ) {
    this.productId = data.productId;
    this.productName = data.productName;
    this.minQuantity = data.currentMinQuantity;
    this.maxQuantity = data.currentMaxQuantity;
  }

  updateMinMaxStock() {
    this.inventoryService.updateMinMaxStock(this.productId, this.minQuantity, this.maxQuantity)
      .subscribe({
        next: (response) => {
          this.snackbarService.openSnackBar(response, GlobalConstants.success);
          this.onUpdate.emit();
          this.dialogRef.close();
        },
        error: (error) => {
          this.snackbarService.openSnackBar(error.error?.message || GlobalConstants.genericError, GlobalConstants.error);
        }
      });
  }

  close() {
    this.dialogRef.close();
  }
}