import { Component, OnInit, Input, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { ChatService } from '../services/chat.service';

interface ChatMessage {
  id: number;
  content: string;
  fromUserId: number;
  toUserId: number;
  timestamp: Date;
  read: boolean;
}

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit, AfterViewChecked {
  @Input() isAdmin: boolean = false;
  @ViewChild('messageContainer') private messageContainer!: ElementRef;
  
  messages: ChatMessage[] = [];
  newMessage: string = '';
  currentUserId: number | null = null;
  selectedUserId: number | null = null;
  unreadMessages: ChatMessage[] = [];
  loading: boolean = false;
  error: string = '';
  isChatOpen: boolean = false;

  constructor(private chatService: ChatService) {}

  ngOnInit() {
    this.currentUserId = parseInt(localStorage.getItem('userId') || '0');
    this.loadUnreadMessages();
   
    // Interval để load tin nhắn thường xuyên
    setInterval(() => {
      if (this.selectedUserId) {
        this.loadMessages(this.selectedUserId);
      }
      this.loadUnreadMessages();
    }, 4000); // 4 giây
  
    if (this.isAdmin) {
      this.isChatOpen = true;
    }
  }
  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  toggleChat() {
    this.isChatOpen = !this.isChatOpen;
    if (this.isChatOpen && !this.selectedUserId) {
      this.selectedUserId = 1;
      this.loadMessages(this.selectedUserId);
    }
  }

  scrollToBottom(): void {
    try {
      this.messageContainer.nativeElement.scrollTop =
        this.messageContainer.nativeElement.scrollHeight;
    } catch(err) {
      console.error('Error scrolling to bottom:', err);
    }
  }

  loadMessages(userId: number) {
    this.loading = true;
    this.selectedUserId = userId;
   
    // Lưu vị trí cuộn hiện tại
    const scrollPos = this.messageContainer.nativeElement.scrollTop;
    const isScrolledToBottom = this.messageContainer.nativeElement.scrollHeight - this.messageContainer.nativeElement.scrollTop === this.messageContainer.nativeElement.clientHeight;
   
    this.chatService.getMessages(userId).subscribe({
        next: (response: ChatMessage[]) => {
            this.messages = response;
            this.loading = false;
           
            this.messages.forEach(msg => {
                if (!msg.read && msg.toUserId === this.currentUserId) {
                    this.markMessageAsRead(msg.id);
                }
            });


            // Khôi phục vị trí cuộn sau khi cập nhật tin nhắn
            setTimeout(() => {
                if (isScrolledToBottom) {
                    this.scrollToBottom();
                } else {
                    this.messageContainer.nativeElement.scrollTop = scrollPos;
                }
            }, 0);
        },
        error: (error) => {
            console.error('Error loading messages:', error);
            this.error = 'Failed to load messages';
            this.loading = false;
        }
    });
}

loadUnreadMessages() {
  this.chatService.getUnreadMessages().subscribe({
    next: (response: ChatMessage[]) => {
      // Lọc tin nhắn chưa đọc phù hợp với người dùng hiện tại
      this.unreadMessages = response
        .filter(msg => msg.toUserId === this.currentUserId)
        // Sắp xếp theo thứ tự thời gian mới nhất
        .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

      // Nếu không có admin được chọn, tự động chọn admin từ tin nhắn chưa đọc
      if (!this.selectedUserId && this.unreadMessages.length > 0) {
        this.autoSelectAdminForChat();
      }
    },
    error: (error) => {
      console.error('Error loading unread messages:', error);
    }
  });
}
 // Phương thức tự động chọn admin để chat dựa trên tin nhắn chưa đọc
  autoSelectAdminForChat() {
    if (this.unreadMessages.length > 0) {
      // Chọn admin của tin nhắn chưa đọc gần đây nhất
      const latestUnreadMessage = this.unreadMessages[0];
      this.selectedUserId = latestUnreadMessage.fromUserId;
      this.loadMessages(this.selectedUserId);
    }
  }
  sendMessage() {
    if (!this.newMessage.trim() || !this.selectedUserId) {
      console.log('Message empty or no selectedUserId');
      return;
    }
     
    const messageData = {
      content: this.newMessage.trim(),
      toUserId: this.selectedUserId.toString()
    };
     
    console.log('Preparing to send message:', messageData);
    this.loading = true;
     
    this.chatService.sendMessage(messageData).subscribe({
      next: (response: any) => {  // Thêm kiểu any để linh hoạt hơn
        console.log('Message sent successfully:', response);
        
        // Tạo tin nhắn mới với thông tin từ response hoặc mặc định
        const newMessage: ChatMessage = {
          id: response?.id || Date.now(), // Sử dụng timestamp nếu không có ID
          content: this.newMessage.trim(),
          fromUserId: this.currentUserId!,
          toUserId: this.selectedUserId!,
          timestamp: new Date(),
          read: false
        };
        
        // Thêm tin nhắn mới vào mảng
        this.messages = [...this.messages, newMessage];
        
        this.newMessage = '';
        this.scrollToBottom();
        this.loading = false;
        
        // Load lại tin nhắn để đảm bảo đồng bộ với server
        this.loadMessages(this.selectedUserId!);
      },
      error: (error) => {
        console.error('Error details:', error);
        this.error = `Failed to send message: ${error.message || 'Unknown error'}`;
        this.loading = false;
      }
    });
  }

  markMessageAsRead(messageId: number) {
    this.chatService.markAsRead(messageId).subscribe({
      next: () => {
        this.loadUnreadMessages();
      },
      error: (error) => {
        console.error('Error marking message as read:', error);
      }
    });
  }

  getUnreadCount(userId: number): number {
    return this.unreadMessages.filter(msg => msg.fromUserId === userId).length;
  }

  isMyMessage(message: ChatMessage): boolean {
    return message.fromUserId === this.currentUserId;
  }
}
