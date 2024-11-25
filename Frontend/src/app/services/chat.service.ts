// chat.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  sendMessage(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/v1/message/send`, data);
  }

  getMessages(userId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/api/v1/message/get/${userId}`);
  }

  getUnreadMessages(): Observable<any> {
    return this.http.get(`${this.apiUrl}/api/v1/message/unread`);
  }
  getAllMessages(): Observable<any> {
    return this.http.get(`${this.apiUrl}/api/v1/message/all`);
  }
  markAsRead(messageId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/api/v1/message/read/${messageId}`, {});
  }
}