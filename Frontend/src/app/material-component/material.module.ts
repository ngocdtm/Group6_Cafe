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
import { ImagePreviewDialogComponent } from "./dialog/image-preview-dialog/image-preview-dialog.component";
import { ProductComponent } from "./dialog/product/product.component";
import { ViewBillProductsComponent } from "./dialog/view-bill-products/view-bill-products.component";
import { ManageCategoryComponent } from "./manage-category/manage-category.component";
import { ManageCouponComponent } from "./manage-coupon/manage-coupon.component";
import { ManageOrderComponent } from "./manage-order/manage-order.component";
import { ManageProductComponent } from "./manage-product/manage-product.component";
import { ManageUserComponent } from "./manage-user/manage-user.component";
import { MaterialRoutes } from "./material.routing";
import { ViewBillComponent } from "./view-bill/view-bill.component";
import { ViewDetailProductComponent } from "./view-detail-product/view-detail-product.component";


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
    CouponComponent
  ]
})
export class MaterialComponentsModule {}