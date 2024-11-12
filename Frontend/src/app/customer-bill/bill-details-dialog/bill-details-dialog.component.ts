import { Component, Inject, OnInit } from '@angular/core';
import { ThemePalette } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { Bill, BillItem, BillService } from 'src/app/services/bill.service';
import { ProductService } from 'src/app/services/product.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-bill-details-dialog',
  templateUrl: './bill-details-dialog.component.html',
  styleUrls: ['./bill-details-dialog.component.scss']
})
export class BillDetailsDialogComponent implements OnInit {
  isLoading = false;
  bill: Bill;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: Bill,
    private dialogRef: MatDialogRef<BillDetailsDialogComponent>,
    private billService: BillService,
    private productService: ProductService  
  ) {
    this.bill = data;
  }

  ngOnInit() {
    // Không cần loadProductDetails nữa vì dữ liệu đã có từ backend
  }

  getImageUrl(item: BillItem): string {
    if (item?.productImages && item.productImages.length > 0) {
      // Sử dụng service để lấy đường dẫn ảnh đúng, tương tự như BestSellerComponent
      return this.productService.getImageUrl(item.productImages[0]);
    }
    return 'assets/images/placeholder.png';
  }

  getProductDetails(item: BillItem): string {
    let details = item.productName;
    if (item.productCategory) {
      details += ` - ${item.productCategory}`;
    }
    if (item.productDescription) {
      details += `\n${item.productDescription}`;
    }
    return details;
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  }

  getOrderTypeLabel(type: string): string {
    return type === 'ONLINE' ? 'Online Order' : 'In-Store Purchase';
  }
  
  getOrderStatusColor(status: string): ThemePalette {
    switch (status?.toUpperCase()) {
      case 'PENDING': return 'warn';
      case 'CONFIRMED': return 'primary';
      case 'PROCESSING': return 'accent';
      case 'COMPLETED': return 'primary';
      case 'CANCELLED': return 'warn';
      default: return undefined;
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

  downloadPdf(): void {
    this.isLoading = true;
    this.billService.getPdf({ uuid: this.data.uuid }).subscribe({
      next: (response: Blob) => {
        const blob = new Blob([response], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `order-${this.data.uuid}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading PDF:', error);
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }
}