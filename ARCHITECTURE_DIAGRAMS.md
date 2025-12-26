# Sơ Đồ Kiến Trúc Dự Án Tin Thể Thao

## 1. Tổng Quan Hệ Thống

Sơ đồ này mô tả cách các thành phần trong hệ thống tương tác với nhau, từ việc thu thập dữ liệu đến hiển thị cho người dùng.

```mermaid
graph TB
    subgraph "Nguồn Dữ Liệu Bên Ngoài"
        VnExpress["VnExpress<br/>Nguồn tin tức"]
        Robong["Robong<br/>Nguồn lịch đấu"]
    end
    
    subgraph "Lớp Thu Thập Dữ Liệu"
        NewsCrawler["Crawler Tin Tức<br/>Python"]
        MatchCrawler["Crawler Lịch Đấu<br/>Python"]
    end
    
    subgraph "Cơ Sở Dữ Liệu"
        MySQL[("MySQL Database<br/>Lưu trữ:<br/>- Bài viết<br/>- Người dùng<br/>- Lịch đấu")]
    end
    
    subgraph "Backend API"
        PHPAPI["API Server<br/>PHP REST API"]
        Router["Router<br/>Định tuyến"]
        Controllers["Controllers<br/>Xử lý logic:<br/>- Bài viết<br/>- Xác thực<br/>- Lịch đấu"]
        Middleware["Middleware<br/>Bảo mật:<br/>- Xác thực<br/>- Giới hạn truy cập"]
    end
    
    subgraph "Ứng Dụng Android"
        AndroidApp["Ứng Dụng Android<br/>Kotlin + Compose"]
        UI["Màn Hình UI<br/>Trang chủ, Bài viết, Hồ sơ"]
        ViewModels["ViewModels<br/>Xử lý logic giao diện"]
        Repositories["Repositories<br/>Quản lý dữ liệu"]
        LocalDB[("Room Database<br/>Cache cục bộ")]
        ApiServices["API Services<br/>Kết nối API"]
    end
    
    VnExpress -->|"Thu thập tin"| NewsCrawler
    Robong -->|"Thu thập lịch đấu"| MatchCrawler
    NewsCrawler -->|"Lưu vào"| MySQL
    MatchCrawler -->|"Lưu vào"| MySQL
    MySQL -->|"Cung cấp dữ liệu"| PHPAPI
    PHPAPI --> Router
    Router --> Middleware
    Middleware --> Controllers
    Controllers -->|"Truy vấn"| MySQL
    AndroidApp --> ApiServices
    ApiServices -->|"Gọi API"| PHPAPI
    AndroidApp --> Repositories
    Repositories --> ApiServices
    Repositories -->|"Lưu cache"| LocalDB
    ViewModels --> Repositories
    UI --> ViewModels
```

## 2. Tổng Quan Kiến Trúc Android App

Sơ đồ tổng quan về kiến trúc MVVM của ứng dụng Android.

```mermaid
graph TB
    subgraph "Lớp Giao Diện"
        UI["Màn Hình UI<br/>Compose Screens"]
    end
    
    subgraph "Lớp ViewModel"
        VM["ViewModels<br/>Xử lý logic"]
    end
    
    subgraph "Lớp Repository"
        Repo["Repositories<br/>Quản lý dữ liệu"]
    end
    
    subgraph "Lớp Dữ Liệu"
        Data["Data Sources<br/>API, Database, Cache"]
    end
    
    UI --> VM
    VM --> Repo
    Repo --> Data
```

## 2.1. Chi Tiết Lớp Giao Diện (UI Layer)

### 2.1.1. Cấu Trúc Màn Hình

Sơ đồ mô tả các màn hình chính trong ứng dụng và mối quan hệ giữa chúng.

