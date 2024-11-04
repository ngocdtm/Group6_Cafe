import { NgxUiLoaderService } from 'ngx-ui-loader';
import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { Router } from '@angular/router';
import { MatTableDataSource } from '@angular/material/table';
import { GlobalConstants } from 'src/app/shared/global-constants';
import { ViewDetailProductComponent } from '../view-detail-product/view-detail-product.component';
import { ProductService } from 'src/app/services/product.service';
import { ConfirmationComponent } from '../../dialog/confirmation/confirmation.component';
import { ImagePreviewDialogComponent } from '../image-preview-dialog/image-preview-dialog.component';
import { ProductComponent } from '../product/product.component';
import { forkJoin } from 'rxjs';
import { ProductHistoryComponent } from '../product-history/product-history.component';


@Component({
  selector: 'app-manage-product',
  templateUrl: './manage-product.component.html',
  styleUrls: ['./manage-product.component.scss']
})
export class ManageProductComponent implements OnInit {


  displayedColumns: string[] = ['images', 'name', 'categoryName', 'price', 'originalPrice', 'action'];
 
  dataSource: MatTableDataSource<any>;
  activeDataSource: MatTableDataSource<any>;
  deletedDataSource: MatTableDataSource<any>;
  responseMessage: any;
  categories: Set<string> = new Set();
  selectedCategory: string = '';
  searchText: string = '';
  selectedTab: number = 0;


  constructor(
    public productService: ProductService,
    private ngxService: NgxUiLoaderService,
    private dialog: MatDialog,
    private snackbarService: SnackbarService,
    private router: Router
  ) {
    this.dataSource = new MatTableDataSource();
    this.activeDataSource = new MatTableDataSource();
    this.deletedDataSource = new MatTableDataSource();
  }


  ngOnInit(): void {
    this.loadAllData();
  }


  loadAllData() {
    this.ngxService.start();
    // Load both active and deleted products
    forkJoin({
      active: this.productService.getActiveProducts(),
      deleted: this.productService.getDeletedProducts()
    }).subscribe({
      next: (response: any) => {
        this.ngxService.stop();
       
        // Process active products
        this.activeDataSource = new MatTableDataSource(
          response.active.map((product: any) => ({
            ...product,
            images: product.images || []
          }))
        );
       
        // Process deleted products
        this.deletedDataSource = new MatTableDataSource(
          response.deleted.map((product: any) => ({
            ...product,
            images: product.images || []
          }))
        );


        // Gather all unique categories from both active and deleted products
        this.categories = new Set([
          ...response.active.map((product: any) => product.categoryName),
          ...response.deleted.map((product: any) => product.categoryName)
        ]);


        // Apply filters for both tables
        this.applyFilters();
      },
      error: (error) => {
        this.handleError(error);
      }
    });
  }


  loadData() {
    if (this.selectedTab === 0) {
      this.loadActiveProducts();
    } else {
      this.loadDeletedProducts();
    }
  }


  loadActiveProducts() {
    this.productService.getActiveProducts().subscribe(
      (response: any) => {
        this.ngxService.stop();
        this.activeDataSource = new MatTableDataSource(response.map((product: any) => ({
          ...product,
          images: product.images || []
        })));
        this.dataSource = this.activeDataSource;
        this.categories = new Set(response.map((product: any) => product.categoryName));
        this.applyFilters();
      },
      (error: any) => {
        this.handleError(error);
      }
    );
  }


  loadDeletedProducts() {
    this.productService.getDeletedProducts().subscribe(
      (response: any) => {
        this.ngxService.stop();
        this.deletedDataSource = new MatTableDataSource(response.map((product: any) => ({
          ...product,
          images: product.images || []
        })));
        this.dataSource = this.deletedDataSource;
        this.categories = new Set(response.map((product: any) => product.categoryName));
        this.applyFilters();
      },
      (error: any) => {
        this.handleError(error);
      }
    );
  }


  onTabChange(event: any) {
    this.selectedTab = event.index;
    this.searchText = '';
    this.selectedCategory = '';
    this.loadData();
  }


