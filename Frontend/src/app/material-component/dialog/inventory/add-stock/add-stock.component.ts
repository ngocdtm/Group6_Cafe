import { Component, EventEmitter, Inject, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { InventoryService } from 'src/app/services/inventory.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';

@Component({
  selector: 'app-add-stock',
  templateUrl: './add-stock.component.html',
  styleUrls: ['./add-stock.component.scss']
})
export class AddStockComponent implements OnInit {

  onAdd = new EventEmitter();
  addStockForm: any = FormGroup;
  dialogAction: string = "Add";
  action: string = "Add";
  responseMessage: string = "";
  isInitialSetup: boolean = false;

  constructor(
    @Inject(MAT_DIALOG_DATA) public dialogData: any,
    private formBuilder: FormBuilder,
    private inventoryService: InventoryService,
    private dialogRef: MatDialogRef<AddStockComponent>,
    private ngxService: NgxUiLoaderService,
    private snackbarService: SnackbarService
  ) {}

  ngOnInit(): void {
    this.isInitialSetup = this.dialogData.isInitialSetup || false;
    
    // Khởi tạo form với các validators phù hợp
    this.addStockForm = this.formBuilder.group({
      quantity: [null, [Validators.required, Validators.min(1)]],
      note: [''],
      minQuantity: [{ 
        value: null, 
        disabled: !this.isInitialSetup 
      }, [Validators.required, Validators.min(0)]],
      maxQuantity: [{ 
        value: null, 
        disabled: !this.isInitialSetup 
      }, [Validators.required, Validators.min(1)]]
    }, {
      validators: [this.maxQuantityValidator()]
    });

    // Nếu không phải initial setup, set giá trị mặc định từ dialogData
    if (!this.isInitialSetup) {
      this.addStockForm.patchValue({
        minQuantity: this.dialogData.minQuantity,
        maxQuantity: this.dialogData.maxQuantity
      });
    }
  }

  // Custom validator để kiểm tra maxQuantity > minQuantity
  maxQuantityValidator() {
    return (formGroup: FormGroup) => {
      const minQuantity = formGroup.get('minQuantity')?.value;
      const maxQuantity = formGroup.get('maxQuantity')?.value;

      if (minQuantity && maxQuantity && maxQuantity <= minQuantity) {
        formGroup.get('maxQuantity')?.setErrors({ maxQuantityInvalid: true });
      }
      return null;
    };
  }

  validateForm() {
    if (this.addStockForm.get('quantity')?.value > this.dialogData.maxQuantity && !this.isInitialSetup) {
      return false;
    }
    return this.addStockForm.valid;
  }

  handleSubmit() {
    if (this.validateForm()) {
      this.ngxService.start();
      
      let formData: any = {
        productId: this.dialogData.productId,
        quantity: this.addStockForm.get('quantity')?.value,
        note: this.addStockForm.get('note')?.value
      };

      // Thêm minQuantity và maxQuantity nếu là initial setup
      if (this.isInitialSetup) {
        formData.minQuantity = this.addStockForm.get('minQuantity')?.value;
        formData.maxQuantity = this.addStockForm.get('maxQuantity')?.value;
      }

      this.inventoryService.addStock(
        formData.productId,
        formData.quantity,
        formData.note
      ).subscribe({
        next: (response: any) => {
          this.ngxService.stop();
          this.responseMessage = response?.message;
          this.snackbarService.openSnackBar(this.responseMessage, "success");
          this.onAdd.emit();
          this.dialogRef.close();
        },
        error: (error) => {
          this.ngxService.stop();
          if (error.error?.message) {
            this.responseMessage = error.error?.message;
          }
          else {
            this.responseMessage = GlobalConstants.genericError;
          }
          this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
        }
      });
    }
  }

  // Helper methods for template
  getErrorMessage(field: string) {
    if (field === 'quantity') {
      if (this.addStockForm.get('quantity')?.hasError('required')) {
        return 'Quantity is required';
      }
      if (this.addStockForm.get('quantity')?.hasError('min')) {
        return 'Quantity must be greater than 0';
      }
      if (!this.isInitialSetup && this.addStockForm.get('quantity')?.value > this.dialogData.maxQuantity) {
        return `Quantity cannot exceed maximum stock (${this.dialogData.maxQuantity})`;
      }
    }
    if (field === 'minQuantity') {
      if (this.addStockForm.get('minQuantity')?.hasError('required')) {
        return 'Minimum quantity is required';
      }
      if (this.addStockForm.get('minQuantity')?.hasError('min')) {
        return 'Minimum quantity cannot be negative';
      }
    }
    if (field === 'maxQuantity') {
      if (this.addStockForm.get('maxQuantity')?.hasError('required')) {
        return 'Maximum quantity is required';
      }
      if (this.addStockForm.get('maxQuantity')?.hasError('min')) {
        return 'Maximum quantity must be greater than 0';
      }
      if (this.addStockForm.get('maxQuantity')?.hasError('maxQuantityInvalid')) {
        return 'Maximum quantity must be greater than minimum quantity';
      }
    }
    return '';
  }
}