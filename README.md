# Automated Savings Goal Tracker

## Overview

The Automated Savings Goal Tracker is a Spring Boot application that helps users set and achieve their financial goals through automated savings rules. The application connects to users' bank accounts, tracks their spending habits, and automatically transfers money to savings goals based on customizable rules.

## Features

- **User Authentication**: Secure JWT-based authentication system
- **Bank Account Integration**: Connect to bank accounts using Plaid API
- **Savings Goals**: Create and manage savings goals with target amounts and dates
- **Automated Savings Rules**: Set up rules to automatically save money based on:

  - Fixed amounts on regular schedules
  - Round-ups from transactions
  - Percentage of income
  - Spending-based triggers
- **Progress Tracking**: Monitor savings progress with visual indicators and projections
- **Transaction History**: View all savings transactions and their associated rules
- **Goal Recommendations**: Receive personalized recommendations based on spending habits

## Technical Stack

- **Backend**: Java 11 with Spring Boot 2.7.x
- **Database**: H2 Database (can be configured for MySQL, PostgreSQL)
- **Security**: Spring Security with JWT authentication
- **API Documentation**: Swagger/OpenAPI
- **Banking Integration**: Plaid API
- **Build Tool**: Maven

## API Endpoints

### Authentication

- `POST /api/auth/register`- Register a new user
- `POST /api/auth/login` - Login and get JWT token

### Bank Accounts

- `GET /api/accounts` - Get all user accounts
- `GET /api/accounts/{id}` - Get account by ID
- `POST /api/accounts` - Add a new bank account manually
- `POST /api/accounts/link` - Link a bank account via plaid
- `DELETE /api/accounts{id}` - Delete a bank account
- `GET /api/accounts/{id}/balance` - Refresh account balance

### Savings Goals

- `GET /api/goals` - Get all user goals
- `GET /api/goals/{id}` - Get goal by ID
- `POST /api/goals` - Create a new savings goal
- `PUT /api/goals/{id}` - Update a savings goal
- `DELETE /api/goals/{id}` - Delete a savings goal
- `GET /api/goals/{id}/progress` - Get goal progress details
- `GET /api/goals/{id}/transactions` - Get all transactions for a goal

### Savings Rules

- `GET /api/rules/goal/{goalId}` - Get all rules for a goal
- `GET /api/rules/{id}` - Get rule by ID
- `POST /api/rules` - Create a new savings rule
- `PUT /api/rules/{id}` - Update a savings rule
- `DELETE /api/rules/{id}` - Delete a savings rule
- `PUT /api/rules/{id}/toggle` - Toggle rule active status

### Transactions

- `GET /api/transactions/goal/{goalId}` - Get all transactions for a goal
- `GET /api/transactions?start={date}&end={date}` - Get transactions between dates
- `POST /api/transactions/manual` - Create a manual transaction
- `POST /api/transactions/rule/{ruleId}` - Trigger a rule-based transaction

## Setup and Installation

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Plaid API credentials(for backend account integration)

## Configuration

1. Clone the repository:

```
git clone https://github.com/yourusername/savings-goal-tracker.git
cd savings-goal-tracker

```

2. Configure application properties in `src/main/resources/application.properties`:

```
# Database Configuration
spring.datasource.url=jdbc:h2:mem:savingsdb
spring.datasource.username=sa
spring.datasource.password=password

# JWT Configuration
app.jwt.secret=YourJwtSecretKey
app.jwt.expiration=86400000

# Plaid API Configuration
plaid.client.id=your_client_id
plaid.secret=your_secret
plaid.public.key=your_public_key
plaid.environment=sandbox

```

## Building and Running

1. Build the application:

`mvn clean package`

2. Run the application:

`java -jar target/savings-goal-tracker-0.0.1-SNAPSHOT.jar`

*Or Using Maven:*

`mvn spring-boot:run`

3. The application will be available at `http://localhost:8080`

## Usage Examples

### Register a New User

```
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "email": "user1@example.com",
    "password": "password123"
  }'

```

### Login

```
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "password123"
  }'

```

### Response

```
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

### Create a Saving Goal

```
curl -X POST http://localhost:8080/api/goals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "Vacation Fund",
    "targetAmount": 5000.00,
    "targetDate": "2023-12-31",
    "destinationAccountId": 1
  }'

```

### Create a Savings Rule

```
curl -X POST http://localhost:8080/api/rules \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "name": "Weekly Savings",
    "description": "Save $50 every week",
    "ruleType": "FIXED_AMOUNT",
    "ruleCondition": "WEEKLY",
    "amount": 50.00,
    "savingsGoalId": 1,
    "sourceAccountId": 1
  }'

```

## Security Considerations

- All API endpoints (except authentication) require JWT authentication
- Passwords are encrypted using Bcrypt before storage
- HTTPS should be enabled in production
- Plaid API keys should be kept secure and not commited to version control

## Future Enhancements

- Email notifications for goal achievements and rule triggers
- Mobile app integration
- Support for multiple currencies
- Advancced analysis and spending insights
- Social features for shared goals

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit your changes: `git commit -am 'Add  new feature'`
4. Push to the branch: `git push origin feature/new-feature`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details