```mermaid
graph TB
    subgraph "Màn Hình Chính"
        HomeScreen["HomeScreen<br/>Trang chủ - Hiển thị tin nổi bật"]
        ArticlesScreen["ArticlesScreen<br/>Danh sách bài viết"]
        ArticleDetailScreen["ArticleDetailScreen<br/>Chi tiết bài viết"]
        ProfileScreen["ProfileScreen<br/>Hồ sơ người dùng"]
        SearchScreen["SearchScreen<br/>Tìm kiếm bài viết"]
        MatchesScreen["MatchesScreen<br/>Lịch thi đấu"]
        VideosScreen["VideosScreen<br/>Video highlights"]
    end
    
    subgraph "Màn Hình Xác Thực"
        LoginScreen["LoginScreen<br/>Đăng nhập"]
        RegisterScreen["RegisterScreen<br/>Đăng ký tài khoản"]
        EmailVerificationScreen["EmailVerificationScreen<br/>Xác thực email"]
    end
    
    subgraph "Màn Hình Quản Lý Người Dùng"
        CreatePostScreen["CreatePostScreen<br/>Tạo bài viết mới"]
        MyPostsScreen["MyPostsScreen<br/>Bài viết của tôi"]
        SavedArticlesScreen["SavedArticlesScreen<br/>Bài viết đã lưu"]
        EditProfileScreen["EditProfileScreen<br/>Chỉnh sửa hồ sơ"]
        ChangePasswordScreen["ChangePasswordScreen<br/>Đổi mật khẩu"]
    end
    
    HomeScreen --> ArticlesScreen
    HomeScreen --> MatchesScreen
    HomeScreen --> VideosScreen
    ArticlesScreen --> ArticleDetailScreen
    ProfileScreen --> EditProfileScreen
    ProfileScreen --> SavedArticlesScreen
    ProfileScreen --> MyPostsScreen
    ProfileScreen --> ChangePasswordScreen
    MyPostsScreen --> CreatePostScreen
    LoginScreen --> RegisterScreen
    RegisterScreen --> EmailVerificationScreen
```

### 2.1.2. Thành Phần Giao Diện (UI Components)

Sơ đồ mô tả các thành phần giao diện có thể tái sử dụng trong ứng dụng.

```mermaid
graph TB
    subgraph "Thành Phần Hiển Thị Nội Dung"
        ArticleCard["ArticleCard<br/>Thẻ hiển thị bài viết"]
        VideoCard["VideoCard<br/>Thẻ hiển thị video"]
        CommentItem["CommentItem<br/>Mục bình luận"]
        HtmlText["HtmlText<br/>Hiển thị HTML"]
    end
    
    subgraph "Thành Phần Tương Tác"
        RichTextEditor["RichTextEditor<br/>Soạn thảo văn bản"]
        RichTextToolbar["RichTextToolbar<br/>Thanh công cụ"]
        ImagePickerDialog["ImagePickerDialog<br/>Chọn ảnh"]
        PostPreviewDialog["PostPreviewDialog<br/>Xem trước bài viết"]
        AvatarEditorDialog["AvatarEditorDialog<br/>Chỉnh sửa avatar"]
        PullToRefresh["PullToRefresh<br/>Kéo để làm mới"]
    end
    
    subgraph "Thành Phần Trạng Thái"
        ShimmerEffect["ShimmerEffect<br/>Hiệu ứng loading"]
        EmptyState["EmptyState<br/>Trạng thái rỗng"]
        ErrorState["ErrorState<br/>Trạng thái lỗi"]
    end
    
    subgraph "Thành Phần Khác"
        Animations["Animations<br/>Hiệu ứng chuyển động"]
    end
    
    ArticleCard --> HtmlText
    ArticleDetailScreen --> CommentItem
    ArticleDetailScreen --> HtmlText
    VideosScreen --> VideoCard
    CreatePostScreen --> RichTextEditor
    RichTextEditor --> RichTextToolbar
    RichTextEditor --> ImagePickerDialog
    CreatePostScreen --> PostPreviewDialog
    ProfileScreen --> AvatarEditorDialog
    ArticlesScreen --> PullToRefresh
    ArticlesScreen --> ShimmerEffect
    ArticlesScreen --> EmptyState
    ArticlesScreen --> ErrorState
```

### 2.1.3. Luồng Điều Hướng Màn Hình

Sơ đồ mô tả cách người dùng điều hướng giữa các màn hình. Người dùng chưa đăng nhập vẫn có thể vào HomeScreen và xem nội dung, nhưng không thể like hoặc bình luận bài viết.

