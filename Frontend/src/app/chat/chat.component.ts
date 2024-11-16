// chat.component.ts
import { Component, OnInit, Input } from '@angular/core';
import { ChatService, User, ChatMessage } from '../services/chat.service';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit {
  @Input() isAdmin: boolean = false;
  users: User[] = [];
  selectedUser!: User;
  messages: ChatMessage[] = [];
  newMessage: string = '';
  currentUser: User;

  constructor(private chatService: ChatService) {
    // Initialize currentUser - you might want to get this from your auth service
    this.currentUser = {
      id: this.isAdmin ? 'admin' : 'user',
      name: this.isAdmin ? 'Admin' : 'User',
      email: this.isAdmin ? 'admin@example.com' : 'user@example.com',
      role: this.isAdmin ? 'admin' : 'user'
    };
  }

  ngOnInit() {
    this.chatService.getUsers().subscribe(users => {
      this.users = users;
      // Find first user of opposite role
      const oppositeRole = this.isAdmin ? 'user' : 'admin';
      this.selectedUser = users.find(u => u.role === oppositeRole) || users[0];
      if (this.selectedUser) {
        this.getConversation();
      }
    });
  }

  getConversation() {
    if (this.selectedUser) {
      this.chatService.getConversation(this.currentUser.id, this.selectedUser.id)
      .subscribe({
        next: (messages) => {
          this.messages = messages;
        },
        error: (error: any) => {
          console.error('Error fetching conversation:', error);
        }
        });
    }
  }

  sendMessage() {
    if (this.newMessage.trim() && this.selectedUser) {
      const message: ChatMessage = {
        sender: this.currentUser,
        receiver: this.selectedUser,
        content: this.newMessage,
        timestamp: new Date(),
        status: 'SENT'
      };

      this.chatService.sendMessage(message).subscribe(sent => {
        this.messages.push(sent);
        this.newMessage = '';
        this.getConversation();
      });
    }
  }

  selectUser(user: User) {
    this.selectedUser = user;
    this.getConversation();
  }
}