import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FlexLayoutModule } from '@angular/flex-layout';
import { CustomerRoutingModule } from './customer-routing.module';
import { HomeComponent } from './home/home.component';
import { ProductPageComponent } from './product-page/product-page.component';
import { BestSellerComponent } from './best-seller/best-seller.component';
import { MaterialModule } from './shared/material-module';
import { CustomerMenuItems } from './shared/customer-menu-items';
import { MatSliderModule } from '@angular/material/slider';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CustomerLayoutComponent } from './layouts/full/customer-layout/customer-layout.component';
import { SearchPageComponent } from './search-page/search-page.component';
import { SearchToolComponent } from './search-tool/search-tool.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { ProductHistoryComponent } from './material-component/product-history/product-history.component';

@NgModule({
  declarations: [
    HomeComponent,
    ProductPageComponent,
    BestSellerComponent,
    CustomerLayoutComponent,
    SearchPageComponent,
    SearchToolComponent,
    ProductHistoryComponent
  ],
  imports: [
    FormsModule,
    CommonModule,
    RouterModule,
    MaterialModule,
    FlexLayoutModule,
    CustomerRoutingModule,
    MatSliderModule,
    MatSliderModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ],
  providers: [CustomerMenuItems]
})
export class CustomerModule { }