import { DatePipe } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ProductHistory, ProductService } from 'src/app/services/product.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';

@Component({
  selector: 'app-product-history',
  templateUrl: './product-history.component.html',
  styleUrls: ['./product-history.component.scss'],
  providers: [DatePipe]
})
export class ProductHistoryComponent implements OnInit {

  historyData: ProductHistory[] = [];
  loading = false;
  error = '';
  displayedColumns = ['modifiedDate', 'modifiedBy', 'action', 'changes'];
  actionIcons : any= {
    'CREATE': 'add_circle',
    'UPDATE': 'edit',
    'DELETE': 'delete',
    'RESTORE': 'restore',
    'ADD_IMAGES': 'add_photo_alternate',
    'DELETE_IMAGES': 'delete_sweep',
    'STATUS_CHANGE': 'swap_horiz'
  };

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { productId: number },
    private productService: ProductService,
    private snackbarService: SnackbarService,
    private datePipe: DatePipe
  ) {}

  ngOnInit() {
    this.loadHistory();
  }

  loadHistory() {
    this.loading = true;
    this.error = '';
  
    this.productService.getProductHistory(this.data.productId).subscribe({
      next: (response: ProductHistory[]) => {
        this.historyData = response.map(history => ({
          ...history,
          modifiedDate: history.modifiedDate,
          formattedChanges: this.formatChanges(history)
        }));
      },
      error: (error) => {
        if (error.status === 401) {
          this.error = 'Unauthorized access';
        } else if (error.status === 404) {
          this.error = 'Product not found';
        } else {
          this.error = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.error, GlobalConstants.error);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  formatDate(dateString: string): string {
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        throw new Error('Invalid date');
      }
      return this.datePipe.transform(date, 'MMM d, y, h:mm:ss a') || dateString;
    } catch (error) {
      console.error('Error formatting date:', error);
      return dateString;
    }
  }

  getActionIcon(action: string): string {
    return this.actionIcons[action] || 'info';
  }

  getActionColor(action: string): string {
    switch (action) {
      case 'CREATE':
        return 'text-green-500';
      case 'DELETE':
        return 'text-red-500';
      case 'RESTORE':
        return 'text-blue-500';
      case 'UPDATE':
        return 'text-orange-500';
      case 'ADD_IMAGES':
        return 'text-purple-500';
      case 'DELETE_IMAGES':
        return 'text-pink-500';
      case 'STATUS_CHANGE':
        return 'text-yellow-500';
      default:
        return 'text-gray-500';
    }
  }

  formatChanges(history: ProductHistory): string {
    try {
      let changes = '';
  
      switch (history.action) {
        case 'CREATE':
          if (history.newData) {
            const newProduct = JSON.parse(history.newData);
            changes = `Created new product: ${newProduct.name}`;
            if (newProduct.price) {
              changes += `\nPrice: ${newProduct.price}`;
            }
            if (newProduct.description) {
              changes += `\nDescription: ${newProduct.description}`;
            }
          }
          break;
  
        case 'UPDATE':
          if (history.previousData) {
            const changesObj = JSON.parse(history.previousData);
            changes = Object.entries(changesObj)
              .map(([field, value]: [string, any]) => {
                if (Array.isArray(value)) {
                  return `Changed ${field} from "${value[0]}" to "${value[1]}"`;
                }
                return '';
              })
              .filter(change => change)
              .join('\n');
          }
          break;
  
        case 'DELETE':
          if (history.previousData) {
            const deletedProduct = JSON.parse(history.previousData);
            changes = `Product "${deletedProduct.name}" was deleted`;
          } else {
            changes = 'Product was deleted';
          }
          break;
  
        case 'RESTORE':
          if (history.newData) {
            const restoredProduct = JSON.parse(history.newData);
            changes = `Product "${restoredProduct.name}" was restored`;
          } else {
            changes = 'Product was restored';
          }
          break;
  
        case 'ADD_IMAGES':
          if (history.newData) {
            const imageData = JSON.parse(history.newData);
            const imageCount = imageData.images?.length || 0;
            changes = `Added ${imageCount} new image${imageCount !== 1 ? 's' : ''}`;
            if (imageData.images?.length > 0) {
              changes += ':\n' + imageData.images.join('\n');
            }
          }
          break;
  
        case 'DELETE_IMAGES':
          if (history.previousData) {
            const imageData = JSON.parse(history.previousData);
            const imageCount = imageData.images?.length || 0;
            changes = `Deleted ${imageCount} image${imageCount !== 1 ? 's' : ''}`;
            if (imageData.images?.length > 0) {
              changes += ':\n' + imageData.images.join('\n');
            }
          }
          break;
  
        case 'STATUS_CHANGE':
          if (history.previousData) {
            const statusData = JSON.parse(history.previousData);
            changes = `Status changed from "${statusData.previousStatus}" to "${statusData.newStatus}"`;
          }
          break;
  
        default:
          changes = history.details || 'No detailed information available';
      }
  
      return changes || 'No changes recorded';
    } catch (error) {
      console.error('Error formatting changes:', error);
      return 'Error formatting change details';
    }
  }
}