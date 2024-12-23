import { Injectable } from "@angular/core";

export interface CustomerMenu {
  state: string;
  name: string;
  type: string;
  icon: string;
  role: string;
}

const CUSTOMER_MENU_ITEMS = [
    { state: 'home', name: 'Home', type: 'link', icon: 'home', role: 'CUSTOMER' },
    { state: 'menu', name: 'Menu', type: 'link', icon: 'menu_book', role: 'CUSTOMER' },
    { state: 'new', name: 'New', type: 'link', icon: 'feed', role: 'CUSTOMER' },
    // { state: 'cart', name: 'Cart', type: 'link', icon: 'shopping_cart', role: 'CUSTOMER' }
];


@Injectable()
export class CustomerMenuItems {
  getMenuItems(): CustomerMenu[] {
    return CUSTOMER_MENU_ITEMS;
  }
}