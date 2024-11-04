import { Component, EventEmitter, Inject, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { InventoryService } from 'src/app/services/inventory.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';

@Component({
  selector: 'app-remove-stock',
  templateUrl: './remove-stock.component.html',
  styleUrls: ['./remove-stock.component.scss']
})
export class RemoveStockComponent implements OnInit {

  onRemove = new EventEmitter();
  removeStockForm:any = FormGroup;
  responseMessage: string = '';
  isInitialSetup: boolean = false;

  constructor(
    @Inject(MAT_DIALOG_DATA) public dialogData: any,
    private formBuilder: FormBuilder,
    private inventoryService: InventoryService,
    private dialogRef: MatDialogRef<RemoveStockComponent>,
    private ngxService: NgxUiLoaderService,
    private snackbarService: SnackbarService
  ) {}

  ngOnInit(): void {
    this.removeStockForm = this.formBuilder.group({
      quantity: [null, [
        Validators.required,
        Validators.min(1),
        Validators.max(this.dialogData.currentQuantity)
      ]],
      note: [null, [Validators.required]]
    });
  }

  handleSubmit() {
    this.ngxService.start();
    const formData = this.removeStockForm.value;
    
    this.inventoryService.removeStock(
      this.dialogData.productId,
      formData.quantity,
      formData.note
    ).subscribe(
      (response: any) => {
        this.ngxService.stop();
        this.responseMessage = response;
        this.snackbarService.openSnackBar(this.responseMessage, "success");
        this.onRemove.emit();
        this.dialogRef.close();
      },
      (error) => {
        this.ngxService.stop();
        if(error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      }
    );
  }

   // Helper methods for template
   getErrorMessage(field: string) {
    if (field === 'quantity') {
      if (this.removeStockForm.get('quantity')?.hasError('required')) {
        return 'Quantity is required';
      }
      if (this.removeStockForm.get('quantity')?.hasError('min')) {
        return 'Quantity must be greater than 0';
      }
      if (!this.isInitialSetup && this.removeStockForm.get('quantity')?.value > this.dialogData.maxQuantity) {
        return `Quantity cannot exceed maximum stock (${this.dialogData.maxQuantity})`;
      }
    }
    if (field === 'minQuantity') {
      if (this.removeStockForm.get('minQuantity')?.hasError('required')) {
        return 'Minimum quantity is required';
      }
      if (this.removeStockForm.get('minQuantity')?.hasError('min')) {
        return 'Minimum quantity cannot be negative';
      }
    }
    if (field === 'maxQuantity') {
      if (this.removeStockForm.get('maxQuantity')?.hasError('required')) {
        return 'Maximum quantity is required';
      }
      if (this.removeStockForm.get('maxQuantity')?.hasError('min')) {
        return 'Maximum quantity must be greater than 0';
      }
      if (this.removeStockForm.get('maxQuantity')?.hasError('maxQuantityInvalid')) {
        return 'Maximum quantity must be greater than minimum quantity';
      }
    }
    return '';
  }
}