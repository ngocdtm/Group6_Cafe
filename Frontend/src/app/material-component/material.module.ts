import { CdkTableModule } from "@angular/cdk/table";
import { CommonModule } from "@angular/common";
import { HttpClientModule } from "@angular/common/http";
import { NgModule } from "@angular/core";
import { FlexLayoutModule } from "@angular/flex-layout";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { MaterialModule } from "../shared/material-module";
import { CategoryComponent } from "./dialog/category/category.component";
import { ChangePasswordComponent } from "./dialog/change-password/change-password.component";
import { ConfirmationComponent } from "./dialog/confirmation/confirmation.component";
import { CouponComponent } from "./dialog/coupon/coupon.component";
import { ImagePreviewDialogComponent } from "./product/image-preview-dialog/image-preview-dialog.component";
import { ProductComponent } from "./product/product/product.component";
import { ViewBillProductsComponent } from "./dialog/view-bill-products/view-bill-products.component";
import { ManageCategoryComponent } from "./manage-category/manage-category.component";
import { ManageCouponComponent } from "./manage-coupon/manage-coupon.component";
import { ManageOrderComponent } from "./manage-order/manage-order.component";
import { ManageProductComponent } from "./product/manage-product/manage-product.component";
import { ManageUserComponent } from "./manage-user/manage-user.component";
import { MaterialRoutes } from "./material.routing";
import { ViewBillComponent } from "./view-bill/view-bill.component";
import { ViewDetailProductComponent } from "./product/view-detail-product/view-detail-product.component";
import { ProductDetailDialogComponent } from './dialog/product-detail-dialog/product-detail-dialog.component';
import { BillDetailsDialogComponent } from '../customer-bill/bill-details-dialog/bill-details-dialog.component';
import { UserDetailsDialogComponent } from './dialog/user-details-dialog/user-details-dialog.component';
import { ManageInventoryComponent } from './inventory/manage-inventory/manage-inventory.component';
import { AddStockComponent } from './inventory/add-stock/add-stock.component';
import { RemoveStockComponent } from './inventory/remove-stock/remove-stock.component';
import { ViewTransactionHistoryComponent } from './inventory/view-transaction-history/view-transaction-history.component';
import { UpdateMinMaxStockComponent } from './inventory/update-min-max-stock/update-min-max-stock.component';
import { ProductHistoryComponent } from './product/product-history/product-history.component';
import { StatisticsComponent } from './statistics/statistics.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(MaterialRoutes),
    MaterialModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    FlexLayoutModule,
    CdkTableModule,
  ],
  providers: [],
  declarations: [
    ViewBillProductsComponent,
    ConfirmationComponent,
    ChangePasswordComponent,
    CategoryComponent,
    ManageCategoryComponent,
    ManageProductComponent,
    ProductComponent,
    ManageOrderComponent,
    ViewBillComponent,
    ManageUserComponent,
    ImagePreviewDialogComponent,
    ViewDetailProductComponent,
    ManageCouponComponent,
    CouponComponent,
    ProductDetailDialogComponent,
    BillDetailsDialogComponent,
    UserDetailsDialogComponent,
    ManageInventoryComponent,
    AddStockComponent,
    RemoveStockComponent,
    ViewTransactionHistoryComponent,
    UpdateMinMaxStockComponent,
    ProductHistoryComponent,
    StatisticsComponent
  ]
})
export class MaterialComponentsModule {}