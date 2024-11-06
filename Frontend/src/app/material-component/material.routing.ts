import { Routes } from '@angular/router';
import { RouteGuardService } from '../services/route-guard.service';
import { ManageCategoryComponent } from './manage-category/manage-category.component';
import { ManageProductComponent } from './product/manage-product/manage-product.component';
import { ManageOrderComponent } from './manage-order/manage-order.component';
import { ViewBillComponent } from './view-bill/view-bill.component';
import { ManageUserComponent } from './manage-user/manage-user.component';
import { ManageCouponComponent } from './manage-coupon/manage-coupon.component';
import { ManageInventoryComponent } from './inventory/manage-inventory/manage-inventory.component';
import { StatisticsComponent } from './statistics/statistics.component';

export const MaterialRoutes: Routes = [
  {
    path: 'category',
    component:ManageCategoryComponent,
    canActivate:[RouteGuardService],
    data:{
      expectedRole: ['ADMIN']
    }
  },
  {
    path: 'product',
    component:ManageProductComponent,
    canActivate:[RouteGuardService],
    data:{
      expectedRole: ['ADMIN']
    }
  },
  {
    path: 'order',
    component:ManageOrderComponent,
    canActivate:[RouteGuardService],
    data:{
      expectedRole: ['ADMIN']
    }
  },
  {
    path: 'bill',
    component:ViewBillComponent,
    canActivate:[RouteGuardService],
    data:{
      expectedRole: ['ADMIN']
    }
  },
  {
    path: 'user',
    component:ManageUserComponent,
    canActivate:[RouteGuardService],
    data:{
      expectedRole: ['ADMIN']
    }
  },
  {
    path: 'coupon',
    component:ManageCouponComponent,
    canActivate:[RouteGuardService],
    data:{
      expectedRole: ['ADMIN']
    }
  },
  {
    path: 'inventory',
    component:ManageInventoryComponent,
    canActivate:[RouteGuardService],
    data:{
      expectedRole: ['ADMIN']
    }
  },
  {
    path: 'statistics',
    component:StatisticsComponent,
    canActivate:[RouteGuardService],
    data:{
      expectedRole: ['ADMIN']
    }
  }
];