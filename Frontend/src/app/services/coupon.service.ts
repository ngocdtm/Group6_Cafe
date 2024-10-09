import { DatePipe } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CouponService {

  url = environment.apiUrl;

  constructor(private httpClient:HttpClient,  private datePipe: DatePipe) { }

  add(data:any){
    if (data.expirationDate) { data.expirationDate = this.datePipe.transform(data.expirationDate, 'yyyy-MM-dd'); }
    return this.httpClient.post(`${this.url}/api/v1/coupon/add`, data,{
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  update(data:any){
    if (data.expirationDate) { data.expirationDate = this.datePipe.transform(data.expirationDate, 'yyyy-MM-dd'); }
    return this.httpClient.post(`${this.url}/api/v1/coupon/update`, data,{
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  getCoupon(){
    return this.httpClient.get(`${this.url}/api/v1/coupon/get`)
  }

  delete(id:any){
    return this.httpClient.post(`${this.url}/api/v1/coupon/delete/${id}`,{
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  getCouponById(id:any){
    return this.httpClient.get(`${this.url}/api/v1/coupon/getById/${id}`)
  }
}