  handleRestoreAction(values: any) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      message: 'restore ' + values.name + ' product?',
      confirmation: true
    };
    const dialogRef = this.dialog.open(ConfirmationComponent, dialogConfig);
    const sub = dialogRef.componentInstance.onEmitStatusChange.subscribe((response) => {
      this.ngxService.start();
      this.restoreProduct(values.id);
      dialogRef.close();
    });
  }


  restoreProduct(id: number) {
    this.productService.restoreProduct(id).subscribe(
      (response: any) => {
        this.ngxService.stop();
        this.responseMessage = response?.message;
        this.snackbarService.openSnackBar(this.responseMessage, "success");
        this.loadData();
      },
      (error: any) => {
        this.handleError(error);
      }
    );
  }


  private handleError(error: any) {
    this.ngxService.stop();
    console.log(error);
    if (error.error?.message) {
      this.responseMessage = error.error?.message;
    } else {
      this.responseMessage = GlobalConstants.genericError;
    }
    this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
  }


  viewImages(product: any): void {
    const images = product.images.map((image: any) => this.productService.getImageUrl(image.imagePath));
    this.dialog.open(ImagePreviewDialogComponent, {
      width: '80%',
      data: { images: images }
    });
  }
 
  tableData() {
    this.productService.getProduct().subscribe(
      (response: any) => {
        this.ngxService.stop();
        this.dataSource = new MatTableDataSource(response.map((product: any) => ({
          ...product,
          images: product.images || []
        })));
        // Extract unique categories
        this.categories = new Set(response.map((product: any) => product.categoryName));
        this.applyFilters();
      },
      (error: any) => {
        this.ngxService.stop();
        console.log(error.error?.message);
        console.error(error.error?.message);
        this.responseMessage = GlobalConstants.genericError;
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      }
    );
  }
  handleViewAction(values:any){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      data:values
    };
    dialogConfig.width = "100%";
    const dialogRef = this.dialog.open(ViewDetailProductComponent,dialogConfig);
    this.router.events.subscribe(()=>{
      dialogRef.close();
    });
  }


   applyFilters() {
    const filterFunction = (data: any): boolean => {
      const matchesSearch = this.searchText ?
        data.name.toLowerCase().includes(this.searchText.toLowerCase()) : true;
     
      const matchesCategory = this.selectedCategory ?
        data.categoryName === this.selectedCategory : true;
     
      return matchesSearch && matchesCategory;
    };


    this.activeDataSource.filterPredicate = filterFunction;
    this.deletedDataSource.filterPredicate = filterFunction;


    // Trigger filtering for both data sources
    this.activeDataSource.filter = 'trigger';
    this.deletedDataSource.filter = 'trigger';
  }


  onSearch(event: Event) {
    this.searchText = (event.target as HTMLInputElement).value;
    this.applyFilters();
  }


  onCategoryChange() {
    this.applyFilters();
  }


  clearFilters() {
    this.searchText = '';
    this.selectedCategory = '';
    this.applyFilters();
  }


  viewHistory(product: any) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = { productId: product.id };
    dialogConfig.width = "800px";
    this.dialog.open(ProductHistoryComponent, dialogConfig);
  }


  handleAddAction(){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      action: 'Add'
    };
    dialogConfig.width = "850px";
    const dialogRef = this.dialog.open(ProductComponent,dialogConfig);
    this.router.events.subscribe(()=>{
      dialogRef.close();
    });
    const sub = dialogRef.componentInstance.onAddProduct.subscribe((response)=>{
      this.tableData();
    });
  }


  handleEditAction(values:any){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      action: 'Edit',
      data: values
    };
    dialogConfig.width = "850px";
    const dialogRef = this.dialog.open(ProductComponent,dialogConfig);
    this.router.events.subscribe(()=>{
      dialogRef.close();
    });
    const sub = dialogRef.componentInstance.onEditProduct.subscribe((response)=>{
      this.tableData();
    });
  }


  handleDeleteAction(values:any){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      message: 'delete ' + values.name + ' product.',
      confirmation:true
    };
    const dialogRef = this.dialog.open(ConfirmationComponent,dialogConfig);
    const sub = dialogRef.componentInstance.onEmitStatusChange.subscribe((response) => {
      this.ngxService.start();
      this.deleteProduct(values.id);
      dialogRef.close();
    });
  }


  deleteProduct(id: any) {
    this.productService.delete(id).subscribe(
      (response: any) => {
        this.ngxService.stop();
        this.responseMessage = response?.message;
        this.snackbarService.openSnackBar(this.responseMessage, "success");
        this.loadData();
      },
      (error: any) => {
        this.handleError(error);
      }
    );
  }


  onChange(status:any,id:any){
    this.ngxService.start();
    var data = {
      status: status.toString(),
      id:id
    }
    this.productService.updateStatus(data).subscribe((response:any)=>{
      this.ngxService.stop();
      this.responseMessage = response?.message;
      this.snackbarService.openSnackBar(this.responseMessage, "success");
    },(error:any)=>{
      this.ngxService.stop();
      console.log(error);
      if(error.error?.message){
        this.responseMessage = error.error?.message;
      }else{
        this.responseMessage = GlobalConstants.genericError;
      }
      this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
    })
  }
}