```mermaid
flowchart TD
    Start([Người dùng mở app]) --> CheckAuth{Đã đăng nhập?}
    
    CheckAuth -->|Chưa| HomeScreen
    CheckAuth -->|Rồi| HomeScreen
    CheckAuth -->|Chưa| LoginScreen
    
    LoginScreen --> RegisterScreen
    RegisterScreen --> EmailVerificationScreen
    EmailVerificationScreen --> HomeScreen
    
    HomeScreen --> ArticlesScreen
    HomeScreen --> MatchesScreen
    HomeScreen --> VideosScreen
    HomeScreen --> SearchScreen
    HomeScreen --> ProfileScreen
    HomeScreen --> LoginScreen
    
    ArticlesScreen --> ArticleDetailScreen
    ArticleDetailScreen --> CheckAuthDetail{Đã đăng nhập?}
    CheckAuthDetail -->|Chưa| LoginPrompt["Hiển thị thông báo:<br/>Cần đăng nhập để like, bình luận"]
    CheckAuthDetail -->|Rồi| ProfileScreen
    LoginPrompt --> LoginScreen
    
    ProfileScreen --> EditProfileScreen
    ProfileScreen --> SavedArticlesScreen
    ProfileScreen --> MyPostsScreen
    ProfileScreen --> ChangePasswordScreen
    
    MyPostsScreen --> CreatePostScreen
    CreatePostScreen --> MyPostsScreen
    
    SearchScreen --> ArticleDetailScreen
    VideosScreen --> ArticleDetailScreen
    MatchesScreen --> ArticleDetailScreen
```

## 2.2. Chi Tiết Lớp ViewModel

### 2.2.1. Cấu Trúc ViewModel

Sơ đồ mô tả các ViewModel và mối quan hệ với Repository.

```mermaid
graph TB
    subgraph "ViewModels Màn Hình Chính"
        HomeVM["HomeViewModel<br/>Quản lý:<br/>Tin nổi bật<br/>Danh mục<br/>Lịch đấu sắp tới"]
        ArticleVM["ArticlesViewModel<br/>Quản lý:<br/>Danh sách bài viết<br/>Phân trang<br/>Lọc theo danh mục"]
        DetailVM["ArticleDetailViewModel<br/>Quản lý:<br/>Chi tiết bài viết<br/>Bình luận<br/>Lượt thích"]
        ProfileVM["ProfileViewModel<br/>Quản lý:<br/>Thông tin người dùng<br/>Avatar<br/>Cài đặt"]
        SearchVM["SearchViewModel<br/>Quản lý:<br/>Tìm kiếm<br/>Lịch sử tìm kiếm<br/>Kết quả"]
        MatchVM["MatchesViewModel<br/>Quản lý:<br/>Lịch đấu<br/>Trận đấu trực tiếp<br/>Kết quả"]
        VideoVM["VideosViewModel<br/>Quản lý:<br/>Danh sách video<br/>Video highlights<br/>Phân trang"]
    end
    
    subgraph "ViewModels Xác Thực"
        AuthVM["AuthViewModel<br/>Quản lý:<br/>Đăng nhập<br/>Đăng ký<br/>Xác thực email"]
    end
    
    subgraph "ViewModels Quản Lý Nội Dung"
        PostVM["PostViewModel<br/>Quản lý:<br/>Tạo bài viết<br/>Chỉnh sửa<br/>Upload ảnh"]
        SavedVM["SavedArticlesViewModel<br/>Quản lý:<br/>Bài viết đã lưu<br/>Xóa khỏi danh sách"]
    end
    
    subgraph "ViewModels Hệ Thống"
        ThemeVM["ThemeViewModel<br/>Quản lý:<br/>Chế độ sáng/tối<br/>Cài đặt giao diện"]
    end
    
    subgraph "Repositories"
        NewsRepo["NewsRepository"]
        ArticleRepo["ArticleRepository"]
        CommentRepo["CommentRepository"]
        UserRepo["UserRepository"]
        AuthRepo["AuthRepository"]
        PostRepo["PostRepository"]
        CategoryRepo["CategoryRepository"]
    end
    
    HomeVM --> NewsRepo
    HomeVM --> CategoryRepo
    ArticleVM --> ArticleRepo
    ArticleVM --> CategoryRepo
    DetailVM --> ArticleRepo
    DetailVM --> CommentRepo
    ProfileVM --> UserRepo
    SearchVM --> ArticleRepo
    MatchVM --> NewsRepo
    VideoVM --> NewsRepo
    AuthVM --> AuthRepo
    PostVM --> PostRepo
    SavedVM --> ArticleRepo
```

### 2.2.2. Luồng Dữ Liệu ViewModel

Sơ đồ mô tả cách ViewModel xử lý dữ liệu từ UI đến Repository.

