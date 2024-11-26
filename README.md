Cafe System Management Project - Backend (Spring Boot - IntelliJ IDEA), Frontend (Angular, TypeScript - VS Code)

* Phần mềm Quản lý hệ thống bán cà phê gồm có trang quản lý cho phía admin và trang chủ cửa hàng dành cho khách hàng xem chi tiết và mua hàng trực tuyến
* Một số chức năng chính:
  - Phía Quản lý:
    + Phản hồi tin nhắn của khách hàng
    + Quản lý xuất nhập kho, trạng thái hàng tồn kho và lịch sử giao dịch
    + Tạo đơn hàng cho khách hàng mua hàng trực tiếp tại cửa hàng
    + Quản lý đơn đặt hàng trên hệ thống (Xem chi tiết các đơn đặt hàng - IN STORE hoặc ONLINE; Xét duyệt đơn hàng)
    + Quản lý sản phẩm được bán và tạm ngưng (Có thể khôi phục sản phẩm tạm ngưng bán nếu có nhu cầu kinh doanh lại)
    + Quản lý tài khoản người dùng trên hệ thống, khóa hoặc tạm dừng tài khoản
    + Quản lý thống kê (Thống kê doanh thu, lợi nhuận, đơn đặt hàng, sản phẩm bán chạy, danh mục bán chạy, thống kê vòng quay hàng tồn kho,...)
  - Phía Khách hàng:
    + Nhắn tin với Chat Support
    + Xem trang chủ cửa hàng
    + Xem chi tiết sản phẩm, chi tiết đánh giá và bình luận cho từng sản phẩm
    + Thêm sản phẩm vào giỏ hàng
    + Đặt hàng
    + Thanh toán trực tuyến qua cổng VNPAY
    + Xem lịch sử các đơn hàng đã đặt
    + Xem chi tiết đơn hàng
    + Đánh giá, bình luận với ảnh đính kèm cho sản phẩm của đơn hàng đã hoàn thành
    + Hủy đơn hàng đối với đơn hàng chưa được xét duyệt
    + Xem và cập nhật thông tin cá nhân, ảnh đại diện

* HƯỚNG DẤN CLONE PROJECT 
1) copy web URL của repository
![image](https://github.com/user-attachments/assets/53ebc2e4-7c9b-4146-af24-84ae3703404e)

2) Mở Command Prompt
   
3) Điều hướng đến thư mục trong ổ đĩa mà bạn muốn lưu project
![image](https://github.com/user-attachments/assets/2016e417-12e3-43cb-98f4-7596704c09aa)

4) Trong Command Prompt, nhập lệnh sau:
   ( git clone + web URL của repository )
![image](https://github.com/user-attachments/assets/ae808127-c523-4d68-a61b-0bc3b171f072)

5) Sử dụng phần mềm để open project backend, frontend tương ứng

6) Sau khi mọi người mở frontend : chạy lệnh "npm i" và để chạy được project phía frontend phải chạy lệnh "ng s"

7) Lưu ý, "ng s" đã sử dụng port 4200 nên nếu đã mở được giao diện rồi thì không cần các lần sau chạy tiếp vì nó sẽ đổi port, mất công lém hihi. Khi nào close tab visual code rồi thì mới khởi động lại server qua lệnh "ng s" hui.

8) Tiếp theo lưu ý vô cùng đặc biệt: muốn test chức năng thì chạy IntelliJ(code backend) mới chạy tiếp tới VisualCode(code frontend)



