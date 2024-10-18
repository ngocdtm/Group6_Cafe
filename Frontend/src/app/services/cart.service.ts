import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartItems: any[] = [];
  cartUpdated = new Subject<void>();


  constructor() { }


  addToCart(product: any) {
    const existingItem = this.cartItems.find(item => item.id === product.id);
    if (existingItem) {
      existingItem.quantity += 1;
    } else {
      this.cartItems.push({ ...product, quantity: 1 });
    }
    this.cartUpdated.next();
  }


  getCartItems() {
    return [...this.cartItems];
  }


  getCartItemCount() {
    return this.cartItems.reduce((total, item) => total + item.quantity, 0);
  }


  removeFromCart(productId: string) {
    const index = this.cartItems.findIndex(item => item.id === productId);
    if (index !== -1) {
      if (this.cartItems[index].quantity > 1) {
        this.cartItems[index].quantity -= 1;
      } else {
        this.cartItems.splice(index, 1);
      }
      this.cartUpdated.next();
    }
  }


  clearCart() {
    this.cartItems = [];
    this.cartUpdated.next();
  }
}