```mermaid
sequenceDiagram
    participant UI as "Màn Hình UI"
    participant VM as "ViewModel"
    participant Repo as "Repository"
    participant Data as "Data Source"
    
    Note over UI,Data: Luồng Đọc Dữ Liệu
    UI->>VM: Yêu cầu dữ liệu
    VM->>VM: Kiểm tra state hiện tại
    VM->>Repo: Gọi hàm lấy dữ liệu
    Repo->>Data: Truy vấn dữ liệu
    Data-->>Repo: Trả về dữ liệu
    Repo-->>VM: Flow/StateFlow
    VM->>VM: Cập nhật state
    VM-->>UI: Emit state mới
    UI->>UI: Re-compose với state mới
    
    Note over UI,Data: Luồng Ghi Dữ Liệu
    UI->>VM: Thao tác người dùng
    VM->>VM: Validate dữ liệu
    VM->>VM: Cập nhật loading state
    VM->>Repo: Gọi hàm ghi dữ liệu
    Repo->>Data: Gửi dữ liệu
    Data-->>Repo: Kết quả
    Repo-->>VM: Result
    alt Thành Công
        VM->>VM: Cập nhật success state
        VM-->>UI: Hiển thị thông báo thành công
    else Thất Bại
        VM->>VM: Cập nhật error state
        VM-->>UI: Hiển thị thông báo lỗi
    end
```

## 3. Luồng Dữ Liệu Trong Hệ Thống

Sơ đồ này mô tả cách dữ liệu di chuyển từ nguồn thu thập đến hiển thị cho người dùng.

### 3.0. Tổng Quan Luồng Dữ Liệu

Sơ đồ tổng quan mô tả hai giai đoạn chính: Thu thập dữ liệu và Người dùng sử dụng.

```mermaid
sequenceDiagram
    participant Crawler as "Crawler Python"
    participant MySQL as "MySQL Database"
    participant API as "PHP API Server"
    participant Repo as "Repository"
    participant LocalDB as "Room Database"
    participant VM as "ViewModel"
    participant UI as "Màn Hình UI"
    
    Note over Crawler,MySQL: GIAI ĐOẠN 1: THU THẬP DỮ LIỆU
    Crawler->>MySQL: Thu thập tin tức từ VnExpress
    Crawler->>MySQL: Thu thập lịch đấu từ Robong
    Crawler->>MySQL: Lưu vào database
    
    Note over API,UI: GIAI ĐOẠN 2: NGƯỜI DÙNG SỬ DỤNG
    UI->>VM: Người dùng mở màn hình
    VM->>Repo: Yêu cầu dữ liệu
    Repo->>LocalDB: Kiểm tra cache cục bộ
    
    alt Có Cache
        LocalDB-->>Repo: Trả về dữ liệu cache
        Repo-->>VM: Dữ liệu từ cache
        VM-->>UI: Hiển thị ngay lập tức
    else Không Có Cache
        Repo->>API: Gửi yêu cầu HTTP
        API->>MySQL: Truy vấn dữ liệu
        MySQL-->>API: Trả về danh sách bài viết
        API-->>Repo: Phản hồi JSON
        Repo->>LocalDB: Lưu vào cache
        Repo-->>VM: Trả về dữ liệu mới
        VM-->>UI: Cập nhật giao diện
    end
    UI->>UI: Vẽ lại màn hình
```

## 3.1. Chi Tiết Luồng Xử Lý Dữ Liệu

### 3.1.1. Tải Dữ Liệu Lần Đầu

Sơ đồ mô tả cách ứng dụng tải dữ liệu lần đầu khi người dùng mở màn hình.

```mermaid
sequenceDiagram
    participant UI as "Màn Hình UI"
    participant VM as "ViewModel"
    participant Repo as "Repository"
    participant Paging as "PagingSource"
    participant LocalDB as "Room Database"
    participant Api as "API Service"
    participant Backend as "Backend API"
    participant MySQL as "MySQL Database"
    
    UI->>VM: Người dùng mở màn hình
    VM->>Repo: getArticles()
    Repo->>LocalDB: Kiểm tra cache
    alt Có Cache
        LocalDB-->>Repo: Trả về dữ liệu cache
        Repo-->>VM: Dữ liệu từ cache
        VM-->>UI: Hiển thị ngay
    else Không Có Cache
        Repo->>Paging: load(LoadParams)
        Paging->>Api: getArticles(page=1, size=20)
        Api->>Backend: HTTP GET /articles?page=1
        Backend->>MySQL: SELECT articles LIMIT 20
        MySQL-->>Backend: Dữ liệu bài viết
        Backend-->>Api: JSON Response
        Api-->>Paging: LoadResult.Page
        Paging->>LocalDB: Lưu vào cache
        Paging-->>Repo: PagingData
        Repo-->>VM: Flow<PagingData>
        VM-->>UI: Hiển thị dữ liệu
    end
```

