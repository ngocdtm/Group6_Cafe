import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { ProductPageComponent } from './product-page/product-page.component';
import { CustomerLayoutComponent } from './layouts/full/customer-layout/customer-layout.component';
// import { CartPageComponent } from './cart-page/cart-page.component';
import { SearchPageComponent } from './search-page/search-page.component';
import { CartComponent } from './cart/cart.component';

const routes: Routes = [
  {
    path: '',
    component: CustomerLayoutComponent,
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: HomeComponent },
      { path: 'menu', component: ProductPageComponent },
      { path: 'search', component: SearchPageComponent },
      { path: 'cart', component: CartComponent }
      // { path: 'new', component: NewComponent }, // Uncomment when you have a NewComponent
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CustomerRoutingModule { }