import { CategoryService } from './../../services/category.service';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { saveAs } from 'file-saver';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { BillService } from 'src/app/services/bill.service';
import { ProductService } from 'src/app/services/product.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';

interface Product {
  id: number;
  quantity: number;
  price: number;
}

@Component({
  selector: 'app-manage-order',
  templateUrl: './manage-order.component.html',
  styleUrls: ['./manage-order.component.scss']
})


export class ManageOrderComponent implements OnInit {
  displayedColumns: string[] = ['name', 'category', 'price', 'quantity', 'total', 'edit'];
  dataSource: any = [];
  manageOrderForm: any = FormGroup;
  categories: any = [];
  products: any = [];
  price: any;
  total: number = 0;
  totalAfterDiscount: number = 0;
  responseMessage: any;
  discount: number = 0;
  appliedCouponCode: any;

  constructor(
    private formBuilder: FormBuilder,
    private categoryService: CategoryService,
    private productService: ProductService,
    private billService: BillService,
    private ngxService: NgxUiLoaderService,
    private snackbarService: SnackbarService
  ) { }

  ngOnInit(): void {
    this.ngxService.start();
    this.getCategories();
    this.initForm();
  }

  initForm() {
    this.manageOrderForm = this.formBuilder.group({
      name: [null, [Validators.required, Validators.pattern(GlobalConstants.nameRegex)]],
      phoneNumber: [null, [Validators.required, Validators.pattern(GlobalConstants.phoneNumberRegex)]],
      paymentMethod: [null, [Validators.required]],
      product: [null, [Validators.required]],
      category: [null, [Validators.required]],
      quantity: [null, [Validators.required]],
      price: [null, [Validators.required]],
      total: [0, [Validators.required]],
      totalAfterDiscount: [0, [Validators.required]],
      discount: [0, [Validators.required]],
      code: [null]
    });
  }

  getCategories() {
    this.categoryService.getFilteredCategory().subscribe(
      (response: any) => {
        this.ngxService.stop();
        this.categories = response;
      },
      (error: any) => {
        this.ngxService.stop();
        this.handleError(error);
      }
    );
  }

  getProductsByCategory(value: any) {
    this.productService.getProductsByCategory(value.id).subscribe(
      (response: any) => {
        this.products = response;
        this.resetProductForm();
      },
      (error: any) => {
        this.handleError(error);
      }
    );
  }

  resetProductForm() {
    this.manageOrderForm.patchValue({
      price: '',
      quantity: '',
      total: 0
    });
  }

  getProductDetails(value: any) {
    this.productService.getById(value.id).subscribe(
      (response: any) => {
        if (!response || response.price <= 0) {
          this.snackbarService.openSnackBar('Invalid product price', 'error');
          return;
        }
        this.price = response.price;
        this.manageOrderForm.patchValue({
          price: response.price,
          quantity: '1',
          total: response.price * 1
        });
      },
      (error: any) => {
        this.handleError(error);
      }
    );
  }

  setQuantity(event: any) {
    const quantity = this.manageOrderForm.get('quantity').value;
    const price = this.manageOrderForm.get('price').value;

    if (quantity > 0) {
      this.manageOrderForm.patchValue({
        total: quantity * price
      });
    } else if (quantity !== '') {
      this.manageOrderForm.patchValue({
        quantity: '1',
        total: price
      });
    }
  }

  validateProductAdd(): boolean {
    const formValues = this.manageOrderForm.value;
    return formValues.total === 0 || 
           formValues.total === null || 
           formValues.quantity <= 0;
  }

  validateSubmit(): boolean {
    const formValues = this.manageOrderForm.value;
    return this.total <= 0 || // Check total > 0
           !formValues.name || 
           !formValues.phoneNumber || 
           !formValues.paymentMethod || 
         this.dataSource.length === 0; // Ensure at least one product is added
  }

  add() {
    const formData = this.manageOrderForm.value;
    const existingProduct = this.dataSource.find((e: { id: number }) => e.id === formData.product.id);

    if (!existingProduct) {
      this.total += formData.total;
      this.dataSource.push({
        id: formData.product.id,
        name: formData.product.name,
        category: formData.category.name,
        quantity: formData.quantity,
        price: formData.price,
        total: formData.total
      });
      this.dataSource = [...this.dataSource];
      this.snackbarService.openSnackBar(GlobalConstants.productAdded, 'success');
    } else {
      this.snackbarService.openSnackBar(GlobalConstants.productExistError, GlobalConstants.error);
    }
  }