### 3.1.2. Phân Trang - Tải Thêm Dữ Liệu

Sơ đồ mô tả cách ứng dụng tự động tải thêm dữ liệu khi người dùng cuộn đến cuối danh sách.

```mermaid
sequenceDiagram
    participant User as "Người Dùng"
    participant UI as "Màn Hình UI"
    participant VM as "ViewModel"
    participant Paging as "PagingSource"
    participant LocalDB as "Room Database"
    participant Api as "API Service"
    participant Backend as "Backend API"
    
    User->>UI: Cuộn đến cuối danh sách
    UI->>VM: Trigger load next page
    VM->>Paging: load(LoadParams.Append)
    Paging->>LocalDB: Kiểm tra cache trang tiếp theo
    alt Có Cache Trang Tiếp Theo
        LocalDB-->>Paging: Dữ liệu từ cache
        Paging-->>VM: LoadResult.Page
        VM-->>UI: Hiển thị thêm dữ liệu
    else Không Có Cache
        Paging->>Api: getArticles(page=2, size=20)
        Api->>Backend: HTTP GET /articles?page=2
        Backend-->>Api: JSON Response
        Api-->>Paging: LoadResult.Page
        Paging->>LocalDB: Lưu trang mới vào cache
        Paging-->>VM: PagingData mới
        VM-->>UI: Cập nhật danh sách
    end
```

### 3.1.3. Refresh - Làm Mới Dữ Liệu

Sơ đồ mô tả cách ứng dụng làm mới dữ liệu khi người dùng kéo xuống để refresh.

```mermaid
sequenceDiagram
    participant User as "Người Dùng"
    participant UI as "Màn Hình UI"
    participant VM as "ViewModel"
    participant Repo as "Repository"
    participant Paging as "PagingSource"
    participant Api as "API Service"
    participant Backend as "Backend API"
    participant LocalDB as "Room Database"
    
    User->>UI: Kéo xuống để refresh
    UI->>VM: refresh()
    VM->>Repo: refreshData()
    Repo->>LocalDB: Xóa cache cũ
    Repo->>Paging: invalidate()
    Paging->>Api: getArticles(page=1, size=20)
    Api->>Backend: HTTP GET /articles?page=1
    Backend-->>Api: Dữ liệu mới nhất
    Api-->>Paging: LoadResult.Page
    Paging->>LocalDB: Xóa cache cũ và lưu dữ liệu mới
    Paging-->>Repo: PagingData mới
    Repo-->>VM: Flow<PagingData>
    VM-->>UI: Cập nhật dữ liệu mới
```

### 3.1.4. Xử Lý Lỗi và Retry

Sơ đồ mô tả cách ứng dụng xử lý lỗi mạng và cho phép người dùng thử lại.

```mermaid
sequenceDiagram
    participant User as "Người Dùng"
    participant UI as "Màn Hình UI"
    participant VM as "ViewModel"
    participant Repo as "Repository"
    participant Paging as "PagingSource"
    participant Api as "API Service"
    participant Backend as "Backend API"
    participant LocalDB as "Room Database"
    
    Paging->>Api: getArticles(page=1)
    Api->>Backend: HTTP GET /articles
    Backend-->>Api: Network Error / Timeout
    Api-->>Paging: LoadResult.Error
    Paging->>LocalDB: Kiểm tra cache
    alt Có Cache
        LocalDB-->>Paging: Dữ liệu cache
        Paging-->>Repo: LoadResult.Page từ cache
        Repo-->>VM: Dữ liệu cache + Error state
        VM-->>UI: Hiển thị cache + thông báo lỗi
        UI->>UI: Hiển thị nút "Thử lại"
    else Không Có Cache
        Paging-->>Repo: LoadResult.Error
        Repo-->>VM: Error state
        VM-->>UI: Hiển thị màn hình lỗi
        UI->>UI: Hiển thị nút "Thử lại"
    end
    
    User->>UI: Nhấn nút "Thử lại"
    UI->>VM: retry()
    VM->>Repo: retryLoad()
    Repo->>Paging: load(LoadParams.Refresh)
    Paging->>Api: getArticles(page=1)
    Api->>Backend: HTTP GET /articles
    alt Thành Công
        Backend-->>Api: Dữ liệu
        Api-->>Paging: LoadResult.Page
        Paging->>LocalDB: Lưu vào cache
        Paging-->>Repo: PagingData
        Repo-->>VM: Success state
        VM-->>UI: Cập nhật dữ liệu
    else Vẫn Lỗi
        Backend-->>Api: Error
        Api-->>Paging: LoadResult.Error
        Paging-->>Repo: Error
        Repo-->>VM: Error state
        VM-->>UI: Hiển thị lỗi lại
    end
```

