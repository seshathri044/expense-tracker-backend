# 💰 ExpenseTracker - Flutter Frontend

A production-ready Flutter mobile application for comprehensive personal finance management. Built with Provider state management, JWT authentication, and seamless integration with Spring Boot REST API backend for real-time expense tracking, income management, and financial analytics.

> **Backend Repository**: [ExpenseTracker Spring Boot API](https://github.com/seshathri044/expense-tracker-backend)

## ✨ Key Features

### 🔐 Authentication & Security
- **JWT Token Authentication** - Secure stateless authentication
- **Email OTP Verification** - Two-step registration process
- **Password Reset Flow** - Email-based OTP password recovery  
- **Secure Token Storage** - Encrypted credential management with SharedPreferences
- **Auto Token Refresh** - Seamless session management

### 💸 Expense Management
- **CRUD Operations** - Create, read, update, and delete expenses
- **Category Organization** - Organize expenses by customizable categories
- **Date-Based Tracking** - Track expenses with precise date filtering
- **Detailed Descriptions** - Add notes and descriptions to each expense
- **Real-Time Updates** - Instant UI updates using Provider state management

### 💵 Income Tracking
- **Multiple Income Sources** - Track income from various sources
- **Income Categories** - Categorize income (salary, freelance, investments, etc.)
- **Historical Records** - Complete income history with date filtering
- **Income vs Expense Analysis** - Compare earnings against spending

### 📊 Statistics & Analytics
- **All-Time Overview** - Total income, expenses, and balance summary
- **Monthly Reports** - Current month income/expense breakdown
- **Top 3 Categories** - See your highest spending categories
- **Yearly Analysis** - Month-by-month trends for any selected year
- **Category Breakdown** - Detailed spending analysis by category with percentages
- **Visual Charts** - Interactive data visualization (ready for chart integration)

### 👤 User Profile
- **Profile Management** - View and update user information
- **Account Settings** - Manage account preferences
- **Secure Logout** - Clean token removal and session termination

## 🛠️ Tech Stack

### Core Framework
- **Flutter 3.x** - Google's UI toolkit for cross-platform development
- **Dart 3.x** - Modern, type-safe programming language

### State Management
- **Provider 6.0+** - Lightweight, powerful state management solution
- **ChangeNotifier** - Reactive state updates across the app

### Backend Integration  
- **HTTP Package** - RESTful API communication
- **Spring Boot REST API** - Production backend with JWT security
- **MySQL Database** - Persistent data storage

### Local Storage
- **SharedPreferences** - Token and user data persistence
- **Secure Storage** - Encrypted credential storage

### API Service Architecture
- **ApiService** - Centralized HTTP request handler
- **AuthService** - Authentication and user management
- **ExpenseService** - Expense CRUD operations
- **IncomeService** - Income management  
- **HomeService** - Dashboard data aggregation
- **StatisticsService** - Analytics and reporting

## 📋 Prerequisites

Ensure you have the following installed:

- **Flutter SDK** - Version 3.0.0 or higher ([Install Flutter](https://docs.flutter.dev/get-started/install))
- **Dart SDK** - Version 3.0.0 or higher (bundled with Flutter)
- **Android Studio** / **VS Code** - With Flutter and Dart plugins
- **Git** - For version control
- **Backend API** - [ExpenseTracker Spring Boot Backend](https://github.com/seshathri044/expense-tracker-backend) running on your server

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/seshathri044/expense-tracker-frontend.git
cd expense-tracker-frontend
```

### 2. Install Dependencies
```bash
flutter pub get
```

### 3. Configure Backend URL

Edit `lib/config/app_config.dart` and update the base URL:

```dart
class AppConfig {
  // Update this to your backend URL
  static const String baseUrl = 'http://YOUR_BACKEND_IP:8080/api';
  
  // Or for production
  static const String baseUrl = 'https://your-domain.com/api';
  
  // ... rest of configuration
}
```

**Important**: 
- For Android emulator: Use `http://10.0.2.2:8080/api`
- For iOS simulator: Use `http://localhost:8080/api`
- For physical devices: Use your computer's local IP (e.g., `http://192.168.1.100:8080/api`)

### 4. Run the Application

```bash
# Check available devices
flutter devices

# Run on connected device
flutter run

# Run on specific device
flutter run -d <device-id>

# Run in debug mode with hot reload
flutter run --debug

# Run in release mode (optimized)
flutter run --release
```

## 📱 Build for Production

### Android APK
```bash
# Build release APK
flutter build apk --release

# Build split APKs by ABI (smaller file size)
flutter build apk --split-per-abi

# Output location: build/app/outputs/flutter-apk/
```

### Android App Bundle (Recommended for Play Store)
```bash
flutter build appbundle --release

# Output location: build/app/outputs/bundle/release/
```

### iOS (macOS required)
```bash
# Build release IPA
flutter build ios --release

# Or build with Xcode
open ios/Runner.xcworkspace
# Then use Xcode to archive and export
```

## 📂 Project Structure

```
lib/
├── config/                    # Configuration files
│   └── app_config.dart       # API endpoints and app constants
│
├── models/                    # Data models
│   ├── user_models.dart      # User and AuthResponse models
│   ├── expense_model.dart    # Expense data model
│   ├── income_model.dart     # Income data model  
│   ├── stats_model.dart      # Statistics and analytics models
│   └── api_response.dart     # Generic API response wrapper
│
├── providers/                 # State management (Provider)
│   ├── auth_provider.dart    # Authentication state
│   ├── expense_provider.dart # Expense state management
│   ├── income_provider.dart  # Income state management
│   └── stats_provider.dart   # Statistics state
│
├── screens/                   # UI screens
│   ├── auth/                 # Authentication screens
│   │   ├── login_screen.dart
│   │   ├── register_screen.dart
│   │   ├── otp_verification_screen.dart
│   │   └── forgot_password_screen.dart
│   ├── home/                 # Dashboard
│   │   └── home_screen.dart
│   ├── expense/              # Expense management
│   │   ├── expense_list_screen.dart
│   │   ├── add_expense_screen.dart
│   │   └── edit_expense_screen.dart
│   ├── income/               # Income management
│   │   ├── income_list_screen.dart
│   │   ├── add_income_screen.dart
│   │   └── edit_income_screen.dart
│   ├── statistics/           # Analytics & reports
│   │   ├── statistics_screen.dart
│   │   └── year_report_screen.dart
│   ├── profile/              # User profile
│   │   └── profile_screen.dart
│   └── splash/               # App initialization
│       └── splash_screen.dart
│
├── services/                  # Backend API services
│   ├── api_service.dart      # Base HTTP client
│   ├── storage_service.dart  # Local storage wrapper
│   ├── auth_service.dart     # Authentication API calls
│   ├── expense_service.dart  # Expense API calls
│   ├── income_service.dart   # Income API calls
│   ├── home_service.dart     # Dashboard data API
│   └── statistics_service.dart # Analytics API calls
│
├── widgets/                   # Reusable widgets
│   ├── common/               # Common UI components
│   ├── charts/               # Chart widgets
│   └── forms/                # Form components
│
├── utils/                     # Utility functions
│   ├── constants.dart        # App-wide constants
│   ├── validators.dart       # Input validation
│   ├── date_formatter.dart   # Date utilities
│   └── currency_formatter.dart # Currency formatting
│
└── main.dart                  # Application entry point
```

## 🔌 API Integration

The app communicates with the Spring Boot backend through these main services:

### Authentication Endpoints
```
POST   /api/register          - Register new user
POST   /api/send-otp          - Send email OTP
POST   /api/verify-otp        - Verify OTP and activate account
POST   /api/login             - User login
POST   /api/send-reset-otp    - Password reset OTP
POST   /api/reset-password    - Reset password with OTP
GET    /api/profile           - Get user profile
POST   /api/logout            - Logout user
```

### Expense Endpoints
```
GET    /api/expense/all       - Get all expenses
POST   /api/expense           - Create new expense
PUT    /api/expense/:id       - Update expense
DELETE /api/expense/:id       - Delete expense
```

### Income Endpoints  
```
GET    /api/income/all        - Get all incomes
POST   /api/income            - Create new income
PUT    /api/income/:id        - Update income
DELETE /api/income/:id        - Delete income
```

### Statistics Endpoints
```
GET    /api/stats             - Get all-time statistics
```

## 🔑 Key Implementation Details

### JWT Token Management
- Tokens are extracted and stored after login/verification
- Username is decoded from JWT payload for display
- Tokens are automatically attached to authenticated requests
- Secure logout clears all stored credentials

### State Management with Provider
```dart
// Example: Expense Provider usage
class ExpenseProvider extends ChangeNotifier {
  List<Expense> _expenses = [];
  
  Future<void> loadExpenses() async {
    final response = await ExpenseService().getExpenses();
    if (response.success) {
      _expenses = response.data!;
      notifyListeners(); // Triggers UI rebuild
    }
  }
}
```

### Error Handling
- All API calls wrapped in try-catch blocks
- User-friendly error messages
- Network error detection and reporting
- Response validation before data parsing

### Date Handling  
- Dates stored in ISO 8601 format (YYYY-MM-DD)
- Client-side date filtering for range queries
- Timezone-aware date comparisons

## 🧪 Testing

### Run Unit Tests
```bash
flutter test
```

### Run Widget Tests
```bash
flutter test test/widget_test.dart
```

### Run Integration Tests
```bash
flutter drive --target=test_driver/app.dart
```

### Generate Coverage Report
```bash
flutter test --coverage
genhtml coverage/lcov.info -o coverage/html
open coverage/html/index.html
```

## 🐛 Troubleshooting

### Common Issues

**1. Cannot connect to backend**
```
Error: SocketException: Failed to connect
Solution: Check your app_config.dart baseUrl matches your backend server
```

**2. Token expired errors**
```
Error: 401 Unauthorized
Solution: Logout and login again to refresh token
```

**3. Date parsing errors**
```
Error: FormatException: Invalid date format
Solution: Ensure backend returns dates in YYYY-MM-DD format
```

**4. Flutter pub get fails**
```bash
# Clean and reinstall dependencies
flutter clean
flutter pub get
```

**5. Build errors on iOS**
```bash
# Clean iOS build
cd ios
pod deintegrate
pod install
cd ..
flutter clean
flutter build ios
```

## 📦 Dependencies

Key packages used in this project:

```yaml
dependencies:
  flutter:
    sdk: flutter
    
  # State Management
  provider: ^6.1.1
  
  # HTTP & API
  http: ^1.1.0
  
  # Local Storage  
  shared_preferences: ^2.2.2
  
  # UI Components
  flutter_svg: ^2.0.9
  google_fonts: ^6.1.0
  
  # Date & Time
  intl: ^0.19.0
  
  # Charts (when implemented)
  fl_chart: ^0.65.0
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add amazing feature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### Coding Guidelines
- Follow Flutter's official style guide
- Write meaningful commit messages
- Add comments for complex logic
- Update documentation for new features
- Test thoroughly before submitting PR

## 📸 Screenshots

> Add screenshots of your app here:
- Login/Registration screens
- Dashboard with statistics  
- Expense list and add expense forms
- Income tracking screens
- Analytics and reports
- User profile

## 🔐 Security Considerations

- ✅ JWT tokens stored securely in SharedPreferences
- ✅ Passwords never stored locally
- ✅ HTTPS recommended for production
- ✅ Token expiration handled gracefully
- ✅ Input validation on all forms
- ⚠️ Consider adding biometric authentication
- ⚠️ Implement certificate pinning for production

## 🚀 Future Enhancements

- [ ] Receipt photo upload and OCR
- [ ] Budget alerts and notifications
- [ ] Recurring expenses/income
- [ ] Multi-currency support
- [ ] Export data to CSV/PDF
- [ ] Biometric authentication
- [ ] Dark mode theme
- [ ] Offline mode with sync
- [ ] Data backup to cloud
- [ ] Expense categories customization

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Seshathri**
- GitHub: [@seshathri044](https://github.com/seshathri044)
- Repository: [expense-tracker-frontend](https://github.com/seshathri044/expense-tracker-frontend)

## 🔗 Related Projects

- **Backend API**: [ExpenseTracker Spring Boot Backend](https://github.com/seshathri044/expense-tracker-backend)
  - Spring Boot 3.x
  - Spring Security with JWT
  - MySQL Database
  - RESTful API

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Troubleshooting](#-troubleshooting) section
2. Review existing [Issues](https://github.com/seshathri044/expense-tracker-frontend/issues)
3. Open a new issue with detailed information
4. Contact: [Open an issue on GitHub]

## 🙏 Acknowledgments

- Flutter team for the amazing framework
- Provider package maintainers
- Spring Boot backend team
- All contributors and supporters

---

**Built with ❤️ using Flutter & Spring Boot**

⭐ Star this repo if you find it helpful!