  handleDeleteAction(index: number, element: any) {
    this.total -= element.total;
    this.dataSource.splice(index, 1);
    this.dataSource = [...this.dataSource];
    // Reset discount details when products are removed
    this.resetDiscountDetails();
  }

  applyCoupon() {
    const couponCode = this.manageOrderForm.get('code').value;
    
    if (!couponCode) {
      this.snackbarService.openSnackBar('Please enter a coupon code', 'error');
      return;
    }

    if (this.total <= 0) {
      this.snackbarService.openSnackBar('Please add products before applying coupon', 'error');
      return;
    }

    const requestData = {
      couponCode: couponCode,
      total: this.total
    };

    this.ngxService.start();
    this.billService.applyCoupon(requestData).subscribe(
      (response: any) => {
        this.ngxService.stop();
        if (response && response.message) {
          this.snackbarService.openSnackBar(response.message, 'success');
          
          // Cập nhật thông tin giảm giá
          this.updateDiscountDetails({
            total: response.total,
            totalAfterDiscount: response.totalAfterDiscount,
            discount: response.discountAmount,
            code: response.couponCode
          });
        }
      },
      (error) => {
        this.ngxService.stop();
        let errorMessage = error.error?.message || GlobalConstants.genericError;
        this.snackbarService.openSnackBar(errorMessage, 'error');
        
        // Reset form nếu có lỗi
        this.manageOrderForm.patchValue({
          code: ''
        });
      }
    );
  }

  removeCoupon() {
    // Reset lại các giá trị liên quan đến giảm giá
    this.resetDiscountDetails();
    this.manageOrderForm.patchValue({
      code: ''
    });
    this.snackbarService.openSnackBar('Coupon removed successfully', 'success');
  }

  updateDiscountDetails(response: any) {
    this.total = response.total;
    this.totalAfterDiscount = response.totalAfterDiscount;
    this.discount = response.discount;
    this.appliedCouponCode = response.code;
    
    // Cập nhật form values
    this.manageOrderForm.patchValue({
      total: this.total,
      totalAfterDiscount: this.totalAfterDiscount,
      discount: this.discount
    });
  }

  resetDiscountDetails() {
    this.totalAfterDiscount = this.total;
    this.discount = 0;
    this.appliedCouponCode = null;
    
    // Reset form values
    this.manageOrderForm.patchValue({
      totalAfterDiscount: this.total,
      discount: 0,
      code: ''
    });
  }
  
  submitAction() {
    const formData = this.manageOrderForm.value;
    if (this.validateSubmit()) {
      this.snackbarService.openSnackBar('Please fill all required fields and add at least one product.', 'error');
      return;
    }

    // Explicitly define the type for the item parameter
  const productDetailsJson = JSON.stringify(this.dataSource.map((item: Product) => ({
    id: item.id,
    quantity: item.quantity,
    price: item.price
  })));

    const orderData = {
        customerName: formData.name, 
        customerPhone: formData.phoneNumber,
        paymentMethod: formData.paymentMethod,
        total: this.total,
        totalAfterDiscount: this.totalAfterDiscount || this.total,
        productDetails: productDetailsJson, // Convert to JSON string
        discount: this.discount || 0,
        couponCode: this.appliedCouponCode || null
    };

    this.ngxService.start();
    this.billService.generateOfflineBill(orderData).subscribe(
        (response: any) => {
            this.downloadFile(response?.uuid);
            this.resetForm();
        },
        (error: any) => {
            this.ngxService.stop();
            this.handleError(error);
        }
    );
}

  downloadFile(fileName: string) {
    const data = { uuid: fileName };
    this.billService.getPdf(data).subscribe(
      (response: any) => {
        const blob = new Blob([response], { type: 'application/pdf' });
        saveAs(blob, fileName + '.pdf');
        this.ngxService.stop();
      },
      (error: any) => {
        this.ngxService.stop();
        this.snackbarService.openSnackBar('Error downloading file', 'Error');
      }
    );
  }

  resetForm() {
    this.manageOrderForm.reset();
    this.dataSource = [];
    this.total = 0;
    this.totalAfterDiscount = 0;
    this.discount = 0;
    this.appliedCouponCode = null;
    this.initForm(); // Reset with default values
  }

  handleError(error: any) {
    console.error(error);
    if (error.error?.message) {
      this.responseMessage = error.error?.message;
    } else {
      this.responseMessage = GlobalConstants.genericError;
    }
    this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
  }
}