## 4. Kiến Trúc Backend API

### 4.1. Cấu Trúc Backend API

Sơ đồ mô tả cấu trúc các thành phần trong backend API.

```mermaid
graph TB
    subgraph "Điểm Vào"
        Index["index.php<br/>Nhận tất cả HTTP requests"]
    end
    
    subgraph "Lớp Định Tuyến"
        Router["Router<br/>Phân tích URL và định tuyến"]
    end
    
    subgraph "Lớp Bảo Mật"
        RateLimit["RateLimiter<br/>Giới hạn số lần truy cập"]
        AuthMW["AuthMiddleware<br/>Xác thực người dùng"]
    end
    
    subgraph "Lớp Controller"
        AuthCtrl["AuthController"]
        ArticleCtrl["ArticleController"]
        MatchCtrl["MatchController"]
        VideoCtrl["VideoController"]
        CommentCtrl["CommentController"]
        ProfileCtrl["ProfileController"]
        SearchCtrl["SearchController"]
    end
    
    subgraph "Lớp Model"
        ArticleModel["Article Model"]
        UserModel["User Model"]
        MatchModel["Match Model"]
    end
    
    subgraph "Lớp Cấu Hình"
        Database["Database<br/>Kết nối MySQL"]
        ResponseHelper["ResponseHelper<br/>Trả về JSON"]
        EmailService["EmailService<br/>Gửi email"]
    end
    
    subgraph "Cơ Sở Dữ Liệu"
        MySQL[("MySQL Database")]
    end
    
    Index --> Router
    Router --> RateLimit
    RateLimit --> AuthMW
    Router --> AuthCtrl
    Router --> ArticleCtrl
    Router --> MatchCtrl
    Router --> VideoCtrl
    Router --> CommentCtrl
    Router --> ProfileCtrl
    Router --> SearchCtrl
    
    AuthCtrl --> UserModel
    ArticleCtrl --> ArticleModel
    MatchCtrl --> MatchModel
    
    ArticleModel --> Database
    UserModel --> Database
    MatchModel --> Database
    
    Database --> MySQL
    
    AuthCtrl --> EmailService
    AuthCtrl --> ResponseHelper
    ArticleCtrl --> ResponseHelper
```

### 4.2. Luồng Xử Lý Request

Sơ đồ mô tả chi tiết cách backend xử lý một request từ Android app.

```mermaid
sequenceDiagram
    participant App as "Android App"
    participant Index as "index.php"
    participant Router as "Router"
    participant RateLimit as "RateLimiter"
    participant AuthMW as "AuthMiddleware"
    participant Controller as "Controller"
    participant Model as "Model"
    participant Database as "Database"
    participant MySQL as "MySQL Database"
    participant Response as "ResponseHelper"
    
    App->>Index: HTTP GET /api/articles?page=1
    Index->>Router: Phân tích URL và định tuyến
    Router->>RateLimit: Kiểm tra rate limit
    alt Chưa vượt quá giới hạn
        RateLimit->>AuthMW: Kiểm tra authentication
        alt Có token hợp lệ hoặc public endpoint
            AuthMW->>Controller: Chuyển đến Controller
            Controller->>Controller: Xử lý logic nghiệp vụ
            Controller->>Model: Gọi phương thức Model
            Model->>Database: Thực thi SQL query
            Database->>MySQL: SELECT articles LIMIT 20
            MySQL-->>Database: Kết quả truy vấn
            Database-->>Model: Dữ liệu
            Model-->>Controller: Dữ liệu đã xử lý
            Controller->>Response: Tạo JSON response
            Response-->>Controller: JSON response
            Controller-->>AuthMW: Response JSON
            AuthMW-->>RateLimit: Response JSON
            RateLimit-->>Router: Response JSON
            Router-->>Index: Response JSON
            Index-->>App: HTTP 200 OK + JSON
        else Token không hợp lệ
            AuthMW-->>App: 401 Unauthorized
        end
    else Vượt quá rate limit
        RateLimit-->>App: 429 Too Many Requests
    end
```

## 5. Luồng Xác Thực Người Dùng

