# Mở Facebook có chủ đích

Ứng dụng Android nhỏ đặt một bước hỏi trước khi bạn tự mở Facebook. Màn hình đầu có bốn lựa chọn: **Tìm kiếm**, **Xem thông báo mới**, **Xem một người**, **Xem News Feed**. Chọn một gợi ý trong kho hoặc tự ghi ý định, sau đó bấm **Mở Facebook**.

Ứng dụng chỉ là một biểu tượng mở Facebook thay thế trên màn hình chính. Nó **không chặn** biểu tượng Facebook gốc, thông báo của Facebook, hoặc các liên kết mở Facebook từ app khác. Để dùng như một “cửa vào”, hãy đặt biểu tượng của ứng dụng này ở vị trí biểu tượng Facebook cũ và chuyển biểu tượng Facebook gốc khỏi màn hình chính. Không cần root, quyền trợ năng hay quyền sử dụng ứng dụng.

## Chức năng

- Bốn loại ý định và kho gợi ý có sẵn cho từng loại.
- Chạm gợi ý để điền vào ô nhập; có thể sửa nội dung trước khi mở.
- **Sửa kho gợi ý** cho phép thêm, sửa, xóa từng mục. Dữ liệu lưu trong `SharedPreferences` riêng của app, chỉ trên máy. Gỡ cài đặt sẽ xóa dữ liệu; không có đồng bộ hoặc sao lưu.
- Nhớ ý định gần nhất để bạn thấy lại khi quay về app.
- Nếu Android không mở được một đường dẫn, app thử đường tiếp theo. Nếu Facebook nhận link nhưng hiển thị sai chỗ, quay lại app và bấm **Facebook mở sai chỗ? Thử cách khác**.

## Cách hoạt động và giới hạn deep link

| Lựa chọn | Đường thử | Tình trạng |
| --- | --- | --- |
| Tìm kiếm | `fb://facewebmodal/f?href=...`, rồi `https://www.facebook.com/search/top/?q=...` | URI `fb://` và đường tìm kiếm là thử nghiệm; có thể mở Feed hoặc trang web thay vì kết quả. |
| Xem thông báo mới | `fb://notifications`, `facewebmodal`, rồi `https://www.facebook.com/notifications/` | URI nội bộ là thử nghiệm; đăng nhập và phiên bản Facebook ảnh hưởng kết quả. |
| Xem một người | Nếu nhập URL HTTPS thuộc `facebook.com`, dùng URL đó; nếu nhập tên, tìm qua `/search/people/?q=...` | URL profile cụ thể đáng tin cậy hơn tìm theo tên; việc Facebook mở đúng màn hình vẫn cần thử trên máy. |
| Xem News Feed | `https://www.facebook.com/` trong app, rồi đường dự phòng | Có thể về màn hình chính Facebook. |

Sau các đường dành riêng cho Facebook, app thử URL bằng trình xử lý mặc định của Android rồi mở activity chính của gói `com.facebook.katana`. **Các đường nội bộ và cấu trúc URL không phải API được Meta bảo đảm cho app bên thứ ba.** Facebook có thể thay đổi hoặc bỏ hỗ trợ bất cứ lúc nào. Android chỉ báo rằng `startActivity()` đã chuyển yêu cầu đi, không cho biết Facebook đã mở đúng màn hình. Vì vậy dự phòng tự động chỉ xảy ra khi Android từ chối mở; nút **Thử cách khác** xử lý trường hợp Facebook nhận link nhưng đến sai nơi.

App không lấy dữ liệu từ Facebook và không đăng nhập thay bạn. Không có kết nối mạng trong app; lưu ý Facebook hoặc trình duyệt vẫn cần mạng để mở nội dung.

## Cài và build

Yêu cầu: Android Studio có **JDK 17**, Android SDK Platform **35**, và Internet khi đồng bộ Gradle lần đầu. Project dùng Android Gradle Plugin **8.7.3**, tương thích Gradle **8.9**. Hỗ trợ Android **8.0 / API 26** trở lên, gồm Huawei Mate 20 Pro chạy Android 9/10.

1. Mở thư mục `FacebookIntentLauncher` bằng Android Studio.
2. Nếu Android Studio hỏi Gradle version vì project không kèm Gradle Wrapper, chọn Gradle **8.9** (hoặc dùng Gradle 8.9 đã cài). Chờ **Sync Project with Gradle Files** hoàn tất.
3. Chọn **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. APK debug ở `app/build/outputs/apk/debug/app-debug.apk`.
5. Chép APK sang điện thoại, mở tệp và cho phép cài ứng dụng từ nguồn đó khi Android hỏi. Hoặc dùng `adb install -r app/build/outputs/apk/debug/app-debug.apk` nếu đã bật USB debugging.

Nếu đã cài JDK 17, Android SDK 35 và Gradle 8.9 trên máy, có thể build từ terminal trong thư mục project:

```sh
gradle :app:assembleDebug
```

Repository có GitHub Actions workflow `Build debug APK`. Sau khi đẩy mã lên GitHub, mở tab **Actions**, chạy workflow hoặc đẩy commit lên nhánh chính, rồi tải artifact **facebook-intent-launcher-debug-apk**. Đây là APK debug dành cho thử nghiệm, không phải bản phát hành đã ký để phân phối lâu dài.

## Thử trên Mate 20 Pro

1. Cài app Facebook chính thức và đăng nhập.
2. Chọn **Tìm kiếm**, nhập một từ khóa rõ ràng, mở và ghi lại vị trí Facebook hiển thị.
3. Quay về app, bấm **Thử cách khác** nếu kết quả không đúng. Làm tương tự với **Thông báo**.
4. Với **Xem một người**, thử cả URL profile cụ thể và tên người. URL profile thường xác định đích chính xác hơn.
5. Nếu Facebook không cài, URL có thể mở trong trình duyệt. Nếu không có app/trình duyệt xử lý, app hiện thông báo lỗi.

## Cấu trúc

- `MainActivity.java`: giao diện và chỉnh sửa kho.
- `IntentBankStore.java`: kho gợi ý mặc định và lưu local.
- `FacebookOpener.java`: tạo URL/URI và thử các đường mở.

## Tài liệu nền

- [Android: gửi Intent và mở ứng dụng khác](https://developer.android.com/training/basics/intents/)
- [Android: quyền nhìn thấy gói ứng dụng khác](https://developer.android.com/training/package-visibility/declaring)
- [Android: `getLaunchIntentForPackage`](https://developer.android.com/reference/android/content/pm/PackageManager#getLaunchIntentForPackage(java.lang.String))
- [Android Gradle Plugin 8.7 và Gradle 8.9](https://developer.android.com/build/releases/agp-8-7-0-release-notes)

## Giấy phép

MIT — xem `LICENSE`. Đây là project độc lập, không liên kết với Meta/Facebook.
