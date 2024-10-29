import { Component, Inject, OnInit } from '@angular/core';
import { ThemePalette } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { forkJoin } from 'rxjs';
import { BillService } from 'src/app/services/bill.service';
import { ProductService } from 'src/app/services/product.service';

@Component({
  selector: 'app-bill-details-dialog',
  templateUrl: './bill-details-dialog.component.html',
  styleUrls: ['./bill-details-dialog.component.scss']
})
export class BillDetailsDialogComponent implements OnInit {
  isLoading = false;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<BillDetailsDialogComponent>,
    private billService: BillService,
    private productService: ProductService
  ) {}

  ngOnInit() {
    // Không cần loadProductDetails nữa vì dữ liệu đã có từ backend
  }

  getImageUrl(item: any): string {
    // Cập nhật cách lấy image url
    if (item?.images && item.images.length > 0) {
      const imagePath = item.images[0].imagePath;
      return imagePath ? this.productService.getImageUrl(imagePath) : 'assets/images/placeholder.png';
    }
    return 'assets/images/placeholder.png';
  }

  getProductName(item: any): string {
    return item?.productName || 'Product Name Not Available';
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
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