### 5.1. Quy Trình Đăng Nhập

Sơ đồ mô tả quy trình đăng nhập của người dùng.

```mermaid
sequenceDiagram
    participant User as "Người Dùng"
    participant UI as "Màn Hình"
    participant VM as "AuthViewModel"
    participant Repo as "AuthRepository"
    participant API as "AuthController"
    participant DB as "Database"
    
    User->>UI: Nhập email và mật khẩu
    UI->>VM: Yêu cầu đăng nhập
    VM->>Repo: login(email, password)
    Repo->>API: POST /auth/login
    API->>DB: Kiểm tra thông tin đăng nhập
    DB-->>API: Dữ liệu người dùng
    alt Thông Tin Hợp Lệ
        API->>DB: Kiểm tra email đã xác thực chưa
        alt Email Chưa Xác Thực
            API-->>Repo: Lỗi: Email chưa xác thực
            Repo-->>VM: Trạng thái lỗi
            VM-->>UI: Hiển thị yêu cầu xác thực email
        else Email Đã Xác Thực
            API->>DB: Tạo token phiên đăng nhập
            API-->>Repo: Token + Thông tin người dùng
            Repo->>Repo: Lưu token vào Preferences
            Repo-->>VM: Thành công
            VM-->>UI: Chuyển đến trang chủ
        end
    else Thông Tin Không Hợp Lệ
        API-->>Repo: Phản hồi lỗi
        Repo-->>VM: Trạng thái lỗi
        VM-->>UI: Hiển thị thông báo lỗi
    end
```

### 5.2. Quy Trình Đăng Ký

Sơ đồ mô tả quy trình đăng ký tài khoản mới của người dùng.

```mermaid
sequenceDiagram
    participant User as "Người Dùng"
    participant UI as "Màn Hình"
    participant VM as "AuthViewModel"
    participant Repo as "AuthRepository"
    participant API as "AuthController"
    participant DB as "Database"
    participant Email as "EmailService"
    
    User->>UI: Điền form đăng ký
    UI->>VM: Yêu cầu đăng ký
    VM->>Repo: register(thông tin người dùng)
    Repo->>API: POST /auth/register
    API->>DB: Tạo tài khoản mới
    API->>Email: Gửi email xác thực
    Email-->>API: Email đã gửi
    API-->>Repo: Thành công
    Repo-->>VM: Thành công
    VM-->>UI: Hiển thị màn hình xác thực email
```

## 6. Luồng Hiển Thị Bài Viết

### 6.1. Tải Danh Sách Bài Viết

Sơ đồ mô tả cách ứng dụng tải và hiển thị danh sách bài viết.

```mermaid
sequenceDiagram
    participant User as "Người Dùng"
    participant UI as "Màn Hình"
    participant VM as "ArticlesViewModel"
    participant Repo as "ArticleRepository"
    participant Paging as "PagingSource"
    participant LocalDB as "Room Database"
    participant API as "ArticleController"
    participant DB as "MySQL Database"
    
    User->>UI: Mở màn hình bài viết
    UI->>VM: Yêu cầu tải bài viết
    VM->>Repo: Lấy danh sách bài viết
    Repo->>LocalDB: Kiểm tra cache
    alt Có Cache
        LocalDB-->>Repo: Trả về bài viết đã cache
        Repo-->>VM: Dữ liệu từ cache
        VM-->>UI: Hiển thị bài viết ngay
    end
    Repo->>Paging: Tải trang
    Paging->>API: GET /articles?page=1
    API->>DB: Truy vấn bài viết
    DB-->>API: Dữ liệu bài viết
    API-->>Paging: Phản hồi JSON
    Paging->>LocalDB: Lưu vào cache
    Paging-->>Repo: Danh sách bài viết
    Repo-->>VM: Cập nhật trạng thái
    VM-->>UI: Hiển thị bài viết
```

### 6.2. Xem Chi Tiết Bài Viết

Sơ đồ mô tả cách ứng dụng tải và hiển thị chi tiết một bài viết.

```mermaid
sequenceDiagram
    participant User as "Người Dùng"
    participant UI as "Màn Hình"
    participant VM as "ArticlesViewModel"
    participant Repo as "ArticleRepository"
    participant API as "ArticleController"
    participant DB as "MySQL Database"
    
    User->>UI: Nhấn vào bài viết
    UI->>VM: Mở chi tiết
    VM->>Repo: Lấy chi tiết bài viết
    Repo->>API: GET /articles/:id
    API->>DB: Lấy bài viết + Tăng lượt xem
    DB-->>API: Dữ liệu bài viết
    API-->>Repo: JSON bài viết
    Repo-->>VM: Chi tiết bài viết
    VM-->>UI: Hiển thị màn hình chi tiết
```

