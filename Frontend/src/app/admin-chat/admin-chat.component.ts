// admin-chat.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ChatService } from '../services/chat.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';


interface Message {
  id: number;
  content: string;
  createdDate: Date;
  fromUserId: number;
  fromUserName: string;
  fromUserAvatar: string;
  toUserId: number;
  toUserName: string;
  toUserAvatar: string;
  seen: boolean;
}


interface ChatUser {
  userId: number;
  name: string;
  avatar: string;
  lastMessage?: string;
  unreadCount?: number;
  lastMessageDate?: Date;
}


@Component({
  selector: 'app-admin-chat',
  templateUrl: './admin-chat.component.html',
  styleUrls: ['./admin-chat.component.scss']
})
export class AdminChatComponent implements OnInit, OnDestroy {
  messages: Message[] = [];
  chatUsers: ChatUser[] = [];
  selectedUser: ChatUser | null = null;
  newMessage: string = '';
  adminId = 1; // Thay bằng ID của admin thực tế
  loading: boolean = false;
  private refreshSubscription?: Subscription;


  constructor(private chatService: ChatService) {}


  ngOnInit() {
    this
    this.refreshSubscription = interval(5000)
      .pipe(
        switchMap(() => this.chatService.getUnreadMessages())
      )
      .subscribe(messages => {
        this.updateChatUsers(messages);
      });


    this.chatService.getUnreadMessages().subscribe(messages => {
      this.updateChatUsers(messages);
    });
  }


  ngOnDestroy() {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }


  private updateChatUsers(messages: Message[]) {
    const usersMap = new Map<number, ChatUser>();
    
    messages.forEach(msg => {
      const userId = msg.fromUserId;
      if (!usersMap.has(userId)) {
        usersMap.set(userId, {
          userId: userId,
          name: msg.fromUserName,
          avatar: msg.fromUserAvatar,
          unreadCount: 1,
          lastMessage: msg.content
        });
      } else {
        const user = usersMap.get(userId)!;
        user.unreadCount = (user.unreadCount || 0) + 1;
        user.lastMessage = msg.content;
      }
    });


    this.chatUsers = Array.from(usersMap.values());
  }


  selectUser(user: ChatUser) {
    this.selectedUser = user;
    this.chatService.getMessages(user.userId).subscribe(messages => {
      this.messages = messages;
      messages
        .filter((msg: { seen: any; fromUserId: number; }) => !msg.seen && msg.fromUserId === user.userId)
        .forEach((msg: { id: number; }) => this.chatService.markAsRead(msg.id).subscribe());
    });
  }


  onSubmit(event: Event) {
    event.preventDefault();
    this.sendMessage();
  }


  sendMessage() {
    if (!this.newMessage.trim() || !this.selectedUser) return;


    const messageData = {
      toUserId: this.selectedUser.userId.toString(),
      content: this.newMessage.trim()
    };


    this.chatService.sendMessage(messageData).subscribe(
      response => {
        if (this.selectedUser) {
          this.chatService.getMessages(this.selectedUser.userId).subscribe(messages => {
            this.messages = messages;
          });
        }
        this.newMessage = '';
      },
      error => {
        console.error('Error sending message:', error);
      }
    );
  }
}


