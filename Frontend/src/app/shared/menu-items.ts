import { Injectable } from "@angular/core";

export interface Menu{
  state:string;
  name:string;
  type:string;
  icon:string;
  role:string;
}

const MENUITEMS = [
  {state:'dashboard',name:'Dashboard',type:'link',icon:'dashboard',role:'ADMIN'},
  {state:'category',name:'Manage Category',type:'link',icon:'category',role:'ADMIN'},
  {state:'product',name:'Manage Product',type:'link',icon:'menu_book',role:'ADMIN'},
  {state:'order',name:'Create Order',type:'link',icon:'shopping_cart_checkout',role:'ADMIN'},
  {state:'bill',name:'Manage Order',type:'link',icon:'backup_table',role:'ADMIN'},
  {state:'user',name:'Manage User',type:'link',icon:'people',role:'ADMIN'},
  {state:'coupon',name:'Manage Coupon',type:'link',icon:'redeem',role:'ADMIN'},
  {state:'inventory',name:'Manage Inventory',type:'link',icon:'inventory',role:'ADMIN'},
  {state:'statistics',name:'Manage Statistics',type:'link',icon:'insert_chart',role:'ADMIN'}
]

@Injectable()
export class MenuItems{
  getMenuItem():Menu[]{
    return MENUITEMS;
  }
}