### 6.3. Thích Bài Viết

Sơ đồ mô tả cách ứng dụng xử lý thao tác thích bài viết.

```mermaid
sequenceDiagram
    participant User as "Người Dùng"
    participant UI as "Màn Hình"
    participant VM as "ArticlesViewModel"
    participant Repo as "ArticleRepository"
    participant API as "ArticleController"
    participant DB as "MySQL Database"
    
    User->>UI: Nhấn nút thích
    UI->>VM: Bật/tắt thích
    VM->>Repo: Toggle like
    Repo->>API: POST /articles/:id/like
    API->>DB: Cập nhật trạng thái thích
    DB-->>API: Trạng thái đã cập nhật
    API-->>Repo: Phản hồi like
    Repo-->>VM: Cập nhật trạng thái like
    VM-->>UI: Cập nhật giao diện
```

## 7. Kiến Trúc Crawler

Sơ đồ này mô tả cách crawler thu thập dữ liệu từ các nguồn bên ngoài và lưu vào database.

```mermaid
graph TB
    subgraph "Scripts Crawler"
        NewsCrawler["crawler.py<br/>Crawler tin tức"]
        MatchCrawler["match_crawler.py<br/>Crawler lịch đấu"]
    end
    
    subgraph "Lớp Parser"
        BaseParser["BaseParser<br/>Lớp cơ sở"]
        VnExpressParser["VnExpressParser<br/>Parser VnExpress"]
        MatchParser["MatchParser<br/>Parser lịch đấu"]
        RobongParser["RobongMatchParser<br/>Parser Robong"]
    end
    
    subgraph "Xử Lý Dữ Liệu"
        DBHandler["DatabaseHandler<br/>Xử lý database"]
    end
    
    subgraph "Cấu Hình"
        Config["config.py<br/>Cấu hình nguồn"]
    end
    
    subgraph "Nguồn Bên Ngoài"
        VnExpress["VnExpress.com"]
        Robong["Robong.com"]
    end
    
    subgraph "Cơ Sở Dữ Liệu"
        MySQL[("MySQL Database")]
    end
    
    NewsCrawler --> Config
    MatchCrawler --> Config
    Config --> VnExpress
    Config --> Robong
    
    NewsCrawler --> VnExpressParser
    MatchCrawler --> MatchParser
    MatchCrawler --> RobongParser
    
    VnExpressParser --> BaseParser
    MatchParser --> BaseParser
    RobongParser --> BaseParser
    
    VnExpressParser -->|"Thu thập"| VnExpress
    MatchParser -->|"Thu thập"| VnExpress
    RobongParser -->|"Thu thập"| Robong
    
    NewsCrawler --> DBHandler
    MatchCrawler --> DBHandler
    DBHandler -->|"Lưu dữ liệu"| MySQL
```

## Tóm Tắt Kiến Trúc

### Ứng Dụng Android
- **Kiến trúc**: MVVM (Model-View-ViewModel) - Tách biệt logic và giao diện
- **UI Framework**: Jetpack Compose - Xây dựng giao diện hiện đại
- **Dependency Injection**: Hilt - Quản lý phụ thuộc tự động
- **Networking**: Retrofit + OkHttp - Giao tiếp với API
- **Local Storage**: Room Database + DataStore Preferences - Lưu trữ cục bộ
- **Paging**: Android Paging 3 - Phân trang dữ liệu
- **Navigation**: Navigation Compose - Điều hướng màn hình

### Backend API
- **Framework**: Custom PHP Router - Router tự xây dựng
- **Pattern**: MVC (Model-View-Controller) - Tách biệt các lớp
- **Database**: MySQL - Cơ sở dữ liệu quan hệ
- **Authentication**: JWT Token-based - Xác thực bằng token
- **Middleware**: Auth, Rate Limiting - Bảo mật và giới hạn truy cập
- **Email**: EmailService - Gửi email xác thực

### Crawler
- **Ngôn ngữ**: Python - Ngôn ngữ lập trình
- **Pattern**: Parser Pattern với BaseParser - Mẫu thiết kế parser
- **Nguồn**: VnExpress, Robong - Các trang web nguồn
- **Lưu trữ**: MySQL Database - Lưu vào database

---