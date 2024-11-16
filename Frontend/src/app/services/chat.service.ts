import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Observable, BehaviorSubject } from 'rxjs';
import { Client } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';

export interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  avatar?: string;
}

export interface ChatMessage {
  id?: string;
  sender: User;
  receiver: User;
  content: string;
  timestamp: Date;
  status: 'SENT' | 'DELIVERED' | 'READ';
}

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private stompClient: Client;
  private messageSubject = new BehaviorSubject<ChatMessage[]>([]);
  messages$ = this.messageSubject.asObservable();

  constructor(private http: HttpClient) {
    this.initializeWebSocketConnection();
  }

  private initializeWebSocketConnection() {
    const socket = new SockJS(`${environment.apiUrl}/ws`);
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log(str),
      onConnect: () => {
        this.stompClient.subscribe('/user/queue/messages', (message) => {
          const newMessage = JSON.parse(message.body);
          const currentMessages = this.messageSubject.getValue();
          this.messageSubject.next([...currentMessages, newMessage]);
        });
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      },
    });

    this.stompClient.activate();
  }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${environment.apiUrl}/api/users`);
  }

  getConversation(userId1: string, userId2: string): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${environment.apiUrl}/api/chat/conversation/${userId1}/${userId2}`);
  }

  sendMessage(message: ChatMessage): Observable<ChatMessage> {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/chat',
        body: JSON.stringify(message),
      });
    }
    return this.http.post<ChatMessage>(`${environment.apiUrl}/api/chat/send`, message);
  }
}
