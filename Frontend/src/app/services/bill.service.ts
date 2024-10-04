import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BillService {

  url = environment.apiUrl;

  constructor(private httpClient:HttpClient) { }

  generateBill(data:any){
    return this.httpClient.post(`${this.url}/api/v1/bill/generateBill`, data,{
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }

  getPdf(data: any){
    return this.httpClient.post(`${this.url}/api/v1/bill/getPdf`, data, {
      headers: new HttpHeaders().set('Content-Type', 'application/json'),
      responseType: 'blob' as 'json'
    });
  }

  getBills(){
    return this.httpClient.get(`${this.url}/api/v1/bill/getBill`)
  }

  delete(id:any){
    return this.httpClient.post(`${this.url}/api/v1/bill/delete/${id}`,{
      headers: new HttpHeaders().set('Content-Type', 'application/json')
    })
  }
}
