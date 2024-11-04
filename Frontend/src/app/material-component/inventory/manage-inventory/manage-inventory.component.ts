import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { InventoryWrapper, InventoryService } from 'src/app/services/inventory.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';
import { RemoveStockComponent } from '../remove-stock/remove-stock.component';
import { AddStockComponent } from '../add-stock/add-stock.component';
import { ViewTransactionHistoryComponent } from '../view-transaction-history/view-transaction-history.component';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { FormControl } from '@angular/forms';
import { Observable } from 'rxjs';
import { Product, ProductService } from 'src/app/services/product.service';
import { map, startWith } from 'rxjs/operators';
import { UpdateMinMaxStockComponent } from '../update-min-max-stock/update-min-max-stock.component';

@Component({
  selector: 'app-manage-inventory',
  templateUrl: './manage-inventory.component.html',
  styleUrls: ['./manage-inventory.component.scss']
})
export class ManageInventoryComponent implements OnInit {

  displayedColumns: string[] = ['productName', 'quantity', 'minQuantity', 'maxQuantity', 'lastUpdated', 'action'];
  dataSource: MatTableDataSource<InventoryWrapper>;
  searchText: string = '';
  responseMessage: string = '';
  lowStockProducts: InventoryWrapper[] = [];

  // Product selection
  productControl = new FormControl('');
  availableProducts: Product[] = [];
  filteredProducts: Observable<Product[]>;

  // Add sorting and pagination
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
    
  constructor(
    private inventoryService: InventoryService,
    private productService: ProductService,
    private ngxService: NgxUiLoaderService,
    private dialog: MatDialog,
    private snackbarService: SnackbarService
  ) {
    this.dataSource = new MatTableDataSource();
    
    // Sửa lại phần khởi tạo filteredProducts
    this.filteredProducts = this.productControl.valueChanges.pipe(
      startWith(''),
      map(value => {
        const search = typeof value === 'string' ? value : value?.name;
        return this._filterProducts(search || '');
      })
    );
  }

  ngOnInit(): void {
    setTimeout(() => {
      this.ngxService.start();
      this.loadInventoryData();
      this.loadAvailableProducts();
    });
  }

  private _filterProducts(value: string): Product[] {
    const filterValue = value.toLowerCase();
    return this.availableProducts.filter(product => 
      product.name.toLowerCase().includes(filterValue));
  }

  // Thêm method để hiển thị tên sản phẩm trong autocomplete
  displayProductFn(product: Product): string {
    return product && product.name ? product.name : '';
  }

  loadAvailableProducts() {
    this.productService.getProduct().subscribe({
      next: (response: any) => {
        this.availableProducts = response.filter((product: Product) => 
          !this.dataSource.data.some(inv => inv.productId === product.id)
        );
      },
      error: (error: any) => {
        console.error(error);
        this.snackbarService.openSnackBar(
          error.error?.message || GlobalConstants.genericError,
          GlobalConstants.error
        );
      }
    });
  }

  handleInitializeInventory(product: Product) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      productId: product.id,
      productName: product.name,
      currentQuantity: 0,
      maxQuantity: 0,
      isInitialSetup: true
    };
    dialogConfig.width = "550px";
    
    const dialogRef = this.dialog.open(AddStockComponent, dialogConfig);
    const dialogComponentInstance = dialogRef.componentInstance;
    
    dialogComponentInstance.onAdd.subscribe(() => {
      this.loadInventoryData();
      this.loadAvailableProducts();
      this.productControl.setValue('');
    });
  }
  
  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadInventoryData() {
    this.inventoryService.getAllInventory().subscribe({
      next: (response: InventoryWrapper[]) => {
        this.ngxService.stop();
        this.dataSource.data = response;
        this.updateLowStockProducts();
        this.setupTableFilters();
      },
      error: (error: any) => {
        this.ngxService.stop();
        console.error(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      }
    });
  }

  updateLowStockProducts() {
    this.lowStockProducts = this.dataSource.data.filter(
      item => item.quantity <= item.minQuantity
    );
  }

  setupTableFilters() {
    this.dataSource.filterPredicate = (data: InventoryWrapper, filter: string) => {
      return data.productName.toLowerCase().includes(filter.toLowerCase()) ||
             data.quantity.toString().includes(filter) ||
             data.minQuantity.toString().includes(filter) ||
             data.maxQuantity.toString().includes(filter);
    };
  }

  applyFilter() {
    this.dataSource.filter = this.searchText.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  handleUpdateMinMaxStock(inventory: InventoryWrapper) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      productId: inventory.productId,
      productName: inventory.productName,
      currentMinQuantity: inventory.minQuantity,
      currentMaxQuantity: inventory.maxQuantity
    };
    dialogConfig.width = "550px";
  
    const dialogRef = this.dialog.open(UpdateMinMaxStockComponent, dialogConfig);
    const dialogComponentInstance = dialogRef.componentInstance;
  
    dialogComponentInstance.onUpdate.subscribe(() => {
      this.loadInventoryData();
    });
  }

  handleAddStock(inventory: InventoryWrapper) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      productId: inventory.productId,
      productName: inventory.productName,
      currentQuantity: inventory.quantity,
      maxQuantity: inventory.maxQuantity
    };
    dialogConfig.width = "550px";
    
    const dialogRef = this.dialog.open(AddStockComponent, dialogConfig);
    const dialogComponentInstance = dialogRef.componentInstance;
    
    dialogComponentInstance.onAdd.subscribe(() => {
      this.loadInventoryData();
    });
  }

  handleRemoveStock(inventory: InventoryWrapper) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      productId: inventory.productId,
      productName: inventory.productName,
      currentQuantity: inventory.quantity,
      minQuantity: inventory.minQuantity
    };
    dialogConfig.width = "550px";
    
    const dialogRef = this.dialog.open(RemoveStockComponent, dialogConfig);
    const dialogComponentInstance = dialogRef.componentInstance;
    
    dialogComponentInstance.onRemove.subscribe(() => {
      this.loadInventoryData();
    });
  }

  viewTransactionHistory(inventory: InventoryWrapper) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      productId: inventory.productId,
      productName: inventory.productName
    };
    dialogConfig.width = "850px";
    
    this.dialog.open(ViewTransactionHistoryComponent, dialogConfig);
  }

  getStockStatus(inventory: InventoryWrapper): { color: string; message: string } {
    const stockPercentage = (inventory.quantity / inventory.maxQuantity) * 100;
    
    if (inventory.quantity <= inventory.minQuantity) {
      return { color: '#dc3545', message: 'Low Stock' };
    } else if (stockPercentage >= 90) {
      return { color: '#28a745', message: 'Well Stocked' };
    } else if (stockPercentage >= 50) {
      return { color: '#28a745', message: 'Good' };
    } else {
      return { color: '#ffc107', message: 'Medium' };
    }
  }

  exportToExcel() {
    // Implement export functionality if needed
    // You can use libraries like xlsx for this
  }

}