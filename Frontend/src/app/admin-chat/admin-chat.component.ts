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
  adminId: number | null = null; // Changed to be dynamically set
  loading: boolean = false;
  private refreshSubscription?: Subscription;

  constructor(private chatService: ChatService) {}

  ngOnInit() {
    // Retrieve admin ID from localStorage or wherever it's stored
    this.adminId = parseInt(localStorage.getItem('userId') || '0');
    
    if (!this.adminId || this.adminId === 0) {
      console.error('No admin ID found. Please log in.');
      return;
    }

    // Load unread messages on initial load
    this.chatService.getUnreadMessages().subscribe(messages => {
      this.updateChatUsers(messages);
    });
  
    // Set up periodic refresh of unread messages
    this.refreshSubscription = interval(5000)
      .pipe(
        switchMap(() => this.chatService.getUnreadMessages())
      )
      .subscribe(messages => {
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
    const uniqueMessageIds = new Set<number>();
  
    messages.forEach(msg => {
      const userId = msg.fromUserId;
  
      if (!uniqueMessageIds.has(msg.id)) {
        uniqueMessageIds.add(msg.id);
  
        if (!usersMap.has(userId)) {
          usersMap.set(userId, {
            userId: userId,
            name: msg.fromUserName,
            avatar: msg.fromUserAvatar,
            unreadCount: 1,
            lastMessage: msg.content,
            lastMessageDate: msg.createdDate
          });
        } else {
          const user = usersMap.get(userId)!;
          user.unreadCount = (user.unreadCount || 0) + 1;
          
          if (!user.lastMessageDate || msg.createdDate > user.lastMessageDate) {
            user.lastMessage = msg.content;
            user.lastMessageDate = msg.createdDate;
          }
        }
      }
    });
  
    this.chatUsers = Array.from(usersMap.values())
      .sort((a, b) => (b.lastMessageDate?.getTime() || 0) - (a.lastMessageDate?.getTime() || 0));
  }

  selectUser(user: ChatUser) {
    this.selectedUser = user;
    this.loading = true;
  
    this.chatService.getMessages(user.userId).subscribe({
      next: (messages) => {
        // Lọc chỉ lấy các tin nhắn giữa admin và user được chọn
        this.messages = messages.filter((msg: { fromUserId: number | null; toUserId: number | null; }) => 
          (msg.fromUserId === this.adminId && msg.toUserId === user.userId) || 
          (msg.fromUserId === user.userId && msg.toUserId === this.adminId)
        );
        
        this.loading = false;
  
        // Đánh dấu các tin nhắn chưa đọc là đã đọc
        messages
          .filter((msg: { seen: any; fromUserId: number; toUserId: number | null; }) => !msg.seen && msg.fromUserId === user.userId && msg.toUserId === this.adminId)
          .forEach((msg: { id: number; }) => this.chatService.markAsRead(msg.id).subscribe());
  
        // Cập nhật lại unreadCount cho user được chọn
        const userToUpdate = this.chatUsers.find(u => u.userId === user.userId);
        if (userToUpdate) {
          userToUpdate.unreadCount = 0;
        }
      },
      error: (error) => {
        console.error('Error fetching messages:', error);
        this.loading = false;
      }
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
  
    this.chatService.sendMessage(messageData).subscribe({
      next: (response) => {
        // Reload messages immediately after sending
        if (this.selectedUser) {
          this.chatService.getMessages(this.selectedUser.userId).subscribe(messages => {
            this.messages = messages;
          });
        }
        this.newMessage = '';
      },
      error: (error) => {
        console.error('Error sending message:', error);
      }
    });
  }
}