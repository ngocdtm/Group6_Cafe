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
  {state:'product',name:'Manage Product',type:'link',icon:'inventory_2',role:'ADMIN'},
  {state:'order',name:'Manage Order',type:'link',icon:'shopping_cart',role:'ADMIN'},
  {state:'bill',name:'View Bill',type:'link',icon:'backup_table',role:'ADMIN'},
  {state:'user',name:'Manage User',type:'link',icon:'people',role:'ADMIN'},
  {state:'coupon',name:'Manage Coupon',type:'link',icon:'redeem',role:'ADMIN'}
]

@Injectable()
export class MenuItems{
  getMenuItem():Menu[]{
    return MENUITEMS;
  }
}
