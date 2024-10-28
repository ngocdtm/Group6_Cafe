import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { ProductPageComponent } from './product-page/product-page.component';
import { CustomerLayoutComponent } from './layouts/full/customer-layout/customer-layout.component';
import { SearchPageComponent } from './search-page/search-page.component';
import { CartComponent } from './cart/cart.component';
import { CheckoutComponent } from './checkout/checkout.component';
import { ProfileComponent } from './profile/profile.component';
import { BillHistoryComponent } from './bill-history/bill-history.component';
import { RecentlyViewedComponent } from './recently-viewed/recently-viewed.component';

const routes: Routes = [
  {
    path: '',
    component: CustomerLayoutComponent,
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: HomeComponent },
      { path: 'menu', component: ProductPageComponent },
      { path: 'search', component: SearchPageComponent },
      { path: 'cart', component: CartComponent },
      { path: 'checkout', component: CheckoutComponent },
      { path: 'profile', component: ProfileComponent },
      { path: 'bill-history', component: BillHistoryComponent },
      { path: 'recently-viewed', component: RecentlyViewedComponent }
      // { path: 'new', component: NewComponent }, // Uncomment when you have a NewComponent
    ]
  }
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CustomerRoutingModule { }

