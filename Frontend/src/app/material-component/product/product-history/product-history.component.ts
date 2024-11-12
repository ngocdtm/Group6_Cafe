import { DatePipe } from '@angular/common';
import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
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

  dataSource: MatTableDataSource<ProductHistory>;
  displayedColumns = ['modifiedDate', 'modifiedBy', 'action', 'changes'];
  actionIcons: any = {
    'CREATE': 'add_circle',
    'UPDATE': 'edit',
    'DELETE': 'delete',
    'RESTORE': 'restore',
    'ADD_IMAGES': 'add_photo_alternate',
    'DELETE_IMAGES': 'delete_sweep',
    'STATUS_CHANGE': 'swap_horiz'
  };

  // Add sorting and pagination
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  searchText: string = '';
  
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { productId: number },
    private productService: ProductService,
    private snackbarService: SnackbarService
  ) {
    this.dataSource = new MatTableDataSource();
  }

  ngOnInit() {
    this.loadHistory();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadHistory() {
    this.productService.getProductHistory(this.data.productId).subscribe({
      next: (response: ProductHistory[]) => {
        this.dataSource = new MatTableDataSource<ProductHistory>(
          response.map(history => ({
            ...history,
            modifiedDate: history.modifiedDate,
            formattedChanges: this.formatChanges(history)
          }))
        );
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.applyFilter();
      },
      error: (error) => {
        let errorMessage = GlobalConstants.genericError;
        if (error.status === 401) {
          errorMessage = 'Unauthorized access';
        } else if (error.status === 404) {
          errorMessage = 'Product not found';
        }
        this.snackbarService.openSnackBar(errorMessage, GlobalConstants.error);
      }
    });
  }

  applyFilter() {
    this.dataSource.filterPredicate = (data: ProductHistory, filter: string) => {
      return (
        data.modifiedBy.toLowerCase().includes(filter.toLowerCase()) ||
        data.action.toLowerCase().includes(filter.toLowerCase())
      );
    };
    this.dataSource.filter = this.searchText.trim().toLowerCase();
  }

  clearSearch() {
    this.searchText = '';
    this.applyFilter();
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